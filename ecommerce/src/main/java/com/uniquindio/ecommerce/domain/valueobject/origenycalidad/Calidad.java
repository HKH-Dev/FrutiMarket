package com.uniquindio.ecommerce.domain.valueobject.origenycalidad;

import com.uniquindio.ecommerce.domain.exception.ReglaDeNegocioVioladaException;

import java.util.Objects;

/**
 * Value Object: calidad del lote en pocos campos que el campesino puede llenar sin
 * equipos de medicion. Produce un puntaje de 0 a 100 para comparar lotes entre si y
 * que el comprador escoja el mejor.
 */
public final class Calidad {

    /** Clasificacion comercial del producto. Cada categoria parte de un puntaje base. */
    public enum Categoria {
        EXTRA(100), PRIMERA(85), SEGUNDA(65), INDUSTRIAL(40);

        private final int puntajeBase;

        Categoria(int puntajeBase) {
            this.puntajeBase = puntajeBase;
        }

        public int puntajeBase() {
            return puntajeBase;
        }
    }

    private static final int BONO_CERTIFICADO = 5;

    private final Categoria categoria;
    private final int porcentajeDefectos;
    private final boolean certificadoOrganico;

    private Calidad(Categoria categoria, int porcentajeDefectos, boolean certificadoOrganico) {
        this.categoria = categoria;
        this.porcentajeDefectos = porcentajeDefectos;
        this.certificadoOrganico = certificadoOrganico;
    }

    /**
     * @param categoria           extra, primera, segunda o industrial.
     * @param porcentajeDefectos  porcentaje aproximado de unidades golpeadas, manchadas o fuera de tamano (0 a 100).
     * @param certificadoOrganico si el cultivo tiene certificacion organica vigente.
     */
    public static Calidad de(Categoria categoria, int porcentajeDefectos, boolean certificadoOrganico) {
        ReglaDeNegocioVioladaException.validar(categoria != null, "INV-CALIDAD", "La calidad debe indicar su categoria.");
        ReglaDeNegocioVioladaException.validar(porcentajeDefectos >= 0 && porcentajeDefectos <= 100, "INV-CALIDAD",
                "El porcentaje de defectos debe estar entre 0 y 100. Recibido: " + porcentajeDefectos);
        return new Calidad(categoria, porcentajeDefectos, certificadoOrganico);
    }

    /** Puntaje de 0 a 100: base de la categoria, menos la mitad de los defectos, mas un bono si es organico. */
    public int puntaje() {
        int puntaje = categoria.puntajeBase() - porcentajeDefectos / 2 + (certificadoOrganico ? BONO_CERTIFICADO : 0);
        return Math.max(0, Math.min(100, puntaje));
    }

    /** Regla 18: la calidad observada esta por debajo de esta, mas alla de la tolerancia. */
    public boolean superaPorMasDe(Calidad observada, int tolerancia) {
        return puntaje() - observada.puntaje() > tolerancia;
    }

    public Categoria categoria() { return categoria; }
    public int porcentajeDefectos() { return porcentajeDefectos; }
    public boolean certificadoOrganico() { return certificadoOrganico; }

    @Override
    public boolean equals(Object o) {
        return o instanceof Calidad otra && categoria == otra.categoria
                && porcentajeDefectos == otra.porcentajeDefectos && certificadoOrganico == otra.certificadoOrganico;
    }

    @Override
    public int hashCode() {
        return Objects.hash(categoria, porcentajeDefectos, certificadoOrganico);
    }

    @Override
    public String toString() {
        return categoria + " (" + porcentajeDefectos + "% defectos" + (certificadoOrganico ? ", organico" : "")
                + ") = " + puntaje() + " pts";
    }
}
