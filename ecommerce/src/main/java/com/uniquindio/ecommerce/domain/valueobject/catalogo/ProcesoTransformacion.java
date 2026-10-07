package com.uniquindio.ecommerce.domain.valueobject.catalogo;

/** Procesos artesanales admitidos para convertir materia prima en producto transformado. */
public enum ProcesoTransformacion {
    MERMELADA("Mermelada", 365),
    DESHIDRATADO("Deshidratado", 180),
    CONSERVA("Conserva", 540),
    ENCURTIDO("Encurtido", 365),
    HARINA_ARTESANAL("Harina artesanal", 240),
    PULPA_CONGELADA("Pulpa congelada", 300);

    private final String etiqueta;
    private final int vidaUtilMaximaDias;

    ProcesoTransformacion(String etiqueta, int vidaUtilMaximaDias) {
        this.etiqueta = etiqueta;
        this.vidaUtilMaximaDias = vidaUtilMaximaDias;
    }

    public String etiqueta() {
        return etiqueta;
    }

    /** Tope de vida util que el campesino puede declarar para este proceso. */
    public int vidaUtilMaximaDias() {
        return vidaUtilMaximaDias;
    }
}
