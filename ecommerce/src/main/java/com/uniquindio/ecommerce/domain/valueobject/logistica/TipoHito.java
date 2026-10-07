package com.uniquindio.ecommerce.domain.valueobject.logistica;

/** Clase de evento registrado en la guia de despacho durante el traslado del lote. */
public enum TipoHito {
    SALIDA_FINCA("Salida de finca"),
    INGRESO_PUNTO_ACOPIO("Ingreso a punto de acopio"),
    SALIDA_PUNTO_ACOPIO("Salida de punto de acopio"),
    INGRESO_CENTRO_REDISTRIBUCION("Ingreso a centro de redistribucion"),
    SALIDA_CENTRO_REDISTRIBUCION("Salida de centro de redistribucion"),
    VERIFICACION_TEMPERATURA("Verificacion de temperatura"),
    INCIDENCIA("Incidencia"),
    ENTREGA_DESTINATARIO("Entrega al destinatario");

    private final String etiqueta;

    TipoHito(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public String etiqueta() {
        return etiqueta;
    }

    public boolean esTerminal() {
        return this == ENTREGA_DESTINATARIO;
    }
}
