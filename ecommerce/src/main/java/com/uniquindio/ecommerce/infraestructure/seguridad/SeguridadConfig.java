package com.uniquindio.ecommerce.infraestructure.seguridad;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

/**
 * Seguridad con JWT (servidor de recursos OAuth2 de Spring con clave simetrica HS256).
 *
 * <ul>
 *   <li>Sin sesiones: cada peticion trae su token en {@code Authorization: Bearer ...}.</li>
 *   <li>{@code /api/reportes/**} exige rol ADMIN. Sin token responde 401; con otro rol, 403.</li>
 *   <li>El resto de la API sigue abierta mientras el equipo no implemente el registro de usuarios.</li>
 * </ul>
 */
@Configuration
@EnableConfigurationProperties(PropiedadesSeguridad.class)
public class SeguridadConfig {

    /** Claim del token donde viajan los roles. */
    static final String CLAIM_ROLES = "roles";

    @Bean
    public SecurityFilterChain cadenaDeSeguridad(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sesion -> sesion.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // La consola H2 se muestra dentro de un iframe del mismo sitio.
                .headers(cabeceras -> cabeceras.frameOptions(marco -> marco.sameOrigin()))
                .authorizeHttpRequests(rutas -> rutas
                        .requestMatchers("/api/reportes/**").hasRole(Rol.ADMIN.name())
                        .anyRequest().permitAll())
                .oauth2ResourceServer(servidor -> servidor
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(convertidorDeRoles())));
        return http.build();
    }

    /** Lee el claim "roles" del token y lo convierte en autoridades ROLE_ADMIN, ROLE_CAMPESINO... */
    private static JwtAuthenticationConverter convertidorDeRoles() {
        JwtGrantedAuthoritiesConverter roles = new JwtGrantedAuthoritiesConverter();
        roles.setAuthoritiesClaimName(CLAIM_ROLES);
        roles.setAuthorityPrefix("ROLE_");
        JwtAuthenticationConverter convertidor = new JwtAuthenticationConverter();
        convertidor.setJwtGrantedAuthoritiesConverter(roles);
        return convertidor;
    }

    @Bean
    public JwtDecoder jwtDecoder(PropiedadesSeguridad propiedades) {
        return NimbusJwtDecoder.withSecretKey(clave(propiedades)).macAlgorithm(MacAlgorithm.HS256).build();
    }

    @Bean
    public JwtEncoder jwtEncoder(PropiedadesSeguridad propiedades) {
        return new NimbusJwtEncoder(new ImmutableSecret<>(clave(propiedades)));
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /** Usuarios de demostracion en memoria; las claves se guardan cifradas con BCrypt. */
    @Bean
    public UserDetailsService usuarios(PropiedadesSeguridad propiedades, PasswordEncoder encoder) {
        InMemoryUserDetailsManager usuarios = new InMemoryUserDetailsManager();
        for (PropiedadesSeguridad.Usuario usuario : propiedades.usuarios()) {
            usuarios.createUser(User.withUsername(usuario.nombre())
                    .password(encoder.encode(usuario.clave()))
                    .roles(usuario.rol().name())
                    .build());
        }
        return usuarios;
    }

    @Bean
    public AuthenticationManager authenticationManager(UserDetailsService usuarios, PasswordEncoder encoder) {
        DaoAuthenticationProvider proveedor = new DaoAuthenticationProvider(usuarios);
        proveedor.setPasswordEncoder(encoder);
        return new ProviderManager(proveedor);
    }

    private static SecretKey clave(PropiedadesSeguridad propiedades) {
        return new SecretKeySpec(propiedades.jwtSecreto().getBytes(StandardCharsets.UTF_8), "HmacSHA256");
    }
}
