package com.uniquindio.ecommerce.infraestructure;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.time.Duration;
import java.util.List;

/**
 * CORS: permite que un frontend servido desde otro origen (p. ej. Angular en http://localhost:4200)
 * llame a la API. Spring Security lo aplica con {@code http.cors(...)} en {@code SeguridadConfig}.
 *
 * <p>En desarrollo el frontend usa el proxy de Angular y no necesita CORS; esto es para cuando
 * frontend y backend se publican en dominios distintos.</p>
 */
@Configuration
public class CorsConfig {

    @Bean
    public CorsConfigurationSource corsConfigurationSource(
            @Value("${frutimarket.cors.origenes:http://localhost:4200}") List<String> origenes) {
        CorsConfiguration cors = new CorsConfiguration();
        cors.setAllowedOrigins(origenes);
        cors.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        cors.setAllowedHeaders(List.of(HttpHeaders.AUTHORIZATION, HttpHeaders.CONTENT_TYPE));
        cors.setExposedHeaders(List.of(HttpHeaders.LOCATION));
        cors.setMaxAge(Duration.ofHours(1));

        UrlBasedCorsConfigurationSource fuente = new UrlBasedCorsConfigurationSource();
        fuente.registerCorsConfiguration("/api/**", cors);
        return fuente;
    }
}
