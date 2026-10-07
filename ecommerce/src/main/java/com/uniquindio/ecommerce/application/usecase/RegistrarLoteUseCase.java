package com.uniquindio.ecommerce.application.usecase;

import com.uniquindio.ecommerce.application.ServicioDeAplicacion;
import com.uniquindio.ecommerce.application.port.in.RegistrarLoteUseCase;
import com.uniquindio.ecommerce.application.port.out.AutorizacionCampesinoPort;
import com.uniquindio.ecommerce.application.port.out.PublicadorEventos;
import com.uniquindio.ecommerce.application.port.out.Reloj;
import com.uniquindio.ecommerce.domain.catalogo.Lote;
import com.uniquindio.ecommerce.domain.exception.ReglaDeNegocioVioladaException;
import com.uniquindio.ecommerce.domain.repository.LoteRepository;
import com.uniquindio.ecommerce.domain.valueobject.identidad.CampesinoId;
import com.uniquindio.ecommerce.domain.valueobject.identidad.LoteId;

import java.util.Objects;

/**
 * Implementacion del caso de uso <b>Registrar lote</b>.
 *
 * <p>Reparto de responsabilidades, que es lo que hace util a esta capa:</p>
 * <ul>
 *   <li><b>Regla 1</b> (campesino autorizado) se resuelve aqui, porque exige
 *       consultar otro agregado.</li>
 *   <li><b>Unicidad del codigo</b> se resuelve aqui, porque exige mirar toda la
 *       coleccion de lotes y ningun lote puede saber si otro comparte su codigo.</li>
 *   <li><b>Reglas 2 y 8</b> y todas las invariantes estructurales se resuelven en
 *       la factoria del agregado, no aqui. El servicio no repite esas validaciones.</li>
 * </ul>
 */
public class RegistrarLoteService extends ServicioDeAplicacion implements RegistrarLoteUseCase {

    private final AutorizacionCampesinoPort autorizacionCampesino;

    public RegistrarLoteService(LoteRepository loteRepository, Reloj reloj, PublicadorEventos publicadorEventos, AutorizacionCampesinoPort autorizacionCampesino) {
        super(loteRepository, reloj, publicadorEventos);
        this.autorizacionCampesino = Objects.requireNonNull(autorizacionCampesino,
                "El puerto de autorizacion de campesinos es obligatorio.");
    }

    @Override
    public LoteId registrarMateriaPrima(RegistrarMateriaPrimaCommand comando) {
        Objects.requireNonNull(comando, "El comando de registro es obligatorio.");
        exigirCampesinoAutorizado(comando.campesino());
        exigirCodigoDisponible(comando.codigo());

        Lote lote = Lote.registrarMateriaPrima(
                comando.codigo(),
                comando.producto(),
                comando.campesino(),
                comando.fichaTrazabilidad(),
                comando.tipoCultivo(),
                comando.cantidadInicial(),
                comando.precioFinca(),
                comando.calibre(),
                comando.temporada(),
                comando.fechaCosecha(),
                comando.vidaUtilDias(),
                comando.merma(),
                reloj.ahora());

        return persistirYPublicar(lote).id();
    }

    @Override
    public LoteId registrarTransformado(RegistrarTransformadoCommand comando) {
        Objects.requireNonNull(comando, "El comando de registro es obligatorio.");
        exigirCampesinoAutorizado(comando.campesino());
        exigirCodigoDisponible(comando.codigo());

        Lote lote = Lote.registrarTransformado(
                comando.codigo(),
                comando.producto(),
                comando.campesino(),
                comando.fichaTrazabilidad(),
                comando.tipoCultivo(),
                comando.cantidadInicial(),
                comando.precioFinca(),
                comando.fichaTransformacion(),
                comando.merma(),
                reloj.ahora());

        return persistirYPublicar(lote).id();
    }

    /** Regla 1: solo un campesino registrado y autorizado puede publicar en la plataforma. */
    private void exigirCampesinoAutorizado(CampesinoId campesino) {
        if (!autorizacionCampesino.estaAutorizadoParaPublicar(campesino)) {
            throw new ReglaDeNegocioVioladaException("R1",
                    "El campesino " + campesino + " no esta registrado y autorizado para publicar en la plataforma.");
        }
    }

