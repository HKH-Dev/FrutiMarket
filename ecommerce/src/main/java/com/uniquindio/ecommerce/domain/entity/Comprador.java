package com.uniquindio.ecommerce.domain.entity;

import com.uniquindio.ecommerce.domain.exception.ReglaDeNegocioVioladaException;
import com.uniquindio.ecommerce.domain.valueobject.identidad.CompradorId;

/**
 * Entidad: quien compra en el marketplace. Un consumidor, una tienda, un restaurante o
 * un mayorista; todos compran igual, solo cambia su tipo. Su municipio decide si un
 * almacenamiento puede entregarle (regla 15).
 */
public class Comprador {

    public enum Tipo { CONSUMIDOR, TIENDA, RESTAURANTE, MAYORISTA }

    private final CompradorId id;
    private final String nombre;
    private final Tipo tipo;
    private String municipio;
    private boolean activo;

    private Comprador(Builder builder) {
        this.id = builder.id;
        this.nombre = builder.nombre;
        this.tipo = builder.tipo;
        this.municipio = builder.municipio;
        this.activo = true;
    }

    public static Builder builder() {
        return new Builder();
    }

    public void cambiarMunicipio(String nuevoMunicipio) {
        ReglaDeNegocioVioladaException.validar(nuevoMunicipio != null && !nuevoMunicipio.isBlank(), "INV-COMPRADOR",
                "El municipio de entrega es obligatorio.");
        this.municipio = nuevoMunicipio.trim();
    }

    public void desactivar() {
        this.activo = false;
    }

    public CompradorId getId() { return id; }
    public String getNombre() { return nombre; }
    public Tipo getTipo() { return tipo; }
    public String getMunicipio() { return municipio; }
    public boolean estaActivo() { return activo; }

    @Override
    public boolean equals(Object o) {
        return o instanceof Comprador otro && id.equals(otro.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    public static final class Builder {
        private CompradorId id = CompradorId.nuevo();
        private String nombre;
        private Tipo tipo = Tipo.CONSUMIDOR;
        private String municipio;

        private Builder() {
        }

        public Builder id(CompradorId id) { this.id = id; return this; }
        public Builder nombre(String nombre) { this.nombre = nombre; return this; }
        public Builder tipo(Tipo tipo) { this.tipo = tipo; return this; }
        public Builder municipio(String municipio) { this.municipio = municipio; return this; }

        public Comprador build() {
            ReglaDeNegocioVioladaException.validar(id != null && tipo != null, "INV-COMPRADOR",
                    "El comprador requiere id y tipo.");
            ReglaDeNegocioVioladaException.validar(nombre != null && !nombre.isBlank(), "INV-COMPRADOR",
                    "El comprador requiere nombre.");
            ReglaDeNegocioVioladaException.validar(municipio != null && !municipio.isBlank(), "INV-COMPRADOR",
                    "El comprador requiere municipio de entrega.");
            nombre = nombre.trim();
            municipio = municipio.trim();
            return new Comprador(this);
        }
    }
}
