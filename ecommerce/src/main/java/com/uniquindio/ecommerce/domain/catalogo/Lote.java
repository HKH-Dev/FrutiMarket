package com.uniquindio.ecommerce.domain.catalogo;


import com.uniquindio.ecommerce.domain.event.*;
import com.uniquindio.ecommerce.domain.exception.*;
import com.uniquindio.ecommerce.domain.valueobject.catalogo.*;
import com.uniquindio.ecommerce.domain.valueobject.identidad.*;
import com.uniquindio.ecommerce.domain.valueobject.logistica.*;
import com.uniquindio.ecommerce.domain.valueobject.origenycalidad.*;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

/**
/**
 * Agregado raiz: cantidad de producto homogenea, cosechada por un campesino
 * en una fecha, con una fecha limite de consumo que gobierna el FEFO (regla 16).
 */
public class Lote {
    private static final ZoneId ZONA_OPERACION = ZoneId.of("America/Bogota");
    // ---------------------------------------------------------------- identidad
    private final LoteId id;
    private final String codigo;
    // ------------------------------------- referencias a otros agregados (por id)
    private final ProductoId producto;
    private final CampesinoId campesinoResponsable;
    private final FincaOrigenId fincaOrigen;
    private PuntoAcopioId puntoAcopioActual;
    // ------------------------------------------------------- naturaleza del lote
    private final TipoLote tipo;
    private final TipoCultivo tipoCultivo;
    private final LocalDate fechaCosecha;
    private final LocalDate fechaLimiteConsumo;
    // ------------------------------------------------------------------ cantidad
    private final Cantidad cantidadInicial;
    private Cantidad cantidadDisponible;
    private final List<ReservaLote> reservas;

    // -------------------------------------------------------- estado y economia
    private EstadoLote estado;
    private EstadoLote estadoPrevioARevision;
    private MotivoRevision motivoRevision;
    private String detalleRevision;
    private PrecioFinca precioFinca;
    private Merma merma;
    private boolean mermaAplicada;
    // ---------------------------------------------------- origen, calidad, ficha
    private final FichaTrazabilidad fichaTrazabilidad;
    private Calibre calibre;
    private final TemporadaCosecha temporada;
    private SelloOrigen selloOrigen;
    private DenominacionOrigen denominacionOrigen;
    private final FichaTransformacion fichaTransformacion;
    private ExcedenteCosecha excedenteCosecha;
    // ------------------------------------------------------------------ logistica
    private CondicionConservacion condicionConservacion;
    private CadenaDeFrio cadenaDeFrio;
    private GuiaDespacho guiaDespacho;
    // ------------------------------------------------------------------ auditoria
    private Instant fechaRegistro;
    private Instant fechaPublicacion;
    private long version;
    private final List<EventoDominio> eventos = new ArrayList<>();


    private Lote(LoteId id, String codigo, ProductoId producto, CampesinoId campesino, FincaOrigenId fincaOrigen, TipoLote tipo, TipoCultivo tipoCultivo, LocalDate fechaCosecha, LocalDate fechaLimiteConsumo, Cantidad cantidadInicial, PrecioFinca precioFinca, Merma merma, FichaTrazabilidad fichaTrazabilidad, Calibre calibre, TemporadaCosecha temporada, FichaTransformacion fichaTransformacion) {
        this.id = id;
        this.codigo = codigo;
        this.producto = producto;
        this.campesinoResponsable = campesino;
        this.fincaOrigen = fincaOrigen;
        this.tipo = tipo;
        this.tipoCultivo = tipoCultivo;
        this.fechaCosecha = fechaCosecha;
        this.fechaLimiteConsumo = fechaLimiteConsumo;
        this.cantidadInicial = cantidadInicial;
        this.cantidadDisponible = cantidadInicial;
        this.precioFinca = precioFinca;
        this.merma = merma;
        this.fichaTrazabilidad = fichaTrazabilidad;
        this.calibre = calibre;
        this.temporada = temporada;
        this.fichaTransformacion = fichaTransformacion;
        this.reservas = new ArrayList<>();
        this.estado = EstadoLote.BORRADOR;
        this.cadenaDeFrio = CadenaDeFrio.noAplica();
        this.mermaAplicada = false;
        this.version = 0L;
    }

