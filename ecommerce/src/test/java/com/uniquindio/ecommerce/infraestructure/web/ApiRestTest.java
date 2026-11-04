package com.uniquindio.ecommerce.infraestructure.web;

import com.jayway.jsonpath.JsonPath;
import com.uniquindio.ecommerce.infraestructure.seguridad.EmisorTokens;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Recorre la API completa contra H2, usando los datos de ejemplo de data.sql. Las rutas que modifican
 * datos exigen token: se usan tokens reales del campesino o comprador que corresponda.
 */
@SpringBootTest
@AutoConfigureMockMvc
class ApiRestTest {

    private static final String MANGO = "11111111-0000-0000-0000-000000000001";
    private static final String MARIA = "22222222-0000-0000-0000-000000000001";
    private static final String JORGE_NO_AUTORIZADO = "22222222-0000-0000-0000-000000000002";
    private static final String RESTAURANTE = "33333333-0000-0000-0000-000000000001";

    @Autowired
    MockMvc mvc;

    @Autowired
    EmisorTokens emisor;

    @Test
    void registrarPublicarYComprarUnLote() throws Exception {
        String loteId = registrarMango(MARIA, "100");

        postJson("/api/lotes/" + loteId + "/publicar", "", campesino(MARIA))
                .andExpect(status().isNoContent());

        mvc.perform(get("/api/lotes").param("productoId", MANGO))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.id == '" + loteId + "')].estado").value("PUBLICADO"));

        postJson("/api/compras", """
                {"items":[{"loteId":"%s","cantidad":30}]}
                """.formatted(loteId), comprador(RESTAURANTE))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.compradorId").value(RESTAURANTE))
                .andExpect(jsonPath("$.estado").value("COMPLETADA"))
                .andExpect(jsonPath("$.total").value(105000));

        mvc.perform(get("/api/lotes/" + loteId + "/disponible"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valor").value(70))
                .andExpect(jsonPath("$.unidad").value("kg"));

        mvc.perform(get("/api/lotes/" + loteId + "/trazabilidad"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.custodia[0].tipo").value("COSECHA"))
                .andExpect(jsonPath("$.calidadDeclarada.categoria").value("PRIMERA"));
    }

    @Test
    void comprarMasDeLoDisponibleDevuelve422ConLaRegla() throws Exception {
        String loteId = registrarMango(MARIA, "10");
        postJson("/api/lotes/" + loteId + "/publicar", "", campesino(MARIA));

        postJson("/api/compras", """
                {"items":[{"loteId":"%s","cantidad":50}]}
                """.formatted(loteId), comprador(RESTAURANTE))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.regla").value("R3"));

        // La compra fallida no debe haber descontado nada.
        mvc.perform(get("/api/lotes/" + loteId + "/disponible")).andExpect(jsonPath("$.valor").value(10));
    }

    @Test
    void campesinoNoAutorizadoNoPuedeRegistrarLotes() throws Exception {
        postJson("/api/lotes", cuerpoMango("20"), campesino(JORGE_NO_AUTORIZADO))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.regla").value("R1"));
    }

    @Test
    void unCampesinoNoPuedePublicarElLoteDeOtro() throws Exception {
        // Preparar: un lote de Maria
        String loteDeMaria = registrarMango(MARIA, "10");

        // Ejecutar: Jorge intenta publicarlo con SU token (ya no puede mandar el id de Maria en el cuerpo)
        postJson("/api/lotes/" + loteDeMaria + "/publicar", "", campesino(JORGE_NO_AUTORIZADO))
                // Verificar
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.regla").value("R5"));
    }

    @Test
    void registrarseComoCompradorCreaElPerfilYElLoginDevuelveCorreoRolYVencimiento() throws Exception {
        // Preparar
        String correo = "ana" + UUID.randomUUID().toString().substring(0, 6) + "@correo.com";

        // Ejecutar: registro y login
        String registro = postJson("/api/auth/registro", """
                {"correo":"%s","clave":"claveSegura1","rol":"COMPRADOR","nombre":"Ana Lopez","municipio":"Armenia"}
                """.formatted(correo), null)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.rol").value("COMPRADOR"))
                .andReturn().getResponse().getContentAsString();
        String perfilId = JsonPath.read(registro, "$.perfilId");

        String login = postJson("/api/auth/login", """
                {"correo":"%s","clave":"claveSegura1"}
                """.formatted(correo.toUpperCase()), null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.correo").value(correo))
                .andExpect(jsonPath("$.rol").value("COMPRADOR"))
                .andExpect(jsonPath("$.expiraEn").exists())
                .andReturn().getResponse().getContentAsString();

        // Verificar: el token del nuevo comprador compra a su nombre
        String loteId = registrarMango(MARIA, "5");
        postJson("/api/lotes/" + loteId + "/publicar", "", campesino(MARIA));
        postJson("/api/compras", """
                {"items":[{"loteId":"%s","cantidad":1}]}
                """.formatted(loteId), "Bearer " + JsonPath.read(login, "$.token"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.compradorId").value(perfilId));
    }

    /**
     * Flujo de punta a punta de la entrega: un campesino se registra, el ADMIN lo autoriza, el
     * campesino publica, un comprador compra, consulta sus compras y pide el reembolso.
     */
    @Test
    void flujoCompletoDesdeElRegistroHastaElReembolso() throws Exception {
        String sufijo = UUID.randomUUID().toString().substring(0, 6);

        // 1. Un campesino nuevo se registra: todavia no puede ofrecer lotes (regla 1)
        String registro = postJson("/api/auth/registro", """
                {"correo":"luis%s@correo.com","clave":"claveSegura1","rol":"CAMPESINO","nombre":"Luis","numeroIdentificacion":"10%s"}
                """.formatted(sufijo, sufijo.hashCode()), null)
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        String campesinoNuevo = JsonPath.read(registro, "$.perfilId");
        String tokenLuis = iniciarSesion("luis" + sufijo + "@correo.com", "claveSegura1");
        postJson("/api/lotes", cuerpoMango("40"), tokenLuis).andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.regla").value("R1"));

        // 2. El ADMIN lo autoriza; un comprador no puede hacerlo (403)
        postJson("/api/campesinos/" + campesinoNuevo + "/autorizacion", "", comprador(RESTAURANTE))
                .andExpect(status().isForbidden());
        postJson("/api/campesinos/" + campesinoNuevo + "/autorizacion", "", admin())
                .andExpect(status().isNoContent());

        // 3. Ahora registra y publica su lote
        String lote = postJson("/api/lotes", cuerpoMango("40"), tokenLuis)
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        String loteId = JsonPath.read(lote, "$.id");
        postJson("/api/lotes/" + loteId + "/publicar", "", tokenLuis).andExpect(status().isNoContent());

        // 4. Un comprador compra, ve la compra en "mis compras" y pide el reembolso
        String compra = postJson("/api/compras", """
                {"items":[{"loteId":"%s","cantidad":10}]}
                """.formatted(loteId), comprador(RESTAURANTE))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        String compraId = JsonPath.read(compra, "$.compraId");
        mvc.perform(get("/api/compras/mias").header(HttpHeaders.AUTHORIZATION, comprador(RESTAURANTE)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.compraId == '" + compraId + "')].estado").value("COMPLETADA"));
        postJson("/api/compras/" + compraId + "/reembolso", "", comprador(RESTAURANTE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("REEMBOLSADA"));

        // 5. Pedirlo otra vez rompe la regla: ya no esta COMPLETADA
        postJson("/api/compras/" + compraId + "/reembolso", "", comprador(RESTAURANTE))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.regla").value("INV-COMPRA-REEMBOLSO"));
    }

    @Test
    void otroCompradorNoPuedeReembolsarUnaCompraAjena() throws Exception {
        String loteId = registrarMango(MARIA, "5");
        postJson("/api/lotes/" + loteId + "/publicar", "", campesino(MARIA));
        String compra = postJson("/api/compras", """
                {"items":[{"loteId":"%s","cantidad":1}]}
                """.formatted(loteId), comprador(RESTAURANTE))
                .andReturn().getResponse().getContentAsString();

        postJson("/api/compras/" + JsonPath.read(compra, "$.compraId") + "/reembolso", "",
                comprador("33333333-0000-0000-0000-000000000002"))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.regla").value("R19"));
    }

    @Test
    void registrarseComoCampesinoSinIdentificacionDevuelve422() throws Exception {
        postJson("/api/auth/registro", """
                {"correo":"luis@correo.com","clave":"claveSegura1","rol":"CAMPESINO","nombre":"Luis"}
                """, null)
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.regla").value("INV-REGISTRO"));
    }

    @Test
    void cuerpoInvalidoDevuelve400ConErroresPorCampo() throws Exception {
        postJson("/api/lotes", "{\"codigo\":\"\",\"porcentajeDefectos\":150,\"certificadoOrganico\":false}",
                campesino(MARIA))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.codigo").exists())
                .andExpect(jsonPath("$.errores.productoId").exists())
                .andExpect(jsonPath("$.errores.porcentajeDefectos").exists());
    }

    @Test
    void jsonMalFormadoDevuelve400Explicativo() throws Exception {
        postJson("/api/compras", "{\"items\": [", comprador(RESTAURANTE))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Cuerpo de la peticion no valido"));
    }

    @Test
    void catalogoListaProductosActivosOrdenadosPorNombre() throws Exception {
        mvc.perform(get("/api/productos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombre").value("Cafe tostado"))
                .andExpect(jsonPath("$[?(@.id == '" + MANGO + "')].unidadVenta").value("KILOGRAMO"))
                .andExpect(jsonPath("$[?(@.id == '" + MANGO + "')].simboloUnidad").value("kg"))
                .andExpect(jsonPath("$[?(@.id == '" + MANGO + "')].enTemporada").value(true));
    }

    @Test
    void buscarEnElCatalogoDevuelvePaginaConTotales() throws Exception {
        // data.sql trae lotes publicados de mango, mora, platano y cafe
        mvc.perform(get("/api/lotes/buscar").param("texto", "mango").param("pagina", "0").param("tamano", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contenido.length()").value(1))
                .andExpect(jsonPath("$.contenido[0].producto").value("Mango Tommy"))
                .andExpect(jsonPath("$.tamano").value(1))
                .andExpect(jsonPath("$.totalElementos").isNumber())
                .andExpect(jsonPath("$.totalPaginas").isNumber());
    }

    @Test
    void buscarConTamanoDePaginaInvalidoDevuelve400() throws Exception {
        mvc.perform(get("/api/lotes/buscar").param("tamano", "500")).andExpect(status().isBadRequest());
        mvc.perform(get("/api/lotes/buscar").param("pagina", "-1")).andExpect(status().isBadRequest());
    }

    @Test
    void productoInexistenteDevuelve404() throws Exception {
        mvc.perform(get("/api/productos/" + UUID.randomUUID())).andExpect(status().isNotFound());
    }

    @Test
    void enDesarrolloElErrorDeJsonIncluyeLaCausa() throws Exception {
        postJson("/api/compras", "{\"items\": \"no-es-una-lista\"}", comprador(RESTAURANTE))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.causa").exists());
    }

    @Test
    void loteInexistenteDevuelve404() throws Exception {
        mvc.perform(get("/api/lotes/" + UUID.randomUUID() + "/trazabilidad"))
                .andExpect(status().isNotFound())
                .andExpect(header().string("Content-Type", MediaType.APPLICATION_PROBLEM_JSON_VALUE));
    }

    @Test
    void idMalFormadoDevuelve400() throws Exception {
        mvc.perform(get("/api/lotes/no-es-un-uuid/trazabilidad")).andExpect(status().isBadRequest());
    }

    @Test
    void swaggerPublicaLosEndpoints() throws Exception {
        mvc.perform(get("/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paths['/api/lotes']").exists())
                .andExpect(jsonPath("$.paths['/api/compras']").exists())
                .andExpect(jsonPath("$.paths['/api/productos']").exists());
    }

    private String registrarMango(String campesinoId, String kilos) throws Exception {
        String json = postJson("/api/lotes", cuerpoMango(kilos), campesino(campesinoId))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(json, "$.id");
    }

    private static String cuerpoMango(String kilos) {
        return """
                {
                  "codigo": "LOT-%s",
                  "productoId": "%s",
                  "fincaOrigenId": "%s",
                  "tecnicaProduccion": "AGROECOLOGICA",
                  "categoriaCalidad": "PRIMERA",
                  "porcentajeDefectos": 5,
                  "certificadoOrganico": false,
                  "cantidad": %s,
                  "unidadMedida": "KILOGRAMO",
                  "precioPorUnidad": 3500,
                  "fechaCosecha": "%s"
                }
                """.formatted(UUID.randomUUID().toString().substring(0, 8), MANGO, UUID.randomUUID(),
                kilos, LocalDate.now().minusDays(1));
    }

    private String campesino(String campesinoId) {
        return "Bearer " + emisor.emitir("campesino@prueba.com", List.of("CAMPESINO"), UUID.fromString(campesinoId)).token();
    }

    private String comprador(String compradorId) {
        return "Bearer " + emisor.emitir("comprador@prueba.com", List.of("COMPRADOR"), UUID.fromString(compradorId)).token();
    }

    private String admin() {
        return "Bearer " + emisor.emitir("admin@prueba.com", List.of("ADMIN"), null).token();
    }

    private String iniciarSesion(String correo, String clave) throws Exception {
        String login = postJson("/api/auth/login", """
                {"correo":"%s","clave":"%s"}
                """.formatted(correo, clave), null)
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        return "Bearer " + JsonPath.read(login, "$.token");
    }

    /** {@code autorizacion} nulo = sin token. */
    private ResultActions postJson(String url, String cuerpo, String autorizacion) throws Exception {
        var peticion = post(url).contentType(MediaType.APPLICATION_JSON).content(cuerpo);
        if (autorizacion != null) {
            peticion.header(HttpHeaders.AUTHORIZATION, autorizacion);
        }
        return mvc.perform(peticion);
    }
}
