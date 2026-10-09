package com.uniquindio.ecommerce.domain.entity;

import com.uniquindio.ecommerce.domain.exception.ReglaDeNegocioVioladaException;
import com.uniquindio.ecommerce.domain.valueobject.identidad.AlmacenId;
import com.uniquindio.ecommerce.domain.valueobject.identidad.LoteId;
import com.uniquindio.ecommerce.domain.valueobject.logistica.Conservacion;
import com.uniquindio.ecommerce.domain.valueobject.origenycalidad.TipoCultivo;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Agregado raiz: lugar donde se guardan los lotes mientras viajan de la finca al comprador.
 * Reune lo que antes eran el punto de acopio, el centro de redistribucion y la zona de
 * cobertura: los dos son almacenamientos, solo cambia su {@link Tipo}.
 *
 * <p>Invariantes:</p>
 * <ul>
 *   <li><b>INV-CAPACIDAD:</b> nunca guarda mas lotes que su capacidad.</li>
 *   <li><b>R13:</b> nunca recibe un cultivo para el que no esta habilitado.</li>
 *   <li><b>R9:</b> nunca recibe un lote cuya conservacion no puede garantizar (por ejemplo, refrigerado).</li>
 *   <li>Solo puede liberar un lote que efectivamente esta guardado en el.</li>
 * </ul>
 */
public class PuntoAlmacenamiento {

    public enum Tipo {
        /** Cerca de las fincas: consolida lo que traen los campesinos. */
        PUNTO_ACOPIO,
        /** Cerca de los compradores: reparte a la zona que cubre. */
        CENTRO_REDISTRIBUCION
    }

    private final AlmacenId id;
    private final String nombre;
    private final Tipo tipo;
    private final String municipio;
    private final Set<String> municipiosCobertura;
    private final Set<TipoCultivo> cultivosHabilitados;
    private final Set<Conservacion> conservacionesDisponibles;
    private final int capacidadLotes;
    private final Set<LoteId> lotesAlmacenados;

    private PuntoAlmacenamiento(Builder builder) {
        this.id = builder.id;
        this.nombre = builder.nombre;
        this.tipo = builder.tipo;
        this.municipio = builder.municipio;
        this.municipiosCobertura = builder.municipiosCobertura;
        this.cultivosHabilitados = builder.cultivosHabilitados;
        this.conservacionesDisponibles = builder.conservacionesDisponibles;
        this.capacidadLotes = builder.capacidadLotes;
        this.lotesAlmacenados = new HashSet<>();
    }

    public static Builder builder() {
        return new Builder();
    }

    /** Reglas 9 y 13 y capacidad. */
    public void recibirLote(LoteId lote, TipoCultivo cultivo, Conservacion conservacion) {
        ReglaDeNegocioVioladaException.validar(lote != null, "INV-ALMACEN", "El lote es obligatorio.");
        ReglaDeNegocioVioladaException.validar(cultivosHabilitados.contains(cultivo), "R13",
                nombre + " no esta habilitado para recibir " + cultivo.etiqueta() + ".");
        ReglaDeNegocioVioladaException.validar(conservacionesDisponibles.contains(conservacion), "R9",
                nombre + " no puede garantizar conservacion " + conservacion.etiqueta() + ".");
        ReglaDeNegocioVioladaException.validar(!lotesAlmacenados.contains(lote), "INV-ALMACEN",
                "El lote " + lote + " ya esta guardado en " + nombre + ".");
        ReglaDeNegocioVioladaException.validar(espacioDisponible() > 0, "INV-CAPACIDAD",
                nombre + " esta lleno (" + capacidadLotes + " lotes).");
        lotesAlmacenados.add(lote);
    }

    public void liberarLote(LoteId lote) {
        ReglaDeNegocioVioladaException.validar(lotesAlmacenados.contains(lote), "INV-ALMACEN",
                "El lote " + lote + " no esta guardado en " + nombre + ".");
        lotesAlmacenados.remove(lote);
    }

    /** Regla 15: el municipio del comprador esta dentro de la zona que este almacenamiento atiende. */
    public boolean cubre(String municipioDestino) {
        return municipioDestino != null && municipiosCobertura.contains(normalizar(municipioDestino));
    }

    public boolean puedeRecibir(TipoCultivo cultivo, Conservacion conservacion) {
        return cultivosHabilitados.contains(cultivo) && conservacionesDisponibles.contains(conservacion)
                && espacioDisponible() > 0;
    }

