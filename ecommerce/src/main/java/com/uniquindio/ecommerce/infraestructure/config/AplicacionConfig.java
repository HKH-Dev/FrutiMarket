package com.uniquindio.ecommerce.infraestructure.config;

import com.uniquindio.ecommerce.application.port.out.PublicadorEventos;
import com.uniquindio.ecommerce.application.port.out.Reloj;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Implementaciones de los puertos de salida que no son repositorios. */
@Configuration
public class AplicacionConfig {

    @Bean
    public Reloj reloj() {
        return Reloj.delSistema();
    }

    /** Los eventos del dominio se publican como eventos de Spring; cualquier {@code @EventListener} puede oirlos. */
    @Bean
    public PublicadorEventos publicadorEventos(ApplicationEventPublisher publisher) {
        return publisher::publishEvent;
    }
}