    public static Lote registrarMateriaPrima(String codigo, ProductoId producto, CampesinoId campesino, FichaTrazabilidad fichaTrazabilidad, TipoCultivo tipoCultivo, Cantidad cantidadInicial, PrecioFinca precioFinca, Calibre calibre, TemporadaCosecha temporada, LocalDate fechaCosecha, int vidaUtilDias, Merma merma, Instant momento) {
        Objects.requireNonNull(producto, "El lote debe referirse a un producto del catalogo.");
        Objects.requireNonNull(campesino, "Regla 8: el lote debe tener campesino responsable.");
        Objects.requireNonNull(fichaTrazabilidad, "Regla 8: el lote debe tener ficha de trazabilidad.");
        Objects.requireNonNull(tipoCultivo, "El lote debe declarar su tipo de cultivo (regla 13).");
        Objects.requireNonNull(cantidadInicial, "Regla 2: el lote debe tener cantidad.");
        Objects.requireNonNull(temporada, "Un lote de materia prima debe declarar su temporada de cosecha.");
        Objects.requireNonNull(fechaCosecha, "El lote debe declarar su fecha de cosecha.");
        Objects.requireNonNull(momento, "El momento de registro no puede ser nulo.");

        exigirCodigo(codigo);
        LocalDate hoy = LocalDate.ofInstant(momento, ZONA_OPERACION);
        fichaTrazabilidad.validarContra(hoy);

        if (cantidadInicial.esCero()) {
            throw new ReglaDeNegocioVioladaException("R2", "Un lote no puede registrarse con cantidad cero.");
        }
        if (fechaCosecha.isAfter(hoy)) {
            throw new ReglaDeNegocioVioladaException("INV-COSECHA", "La fecha de cosecha (" + fechaCosecha + ") no puede ser futura.");
        }
        if (vidaUtilDias < 0) {
            throw new ReglaDeNegocioVioladaException("INV-VIDA-UTIL", "La vida util no puede ser negativa.");
        }

        LocalDate limite = vidaUtilDias > 0 ? fechaCosecha.plusDays(vidaUtilDias) : null;
        if (limite != null && limite.isBefore(hoy)) {
            throw new ReglaDeNegocioVioladaException("INV-VIDA-UTIL", "El lote ya estaria vencido al registrarse: su limite de consumo seria " + limite + ".");
        }

        Lote lote = new Lote(LoteId.nuevo(), codigo.trim(), producto, campesino,
                fichaTrazabilidad.fincaOrigen(), TipoLote.MATERIA_PRIMA, tipoCultivo,
                fechaCosecha, limite, cantidadInicial, precioFinca,
                merma == null ? Merma.ninguna() : merma,
                fichaTrazabilidad, calibre == null ? Calibre.noAplica() : calibre,
                temporada, null);
        lote.fechaRegistro = momento;
        lote.cadenaDeFrio = tipoCultivo.esPerecederoPorNaturaleza()
                ? CadenaDeFrio.iniciar(momento)
                : CadenaDeFrio.noAplica();
        lote.registrar(new LoteRegistrado(lote.id, campesino, cantidadInicial, momento));
        return lote;
    }

