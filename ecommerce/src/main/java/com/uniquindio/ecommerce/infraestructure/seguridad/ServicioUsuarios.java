package com.uniquindio.ecommerce.infraestructure.seguridad;

import com.uniquindio.ecommerce.domain.entity.Campesino;
import com.uniquindio.ecommerce.domain.entity.Comprador;
import com.uniquindio.ecommerce.domain.exception.ReglaDeNegocioVioladaException;
import com.uniquindio.ecommerce.domain.repository.CampesinoRepository;
import com.uniquindio.ecommerce.domain.repository.CompradorRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

/**
 * Registro de usuarios. La clave se guarda siempre cifrada con BCrypt.
 *
 * <p>Registrarse como CAMPESINO crea tambien el {@link Campesino} del dominio (sin autorizacion para
 * publicar: la da un ADMIN, regla 1); como COMPRADOR crea el {@link Comprador}. El usuario queda
 * vinculado a ese actor y el token lleva su id, asi nadie actua en nombre de otro.</p>
 */
@Service
@Transactional
public class ServicioUsuarios {

    private final UsuarioJpaRepository usuarios;
    private final PasswordEncoder encoder;
    private final CampesinoRepository campesinos;
    private final CompradorRepository compradores;

    public ServicioUsuarios(UsuarioJpaRepository usuarios, PasswordEncoder encoder,
                            CampesinoRepository campesinos, CompradorRepository compradores) {
        this.usuarios = usuarios;
        this.encoder = encoder;
        this.campesinos = campesinos;
        this.compradores = compradores;
    }

    /** Cualquiera puede registrarse como COMPRADOR o CAMPESINO; el ADMIN solo existe por configuracion. */
    public UsuarioRegistrado registrar(DatosRegistro datos) {
        ReglaDeNegocioVioladaException.validar(datos.rol() == Rol.COMPRADOR || datos.rol() == Rol.CAMPESINO, "INV-ROL",
                "Solo puede registrarse como COMPRADOR o CAMPESINO.");
        String correo = normalizar(datos.correo());
        ReglaDeNegocioVioladaException.validar(!usuarios.existsByCorreo(correo), "INV-CORREO",
                "Ya existe un usuario con el correo " + correo + ".");

        UUID actorId = datos.rol() == Rol.CAMPESINO ? crearCampesino(datos) : crearComprador(datos);
        UsuarioEntity nuevo = usuarios.save(new UsuarioEntity(UUID.randomUUID(), correo,
                encoder.encode(datos.clave()), datos.rol(), actorId));
        return new UsuarioRegistrado(nuevo.getId(), nuevo.getCorreo(), nuevo.getRol(), nuevo.getActorId());
    }

    /** Lo usa el arranque para los usuarios de demostracion (incluido ADMIN). */
    public void crearSiNoExiste(String correo, String clave, Rol rol, UUID actorId) {
        String correoNormalizado = normalizar(correo);
        if (!usuarios.existsByCorreo(correoNormalizado)) {
            usuarios.save(new UsuarioEntity(UUID.randomUUID(), correoNormalizado, encoder.encode(clave), rol, actorId));
        }
    }

    @Transactional(readOnly = true)
    public Optional<UsuarioRegistrado> buscarPorCorreo(String correo) {
        return usuarios.findByCorreo(normalizar(correo))
                .map(u -> new UsuarioRegistrado(u.getId(), u.getCorreo(), u.getRol(), u.getActorId()));
    }

    private UUID crearCampesino(DatosRegistro datos) {
        ReglaDeNegocioVioladaException.validar(datos.numeroIdentificacion() != null
                && !datos.numeroIdentificacion().isBlank(), "INV-REGISTRO",
                "Un campesino debe registrar su numero de identificacion.");
        Campesino campesino = Campesino.builder()
                .nombre(datos.nombre())
                .numeroIdentificacion(datos.numeroIdentificacion().trim())
                .build();
        campesinos.registrar(campesino);
        return campesino.getId().valor();
    }

    private UUID crearComprador(DatosRegistro datos) {
        ReglaDeNegocioVioladaException.validar(datos.municipio() != null && !datos.municipio().isBlank(),
                "INV-REGISTRO", "Un comprador debe registrar el municipio de entrega.");
        Comprador.Builder builder = Comprador.builder().nombre(datos.nombre()).municipio(datos.municipio());
        if (datos.tipoComprador() != null) {
            builder.tipo(datos.tipoComprador());
        }
        Comprador comprador = builder.build();
        compradores.registrar(comprador);
        return comprador.getId().valor();
    }

    static String normalizar(String correo) {
        return correo.trim().toLowerCase(Locale.ROOT);
    }

    public record DatosRegistro(String correo, String clave, Rol rol, String nombre, String numeroIdentificacion,
                                String municipio, Comprador.Tipo tipoComprador) {
    }

    public record UsuarioRegistrado(UUID id, String correo, Rol rol, UUID actorId) {
    }
}
