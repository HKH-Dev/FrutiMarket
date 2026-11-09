package com.uniquindio.ecommerce.integracion;

import com.uniquindio.ecommerce.domain.repository.CuponRepository;
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

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** De punta a punta: descuento de temporada + cupon + pago simulado + reembolso, con los datos de data.sql. */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {"CAMPESINO_CLAVE=campesino123", "COMPRADOR_CLAVE=comprador123", "ADMIN_CLAVE=admin123"})
class CheckoutIntegracionTest {

    private static final String LOTE_MANGO = "66666666-0000-0000-0000-000000000001";
    private static final String MANGO = "11111111-0000-0000-0000-000000000001";

    @LocalServerPort
    int puerto;

    @Autowired
    CuponRepository cupones;

    private RestClient http;
    private String comprador;

    @BeforeEach
    void preparar() {
        http = RestClient.builder().baseUrl("http://localhost:" + puerto)
                .defaultStatusHandler(estado -> true, (peticion, respuesta) -> { }).build();
        comprador = token("comprador@frutimarket.com", "comprador123");
    }

    @Test
    void pagarAplicaDescuentosRechazaSinDejarNadaYElReembolsoLiberaElCupon() {
        // Tarjeta 0000: rechazado, y el cupon sigue libre porque toda la transaccion se deshizo
        ResponseEntity<Map> rechazo = checkout("0000");
        assertEquals(HttpStatus.UNPROCESSABLE_CONTENT, rechazo.getStatusCode());
        assertEquals("R25", rechazo.getBody().get("regla"));
        assertTrue(cupones.porCodigo("BIENVENIDA10").orElseThrow().usadoEn().isEmpty());

        // Pago aprobado: 10 kg x 3500 = 35000; -15% temporada = 29750; -10% cupon = 26775
        ResponseEntity<Map> pago = checkout("4242");
        assertEquals(HttpStatus.CREATED, pago.getStatusCode());
        Map<?, ?> liquidacion = (Map<?, ?>) pago.getBody().get("liquidacion");
        assertEquals(26775.0, ((Number) liquidacion.get("total")).doubleValue());
        assertFalse(cupones.porCodigo("BIENVENIDA10").orElseThrow().usadoEn().isEmpty());

        // Reembolso: el cupon vuelve a estar disponible
        String compraId = (String) ((Map<?, ?>) pago.getBody().get("compra")).get("compraId");
        ResponseEntity<Map> reembolso = http.post().uri("/api/compras/" + compraId + "/reembolso")
                .header(HttpHeaders.AUTHORIZATION, comprador).retrieve().toEntity(Map.class);
        assertEquals(HttpStatus.OK, reembolso.getStatusCode());
        assertTrue(cupones.porCodigo("BIENVENIDA10").orElseThrow().usadoEn().isEmpty());
    }

    @Test
    void favoritosYRutasPorRol() {
        assertEquals(HttpStatus.NO_CONTENT, http.put().uri("/api/favoritos/" + MANGO)
                .header(HttpHeaders.AUTHORIZATION, comprador).retrieve().toBodilessEntity().getStatusCode());
        List<?> favoritos = http.get().uri("/api/favoritos").header(HttpHeaders.AUTHORIZATION, comprador).retrieve().body(List.class);
        assertTrue(favoritos.contains(MANGO));

        assertEquals(HttpStatus.UNAUTHORIZED, http.get().uri("/api/favoritos").retrieve().toBodilessEntity().getStatusCode());
        assertEquals(HttpStatus.FORBIDDEN, http.get().uri("/api/paneles/admin/resumen")
                .header(HttpHeaders.AUTHORIZATION, comprador).retrieve().toBodilessEntity().getStatusCode());
        assertEquals(HttpStatus.OK, http.get().uri("/api/ofertas/vigentes").retrieve().toBodilessEntity().getStatusCode());
    }

    @Test
    void lasRecomendacionesPersonalizanYElChatEsSoloDeParticipantes() {
        http.put().uri("/api/favoritos/" + MANGO).header(HttpHeaders.AUTHORIZATION, comprador).retrieve().toBodilessEntity();
        List<Map<String, Object>> sugerencias = http.get().uri("/api/recomendaciones")
                .header(HttpHeaders.AUTHORIZATION, comprador).retrieve().body(List.class);
        assertEquals("Está en tus favoritos", sugerencias.get(0).get("motivo"));

        Map<?, ?> conversacion = http.post().uri("/api/chat/conversaciones").contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, comprador).body(Map.of("loteId", LOTE_MANGO)).retrieve().body(Map.class);
        String id = (String) conversacion.get("id");
        assertEquals(HttpStatus.CREATED, http.post().uri("/api/chat/conversaciones/" + id + "/mensajes")
                .contentType(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, comprador)
                .body(Map.of("texto", "Hola")).retrieve().toBodilessEntity().getStatusCode());

        String campesino = token("campesino@frutimarket.com", "campesino123");
        List<?> mensajes = http.get().uri("/api/chat/conversaciones/" + id + "/mensajes")
                .header(HttpHeaders.AUTHORIZATION, campesino).retrieve().body(List.class);
        assertEquals(1, mensajes.size());
    }

    private ResponseEntity<Map> checkout(String digitos) {
        return http.post().uri("/api/pagos/checkout").contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, comprador)
                .body(Map.of("items", List.of(Map.of("loteId", LOTE_MANGO, "cantidad", 10)), "cupon", "BIENVENIDA10",
                        "metodo", "TARJETA", "ultimosDigitos", digitos))
                .retrieve().toEntity(Map.class);
    }

    private String token(String correo, String clave) {
        Map<?, ?> r = http.post().uri("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("correo", correo, "clave", clave)).retrieve().body(Map.class);
        return "Bearer " + r.get("token");
    }
}