    public static Lote registrarTransformado(String codigo, ProductoId producto, CampesinoId campesino, FichaTrazabilidad fichaTrazabilidad, TipoCultivo tipoCultivo, Cantidad cantidadInicial, PrecioFinca precioFinca, FichaTransformacion fichaTransformacion, Merma merma, Instant momento) {
        Objects.requireNonNull(producto, "El lote debe referirse a un producto del catalogo.");
        Objects.requireNonNull(campesino, "Regla 8: el lote debe tener campesino responsable.");
        Objects.requireNonNull(fichaTrazabilidad, "Regla 8: el lote debe tener ficha de trazabilidad.");
        Objects.requireNonNull(cantidadInicial, "Regla 2: el lote debe tener cantidad.");
        Objects.requireNonNull(fichaTransformacion, "Un producto transformado exige ficha de transformacion.");
        Objects.requireNonNull(momento, "El momento de registro no puede ser nulo.");

        exigirCodigo(codigo);
        LocalDate hoy = LocalDate.ofInstant(momento, ZONA_OPERACION);
        fichaTrazabilidad.validarContra(hoy);
        fichaTransformacion.validarContra(hoy);

        if (cantidadInicial.esCero()) {
            throw new ReglaDeNegocioVioladaException("R2",
                    "Un lote no puede registrarse con cantidad cero.");
        }
        LocalDate limite = fichaTransformacion.fechaLimiteConsumo();
        if (limite.isBefore(hoy)) {
            throw new ReglaDeNegocioVioladaException("INV-VIDA-UTIL",
                    "El producto transformado ya estaria vencido: su limite de consumo es " + limite + ".");
        }

        Lote lote = new Lote(LoteId.nuevo(), codigo.trim(), producto, campesino,
                fichaTrazabilidad.fincaOrigen(), TipoLote.TRANSFORMADO,
                tipoCultivo == null ? TipoCultivo.DERIVADO_PROCESADO : tipoCultivo,
                fichaTransformacion.fechaElaboracion(), limite, cantidadInicial, precioFinca,
                merma == null ? Merma.ninguna() : merma, fichaTrazabilidad,
                Calibre.noAplica(), TemporadaCosecha.permanente(), fichaTransformacion);
        lote.fechaRegistro = momento;
        lote.cadenaDeFrio = CadenaDeFrio.noAplica();
        lote.registrar(new LoteRegistrado(lote.id, campesino, cantidadInicial, momento));
        return lote;
    }

    private static void exigirCodigo(String codigo) {
        if (codigo == null || codigo.isBlank()) {
            throw new ReglaDeNegocioVioladaException("INV-CODIGO",
                    "Todo lote debe tener un codigo con el que el campesino lo identifique.");
        }
    }

    public void marcarVencido(LocalDate hoy) {
        ReglaNegocioException.validar(estaVencido(hoy), "El lote " + codigo + " aun no vence");
        this.estado = EstadoLote.VENCIDO;
    }

    public boolean estaVencido(LocalDate hoy) {
        return hoy.isAfter(fechaLimiteConsumo);
    }

