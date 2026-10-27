package com.uniquindio.ecommerce.infraestructure.web;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.time.LocalDate;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Recorre la API completa contra H2, usando los datos de ejemplo de data.sql. */
@SpringBootTest
@AutoConfigureMockMvc
class ApiRestTest {

    private static final String MANGO = "11111111-0000-0000-0000-000000000001";
    private static final String MARIA = "22222222-0000-0000-0000-000000000001";
    private static final String JORGE_NO_AUTORIZADO = "22222222-0000-0000-0000-000000000002";
    private static final String RESTAURANTE = "33333333-0000-0000-0000-000000000001";

    @Autowired
    MockMvc mvc;

    @Test
    void registrarPublicarYComprarUnLote() throws Exception {
        String loteId = registrarMango(MARIA, "100");

        postJson("/api/lotes/" + loteId + "/publicar", "{\"campesinoId\":\"" + MARIA + "\"}")
                .andExpect(status().isNoContent());

        mvc.perform(get("/api/lotes").param("productoId", MANGO))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.id == '" + loteId + "')].estado").value("PUBLICADO"));

        postJson("/api/compras", """
                {"compradorId":"%s","items":[{"loteId":"%s","cantidad":30}]}
                """.formatted(RESTAURANTE, loteId))
                .andExpect(status().isCreated())
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
        postJson("/api/lotes/" + loteId + "/publicar", "{\"campesinoId\":\"" + MARIA + "\"}");

        postJson("/api/compras", """
                {"compradorId":"%s","items":[{"loteId":"%s","cantidad":50}]}
                """.formatted(RESTAURANTE, loteId))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.regla").value("R3"));

        // La compra fallida no debe haber descontado nada.
        mvc.perform(get("/api/lotes/" + loteId + "/disponible")).andExpect(jsonPath("$.valor").value(10));
    }

    @Test
    void campesinoNoAutorizadoNoPuedeRegistrarLotes() throws Exception {
        postJson("/api/lotes", cuerpoMango(JORGE_NO_AUTORIZADO, "20"))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.regla").value("R1"));
    }

    @Test
    void cuerpoInvalidoDevuelve400ConErroresPorCampo() throws Exception {
        postJson("/api/lotes", "{\"codigo\":\"\",\"porcentajeDefectos\":150,\"certificadoOrganico\":false}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.codigo").exists())
                .andExpect(jsonPath("$.errores.productoId").exists())
                .andExpect(jsonPath("$.errores.porcentajeDefectos").exists());
    }

    @Test
    void jsonMalFormadoDevuelve400Explicativo() throws Exception {
        postJson("/api/compras", "{\"compradorId\": 12,")
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
    void productoInexistenteDevuelve404() throws Exception {
        mvc.perform(get("/api/productos/" + UUID.randomUUID())).andExpect(status().isNotFound());
    }

    @Test
    void enDesarrolloElErrorDeJsonIncluyeLaCausa() throws Exception {
        postJson("/api/compras", "{\"compradorId\": \"no-uuid\", \"items\": []}")
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
        String json = postJson("/api/lotes", cuerpoMango(campesinoId, kilos))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(json, "$.id");
    }

    private static String cuerpoMango(String campesinoId, String kilos) {
        return """
                {
                  "codigo": "LOT-%s",
                  "productoId": "%s",
                  "campesinoId": "%s",
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
                """.formatted(UUID.randomUUID().toString().substring(0, 8), MANGO, campesinoId, UUID.randomUUID(),
                kilos, LocalDate.now().minusDays(1));
    }

    private ResultActions postJson(String url, String cuerpo) throws Exception {
        return mvc.perform(post(url).contentType(MediaType.APPLICATION_JSON).content(cuerpo));
    }
}
