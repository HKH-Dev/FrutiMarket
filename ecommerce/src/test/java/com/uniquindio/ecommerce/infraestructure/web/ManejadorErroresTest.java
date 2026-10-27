package com.uniquindio.ecommerce.infraestructure.web;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.mock.http.MockHttpInputMessage;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.ServletWebRequest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class ManejadorErroresTest {

    private static final HttpMessageNotReadableException JSON_ILEGIBLE = new HttpMessageNotReadableException(
            "fallo", new IllegalArgumentException("Cannot deserialize com.uniquindio.Clase"), new MockHttpInputMessage(new byte[0]));

    @Test
    void enProduccionNoExponeLaCausaTecnica() {
        ProblemDetail problema = leer(new ManejadorErrores(false));

        assertEquals(HttpStatus.BAD_REQUEST.value(), problema.getStatus());
        assertNull(problema.getProperties() == null ? null : problema.getProperties().get("causa"));
    }

    @Test
    void enDesarrolloIncluyeLaCausaTecnica() {
        ProblemDetail problema = leer(new ManejadorErrores(true));

        assertNotNull(problema.getProperties().get("causa"));
    }

    private static ProblemDetail leer(ManejadorErrores manejador) {
        return (ProblemDetail) manejador.handleHttpMessageNotReadable(JSON_ILEGIBLE, new HttpHeaders(),
                HttpStatus.BAD_REQUEST, new ServletWebRequest(new MockHttpServletRequest())).getBody();
    }
}