    /** Dias restantes para el FEFO; negativo si ya vencio. */
    public long diasParaVencer(LocalDate hoy) {
        return java.time.temporal.ChronoUnit.DAYS.between(hoy, fechaLimiteConsumo);
    }

//    ---------------------------------------------------------------------------
/**

//    public static Lote rehidratar(String id, String codigo, Campesino campesino, Producto producto,
//                                  TipoCultivo tipoCultivo, UnidadMedida unidadMedida,
//                                  BigDecimal cantidadInicial, BigDecimal cantidadDisponible,
//                                  BigDecimal precioUnitario, LocalDate fechaCosecha,
//                                  LocalDate fechaLimiteConsumo, LocalDateTime fechaRegistro,
//                                  EstadoLote estado, PuntoAcopio puntoAcopio) {
//        Lote lote = new Builder()
//                .id(id).codigo(codigo).campesino(campesino).producto(producto)
//                .tipoCultivo(tipoCultivo).unidadMedida(unidadMedida)
//                .cantidadInicial(cantidadInicial).precioUnitario(precioUnitario)
//                .fechaCosecha(fechaCosecha).fechaLimiteConsumo(fechaLimiteConsumo)
//                .fechaRegistro(fechaRegistro)
//                .build();
//        lote.cantidadDisponible = cantidadDisponible;
//        lote.estado = estado;
//        lote.puntoAcopio = puntoAcopio;
//        return lote;
//    }

//    private void validarInvariantes() {
////        ReglaNegocioException.validar(id != null && !id.isBlank(), "El lote requiere id");
//        ReglaNegocioException.validar(id != null && !id.);
//        ReglaNegocioException.validar(codigo != null && !codigo.isBlank(), "El lote requiere codigo");
//        ReglaNegocioException.validar(campesino != null, "El lote requiere un campesino responsable");
//        ReglaNegocioException.validar(producto != null, "El lote requiere un producto del catalogo");
//        ReglaNegocioException.validar(tipoCultivo != null, "El lote requiere tipo de cultivo");
//        ReglaNegocioException.validar(unidadMedida != null, "El lote requiere unidad de medida");
//        ReglaNegocioException.validar(cantidadInicial != null
//                && cantidadInicial.compareTo(BigDecimal.ZERO) > 0, "La cantidad inicial debe ser mayor a cero");
//        ReglaNegocioException.validar(precioUnitario != null
//                && precioUnitario.compareTo(BigDecimal.ZERO) > 0, "El precio unitario debe ser mayor a cero");
//        ReglaNegocioException.validar(fechaCosecha != null, "El lote requiere fecha de cosecha");
//        ReglaNegocioException.validar(fechaLimiteConsumo != null, "El lote requiere fecha limite de consumo");
//        ReglaNegocioException.validar(fechaLimiteConsumo.isAfter(fechaCosecha),
//                "La fecha limite de consumo debe ser posterior a la cosecha");
//    }

    // ---------- comportamiento ----------
//
//    public void publicar() {
//        ReglaNegocioException.validar(estado == EstadoLote.REGISTRADO || estado == EstadoLote.EN_ACOPIO,
//                "Solo un lote registrado o en acopio puede publicarse. Estado actual: " + estado);
//        ReglaNegocioException.validar(tieneDisponibilidad(), "No se publica un lote sin cantidad disponible");
//        this.estado = EstadoLote.PUBLICADO;
//    }
//
//    public void ingresarAPuntoAcopio(PuntoAcopio destino) {
//        ReglaNegocioException.validar(destino != null, "El punto de acopio es obligatorio");
//        ReglaNegocioException.validar(estado.esActivo(), "Un lote " + estado + " no puede ingresar a acopio");
//        this.puntoAcopio = destino;
//        this.estado = EstadoLote.EN_ACOPIO;
//    }


//    public void descontar(BigDecimal cantidad) {
//        ReglaNegocioException.validar(cantidad != null && cantidad.compareTo(BigDecimal.ZERO) > 0,
//                "La cantidad a descontar debe ser mayor a cero");
//        ReglaNegocioException.validar(estado.permiteVenta(),
//                "El lote " + codigo + " no admite salidas en estado " + estado);
//        ReglaNegocioException.validar(cantidad.compareTo(cantidadDisponible) <= 0,
//                "Cantidad solicitada (" + cantidad + ") supera la disponible (" + cantidadDisponible + ")");
//        this.cantidadDisponible = cantidadDisponible.subtract(cantidad);
//        if (cantidadDisponible.compareTo(BigDecimal.ZERO) == 0) this.estado = EstadoLote.AGOTADO;
//    }



//    public void cerrar() {
//        ReglaNegocioException.validar(estado != EstadoLote.CERRADO, "El lote ya esta cerrado");
//        this.estado = EstadoLote.CERRADO;
//    }



//    public boolean tieneDisponibilidad() {
//        return cantidadDisponible.compareTo(BigDecimal.ZERO) > 0;
//    }

//    public boolean estaDisponibleParaVenta(LocalDate hoy) {
//        return estado.permiteVenta() && tieneDisponibilidad() && !estaVencido(hoy);
//    }



//    public BigDecimal valorInventario() {
//        return precioUnitario.multiply(cantidadDisponible);
//    }
    **/
    // =====================================================================
    //  CONSULTAS
    // =====================================================================

