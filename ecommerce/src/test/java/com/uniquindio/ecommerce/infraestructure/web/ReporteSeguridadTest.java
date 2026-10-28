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

import java.util.List;

import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Punto 1: una ruta protegida ({@code GET /api/reportes/productos-mas-vendidos}, solo ADMIN).
 *
 * <p>Los tokens son JWT reales, firmados con la misma clave que usa la aplicacion, asi que la
 * prueba pasa por toda la cadena de seguridad: leer el encabezado, validar la firma y revisar el rol.</p>
 */
@SpringBootTest(properties = "ADMIN_CLAVE=admin123") // independiente del .env de cada uno
@AutoConfigureMockMvc
class ReporteSeguridadTest {

    private static final String RUTA_PROTEGIDA = "/api/reportes/productos-mas-vendidos";

    @Autowired
    MockMvc mvc;

    @Autowired
    EmisorTokens emisor;

    @Test
    void sinTokenResponde401() throws Exception {
        // Preparar: ninguna cabecera Authorization

        // Ejecutar y verificar
        mvc.perform(get(RUTA_PROTEGIDA))
                .andExpect(status().isUnauthorized())
                .andExpect(header().string(HttpHeaders.WWW_AUTHENTICATE, startsWith("Bearer")));
    }

    @Test
    void conTokenDeRolEquivocadoResponde403() throws Exception {
        // Preparar: token valido, pero de un comprador
        String token = emisor.emitir("comprador", List.of("COMPRADOR")).token();

        // Ejecutar y verificar
        mvc.perform(get(RUTA_PROTEGIDA).header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void conTokenDeAdminResponde200() throws Exception {
        // Preparar: token valido con rol ADMIN
        String token = emisor.emitir("admin", List.of("ADMIN")).token();

        // Ejecutar y verificar
        mvc.perform(get(RUTA_PROTEGIDA).header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void tokenAlteradoResponde401() throws Exception {
        // Preparar: token de ADMIN al que se le cambia la firma
        String token = emisor.emitir("admin", List.of("ADMIN")).token();
        String alterado = token.substring(0, token.length() - 4) + "AAAA";

        // Ejecutar y verificar
        mvc.perform(get(RUTA_PROTEGIDA).header(HttpHeaders.AUTHORIZATION, "Bearer " + alterado))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void loginDeAdminEntregaUnTokenQueAbreLaRutaProtegida() throws Exception {
        // Preparar y ejecutar: iniciar sesion con el usuario de demostracion
        String respuesta = mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"usuario\":\"admin\",\"clave\":\"admin123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.roles[0]").value("ADMIN"))
                .andReturn().getResponse().getContentAsString();
        String token = JsonPath.read(respuesta, "$.token");

        // Verificar: el token sirve en la ruta protegida
        mvc.perform(get(RUTA_PROTEGIDA).header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void loginConClaveIncorrectaResponde401() throws Exception {
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"usuario\":\"admin\",\"clave\":\"equivocada\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.title").value("No autenticado"));
    }
}
