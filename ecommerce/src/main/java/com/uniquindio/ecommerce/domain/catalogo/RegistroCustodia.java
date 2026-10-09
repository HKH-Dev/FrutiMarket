package com.uniquindio.ecommerce.domain.catalogo;

import com.uniquindio.ecommerce.domain.exception.ReglaDeNegocioVioladaException;
import com.uniquindio.ecommerce.domain.valueobject.identidad.AlmacenId;
import com.uniquindio.ecommerce.domain.valueobject.identidad.GuiaDespachoId;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

/**
 * Value Object interno del {@link Lote}: un eslabon de la cadena de custodia.
 *
 * <p>Reemplaza a los hitos de despacho, el tipo de hito, la ubicacion y la ventana de
 * entrega. La lista de registros de un lote es su trazabilidad completa: quien lo tuvo,
 * donde, cuando y que se hizo con el. Los registros hechos durante un traslado llevan
 * el id de la {@link GuiaDespacho} que los acompana. Nunca se corrige un registro:
 * solo se agregan nuevos.</p>
 */
public final class RegistroCustodia {

    public enum Tipo {
        COSECHA("Cosecha en finca"),
        INGRESO_ALMACEN("Ingreso a almacenamiento"),
        SALIDA_ALMACEN("Salida de almacenamiento"),
        CONTROL_TEMPERATURA("Control de temperatura"),
        CONTROL_CALIDAD("Inspeccion de calidad"),
        INCIDENCIA("Incidencia"),
        ENTREGA("Entrega al comprador");

        private final String etiqueta;

        Tipo(String etiqueta) {
            this.etiqueta = etiqueta;
        }

        public String etiqueta() {
            return etiqueta;
        }

        /** Un registro terminal cierra la guia de despacho en la que se anota. */
        public boolean esTerminal() {
            return this == ENTREGA;
        }
    }

    private final Tipo tipo;
    private final AlmacenId almacen;
    private final GuiaDespachoId guia;
    private final String lugar;
    private final String responsable;
    private final Instant momento;
    private final BigDecimal temperaturaC;
    private final String observacion;

    private RegistroCustodia(Tipo tipo, AlmacenId almacen, GuiaDespachoId guia, String lugar, String responsable,
                             Instant momento, BigDecimal temperaturaC, String observacion) {
        this.tipo = tipo;
        this.almacen = almacen;
        this.guia = guia;
        this.lugar = lugar;
        this.responsable = responsable;
        this.momento = momento;
        this.temperaturaC = temperaturaC;
        this.observacion = observacion;
    }

    /** Registro fuera de un traslado (cosecha, almacenamiento, inspeccion). Solo el lote los crea. */
    static RegistroCustodia crear(Tipo tipo, AlmacenId almacen, String lugar, String responsable,
                                  Instant momento, BigDecimal temperaturaC, String observacion) {
        return crear(tipo, almacen, null, lugar, responsable, momento, temperaturaC, observacion);
    }

    /** Registro anotado en una guia de despacho durante un traslado. */
    static RegistroCustodia crear(Tipo tipo, AlmacenId almacen, GuiaDespachoId guia, String lugar, String responsable,
                                  Instant momento, BigDecimal temperaturaC, String observacion) {
        ReglaDeNegocioVioladaException.validar(tipo != null, "INV-CUSTODIA", "El registro de custodia debe tener tipo.");
        ReglaDeNegocioVioladaException.validar(momento != null, "INV-CUSTODIA", "El registro de custodia debe tener momento.");
        ReglaDeNegocioVioladaException.validar(responsable != null && !responsable.isBlank(), "INV-CUSTODIA",
                "Todo registro de custodia debe indicar quien es el responsable.");
        ReglaDeNegocioVioladaException.validar(tipo != Tipo.COSECHA || guia == null, "INV-CUSTODIA",
                "La cosecha ocurre en la finca, antes de cualquier guia de despacho.");
        String sitio = (lugar == null || lugar.isBlank()) ? "No registrado" : lugar.trim();
        String nota = (observacion == null) ? "" : observacion.trim();
        return new RegistroCustodia(tipo, almacen, guia, sitio, responsable.trim(), momento, temperaturaC, nota);
    }

    public boolean esDeLaGuia(GuiaDespachoId otraGuia) {
        return guia != null && guia.equals(otraGuia);
    }

    public Tipo tipo() { return tipo; }
    public Optional<AlmacenId> almacen() { return Optional.ofNullable(almacen); }
    public Optional<GuiaDespachoId> guia() { return Optional.ofNullable(guia); }
    public String lugar() { return lugar; }
    public String responsable() { return responsable; }
    public Instant momento() { return momento; }
    public Optional<BigDecimal> temperaturaC() { return Optional.ofNullable(temperaturaC); }
    public String observacion() { return observacion; }

    @Override
    public boolean equals(Object o) {
        return o instanceof RegistroCustodia otro && tipo == otro.tipo && Objects.equals(almacen, otro.almacen)
                && Objects.equals(guia, otro.guia) && lugar.equals(otro.lugar) && responsable.equals(otro.responsable)
                && momento.equals(otro.momento) && Objects.equals(temperaturaC, otro.temperaturaC)
                && observacion.equals(otro.observacion);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tipo, almacen, guia, lugar, responsable, momento, temperaturaC, observacion);
    }

    @Override
    public String toString() {
        return momento + " - " + tipo.etiqueta() + " en " + lugar + " (" + responsable + ")"
                + (observacion.isEmpty() ? "" : ": " + observacion);
    }
}
