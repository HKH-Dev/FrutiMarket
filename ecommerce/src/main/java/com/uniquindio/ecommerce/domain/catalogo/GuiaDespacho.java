package com.uniquindio.ecommerce.domain.catalogo;

import com.uniquindio.ecommerce.domain.exception.ReglaDeNegocioVioladaException;
import com.uniquindio.ecommerce.domain.valueobject.identidad.AlmacenId;
import com.uniquindio.ecommerce.domain.valueobject.identidad.GuiaDespachoId;
import com.uniquindio.ecommerce.domain.valueobject.identidad.LoteId;
import com.uniquindio.ecommerce.domain.valueobject.logistica.Conservacion;
import com.uniquindio.ecommerce.domain.valueobject.logistica.TipoEmpaque;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Documento que acompana el traslado de un lote entre dos puntos.
 *
 * <p><b>Entidad interna del agregado {@code Lote}:</b> tiene identidad propia y cambia
 * conservandola (se le anotan registros), pero no existe sin su lote. Solo el lote la
 * emite y la modifica; por eso sus metodos de cambio son de visibilidad de paquete.</p>
 *
 * <p>El recorrido se anota con {@link RegistroCustodia}, la misma clase de la cadena de
 * custodia del lote: cada registro creado aqui lleva el id de esta guia, asi la
 * trazabilidad del lote muestra en que traslado ocurrio cada cosa.</p>
 *
 * <p>Invariantes:</p>
 * <ul>
 *   <li><b>R12:</b> un lote que requiere cadena de frio nunca viaja en un empaque que no sea aislante termico.</li>
 *   <li><b>INV-GUIA:</b> una guia cerrada o anulada nunca admite registros nuevos.</li>
 *   <li><b>INV-GUIA:</b> los registros siempre se anotan en orden cronologico.</li>
 *   <li><b>INV-GUIA:</b> una guia cerrada nunca puede anularse.</li>
 * </ul>
 */
public class GuiaDespacho {

    public enum Estado {
        EMITIDA, EN_RUTA, CERRADA, ANULADA;

        public boolean permiteRegistros() {
            return this == EMITIDA || this == EN_RUTA;
        }
    }

    private final GuiaDespachoId id;
    private final String numero;
    private final LoteId lote;
    private final AlmacenId almacenOrigen;
    private final String origen;
    private final String destino;
    private final TipoEmpaque empaque;
    private final Duration tiempoTransitoEstimado;
    private final Instant fechaEmision;
    private final List<RegistroCustodia> registros;
    private Estado estado;
    private Instant fechaCierre;

    private GuiaDespacho(GuiaDespachoId id, String numero, LoteId lote, AlmacenId almacenOrigen, String origen,
                         String destino, TipoEmpaque empaque, Duration tiempoTransitoEstimado, Instant fechaEmision) {
        this.id = id;
        this.numero = numero;
        this.lote = lote;
        this.almacenOrigen = almacenOrigen;
        this.origen = origen;
        this.destino = destino;
        this.empaque = empaque;
        this.tiempoTransitoEstimado = tiempoTransitoEstimado;
        this.fechaEmision = fechaEmision;
        this.registros = new ArrayList<>();
        this.estado = Estado.EMITIDA;
    }

    /**
     * Emite la guia y anota la salida del origen como primer registro.
     * Solo el agregado {@code Lote} puede emitir guias.
     */
    static GuiaDespacho emitir(LoteId lote, AlmacenId almacenOrigen, String origen, String destino,
                               TipoEmpaque empaque, Conservacion conservacion, Duration tiempoTransito,
                               String responsable, Instant momento) {
        ReglaDeNegocioVioladaException.validar(lote != null, "INV-GUIA", "La guia debe referirse a un lote.");
        ReglaDeNegocioVioladaException.validar(origen != null && !origen.isBlank(), "INV-GUIA",
                "La guia de despacho debe indicar el origen.");
        ReglaDeNegocioVioladaException.validar(destino != null && !destino.isBlank(), "INV-GUIA",
                "La guia de despacho debe indicar el destino.");
        ReglaDeNegocioVioladaException.validar(empaque != null && conservacion != null, "R12",
                "La guia debe declarar el tipo de empaque y la conservacion del lote.");
        ReglaDeNegocioVioladaException.validar(!conservacion.requiereCadenaFrio() || empaque.esAislanteTermico(), "R12",
                "El lote requiere cadena de frio (" + conservacion.rango() + ") y el empaque "
                        + empaque.etiqueta() + " no es aislante termico.");
        ReglaDeNegocioVioladaException.validar(tiempoTransito != null && !tiempoTransito.isNegative()
                && !tiempoTransito.isZero(), "R10", "La guia debe declarar un tiempo de transito positivo.");
        ReglaDeNegocioVioladaException.validar(momento != null, "INV-GUIA", "La guia debe tener fecha de emision.");

        GuiaDespachoId id = GuiaDespachoId.nuevo();
        String numero = "GD-" + id.valor().toString().substring(0, 8).toUpperCase();
        GuiaDespacho guia = new GuiaDespacho(id, numero, lote, almacenOrigen, origen.trim(), destino.trim(),
                empaque, tiempoTransito, momento);
        guia.registrar(RegistroCustodia.Tipo.SALIDA_ALMACEN, guia.origen, responsable, momento, null,
                "Hacia " + guia.destino + " en " + empaque.etiqueta());
        return guia;
    }

