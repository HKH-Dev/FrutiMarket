package com.uniquindio.ecommerce.domain.entity;

import com.uniquindio.ecommerce.domain.exception.ReglaDeNegocioVioladaException;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Entidad: dos campesinos son el mismo si comparten {@code campesinoId}, aunque
 * el resto de sus datos difiera. Por eso no usa {@code @Data} de Lombok, que
 * compararia todos los campos.
 */
public class Campesino {

    private final UUID campesinoId;
    private final String numeroIdentificacion;
    private double escalaProduccion;

    public Campesino(UUID campesinoId, String numeroIdentificacion, double escalaProduccion) {
        this.campesinoId = Objects.requireNonNull(campesinoId, "El campesino requiere un identificador.");
        if (numeroIdentificacion == null || numeroIdentificacion.isBlank()) {
            throw new ReglaDeNegocioVioladaException("INV-CAMPESINO",
                    "El campesino requiere numero de identificacion.");
        }
        exigirEscalaValida(escalaProduccion);
        this.numeroIdentificacion = numeroIdentificacion.trim();
        this.escalaProduccion = escalaProduccion;
    }

    public Campesino(UUID campesinoId, String numeroIdentificacion) {
        this(campesinoId, numeroIdentificacion, 0);
    }

    /** La escala de produccion nunca puede ser negativa. */
    public void actualizarEscalaProduccion(double nuevaEscala) {
        exigirEscalaValida(nuevaEscala);
        this.escalaProduccion = nuevaEscala;
    }

    private static void exigirEscalaValida(double escala) {
        if (escala < 0) {
            throw new ReglaDeNegocioVioladaException("INV-CAMPESINO",
                    "La escala de produccion no puede ser negativa: " + escala);
        }
    }

    public UUID getCampesinoId() {
        return campesinoId;
    }

    public String getNumeroIdentificacion() {
        return numeroIdentificacion;
    }

    public double getEscalaProduccion() {
        return escalaProduccion;
    }

    private void publicarProducto(){

    }

    private  void actualizarProducto(){}

    private  void eliminarProducto(){}

    private List<Compra> consultarVentas(){
        return null;
    }

    private double obtenerCalificacionPromedio(){
        return 0;
    }

    private void responderComentario(){}

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        return o instanceof Campesino otro && campesinoId.equals(otro.campesinoId);
    }

    @Override
    public int hashCode() {
        return campesinoId.hashCode();
    }
}

