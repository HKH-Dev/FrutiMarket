package com.uniquindio.ecommerce.domain.comercial;

import com.uniquindio.ecommerce.domain.exception.ReglaDeNegocioVioladaException;
import com.uniquindio.ecommerce.domain.valueobject.identidad.ProductoId;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

/**
 * Campana de descuento por temporada. Si no tiene producto, aplica a todo el catalogo.
 *
 * <p>Invariantes: <b>R21</b> el porcentaje va de 1 a 70; <b>R22</b> la vigencia no puede terminar antes de empezar.</p>
 */
public final class DescuentoTemporada {

    public static final int PORCENTAJE_MAXIMO = 70;

    private final UUID id;
    private final String nombre;
    private final ProductoId producto;
    private final int porcentaje;
    private final LocalDate inicio;
    private final LocalDate fin;
    private boolean activo;

    private DescuentoTemporada(UUID id, String nombre, ProductoId producto, int porcentaje, LocalDate inicio,
                               LocalDate fin, boolean activo) {
        this.id = id;
        this.nombre = nombre;
        this.producto = producto;
        this.porcentaje = porcentaje;
        this.inicio = inicio;
        this.fin = fin;
        this.activo = activo;
    }

    public static DescuentoTemporada crear(String nombre, ProductoId producto, int porcentaje, LocalDate inicio, LocalDate fin) {
        ReglaDeNegocioVioladaException.validar(nombre != null && !nombre.isBlank(), "R21", "La campana requiere un nombre.");
        ReglaDeNegocioVioladaException.validar(porcentaje >= 1 && porcentaje <= PORCENTAJE_MAXIMO, "R21",
                "El descuento debe estar entre 1% y " + PORCENTAJE_MAXIMO + "%.");
        ReglaDeNegocioVioladaException.validar(inicio != null && fin != null && !fin.isBefore(inicio), "R22",
                "La fecha final de la campana no puede ser anterior a la inicial.");
        return new DescuentoTemporada(UUID.randomUUID(), nombre.trim(), producto, porcentaje, inicio, fin, true);
    }

    /** Solo para la capa de persistencia. */
    public static DescuentoTemporada reconstituir(UUID id, String nombre, ProductoId producto, int porcentaje,
                                                  LocalDate inicio, LocalDate fin, boolean activo) {
        return new DescuentoTemporada(id, nombre, producto, porcentaje, inicio, fin, activo);
    }

    public boolean vigenteEl(LocalDate fecha) {
        return activo && !fecha.isBefore(inicio) && !fecha.isAfter(fin);
    }

    public boolean aplicaA(ProductoId candidato, LocalDate fecha) {
        return vigenteEl(fecha) && (producto == null || producto.equals(candidato));
    }

    public void desactivar() {
        this.activo = false;
    }

    public UUID id() { return id; }
    public String nombre() { return nombre; }
    public Optional<ProductoId> producto() { return Optional.ofNullable(producto); }
    public int porcentaje() { return porcentaje; }
    public LocalDate inicio() { return inicio; }
    public LocalDate fin() { return fin; }
    public boolean activo() { return activo; }
}