    public int espacioDisponible() {
        return capacidadLotes - lotesAlmacenados.size();
    }

    public boolean guarda(LoteId lote) {
        return lotesAlmacenados.contains(lote);
    }

    private static String normalizar(String texto) {
        return texto.trim().toLowerCase(Locale.ROOT);
    }

    public AlmacenId id() { return id; }
    public String nombre() { return nombre; }
    public Tipo tipo() { return tipo; }
    public String municipio() { return municipio; }
    public Set<String> municipiosCobertura() { return Set.copyOf(municipiosCobertura); }
    public Set<TipoCultivo> cultivosHabilitados() { return Set.copyOf(cultivosHabilitados); }
    public Set<Conservacion> conservacionesDisponibles() { return Set.copyOf(conservacionesDisponibles); }
    public int capacidadLotes() { return capacidadLotes; }
    public Set<LoteId> lotesAlmacenados() { return Set.copyOf(lotesAlmacenados); }

    @Override
    public boolean equals(Object o) {
        return o instanceof PuntoAlmacenamiento otro && id.equals(otro.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return nombre + " (" + tipo + ", " + municipio + ")";
    }

    public static final class Builder {
        private AlmacenId id = AlmacenId.nuevo();
        private String nombre;
        private Tipo tipo;
        private String municipio;
        private Set<String> municipiosCobertura = new HashSet<>();
        private Set<TipoCultivo> cultivosHabilitados = EnumSet.noneOf(TipoCultivo.class);
        private Set<Conservacion> conservacionesDisponibles = EnumSet.of(Conservacion.AMBIENTE);
        private int capacidadLotes;

        private Builder() {
        }

        public Builder id(AlmacenId id) { this.id = id; return this; }
        public Builder nombre(String nombre) { this.nombre = nombre; return this; }
        public Builder tipo(Tipo tipo) { this.tipo = tipo; return this; }
        public Builder municipio(String municipio) { this.municipio = municipio; return this; }
        public Builder municipiosCobertura(Set<String> municipios) { this.municipiosCobertura = municipios; return this; }
        public Builder cultivosHabilitados(Set<TipoCultivo> cultivos) { this.cultivosHabilitados = cultivos; return this; }
        public Builder conservacionesDisponibles(Set<Conservacion> conservaciones) { this.conservacionesDisponibles = conservaciones; return this; }
        public Builder capacidadLotes(int capacidad) { this.capacidadLotes = capacidad; return this; }

        public PuntoAlmacenamiento build() {
            ReglaDeNegocioVioladaException.validar(id != null && tipo != null, "INV-ALMACEN",
                    "El almacenamiento requiere id y tipo.");
            ReglaDeNegocioVioladaException.validar(nombre != null && !nombre.isBlank(), "INV-ALMACEN",
                    "El almacenamiento requiere nombre.");
            ReglaDeNegocioVioladaException.validar(municipio != null && !municipio.isBlank(), "INV-ALMACEN",
                    "El almacenamiento requiere municipio.");
            ReglaDeNegocioVioladaException.validar(capacidadLotes > 0, "INV-CAPACIDAD",
                    "La capacidad debe ser mayor que cero.");
            ReglaDeNegocioVioladaException.validar(cultivosHabilitados != null && !cultivosHabilitados.isEmpty(), "R13",
                    "El almacenamiento debe estar habilitado al menos para un cultivo.");
            ReglaDeNegocioVioladaException.validar(conservacionesDisponibles != null && !conservacionesDisponibles.isEmpty(),
                    "R9", "El almacenamiento debe ofrecer al menos una conservacion.");
            ReglaDeNegocioVioladaException.validar(municipiosCobertura != null, "R15", "La cobertura no puede ser nula.");
            nombre = nombre.trim();
            municipio = municipio.trim();
            Set<String> cobertura = municipiosCobertura.stream().map(PuntoAlmacenamiento::normalizar)
                    .collect(Collectors.toCollection(HashSet::new));
            cobertura.add(normalizar(municipio));
            municipiosCobertura = cobertura;
            cultivosHabilitados = EnumSet.copyOf(cultivosHabilitados);
            conservacionesDisponibles = EnumSet.copyOf(conservacionesDisponibles);
            return new PuntoAlmacenamiento(this);
        }
    }
}
