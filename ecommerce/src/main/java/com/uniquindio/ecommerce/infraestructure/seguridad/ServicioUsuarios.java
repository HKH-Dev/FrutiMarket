package com.uniquindio.ecommerce.infraestructure.seguridad;

import com.uniquindio.ecommerce.domain.exception.ReglaDeNegocioVioladaException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.UUID;

/** Registro de usuarios. La clave se guarda siempre cifrada con BCrypt. */
@Service
@Transactional
public class ServicioUsuarios {

    private final UsuarioJpaRepository usuarios;
    private final PasswordEncoder encoder;

    public ServicioUsuarios(UsuarioJpaRepository usuarios, PasswordEncoder encoder) {
        this.usuarios = usuarios;
        this.encoder = encoder;
    }

    /** Cualquiera puede registrarse como COMPRADOR o CAMPESINO; el ADMIN solo existe por configuracion. */
    public UsuarioRegistrado registrar(String correo, String clave, Rol rol) {
        ReglaDeNegocioVioladaException.validar(rol == Rol.COMPRADOR || rol == Rol.CAMPESINO, "INV-ROL",
                "Solo puede registrarse como COMPRADOR o CAMPESINO.");
        String correoNormalizado = normalizar(correo);
        ReglaDeNegocioVioladaException.validar(!usuarios.existsByCorreo(correoNormalizado), "INV-CORREO",
                "Ya existe un usuario con el correo " + correoNormalizado + ".");
        UsuarioEntity nuevo = usuarios.save(new UsuarioEntity(UUID.randomUUID(), correoNormalizado,
                encoder.encode(clave), rol));
        return new UsuarioRegistrado(nuevo.getId(), nuevo.getCorreo(), nuevo.getRol());
    }

    /** Lo usa el arranque para los usuarios de demostracion (incluido ADMIN). */
    public void crearSiNoExiste(String correo, String clave, Rol rol) {
        String correoNormalizado = normalizar(correo);
        if (!usuarios.existsByCorreo(correoNormalizado)) {
            usuarios.save(new UsuarioEntity(UUID.randomUUID(), correoNormalizado, encoder.encode(clave), rol));
        }
    }

    static String normalizar(String correo) {
        return correo.trim().toLowerCase(Locale.ROOT);
    }

    public record UsuarioRegistrado(UUID id, String correo, Rol rol) {
    }
}