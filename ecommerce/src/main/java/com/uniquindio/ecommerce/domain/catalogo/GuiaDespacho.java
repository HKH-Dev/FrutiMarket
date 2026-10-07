package com.uniquindio.ecommerce.domain.catalogo;



import com.uniquindio.ecommerce.domain.exception.ReglaDeNegocioVioladaException;
import com.uniquindio.ecommerce.domain.valueobject.identidad.*;
import com.uniquindio.ecommerce.domain.valueobject.logistica.*;

import java.time.Instant;
import java.util.*;

/**
 * Documento que acompana el traslado de un lote y registra su recorrido.
 *
 * <p><b>Entidad interna del agregado {@code Lote}.</b> Esta es la unica clase del
 * modelo que no es ni raiz ni Value Object, y conviene entender por que:</p>
 * <ul>
 *   <li><b>Es entidad</b> porque tiene identidad propia y cambia conservandola: se
 *       le van anadiendo hitos y sigue siendo la misma guia.</li>
 *   <li><b>Es interna y no raiz</b> porque no puede existir sin su lote, y porque
 *       las reglas 10, 11, 12 y 17 relacionan el estado de la guia con el estado
 *       del lote. Si fueran agregados separados no habria forma de garantizar esas
 *       reglas en una sola transaccion.</li>
 * </ul>
 *
 * <p>Nunca se accede a ella desde fuera: se obtiene siempre a traves de
 * {@code Lote.guiaDespachoActiva()}, y se modifica llamando metodos del lote.</p>
 */
public class GuiaDespacho {

    private final GuiaDespachoId id;
    private final String numero;
    private final LoteId lote;
    private final String origen;
    private final String destino;
    private final TipoEmpaque empaque;
    private final TiempoTransito tiempoTransitoEstimado;
    private final Instant fechaEmision;
    private final List<HitoDespacho> hitos;
    private EstadoGuia estado;
    private Instant fechaCierre;

    private GuiaDespacho(GuiaDespachoId id, String numero, LoteId lote, String origen, String destino,
                         TipoEmpaque empaque, TiempoTransito tiempoTransitoEstimado, Instant fechaEmision) {
        this.id = id;
        this.numero = numero;
        this.lote = lote;
        this.origen = origen;
        this.destino = destino;
        this.empaque = empaque;
        this.tiempoTransitoEstimado = tiempoTransitoEstimado;
        this.fechaEmision = fechaEmision;
        this.hitos = new ArrayList<>();
        this.estado = EstadoGuia.EMITIDA;
    }

    /**
     * Emite una guia nueva. Es de visibilidad de paquete a proposito: solo el
     * agregado {@code Lote} puede crear guias, nadie mas.
     */
    static GuiaDespacho emitir(LoteId lote, String origen, String destino,
                               TipoEmpaque empaque, TiempoTransito tiempoTransito, Instant momento) {
        Objects.requireNonNull(lote, "La guia debe referirse a un lote.");
        Objects.requireNonNull(empaque, "La guia debe declarar el tipo de empaque (regla 12).");
        Objects.requireNonNull(tiempoTransito, "La guia debe declarar el tiempo de transito (regla 10).");
        Objects.requireNonNull(momento, "La guia debe tener fecha de emision.");
        if (origen == null || origen.isBlank()) {
            throw new ReglaDeNegocioVioladaException("INV-GUIA", "La guia de despacho debe indicar el origen.");
        }
        if (destino == null || destino.isBlank()) {
            throw new ReglaDeNegocioVioladaException("INV-GUIA", "La guia de despacho debe indicar el destino.");
        }
        GuiaDespachoId id = GuiaDespachoId.nuevo();
        String numero = "GD-" + id.valor().toString().substring(0, 8).toUpperCase();
        return new GuiaDespacho(id, numero, lote, origen.trim(), destino.trim(),
                empaque, tiempoTransito, momento);
    }

    /** Anade un paso al recorrido. Los hitos solo se agregan al final, nunca se corrigen. */
    void registrarHito(HitoDespacho hito) {
        Objects.requireNonNull(hito, "El hito no puede ser nulo.");
        if (!estado.permiteRegistrarHitos()) {
            throw new ReglaDeNegocioVioladaException("INV-GUIA",
                    "La guia " + numero + " esta " + estado + " y no admite hitos nuevos.");
        }
        if (!hitos.isEmpty() && hito.momento().isBefore(ultimoHito().orElseThrow().momento())) {
            throw new ReglaDeNegocioVioladaException("INV-GUIA",
                    "Un hito no puede registrarse antes del hito anterior de la misma guia.");
        }
        hitos.add(hito);
        if (estado == EstadoGuia.EMITIDA) {
            estado = EstadoGuia.EN_RUTA;
        }
        if (hito.tipo().esTerminal()) {
            cerrar(hito.momento());
        }
    }

    void cerrar(Instant momento) {
        if (estado == EstadoGuia.CERRADA) {
            return;
        }
        this.estado = EstadoGuia.CERRADA;
        this.fechaCierre = momento;
    }

    void anular(Instant momento) {
        if (estado == EstadoGuia.CERRADA) {
            throw new ReglaDeNegocioVioladaException("INV-GUIA",
                    "Una guia cerrada no puede anularse.");
        }
        this.estado = EstadoGuia.ANULADA;
        this.fechaCierre = momento;
    }

    public Optional<HitoDespacho> ultimoHito() {
        return hitos.isEmpty() ? Optional.empty() : Optional.of(hitos.get(hitos.size() - 1));
    }

    public boolean registroPasoPor(TipoHito tipo) {
        return hitos.stream().anyMatch(h -> h.tipo() == tipo);
    }

    public boolean estaAbierta() {
        return estado.permiteRegistrarHitos();
    }

    public GuiaDespachoId id() {
        return id;
    }

    public String numero() {
        return numero;
    }

    public LoteId lote() {
        return lote;
    }

    public String origen() {
        return origen;
    }

    public String destino() {
        return destino;
    }

    public TipoEmpaque empaque() {
        return empaque;
    }

    public TiempoTransito tiempoTransitoEstimado() {
        return tiempoTransitoEstimado;
    }

    public Instant fechaEmision() {
        return fechaEmision;
    }

    public Optional<Instant> fechaCierre() {
        return Optional.ofNullable(fechaCierre);
    }

    public EstadoGuia estado() {
        return estado;
    }

    /** Copia defensiva: nadie fuera del agregado puede alterar el recorrido. */
    public List<HitoDespacho> hitos() {
        return Collections.unmodifiableList(new ArrayList<>(hitos));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        return o instanceof GuiaDespacho otra && id.equals(otra.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return "GuiaDespacho[" + numero + ", " + origen + " -> " + destino + ", " + estado + "]";
    }
}
