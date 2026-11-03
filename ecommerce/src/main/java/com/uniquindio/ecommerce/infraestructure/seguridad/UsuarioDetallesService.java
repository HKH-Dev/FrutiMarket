package com.uniquindio.ecommerce.infraestructure.seguridad;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Le dice a Spring Security como buscar un usuario por correo: en la tabla usuarios. */
@Service
public class UsuarioDetallesService implements UserDetailsService {

    private final UsuarioJpaRepository usuarios;

    public UsuarioDetallesService(UsuarioJpaRepository usuarios) {
        this.usuarios = usuarios;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String correo) throws UsernameNotFoundException {
        UsuarioEntity usuario = usuarios.findByCorreo(ServicioUsuarios.normalizar(correo))
                .orElseThrow(() -> new UsernameNotFoundException("No existe el usuario " + correo));
        return User.withUsername(usuario.getCorreo())
                .password(usuario.getClaveHash()) // ya viene cifrada con BCrypt
                .roles(usuario.getRol().name())
                .disabled(!usuario.isActivo())
                .build();
    }
}