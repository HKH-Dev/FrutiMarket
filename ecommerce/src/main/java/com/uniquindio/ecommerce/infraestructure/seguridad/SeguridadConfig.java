package com.uniquindio.ecommerce.infraestructure.seguridad;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

/**
 * Seguridad con JWT (servidor de recursos OAuth2 de Spring con clave simetrica HS256).
 *
 * <ul>
 *   <li>Sin sesiones: cada peticion trae su token en {@code Authorization: Bearer ...}.</li>
 *   <li>Rutas por rol (sin token responde 401; con token de otro rol, 403): lo que modifica un lote
 *       solo CAMPESINO; compras, reembolsos y "mis compras" solo COMPRADOR; logistica, calidad,
 *       reservas, autorizaciones y reportes solo ADMIN.</li>
 *   <li>Quien actua lo dice el token (claim {@code actorId}), no el cuerpo de la peticion.</li>
 *   <li>Publicas: registro, login, catalogo y consultas (GET), Swagger y consola H2.</li>
 * </ul>
 */
@Configuration
@EnableConfigurationProperties(PropiedadesSeguridad.class)
public class SeguridadConfig {

    /** Claim del token donde viajan los roles. */
    static final String CLAIM_ROLES = "roles";

    @Bean
    public SecurityFilterChain cadenaDeSeguridad(HttpSecurity http) throws Exception {
        RespuestasDeSeguridad respuestas = new RespuestasDeSeguridad();
        http
                .cors(Customizer.withDefaults()) // usa el CorsConfigurationSource de CorsConfig
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sesion -> sesion.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // La consola H2 se muestra dentro de un iframe del mismo sitio.
                .headers(cabeceras -> cabeceras.frameOptions(marco -> marco.sameOrigin()))
                .authorizeHttpRequests(rutas -> rutas
                        // CAMPESINO (vendedor): registra, publica y administra SUS lotes
                        .requestMatchers(HttpMethod.POST, "/api/lotes", "/api/lotes/*/publicar",
                                "/api/lotes/*/desactivar", "/api/lotes/*/reactivar", "/api/lotes/*/merma")
                        .hasRole(Rol.CAMPESINO.name())
                        .requestMatchers(HttpMethod.PUT, "/api/lotes/*/calidad-declarada").hasRole(Rol.CAMPESINO.name())
                        .requestMatchers(HttpMethod.DELETE, "/api/lotes/*").hasRole(Rol.CAMPESINO.name())
                        .requestMatchers(HttpMethod.GET, "/api/lotes/mios").hasRole(Rol.CAMPESINO.name())
                        // COMPRADOR: compra, pide reembolsos y consulta SUS compras
                        .requestMatchers(HttpMethod.POST, "/api/compras", "/api/compras/*/reembolso")
                        .hasRole(Rol.COMPRADOR.name())
                        .requestMatchers(HttpMethod.GET, "/api/compras/mias").hasRole(Rol.COMPRADOR.name())
                        // ADMIN (operador de la plataforma): logistica, calidad, reservas, autorizaciones y reportes
                        .requestMatchers(HttpMethod.POST, "/api/lotes/*/ingresos", "/api/lotes/*/despachos/**",
                                "/api/lotes/*/entrega", "/api/lotes/*/inspecciones", "/api/lotes/*/temperaturas",
                                "/api/lotes/*/incidencias", "/api/lotes/*/revision/validar", "/api/lotes/*/reservas/**",
                                "/api/campesinos/*/autorizacion")
                        .hasRole(Rol.ADMIN.name())
                        .requestMatchers(HttpMethod.DELETE, "/api/lotes/*/reservas/*").hasRole(Rol.ADMIN.name())
                        .requestMatchers("/api/almacenes/**", "/api/reportes/**").hasRole(Rol.ADMIN.name())
                        // Publico: registro, login, catalogo y consultas (GET), Swagger y consola H2
                        .anyRequest().permitAll())
                .oauth2ResourceServer(servidor -> servidor
                        .authenticationEntryPoint(respuestas)
                        .accessDeniedHandler(respuestas)
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

    private static SecretKey clave(PropiedadesSeguridad propiedades) {
        return new SecretKeySpec(propiedades.jwtSecreto().getBytes(StandardCharsets.UTF_8), "HmacSHA256");
    }
}