    private void exigirCodigoDisponible(String codigo) {
        if (codigo != null && loteRepository.existeCodigo(codigo.trim())) {
            throw new ReglaDeNegocioVioladaException("INV-CODIGO",
                    "Ya existe un lote con el codigo '" + codigo.trim() + "'.");
        }
    }
}
//
///**
// * CU-01: un campesino registra un lote cosechado y opcionalmente lo publica
// * y/o lo ingresa a un punto de acopio en el mismo acto.
// */
//public class RegistrarLoteUseCase {
//
//    private static final int MAX_INTENTOS_CODIGO = 5;
//
//    private final LoteRepository loteRepository;
//    private final CampesinoRepository campesinoRepository;
//    private final ProductoRepository productoRepository;
//    private final PuntoAcopioRepository puntoAcopioRepository;
//    private final Clock reloj;
//
//    public RegistrarLoteUseCase(LoteRepository loteRepository,
//                                CampesinoRepository campesinoRepository,
//                                ProductoRepository productoRepository,
//                                PuntoAcopioRepository puntoAcopioRepository,
//                                Clock reloj) {
//        this.loteRepository = Objects.requireNonNull(loteRepository);
//        this.campesinoRepository = Objects.requireNonNull(campesinoRepository);
//        this.productoRepository = Objects.requireNonNull(productoRepository);
//        this.puntoAcopioRepository = Objects.requireNonNull(puntoAcopioRepository);
//        this.reloj = Objects.requireNonNull(reloj);
//    }
//
//    public Lote ejecutar(RegistrarLoteCommand comando) {
//        Objects.requireNonNull(comando, "El comando es obligatorio");
//        LocalDate hoy = LocalDate.now(reloj);
//
//        validarComando(comando, hoy);
//
//        Campesino campesino = campesinoRepository.buscarPorId(comando.campesinoId())
//                .orElseThrow(() -> new RecursoNoEncontradoException("Campesino", comando.campesinoId()));
//        Producto producto = productoRepository.buscarPorId(comando.productoId())
//                .orElseThrow(() -> new RecursoNoEncontradoException("Producto", comando.productoId()));
//
//        Lote lote = new Lote.Builder()
//                .id(UUID.randomUUID().toString())
//                .codigo(generarCodigoUnico(comando.tipoCultivo(), hoy))
//                .campesino(campesino)
//                .producto(producto)
//                .tipoCultivo(comando.tipoCultivo())
//                .unidadMedida(comando.unidadMedida())
//                .cantidadInicial(comando.cantidad())
//                .precioUnitario(comando.precioUnitario())
//                .fechaCosecha(comando.fechaCosecha())
//                .fechaLimiteConsumo(comando.fechaLimiteConsumo())
//                .fechaRegistro(LocalDateTime.now(reloj))
//                .build();
//
//        if (comando.puntoAcopioId() != null && !comando.puntoAcopioId().isBlank()) {
//            PuntoAcopioId acopio = puntoAcopioRepository.buscarPorId(comando.puntoAcopioId())
//                    .orElseThrow(() -> new RecursoNoEncontradoException("PuntoAcopio", comando.puntoAcopioId()));
//            lote.enviarAPuntoAcopio(acopio, Instant.now(reloj));
//        }
//        if (comando.publicarInmediatamente()) {
//            lote.publicar();
//        }
//
//        return loteRepository.almacenar(lote);
//    }
//
//    private void validarComando(RegistrarLoteCommand c, LocalDate hoy) {
//        ReglaNegocioException.validar(c.campesinoId() != null && !c.campesinoId().isBlank(),
//                "El id del campesino es obligatorio");
//        ReglaNegocioException.validar(c.productoId() != null && !c.productoId().isBlank(),
//                "El id del producto es obligatorio");
//        ReglaNegocioException.validar(c.tipoCultivo() != null, "El tipo de cultivo es obligatorio");
//        ReglaNegocioException.validar(c.unidadMedida() != null, "La unidad de medida es obligatoria");
//        ReglaNegocioException.validar(c.cantidad() != null && c.cantidad().compareTo(BigDecimal.ZERO) > 0,
//                "La cantidad debe ser mayor a cero");
//        ReglaNegocioException.validar(c.precioUnitario() != null
//                && c.precioUnitario().compareTo(BigDecimal.ZERO) > 0, "El precio unitario debe ser mayor a cero");
//        ReglaNegocioException.validar(c.fechaCosecha() != null, "La fecha de cosecha es obligatoria");
//        ReglaNegocioException.validar(!c.fechaCosecha().isAfter(hoy),
//                "La fecha de cosecha no puede estar en el futuro");
//        ReglaNegocioException.validar(c.fechaLimiteConsumo() != null, "La fecha limite de consumo es obligatoria");
//        ReglaNegocioException.validar(c.fechaLimiteConsumo().isAfter(hoy),
//                "No se registra un lote que ya esta vencido");
//        ReglaNegocioException.validar(c.fechaLimiteConsumo().isAfter(c.fechaCosecha()),
//                "La fecha limite de consumo debe ser posterior a la cosecha");
//    }
//
//    /** Formato LOT-<CULTIVO3>-<yyyy>-<sufijo>, verificado contra el repositorio. */
//    private String generarCodigoUnico(TipoCultivo tipoCultivo, LocalDate hoy) {
//        String prefijo = "LOT-" + abreviar(tipoCultivo.name()) + "-" + hoy.getYear() + "-";
//        for (int intento = 0; intento < MAX_INTENTOS_CODIGO; intento++) {
//            String candidato = prefijo + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
//            if (!loteRepository.existeCodigo(candidato)) return candidato;
//        }
//        throw new ReglaNegocioException("No fue posible generar un codigo unico de lote");
//    }
//
//    private String abreviar(String nombre) {
//        return nombre.length() <= 3 ? nombre : nombre.substring(0, 3);
//    }
//
//    /** DTO de entrada. Sin tipos de dominio complejos: el caso de uso resuelve las referencias. */
//    public record RegistrarLoteCommand(
//            String campesinoId,
//            String productoId,
//            TipoCultivo tipoCultivo,
//            UnidadMedida unidadMedida,
//            BigDecimal cantidad,
//            BigDecimal precioUnitario,
//            LocalDate fechaCosecha,
//            LocalDate fechaLimiteConsumo,
//            String puntoAcopioId,
//            boolean publicarInmediatamente
//    ) {}
}