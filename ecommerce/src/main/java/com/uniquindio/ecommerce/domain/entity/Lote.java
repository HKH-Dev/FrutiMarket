package com.uniquindio.ecommerce.domain.entity;

import com.uniquindio.ecommerce.domain.exception.ReglaNegocioException;
import com.uniquindio.ecommerce.domain.valueobject.EstadoLote;
import com.uniquindio.ecommerce.domain.valueobject.TipoCultivo;
import com.uniquindio.ecommerce.domain.valueobject.UnidadMedida;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Agregado raiz: cantidad de producto homogenea, cosechada por un campesino
 * en una fecha, con una fecha limite de consumo que gobierna el FEFO (regla 16).
 */
public class Lote {

    private final String id;
    private final String codigo;
    private final Campesino campesino;
    private final Producto producto;
    private final TipoCultivo tipoCultivo;
    private final UnidadMedida unidadMedida;
    private final BigDecimal cantidadInicial;
    private final BigDecimal precioUnitario;
    private final LocalDate fechaCosecha;
    private final LocalDate fechaLimiteConsumo;
    private final LocalDateTime fechaRegistro;

    private BigDecimal cantidadDisponible;
    private EstadoLote estado;
    private PuntoAcopio puntoAcopio;

    private Lote(Builder b) {
        this.id = b.id;
        this.codigo = b.codigo;
        this.campesino = b.campesino;
        this.producto = b.producto;
        this.tipoCultivo = b.tipoCultivo;
        this.unidadMedida = b.unidadMedida;
        this.cantidadInicial = b.cantidadInicial;
        this.cantidadDisponible = b.cantidadInicial;
        this.precioUnitario = b.precioUnitario;
        this.fechaCosecha = b.fechaCosecha;
        this.fechaLimiteConsumo = b.fechaLimiteConsumo;
        this.fechaRegistro = b.fechaRegistro;
        this.estado = EstadoLote.REGISTRADO;
        validarInvariantes();
    }

    /** Reconstruccion desde persistencia: no vuelve a aplicar transiciones. */
    public static Lote rehidratar(String id, String codigo, Campesino campesino, Producto producto,
                                  TipoCultivo tipoCultivo, UnidadMedida unidadMedida,
                                  BigDecimal cantidadInicial, BigDecimal cantidadDisponible,
                                  BigDecimal precioUnitario, LocalDate fechaCosecha,
                                  LocalDate fechaLimiteConsumo, LocalDateTime fechaRegistro,
                                  EstadoLote estado, PuntoAcopio puntoAcopio) {
        Lote lote = new Builder()
                .id(id).codigo(codigo).campesino(campesino).producto(producto)
                .tipoCultivo(tipoCultivo).unidadMedida(unidadMedida)
                .cantidadInicial(cantidadInicial).precioUnitario(precioUnitario)
                .fechaCosecha(fechaCosecha).fechaLimiteConsumo(fechaLimiteConsumo)
                .fechaRegistro(fechaRegistro)
                .build();
        lote.cantidadDisponible = cantidadDisponible;
        lote.estado = estado;
        lote.puntoAcopio = puntoAcopio;
        return lote;
    }

    private void validarInvariantes() {
        ReglaNegocioException.validar(id != null && !id.isBlank(), "El lote requiere id");
        ReglaNegocioException.validar(codigo != null && !codigo.isBlank(), "El lote requiere codigo");
        ReglaNegocioException.validar(campesino != null, "El lote requiere un campesino responsable");
        ReglaNegocioException.validar(producto != null, "El lote requiere un producto del catalogo");
        ReglaNegocioException.validar(tipoCultivo != null, "El lote requiere tipo de cultivo");
        ReglaNegocioException.validar(unidadMedida != null, "El lote requiere unidad de medida");
        ReglaNegocioException.validar(cantidadInicial != null
                && cantidadInicial.compareTo(BigDecimal.ZERO) > 0, "La cantidad inicial debe ser mayor a cero");
        ReglaNegocioException.validar(precioUnitario != null
                && precioUnitario.compareTo(BigDecimal.ZERO) > 0, "El precio unitario debe ser mayor a cero");
        ReglaNegocioException.validar(fechaCosecha != null, "El lote requiere fecha de cosecha");
        ReglaNegocioException.validar(fechaLimiteConsumo != null, "El lote requiere fecha limite de consumo");
        ReglaNegocioException.validar(fechaLimiteConsumo.isAfter(fechaCosecha),
                "La fecha limite de consumo debe ser posterior a la cosecha");
    }

    // ---------- comportamiento ----------

    public void publicar() {
        ReglaNegocioException.validar(estado == EstadoLote.REGISTRADO || estado == EstadoLote.EN_ACOPIO,
                "Solo un lote registrado o en acopio puede publicarse. Estado actual: " + estado);
        ReglaNegocioException.validar(tieneDisponibilidad(), "No se publica un lote sin cantidad disponible");
        this.estado = EstadoLote.PUBLICADO;
    }