    /** Indica si el lote admite pedidos ahora mismo. */
    public boolean estaDisponible() {
        return estado.admitePedidos() && !cantidadDisponible.esCero();
    }

    /**
     * Un lote es perecedero si tiene fecha limite de consumo, o si su cultivo lo es
     * por naturaleza y ningun proceso de transformacion lo estabilizo.
     */
    public boolean esPerecedero() {
        return fechaLimiteConsumo != null
                || (tipo == TipoLote.MATERIA_PRIMA && tipoCultivo.esPerecederoPorNaturaleza());
    }

    /** Regla 10 y 16: tiempo que le queda al lote, recalculado contra la fecha dada. */
    public VidaUtilRestante vidaUtilRestante(Instant momento) {
        if (fechaLimiteConsumo == null) {
            return VidaUtilRestante.noAplica();
        }
        return VidaUtilRestante.calcular(fechaLimiteConsumo, fechaDe(momento));
    }

    public boolean estaEnTemporada(Instant momento) {
        return temporada.contiene(fechaDe(momento));
    }

    public boolean requiereCadenaFrio() {
        return condicionConservacion != null && condicionConservacion.requiereCadenaFrio();
    }

    /** Regla 10, en forma de consulta: responde sin lanzar excepcion. */
    public boolean puedeDespacharseEn(TiempoTransito tiempoTransito, Instant momento) {
        return estado.permiteDespacho()
                && !cadenaDeFrio.estaRota()
                && (selloOrigen == null || selloOrigen.estaVigente(fechaDe(momento)))
                && vidaUtilRestante(momento).alcanzaPara(tiempoTransito.duracion());
    }

    /** Regla 5: hay pedidos que dependen de este lote. */
    public boolean tieneReservasActivas() {
        return !reservas.isEmpty();
    }

//    public Cantidad cantidadReservada() {

//        return reservas.stream()
//                .map(ReservaLote::cantidad)
//                .reduce(Cantidad.cero(cantidadInicial.unidad()), Cantidad::mas);
//    }

    /** Regla 16: los lotes con menor vida util restante se despachan primero. */
    public boolean tieneMasPrioridadDeDespachoQue(Lote otro, Instant momento) {
        Objects.requireNonNull(otro, "El lote comparado no puede ser nulo.");
        return vidaUtilRestante(momento).tieneMasPrioridadQue(otro.vidaUtilRestante(momento));
    }


    // =====================================================================
    //  EVENTOS DE DOMINIO
    // =====================================================================

    /** Eventos acumulados desde la ultima limpieza. El caso de uso los publica tras guardar. */
    public List<EventoDominio> eventos() {
        return Collections.unmodifiableList(new ArrayList<>(eventos));
    }

    public void limpiarEventos() {
        eventos.clear();
    }

    // =====================================================================
    //  INTERNOS
    // =====================================================================

    /** Unico punto por el que pasa cualquier cambio de estado del lote. */
    private void cambiarEstado(EstadoLote destino, String operacion) {
        if (estado == destino) {
            return;
        }
        if (!estado.puedePasarA(destino)) {
            throw new TransicionEstadoInvalidaException(estado.etiqueta(), destino.etiqueta(), operacion);
        }
        this.estado = destino;
    }

    private void ponerEnRevision(MotivoRevision motivo, String detalle, Instant momento) {
        if (estado == EstadoLote.EN_REVISION) {
            return;
        }
        this.estadoPrevioARevision = estado;
        cambiarEstado(EstadoLote.EN_REVISION, "poner en revision");
        this.motivoRevision = motivo;
        this.detalleRevision = detalle;
        tocar();
        registrar(new LotePuestoEnRevision(id, motivo, detalle, momento));
    }

    private void exigirGuiaActiva() {
        if (guiaDespacho == null || !guiaDespacho.estaAbierta()) {
            throw new ReglaDeNegocioVioladaException("INV-GUIA",
                    "El lote no tiene una guia de despacho abierta.");
        }
    }

