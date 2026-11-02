package com.uniquindio.ecommerce.infraestructure;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** El navegador pregunta antes (peticion "preflight" OPTIONS) si un origen puede llamar a la API. */
@SpringBootTest
@AutoConfigureMockMvc
class CorsTest {

    @Autowired
    MockMvc mvc;

    @Test
    void elFrontendDeAngularPuedeLlamarALaApi() throws Exception {
        mvc.perform(options("/api/productos")
                        .header(HttpHeaders.ORIGIN, "http://localhost:4200")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "GET"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "http://localhost:4200"));
    }

    @Test
    void unOrigenDesconocidoEsRechazado() throws Exception {
        mvc.perform(options("/api/productos")
                        .header(HttpHeaders.ORIGIN, "http://sitio-malicioso.example")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "GET"))
                .andExpect(status().isForbidden());
    }
}
