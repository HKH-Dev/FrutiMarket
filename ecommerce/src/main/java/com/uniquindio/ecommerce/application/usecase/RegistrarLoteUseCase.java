package com.uniquindio.ecommerce.application.usecase;

import com.uniquindio.ecommerce.domain.entity.Campesino;
import com.uniquindio.ecommerce.domain.entity.Lote;
import com.uniquindio.ecommerce.domain.entity.Producto;
import com.uniquindio.ecommerce.domain.entity.PuntoAcopio;
import com.uniquindio.ecommerce.domain.exception.RecursoNoEncontradoException;
import com.uniquindio.ecommerce.domain.exception.ReglaNegocioException;
import com.uniquindio.ecommerce.domain.repository.CampesinoRepository;
import com.uniquindio.ecommerce.domain.repository.LoteRepository;
import com.uniquindio.ecommerce.domain.repository.ProductoRepository;
import com.uniquindio.ecommerce.domain.repository.PuntoAcopioRepository;
import com.uniquindio.ecommerce.domain.valueobject.TipoCultivo;
import com.uniquindio.ecommerce.domain.valueobject.UnidadMedida;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * CU-01: un campesino registra un lote cosechado y opcionalmente lo publica
 * y/o lo ingresa a un punto de acopio en el mismo acto.
 */
public class RegistrarLoteUseCase {

    private static final int MAX_INTENTOS_CODIGO = 5;

    private final LoteRepository loteRepository;
    private final CampesinoRepository campesinoRepository;
    private final ProductoRepository productoRepository;
    private final PuntoAcopioRepository puntoAcopioRepository;
    private final Clock reloj;

    public RegistrarLoteUseCase(LoteRepository loteRepository,
                                CampesinoRepository campesinoRepository,
                                ProductoRepository productoRepository,
                                PuntoAcopioRepository puntoAcopioRepository,
                                Clock reloj) {
        this.loteRepository = Objects.requireNonNull(loteRepository);
        this.campesinoRepository = Objects.requireNonNull(campesinoRepository);
        this.productoRepository = Objects.requireNonNull(productoRepository);
        this.puntoAcopioRepository = Objects.requireNonNull(puntoAcopioRepository);
        this.reloj = Objects.requireNonNull(reloj);
    }

    public Lote ejecutar(RegistrarLoteCommand comando) {
        Objects.requireNonNull(comando, "El comando es obligatorio");
        LocalDate hoy = LocalDate.now(reloj);

        validarComando(comando, hoy);

        Campesino campesino = campesinoRepository.buscarPorId(comando.campesinoId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Campesino", comando.campesinoId()));
        Producto producto = productoRepository.buscarPorId(comando.productoId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Producto", comando.productoId()));

        Lote lote = new Lote.Builder()
                .id(UUID.randomUUID().toString())
                .codigo(generarCodigoUnico(comando.tipoCultivo(), hoy))
                .campesino(campesino)
                .producto(producto)
                .tipoCultivo(comando.tipoCultivo())
                .unidadMedida(comando.unidadMedida())
                .cantidadInicial(comando.cantidad())
                .precioUnitario(comando.precioUnitario())
                .fechaCosecha(comando.fechaCosecha())
                .fechaLimiteConsumo(comando.fechaLimiteConsumo())
                .fechaRegistro(LocalDateTime.now(reloj))
                .build();

        if (comando.puntoAcopioId() != null && !comando.puntoAcopioId().isBlank()) {
            PuntoAcopio acopio = puntoAcopioRepository.buscarPorId(comando.puntoAcopioId())
                    .orElseThrow(() -> new RecursoNoEncontradoException("PuntoAcopio", comando.puntoAcopioId()));
            lote.ingresarAPuntoAcopio(acopio);
        }
        if (comando.publicarInmediatamente()) {
            lote.publicar();
        }

        return loteRepository.almacenar(lote);
    }

    private void validarComando(RegistrarLoteCommand c, LocalDate hoy) {
        ReglaNegocioException.validar(c.campesinoId() != null && !c.campesinoId().isBlank(),
                "El id del campesino es obligatorio");
        ReglaNegocioException.validar(c.productoId() != null && !c.productoId().isBlank(),
                "El id del producto es obligatorio");
        ReglaNegocioException.validar(c.tipoCultivo() != null, "El tipo de cultivo es obligatorio");
        ReglaNegocioException.validar(c.unidadMedida() != null, "La unidad de medida es obligatoria");
        ReglaNegocioException.validar(c.cantidad() != null && c.cantidad().compareTo(BigDecimal.ZERO) > 0,
                "La cantidad debe ser mayor a cero");
        ReglaNegocioException.validar(c.precioUnitario() != null
                && c.precioUnitario().compareTo(BigDecimal.ZERO) > 0, "El precio unitario debe ser mayor a cero");
        ReglaNegocioException.validar(c.fechaCosecha() != null, "La fecha de cosecha es obligatoria");
        ReglaNegocioException.validar(!c.fechaCosecha().isAfter(hoy),
                "La fecha de cosecha no puede estar en el futuro");
        ReglaNegocioException.validar(c.fechaLimiteConsumo() != null, "La fecha limite de consumo es obligatoria");
        ReglaNegocioException.validar(c.fechaLimiteConsumo().isAfter(hoy),
                "No se registra un lote que ya esta vencido");
        ReglaNegocioException.validar(c.fechaLimiteConsumo().isAfter(c.fechaCosecha()),
                "La fecha limite de consumo debe ser posterior a la cosecha");
    }

    /** Formato LOT-<CULTIVO3>-<yyyy>-<sufijo>, verificado contra el repositorio. */
    private String generarCodigoUnico(TipoCultivo tipoCultivo, LocalDate hoy) {
        String prefijo = "LOT-" + abreviar(tipoCultivo.name()) + "-" + hoy.getYear() + "-";
        for (int intento = 0; intento < MAX_INTENTOS_CODIGO; intento++) {
            String candidato = prefijo + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
            if (!loteRepository.existeCodigo(candidato)) return candidato;
        }
        throw new ReglaNegocioException("No fue posible generar un codigo unico de lote");
    }

    private String abreviar(String nombre) {
        return nombre.length() <= 3 ? nombre : nombre.substring(0, 3);
    }

    /** DTO de entrada. Sin tipos de dominio complejos: el caso de uso resuelve las referencias. */
    public record RegistrarLoteCommand(
            String campesinoId,
            String productoId,
            TipoCultivo tipoCultivo,
            UnidadMedida unidadMedida,
            BigDecimal cantidad,
            BigDecimal precioUnitario,
            LocalDate fechaCosecha,
            LocalDate fechaLimiteConsumo,
            String puntoAcopioId,
            boolean publicarInmediatamente
    ) {}
}