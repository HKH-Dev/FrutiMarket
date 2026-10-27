package com.uniquindio.ecommerce.infraestructure.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Datos que muestra Swagger UI en /swagger-ui.html. */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI frutiMarketOpenApi() {
        return new OpenAPI().info(new Info()
                .title("FrutiMarket API")
                .description("Comercio de lotes campesinos: registro, publicacion, logistica, calidad y compras.")
                .version("0.0.1")
                .contact(new Contact().name("Programacion Avanzada - Universidad del Quindio")));
    }
}
