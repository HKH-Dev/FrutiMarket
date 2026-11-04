package com.uniquindio.ecommerce.integracion;

import com.uniquindio.ecommerce.domain.catalogo.EstadoLote;
import com.uniquindio.ecommerce.domain.catalogo.Lote;
import com.uniquindio.ecommerce.domain.entity.Compra;
import com.uniquindio.ecommerce.domain.repository.CompraRepository;
import com.uniquindio.ecommerce.domain.repository.LoteRepository;
import com.uniquindio.ecommerce.domain.valueobject.EstadoCompra;
import com.uniquindio.ecommerce.domain.valueobject.catalogo.Cantidad;
import com.uniquindio.ecommerce.domain.valueobject.catalogo.UnidadMedida;
import com.uniquindio.ecommerce.domain.valueobject.identidad.CompradorId;
import com.uniquindio.ecommerce.domain.valueobject.identidad.LoteId;
import com.uniquindio.ecommerce.domain.valueobject.identidad.PedidoId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClient;

import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Punto 3: prueba de integracion de punta a punta, sin simular nada.
 *
 * <p>Levanta el servidor real en un puerto libre y le habla por HTTP, como lo haria el frontend:
 * inicia sesion como campesino y como comprador con los usuarios de demostracion y usa sus tokens.
 * Cada peticion pasa por todas las capas: filtro de seguridad, controller, validacion, caso de uso,
 * dominio, adaptador JPA y la base H2. Al final se revisa la base de datos directamente con los
 * repositorios, para confirmar que lo que dijo la API quedo guardado de verdad.</p>
 */
// Claves fijas de los usuarios de demostracion: la prueba no depende del .env de cada integrante
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {"CAMPESINO_CLAVE=campesino123", "COMPRADOR_CLAVE=comprador123"})
class CompraIntegracionTest {

    // Datos de ejemplo de data.sql
    private static final String MANGO = "11111111-0000-0000-0000-000000000001";
    private static final String RESTAURANTE = "33333333-0000-0000-0000-000000000001";

    @LocalServerPort
    int puerto;

    @Autowired
    LoteRepository lotes;

    @Autowired
    CompraRepository compras;

    private RestClient http;
    private String tokenCampesino;
    private String tokenComprador;

    @BeforeEach
    void crearClienteHttp() {
        // No lanza excepcion en 4xx/5xx: cada prueba revisa el codigo de estado que espera.
        http = RestClient.builder()
                .baseUrl("http://localhost:" + puerto)
                .defaultStatusHandler(estado -> true, (peticion, respuesta) -> { })
                .build();
        tokenCampesino = iniciarSesion("campesino@frutimarket.com", "campesino123");
        tokenComprador = iniciarSesion("comprador@frutimarket.com", "comprador123");
    }

    @Test
    void registrarPublicarYComprarQuedaGuardadoEnLaBaseDeDatos() {
        // Preparar: un campesino registra y publica un lote de 100 kg por la API
        UUID loteId = registrarLote("100");
        ResponseEntity<Void> publicacion = post("/api/lotes/" + loteId + "/publicar", tokenCampesino);
        assertEquals(HttpStatus.NO_CONTENT, publicacion.getStatusCode());

        // Ejecutar: un comprador compra 30 kg
        ResponseEntity<Map> compra = http.post().uri("/api/compras").contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, tokenComprador)
                .body(Map.of("items", new Object[]{Map.of("loteId", loteId, "cantidad", 30)}))
                .retrieve().toEntity(Map.class);

        // Verificar la respuesta HTTP
        assertEquals(HttpStatus.CREATED, compra.getStatusCode());
        assertEquals("COMPLETADA", compra.getBody().get("estado"));

        // Verificar la base de datos: el lote bajo a 70 kg y la compra existe con su detalle
        Lote guardado = lotes.obtenerLote(LoteId.de(loteId)).orElseThrow();
        assertEquals(EstadoLote.PUBLICADO, guardado.estado());
        assertEquals(Cantidad.de("70", UnidadMedida.KILOGRAMO), guardado.cantidadDisponible());

        PedidoId compraId = PedidoId.de(UUID.fromString((String) compra.getBody().get("compraId")));
        Compra compraGuardada = compras.obtenerCompra(compraId).orElseThrow();
        assertEquals(EstadoCompra.COMPLETADA, compraGuardada.getEstado());
        assertEquals(1, compraGuardada.getDetalles().size());
        assertTrue(compras.comprasDelComprador(CompradorId.de(RESTAURANTE)).contains(compraGuardada));
    }

    @Test
    void compraQueRompeUnaReglaNoDejaNadaAMedias() {
        // Preparar: un lote publicado de 10 kg
        UUID loteId = registrarLote("10");
        post("/api/lotes/" + loteId + "/publicar", tokenCampesino);

        // Ejecutar: se piden 4 kg (posible) y luego 50 kg del mismo lote (imposible, regla 3)
        ResponseEntity<Map> respuesta = http.post().uri("/api/compras").contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, tokenComprador)
                .body(Map.of("items", new Object[]{
                        Map.of("loteId", loteId, "cantidad", 4), Map.of("loteId", loteId, "cantidad", 50)}))
                .retrieve().toEntity(Map.class);

        // Verificar: 422 con la regla, y la transaccion se deshizo entera (ni los 4 kg se descontaron)
        assertEquals(HttpStatus.UNPROCESSABLE_CONTENT, respuesta.getStatusCode());
        assertEquals("R3", respuesta.getBody().get("regla"));
        assertEquals(Cantidad.de("10", UnidadMedida.KILOGRAMO),
                lotes.obtenerLote(LoteId.de(loteId)).orElseThrow().cantidadDisponible());
    }

    private UUID registrarLote(String kilos) {
        Map<String, Object> cuerpo = Map.ofEntries(
                Map.entry("codigo", "INT-" + UUID.randomUUID().toString().substring(0, 8)),
                Map.entry("productoId", MANGO),
                Map.entry("fincaOrigenId", UUID.randomUUID().toString()),
                Map.entry("tecnicaProduccion", "AGROECOLOGICA"),
                Map.entry("categoriaCalidad", "PRIMERA"),
                Map.entry("porcentajeDefectos", 5),
                Map.entry("certificadoOrganico", false),
                Map.entry("cantidad", kilos),
                Map.entry("unidadMedida", "KILOGRAMO"),
                Map.entry("precioPorUnidad", 3500),
                Map.entry("fechaCosecha", LocalDate.now().minusDays(1).toString()));
        ResponseEntity<Map> respuesta = http.post().uri("/api/lotes").contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, tokenCampesino)
                .body(cuerpo).retrieve().toEntity(Map.class);
        assertEquals(HttpStatus.CREATED, respuesta.getStatusCode());
        return UUID.fromString((String) respuesta.getBody().get("id"));
    }

    private ResponseEntity<Void> post(String ruta, String autorizacion) {
        return http.post().uri(ruta).header(HttpHeaders.AUTHORIZATION, autorizacion).retrieve().toBodilessEntity();
    }

    /** Login real por HTTP; devuelve el valor para la cabecera Authorization. */
    private String iniciarSesion(String correo, String clave) {
        ResponseEntity<Map> respuesta = http.post().uri("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("correo", correo, "clave", clave)).retrieve().toEntity(Map.class);
        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
        return "Bearer " + respuesta.getBody().get("token");
    }
}
