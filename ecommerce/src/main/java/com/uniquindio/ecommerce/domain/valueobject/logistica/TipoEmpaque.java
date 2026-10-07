package com.uniquindio.ecommerce.domain.valueobject.logistica;

/**
 * Empaque con el que sale un lote desde el punto de acopio.
 *
 * <p><b>Enum</b>. Cada constante declara si aisla termicamente y si es hermetico,
 * que es exactamente lo que necesita la regla 12 para decidir la compatibilidad
 * con la condicion de conservacion del lote.</p>
 */
public enum TipoEmpaque {
    CANASTILLA_PLASTICA("Canastilla plastica", false, false),
    CAJA_CARTON("Caja de carton", false, false),
    SACO_FIBRA("Saco de fibra", false, false),
    CONTENEDOR_ISOTERMICO("Contenedor isotermico", true, false),
    CAJA_REFRIGERADA("Caja refrigerada activa", true, true),
    BOLSA_VACIO("Bolsa al vacio", false, true),
    FRASCO_VIDRIO("Frasco de vidrio", false, true);

    private final String etiqueta;
    private final boolean aislanteTermico;
    private final boolean hermetico;

    TipoEmpaque(String etiqueta, boolean aislanteTermico, boolean hermetico) {
        this.etiqueta = etiqueta;
        this.aislanteTermico = aislanteTermico;
        this.hermetico = hermetico;
    }

    public String etiqueta() {
        return etiqueta;
    }

    public boolean esAislanteTermico() {
        return aislanteTermico;
    }

    public boolean esHermetico() {
        return hermetico;
    }
}