    private void registrar(EventoDominio evento) {
        eventos.add(evento);
    }

    private void tocar() {
        this.version++;
    }

    private LocalDate fechaDe(Instant momento) {
        Objects.requireNonNull(momento, "El momento no puede ser nulo.");
        return LocalDate.ofInstant(momento, ZONA_OPERACION);
    }

    // =====================================================================
    //  ACCESORES DE SOLO LECTURA
    // =====================================================================

    public LoteId id() {
        return id;
    }

    public String codigo() {
        return codigo;
    }

    public ProductoId producto() {
        return producto;
    }

    public CampesinoId campesinoResponsable() {
        return campesinoResponsable;
    }

    public FincaOrigenId fincaOrigen() {
        return fincaOrigen;
    }

    public Optional<PuntoAcopioId> puntoAcopioActual() {
        return Optional.ofNullable(puntoAcopioActual);
    }

    public TipoLote tipo() {
        return tipo;
    }

    public TipoCultivo tipoCultivo() {
        return tipoCultivo;
    }

    public LocalDate fechaCosecha() {
        return fechaCosecha;
    }

    public Optional<LocalDate> fechaLimiteConsumo() {
        return Optional.ofNullable(fechaLimiteConsumo);
    }

    public Cantidad cantidadInicial() {
        return cantidadInicial;
    }

    public Cantidad cantidadDisponible() {
        return cantidadDisponible;
    }

    public EstadoLote estado() {
        return estado;
    }

    public Optional<MotivoRevision> motivoRevision() {
        return Optional.ofNullable(motivoRevision);
    }

    public Optional<String> detalleRevision() {
        return Optional.ofNullable(detalleRevision);
    }

    public PrecioFinca precioFinca() {
        return precioFinca;
    }

    public Merma merma() {
        return merma;
    }

    public boolean mermaAplicada() {
        return mermaAplicada;
    }

    public FichaTrazabilidad fichaTrazabilidad() {
        return fichaTrazabilidad;
    }

    public Calibre calibre() {
        return calibre;
    }

    public TemporadaCosecha temporada() {
        return temporada;
    }

    public Optional<SelloOrigen> selloOrigen() {
        return Optional.ofNullable(selloOrigen);
    }

    public Optional<DenominacionOrigen> denominacionOrigen() {
        return Optional.ofNullable(denominacionOrigen);
    }

    public Optional<FichaTransformacion> fichaTransformacion() {
        return Optional.ofNullable(fichaTransformacion);
    }

    public Optional<ExcedenteCosecha> excedenteCosecha() {
        return Optional.ofNullable(excedenteCosecha);
    }

    public Optional<CondicionConservacion> condicionConservacion() {
        return Optional.ofNullable(condicionConservacion);
    }

    public CadenaDeFrio cadenaDeFrio() {
        return cadenaDeFrio;
    }

    public Optional<GuiaDespacho> guiaDespacho() {
        return Optional.ofNullable(guiaDespacho);
    }

    /** Copia defensiva: las reservas solo se modifican por los metodos del agregado. */
    public List<ReservaLote> reservas() {
        return Collections.unmodifiableList(new ArrayList<>(reservas));
    }

    public Optional<Instant> fechaRegistro() {
        return Optional.ofNullable(fechaRegistro);
    }

    public Optional<Instant> fechaPublicacion() {
        return Optional.ofNullable(fechaPublicacion);
    }

    /** Version para control de concurrencia optimista en el adaptador de persistencia. */
    public long version() {
        return version;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        return o instanceof Lote otro && id.equals(otro.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return "Lote[" + codigo + ", " + estado.etiqueta() + ", disponible " + cantidadDisponible + "]";
    }

//
//    @Override public boolean equals(Object o) {
//        if (this == o) return true;
//        if (!(o instanceof Lote other)) return false;
//        return id.equals(other.id);
//    }
//
//    @Override public int hashCode() { return Objects.hash(id); }

}
