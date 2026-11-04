package com.uniquindio.ecommerce.infraestructure.seguridad;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/** Al arrancar guarda en la base los usuarios de demostracion de application.properties (una vez). */
@Component
class SembradorUsuarios implements ApplicationRunner {

    private final PropiedadesSeguridad propiedades;
    private final ServicioUsuarios servicio;

    SembradorUsuarios(PropiedadesSeguridad propiedades, ServicioUsuarios servicio) {
        this.propiedades = propiedades;
        this.servicio = servicio;
    }

    @Override
    public void run(ApplicationArguments args) {
        for (PropiedadesSeguridad.Usuario usuario : propiedades.usuarios()) {
            servicio.crearSiNoExiste(usuario.correo(), usuario.clave(), usuario.rol(), usuario.actorId());
        }
    }
}