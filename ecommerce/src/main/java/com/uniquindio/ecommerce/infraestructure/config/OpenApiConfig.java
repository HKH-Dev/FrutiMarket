package com.uniquindio.ecommerce.infraestructure.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Datos que muestra Swagger UI en /swagger-ui.html. */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI frutiMarketOpenApi() {
        // Agrega el boton "Authorize" de Swagger para enviar el token JWT en las rutas protegidas.
        return new OpenAPI()
                .components(new Components().addSecuritySchemes("bearerAuth", new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP).scheme("bearer").bearerFormat("JWT")
                        .description("Token de POST /api/auth/login (sin la palabra Bearer)")))
                .info(new Info()
                .title("FrutiMarket API")
                .description("Comercio de lotes campesinos: registro, publicacion, logistica, calidad y compras.")
                .version("0.0.1")
                .contact(new Contact().name("Programacion Avanzada - Universidad del Quindio")));
    }
}
