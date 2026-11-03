package com.uniquindio.ecommerce.infraestructure.seguridad;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;

import java.io.IOException;

/** JSON claro para los errores de seguridad: 401 (sin token o token invalido) y 403 (rol equivocado). */
public class RespuestasDeSeguridad implements AuthenticationEntryPoint, AccessDeniedHandler {

    @Override
    public void commence(HttpServletRequest peticion, HttpServletResponse respuesta, AuthenticationException ex)
            throws IOException {
        respuesta.setHeader("WWW-Authenticate", "Bearer");
        escribir(respuesta, 401, "No autenticado",
                "Falta el token o no es valido. Inicie sesion en POST /api/auth/login y envielo como Bearer.");
    }

    @Override
    public void handle(HttpServletRequest peticion, HttpServletResponse respuesta, AccessDeniedException ex)
            throws IOException {
        escribir(respuesta, 403, "Acceso denegado", "Su rol no tiene permiso para usar esta ruta.");
    }

    private static void escribir(HttpServletResponse respuesta, int estado, String titulo, String detalle)
            throws IOException {
        respuesta.setStatus(estado);
        respuesta.setContentType("application/problem+json");
        respuesta.setCharacterEncoding("UTF-8");
        respuesta.getWriter().write("{\"title\":\"" + titulo + "\",\"status\":" + estado
                + ",\"detail\":\"" + detalle + "\"}");
    }
}