    public void ingresarAPuntoAcopio(PuntoAcopio destino) {
        ReglaNegocioException.validar(destino != null, "El punto de acopio es obligatorio");
        ReglaNegocioException.validar(estado.esActivo(), "Un lote " + estado + " no puede ingresar a acopio");
        this.puntoAcopio = destino;
        this.estado = EstadoLote.EN_ACOPIO;
    }

    /** Descuenta cantidad por venta o despacho. Pasa a AGOTADO al llegar a cero. */
    public void descontar(BigDecimal cantidad) {
        ReglaNegocioException.validar(cantidad != null && cantidad.compareTo(BigDecimal.ZERO) > 0,
                "La cantidad a descontar debe ser mayor a cero");
        ReglaNegocioException.validar(estado.permiteVenta(),
                "El lote " + codigo + " no admite salidas en estado " + estado);
        ReglaNegocioException.validar(cantidad.compareTo(cantidadDisponible) <= 0,
                "Cantidad solicitada (" + cantidad + ") supera la disponible (" + cantidadDisponible + ")");
        this.cantidadDisponible = cantidadDisponible.subtract(cantidad);
        if (cantidadDisponible.compareTo(BigDecimal.ZERO) == 0) this.estado = EstadoLote.AGOTADO;
    }

    public void marcarVencido(LocalDate hoy) {
        ReglaNegocioException.validar(estaVencido(hoy), "El lote " + codigo + " aun no vence");
        this.estado = EstadoLote.VENCIDO;
    }

    public void cerrar() {
        ReglaNegocioException.validar(estado != EstadoLote.CERRADO, "El lote ya esta cerrado");
        this.estado = EstadoLote.CERRADO;
    }

    public boolean estaVencido(LocalDate hoy) {
        return hoy.isAfter(fechaLimiteConsumo);
    }

    public boolean tieneDisponibilidad() {
        return cantidadDisponible.compareTo(BigDecimal.ZERO) > 0;
    }

    public boolean estaDisponibleParaVenta(LocalDate hoy) {
        return estado.permiteVenta() && tieneDisponibilidad() && !estaVencido(hoy);
    }

    /** Dias restantes para el FEFO; negativo si ya vencio. */
    public long diasParaVencer(LocalDate hoy) {
        return java.time.temporal.ChronoUnit.DAYS.between(hoy, fechaLimiteConsumo);
    }

    public BigDecimal valorInventario() {
        return precioUnitario.multiply(cantidadDisponible);
    }

    // ---------- accesores ----------

    public String getId() { return id; }
    public String getCodigo() { return codigo; }
    public Campesino getCampesino() { return campesino; }
    public Producto getProducto() { return producto; }
    public TipoCultivo getTipoCultivo() { return tipoCultivo; }
    public UnidadMedida getUnidadMedida() { return unidadMedida; }
    public BigDecimal getCantidadInicial() { return cantidadInicial; }
    public BigDecimal getCantidadDisponible() { return cantidadDisponible; }
    public BigDecimal getPrecioUnitario() { return precioUnitario; }
    public LocalDate getFechaCosecha() { return fechaCosecha; }
    public LocalDate getFechaLimiteConsumo() { return fechaLimiteConsumo; }
    public LocalDateTime getFechaRegistro() { return fechaRegistro; }
    public EstadoLote getEstado() { return estado; }
    public PuntoAcopio getPuntoAcopio() { return puntoAcopio; }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Lote other)) return false;
        return id.equals(other.id);
    }

    @Override public int hashCode() { return Objects.hash(id); }

    @Override public String toString() {
        return "Lote{" + codigo + ", " + cantidadDisponible + " " + unidadMedida.getSimbolo()
                + ", vence " + fechaLimiteConsumo + ", " + estado + "}";
    }

    // ---------- builder ----------

    public static class Builder {
        private String id;
        private String codigo;
        private Campesino campesino;
        private Producto producto;
        private TipoCultivo tipoCultivo;
        private UnidadMedida unidadMedida;
        private BigDecimal cantidadInicial;
        private BigDecimal precioUnitario;
        private LocalDate fechaCosecha;
        private LocalDate fechaLimiteConsumo;
        private LocalDateTime fechaRegistro = LocalDateTime.now();

        public Builder id(String v) { this.id = v; return this; }
        public Builder codigo(String v) { this.codigo = v; return this; }
        public Builder campesino(Campesino v) { this.campesino = v; return this; }
        public Builder producto(Producto v) { this.producto = v; return this; }
        public Builder tipoCultivo(TipoCultivo v) { this.tipoCultivo = v; return this; }
        public Builder unidadMedida(UnidadMedida v) { this.unidadMedida = v; return this; }
        public Builder cantidadInicial(BigDecimal v) { this.cantidadInicial = v; return this; }
        public Builder precioUnitario(BigDecimal v) { this.precioUnitario = v; return this; }
        public Builder fechaCosecha(LocalDate v) { this.fechaCosecha = v; return this; }
        public Builder fechaLimiteConsumo(LocalDate v) { this.fechaLimiteConsumo = v; return this; }
        public Builder fechaRegistro(LocalDateTime v) { this.fechaRegistro = v; return this; }

        public Lote build() { return new Lote(this); }
    }
}
