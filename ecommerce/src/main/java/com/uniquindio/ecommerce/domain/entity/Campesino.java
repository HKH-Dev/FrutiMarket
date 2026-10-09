package com.uniquindio.ecommerce.domain.entity;

import com.uniquindio.ecommerce.domain.exception.ReglaDeNegocioVioladaException;
import com.uniquindio.ecommerce.domain.valueobject.identidad.CampesinoId;

/**
 * Entidad: dos campesinos son el mismo si comparten id, aunque cambien sus datos.
 * Regla 1: solo un campesino autorizado puede publicar lotes.
 */
public class Campesino {

    private final CampesinoId id;
    private final String nombre;
    private final String numeroIdentificacion;
    private double escalaProduccion;
    private boolean autorizadoParaPublicar;

    private Campesino(Builder builder) {
        this.id = builder.id;
        this.nombre = builder.nombre;
        this.numeroIdentificacion = builder.numeroIdentificacion;
        this.escalaProduccion = builder.escalaProduccion;
        this.autorizadoParaPublicar = false;
    }

    public static Builder builder() {
        return new Builder();
    }

    public void autorizarParaPublicar() {
        this.autorizadoParaPublicar = true;
    }

    public void revocarAutorizacion() {
        this.autorizadoParaPublicar = false;
    }

    /** La escala de produccion nunca puede ser negativa. */
    public void actualizarEscalaProduccion(double nuevaEscala) {
        ReglaDeNegocioVioladaException.validar(nuevaEscala >= 0, "INV-CAMPESINO",
                "La escala de produccion no puede ser negativa: " + nuevaEscala);
        this.escalaProduccion = nuevaEscala;
    }

    public boolean estaAutorizadoParaPublicar() { return autorizadoParaPublicar; }
    public CampesinoId getId() { return id; }
    public String getNombre() { return nombre; }
    public String getNumeroIdentificacion() { return numeroIdentificacion; }
    public double getEscalaProduccion() { return escalaProduccion; }

    @Override
    public boolean equals(Object o) {
        return o instanceof Campesino otro && id.equals(otro.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    public static final class Builder {
        private CampesinoId id = CampesinoId.nuevo();
        private String nombre;
        private String numeroIdentificacion;
        private double escalaProduccion;

        private Builder() {
        }

        public Builder id(CampesinoId id) { this.id = id; return this; }
        public Builder nombre(String nombre) { this.nombre = nombre; return this; }
        public Builder numeroIdentificacion(String numero) { this.numeroIdentificacion = numero; return this; }
        public Builder escalaProduccion(double escala) { this.escalaProduccion = escala; return this; }

        public Campesino build() {
            ReglaDeNegocioVioladaException.validar(id != null, "INV-CAMPESINO", "El campesino requiere id.");
            ReglaDeNegocioVioladaException.validar(nombre != null && !nombre.isBlank(), "INV-CAMPESINO",
                    "El campesino requiere nombre.");
            ReglaDeNegocioVioladaException.validar(numeroIdentificacion != null && !numeroIdentificacion.isBlank(),
                    "INV-CAMPESINO", "El campesino requiere numero de identificacion.");
            ReglaDeNegocioVioladaException.validar(escalaProduccion >= 0, "INV-CAMPESINO",
                    "La escala de produccion no puede ser negativa.");
            return new Campesino(this);
        }
    }
}