    /**
     * Anota un paso del recorrido y lo devuelve, para que el lote lo agregue tambien a su
     * cadena de custodia. Un registro de entrega cierra la guia.
     */
    RegistroCustodia registrar(RegistroCustodia.Tipo tipo, String lugar, String responsable, Instant momento,
                               BigDecimal temperaturaC, String observacion) {
        ReglaDeNegocioVioladaException.validar(estado.permiteRegistros(), "INV-GUIA",
                "La guia " + numero + " esta " + estado + " y no admite registros nuevos.");
        ReglaDeNegocioVioladaException.validar(tipo != RegistroCustodia.Tipo.COSECHA
                        && tipo != RegistroCustodia.Tipo.INGRESO_ALMACEN, "INV-GUIA",
                "Una guia solo registra lo que ocurre durante el traslado.");
        ReglaDeNegocioVioladaException.validar(momento != null && ultimoRegistro()
                        .map(ultimo -> !momento.isBefore(ultimo.momento())).orElse(true), "INV-GUIA",
                "Un registro no puede anotarse antes del registro anterior de la misma guia.");

        AlmacenId almacen = tipo == RegistroCustodia.Tipo.SALIDA_ALMACEN ? almacenOrigen : null;
        RegistroCustodia registro = RegistroCustodia.crear(tipo, almacen, id, lugar, responsable, momento,
                temperaturaC, observacion);
        registros.add(registro);
        if (estado == Estado.EMITIDA && tipo != RegistroCustodia.Tipo.SALIDA_ALMACEN) {
            estado = Estado.EN_RUTA;
        }
        if (tipo.esTerminal()) {
            cerrar(momento);
        }
        return registro;
    }

    /** Cierra la guia cuando el lote llega a un almacenamiento de destino. */
    void cerrar(Instant momento) {
        if (estado == Estado.CERRADA) {
            return;
        }
        ReglaDeNegocioVioladaException.validar(estado != Estado.ANULADA, "INV-GUIA", "Una guia anulada no puede cerrarse.");
        this.estado = Estado.CERRADA;
        this.fechaCierre = momento;
    }

    void anular(Instant momento) {
        ReglaDeNegocioVioladaException.validar(estado != Estado.CERRADA, "INV-GUIA", "Una guia cerrada no puede anularse.");
        this.estado = Estado.ANULADA;
        this.fechaCierre = momento;
    }

    public Optional<RegistroCustodia> ultimoRegistro() {
        return registros.isEmpty() ? Optional.empty() : Optional.of(registros.get(registros.size() - 1));
    }

    public boolean registroPasoPor(RegistroCustodia.Tipo tipo) {
        return registros.stream().anyMatch(registro -> registro.tipo() == tipo);
    }

    public boolean estaAbierta() {
        return estado.permiteRegistros();
    }

    /** Llegada estimada: la usa el caso de uso para comprobar la ventana de entrega del comprador. */
    public Instant llegadaEstimada() {
        return fechaEmision.plus(tiempoTransitoEstimado);
    }

    public GuiaDespachoId id() { return id; }
    public String numero() { return numero; }
    public LoteId lote() { return lote; }
    public Optional<AlmacenId> almacenOrigen() { return Optional.ofNullable(almacenOrigen); }
    public String origen() { return origen; }
    public String destino() { return destino; }
    public TipoEmpaque empaque() { return empaque; }
    public Duration tiempoTransitoEstimado() { return tiempoTransitoEstimado; }
    public Instant fechaEmision() { return fechaEmision; }
    public Optional<Instant> fechaCierre() { return Optional.ofNullable(fechaCierre); }
    public Estado estado() { return estado; }
    public List<RegistroCustodia> registros() { return List.copyOf(registros); }

    @Override
    public boolean equals(Object o) {
        return o instanceof GuiaDespacho otra && id.equals(otra.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return "GuiaDespacho[" + numero + ", " + origen + " -> " + destino + ", " + estado + "]";
    }
}
