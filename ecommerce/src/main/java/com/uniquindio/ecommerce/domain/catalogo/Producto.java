package com.uniquindio.ecommerce.domain.catalogo;

import com.uniquindio.ecommerce.domain.exception.ReglaDeNegocioVioladaException;
import com.uniquindio.ecommerce.domain.valueobject.catalogo.UnidadMedida;
import com.uniquindio.ecommerce.domain.valueobject.identidad.ProductoId;
import com.uniquindio.ecommerce.domain.valueobject.logistica.Conservacion;
import com.uniquindio.ecommerce.domain.valueobject.origenycalidad.TemporadaCosecha;
import com.uniquindio.ecommerce.domain.valueobject.origenycalidad.TipoCultivo;

import java.time.LocalDate;

/**
 * Agregado raiz del catalogo: lo que se vende (mango tommy, mora, mermelada de mora).
 * Varios campesinos publican lotes de un mismo producto; el producto define lo que
 * todos sus lotes comparten: unidad de venta, temporada, vida util y conservacion.
 *
 * <p>Invariantes:</p>
 * <ul>
 *   <li>Nunca puede tener vida util negativa.</li>
 *   <li>Un producto TRANSFORMADO siempre esta disponible todo el ano (no depende de cosecha).</li>
 *   <li>Un producto inactivo nunca admite lotes nuevos (lo comprueba el {@code Builder} de {@link Lote}).</li>
 * </ul>
 */
public class Producto {

    /** Linea del catalogo: el producto se vende como sale del campo o procesado. */
    public enum Linea { MATERIA_PRIMA, TRANSFORMADO }

    private final ProductoId id;
    private final String nombre;
    private final Linea linea;
    private final TipoCultivo tipoCultivo;
    private final UnidadMedida unidadVenta;
    private final TemporadaCosecha temporada;
    private final Conservacion conservacion;
    private int vidaUtilDias;
    private boolean activo;

    private Producto(Builder builder) {
        this.id = builder.id;
        this.nombre = builder.nombre;
        this.linea = builder.linea;
        this.tipoCultivo = builder.tipoCultivo;
        this.unidadVenta = builder.unidadVenta;
        this.temporada = builder.temporada;
        this.conservacion = builder.conservacion;
        this.vidaUtilDias = builder.vidaUtilDias;
        this.activo = true;
    }

    public static Builder builder() {
        return new Builder();
    }

    public void actualizarVidaUtil(int dias) {
        ReglaDeNegocioVioladaException.validar(dias >= 0, "INV-PRODUCTO", "La vida util no puede ser negativa.");
        this.vidaUtilDias = dias;
    }

    public void desactivar() {
        this.activo = false;
    }

    public void activar() {
        this.activo = true;
    }

    public boolean estaEnTemporada(LocalDate fecha) {
        return temporada.contiene(fecha);
    }

    public boolean esPerecedero() {
        return vidaUtilDias > 0;
    }

    public ProductoId id() { return id; }
    public String nombre() { return nombre; }
    public Linea linea() { return linea; }
    public TipoCultivo tipoCultivo() { return tipoCultivo; }
    public UnidadMedida unidadVenta() { return unidadVenta; }
    public TemporadaCosecha temporada() { return temporada; }
    public Conservacion conservacion() { return conservacion; }
    public int vidaUtilDias() { return vidaUtilDias; }
    public boolean estaActivo() { return activo; }

    @Override
    public boolean equals(Object o) {
        return o instanceof Producto otro && id.equals(otro.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return nombre + " (" + linea + ", " + unidadVenta.simbolo() + ")";
    }

    public static final class Builder {
        private ProductoId id = ProductoId.nuevo();
        private String nombre;
        private Linea linea = Linea.MATERIA_PRIMA;
        private TipoCultivo tipoCultivo;
        private UnidadMedida unidadVenta;
        private TemporadaCosecha temporada = TemporadaCosecha.permanente();
        private Conservacion conservacion = Conservacion.AMBIENTE;
        private int vidaUtilDias;

        private Builder() {
        }

        public Builder id(ProductoId id) { this.id = id; return this; }
        public Builder nombre(String nombre) { this.nombre = nombre; return this; }
        public Builder linea(Linea linea) { this.linea = linea; return this; }
        public Builder tipoCultivo(TipoCultivo tipoCultivo) { this.tipoCultivo = tipoCultivo; return this; }
        public Builder unidadVenta(UnidadMedida unidad) { this.unidadVenta = unidad; return this; }
        public Builder temporada(TemporadaCosecha temporada) { this.temporada = temporada; return this; }
        public Builder conservacion(Conservacion conservacion) { this.conservacion = conservacion; return this; }
        public Builder vidaUtilDias(int dias) { this.vidaUtilDias = dias; return this; }

        public Producto build() {
            ReglaDeNegocioVioladaException.validar(id != null, "INV-PRODUCTO", "El producto requiere id.");
            ReglaDeNegocioVioladaException.validar(nombre != null && !nombre.isBlank(), "INV-PRODUCTO",
                    "El producto requiere nombre.");
            ReglaDeNegocioVioladaException.validar(linea != null && tipoCultivo != null && unidadVenta != null,
                    "INV-PRODUCTO", "El producto requiere linea, tipo de cultivo y unidad de venta.");
            ReglaDeNegocioVioladaException.validar(temporada != null && conservacion != null, "INV-PRODUCTO",
                    "El producto requiere temporada y conservacion.");
            ReglaDeNegocioVioladaException.validar(vidaUtilDias >= 0, "INV-PRODUCTO", "La vida util no puede ser negativa.");
            ReglaDeNegocioVioladaException.validar(linea == Linea.MATERIA_PRIMA || temporada.esTodoElAnio(), "INV-PRODUCTO",
                    "Un producto transformado no depende de temporada de cosecha.");
            nombre = nombre.trim();
            return new Producto(this);
        }
    }
}
