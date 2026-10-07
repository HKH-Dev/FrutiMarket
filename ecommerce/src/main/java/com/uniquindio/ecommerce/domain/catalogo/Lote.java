package com.uniquindio.ecommerce.domain.catalogo;


import com.uniquindio.ecommerce.domain.event.*;
import com.uniquindio.ecommerce.domain.exception.*;
import com.uniquindio.ecommerce.domain.valueobject.catalogo.*;
import com.uniquindio.ecommerce.domain.valueobject.identidad.*;
import com.uniquindio.ecommerce.domain.valueobject.logistica.*;
import com.uniquindio.ecommerce.domain.valueobject.origenycalidad.*;

import java.math.BigDecimal;
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
    // =====================================================================
    //  FACTORIAS
    // =====================================================================

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
    // =====================================================================
    //  COMANDOS DEL CATALOGO
    // =====================================================================

    /**
     * Publica el lote en el catalogo.
     *
     * <p>Reune las reglas 2 y 8 en un solo punto: el lote no llega a
     * {@link EstadoLote#PUBLICADO} sin precio, cantidad, campesino, finca y ficha de
     * trazabilidad, y un lote de materia prima tampoco si la fecha esta fuera de su
     * temporada declarada. Aplica la merma antes de publicar, para que el catalogo
     * nunca ofrezca producto que ya se sabe que se va a perder.</p>
     */
    public void publicar(Instant momento) {
        Objects.requireNonNull(momento, "El momento de publicacion no puede ser nulo.");
        LocalDate hoy = fechaDe(momento);

        Optional<String> impedimento = motivoNoPublicable(momento);
        if (impedimento.isPresent()) {
            throw new LoteNoPublicableException("R2/R8", impedimento.get());
        }
        if (!mermaAplicada && !merma.esNula()) {
            aplicarMerma(momento);
        }
        if (cantidadDisponible.esCero()) {
            throw new LoteNoPublicableException("R2",
                    "tras aplicar la merma del " + merma + " no queda cantidad disponible.");
        }
        cambiarEstado(EstadoLote.PUBLICADO, "publicar");
        this.fechaPublicacion = momento;
        registrar(new LotePublicado(id, producto, cantidadDisponible, momento));
        if (fechaLimiteConsumo != null && fechaLimiteConsumo.isBefore(hoy)) {
            marcarVencido(momento);
        }
    }

    /**
     * Explica por que el lote no puede publicarse, o {@link Optional#empty()} si si
     * puede. Existe para que la interfaz pueda mostrarle al campesino que le falta
     * sin tener que provocar una excepcion.
     */
    public Optional<String> motivoNoPublicable(Instant momento) {
        LocalDate hoy = fechaDe(momento);
        if (estado != EstadoLote.BORRADOR && estado != EstadoLote.DESACTIVADO
                && estado != EstadoLote.EN_REVISION) {
            return Optional.of("el lote esta en estado " + estado.etiqueta()
                    + " y no puede volver a publicarse.");
        }
        if (estado == EstadoLote.EN_REVISION) {
            return Optional.of("el lote esta en revision por "
                    + (motivoRevision == null ? "causa no registrada" : motivoRevision.descripcion())
                    + "; debe validarse antes de publicar (regla 11).");
        }
        if (precioFinca == null) {
            return Optional.of("no tiene precio de finca (regla 2).");
        }
        if (cantidadDisponible.esCero()) {
            return Optional.of("no tiene cantidad disponible (regla 2).");
        }
        if (!fichaTrazabilidad.estaCompleta(hoy)) {
            return Optional.of("su ficha de trazabilidad no esta completa (regla 8).");
        }
        if (tipo.exigeFichaTransformacion() && fichaTransformacion == null) {
            return Optional.of("es un producto transformado y no tiene ficha de transformacion.");
        }
        if (tipo.exigeTemporadaDeCosecha() && !temporada.contiene(hoy)) {
            return Optional.of("la fecha actual (" + hoy + ") esta fuera de su temporada de cosecha "
                    + temporada + ".");
        }
        if (fechaLimiteConsumo != null && fechaLimiteConsumo.isBefore(hoy)) {
            return Optional.of("su vida util ya vencio el " + fechaLimiteConsumo + ".");
        }
        return Optional.empty();
    }

    /**
     * Descuenta del lote la perdida esperada declarada como merma.
     *
     * <p>Se aplica una sola vez: volver a llamarla no vuelve a descontar. El glosario
     * es explicito en que la merma ajusta la <i>cantidad</i>, no el precio.</p>
     */
    public void aplicarMerma(Instant momento) {
        Objects.requireNonNull(momento, "El momento no puede ser nulo.");
        if (mermaAplicada || merma.esNula()) {
            return;
        }
        Cantidad perdida = merma.perdidaSobre(cantidadDisponible);
        this.cantidadDisponible = cantidadDisponible.menos(perdida);
        this.mermaAplicada = true;
        tocar();
        registrar(new MermaAplicada(id, merma, perdida, cantidadDisponible, momento));
    }

    /** Redefine la merma esperada. Solo es posible antes de publicar. */
    public void redefinirMerma(Merma nuevaMerma, Instant momento) {
        Objects.requireNonNull(nuevaMerma, "La merma no puede ser nula.");
        if (estado != EstadoLote.BORRADOR) {
            throw new ReglaDeNegocioVioladaException("INV-MERMA",
                    "La merma esperada solo puede redefinirse mientras el lote esta en borrador.");
        }
        this.merma = nuevaMerma;
        this.mermaAplicada = false;
        tocar();
    }

    /**
     * Resta cantidad del disponible. Es la operacion primitiva sobre la que se
     * apoyan {@link #reservar} y {@link #confirmarSalida}; se expone porque el
     * diagrama de clases la declara, pero el camino normal es reservar.
     */
    public void reducirCantidad(Cantidad cantidad, Instant momento) {
        Objects.requireNonNull(cantidad, "La cantidad a reducir no puede ser nula.");
        if (cantidad.esMayorQue(cantidadDisponible)) {
            throw new CantidadInsuficienteException(cantidad.toString(), cantidadDisponible.toString());
        }
        this.cantidadDisponible = cantidadDisponible.menos(cantidad);
        tocar();
        if (cantidadDisponible.esCero() && estado == EstadoLote.PUBLICADO) {
            agotar(momento);
        }
    }

    /**
     * Regla 3: compromete cantidad para un pedido, siempre que no supere la
     * disponible.
     *
     * <p>Esta es la razon principal por la que la cantidad vive dentro del agregado:
     * si la comprobacion se hiciera en el caso de uso, dos pedidos simultaneos
     * podrian leer el mismo disponible y venderlo dos veces.</p>
     */
    public void reservar(PedidoId pedido, Cantidad cantidad, Instant momento) {
        Objects.requireNonNull(pedido, "La reserva debe indicar el pedido.");
        Objects.requireNonNull(cantidad, "La reserva debe indicar la cantidad.");
        if (!estado.admitePedidos()) {
            throw new ReglaDeNegocioVioladaException("R3",
                    "El lote esta en estado " + estado.etiqueta() + " y no admite pedidos nuevos.");
        }
        if (cantidad.esCero()) {
            throw new ReglaDeNegocioVioladaException("R3",
                    "Un pedido no puede reservar cantidad cero.");
        }
        if (cantidad.esMayorQue(cantidadDisponible)) {
            throw new CantidadInsuficienteException(cantidad.toString(), cantidadDisponible.toString());
        }
        if (reservas.stream().anyMatch(r -> r.pedido().equals(pedido))) {
            throw new ReglaDeNegocioVioladaException("R6",
                    "El pedido " + pedido + " ya tiene una reserva activa sobre este lote.");
        }
        this.cantidadDisponible = cantidadDisponible.menos(cantidad);
        this.reservas.add(new ReservaLote(pedido, cantidad, momento));
        tocar();
        registrar(new CantidadReservada(id, pedido, cantidad, cantidadDisponible, momento));
        if (cantidadDisponible.esCero()) {
            agotar(momento);
        }
    }

    /** Devuelve al lote la cantidad de un pedido anulado. */
    public void liberarReserva(PedidoId pedido, Instant momento) {
        Objects.requireNonNull(pedido, "El pedido no puede ser nulo.");
        ReservaLote reserva = reservas.stream()
                .filter(r -> r.pedido().equals(pedido))
                .findFirst()
                .orElseThrow(() -> new ReglaDeNegocioVioladaException("INV-RESERVA",
                        "El pedido " + pedido + " no tiene reserva activa sobre este lote."));
        reservas.remove(reserva);
        this.cantidadDisponible = cantidadDisponible.mas(reserva.cantidad());
        tocar();
        registrar(new ReservaLiberada(id, pedido, reserva.cantidad(), momento));
        if (estado == EstadoLote.AGOTADO && !cantidadDisponible.esCero()) {
            cambiarEstado(EstadoLote.PUBLICADO, "liberar reserva");
        }
    }

    /** Confirma la salida fisica del producto reservado: la reserva deja de estar activa. */
    public void confirmarSalida(PedidoId pedido, Instant momento) {
        Objects.requireNonNull(pedido, "El pedido no puede ser nulo.");
        ReservaLote reserva = reservas.stream()
                .filter(r -> r.pedido().equals(pedido))
                .findFirst()
                .orElseThrow(() -> new ReglaDeNegocioVioladaException("INV-RESERVA",
                        "El pedido " + pedido + " no tiene reserva activa sobre este lote."));
        reservas.remove(reserva);
        tocar();
    }

    /** Marca el lote como agotado. Se invoca sola cuando la cantidad llega a cero. */
    public void agotar(Instant momento) {
        if (estado == EstadoLote.AGOTADO) {
            return;
        }
        cambiarEstado(EstadoLote.AGOTADO, "agotar");
        registrar(new LoteAgotado(id, momento));
    }

    /**
     * Regla 5: retira el lote de la venta sin borrarlo. Es la unica via para dejar
     * de vender un lote que tiene pedidos activos.
     */
    public void desactivar(String motivo, Instant momento) {
        cambiarEstado(EstadoLote.DESACTIVADO, "desactivar");
        this.detalleRevision = (motivo == null || motivo.isBlank()) ? "Desactivado por el campesino" : motivo.trim();
        tocar();
    }

    /** Vuelve a poner en el catalogo un lote desactivado. */
    public void reactivar(Instant momento) {
        if (motivoNoPublicable(momento).isPresent() && estado == EstadoLote.DESACTIVADO) {
            throw new LoteNoPublicableException("R2/R8",
                    motivoNoPublicable(momento).orElse("no cumple las condiciones para volver al catalogo."));
        }
        cambiarEstado(EstadoLote.PUBLICADO, "reactivar");
        this.detalleRevision = null;
        tocar();
    }

    /**
     * Regla 5: baja definitiva. Se rechaza mientras existan reservas activas; para
     * ese caso el negocio obliga a desactivar en lugar de eliminar.
     */
    public void retirar(Instant momento) {
        if (tieneReservasActivas()) {
            throw new ReglaDeNegocioVioladaException("R5",
                    "El lote tiene " + reservas.size() + " pedido(s) activo(s) y no puede retirarse. "
                            + "Debe desactivarse para evitar ventas nuevas.");
        }
        cambiarEstado(EstadoLote.RETIRADO, "retirar");
        tocar();
    }

    /**
     * Regla 10: un lote en transito no puede ser modificado.
     */
    // =====================================================================
    //  COMANDOS DE LOGISTICA
    // =====================================================================

    /**
     * Declara la condicion de conservacion del lote. Es requisito de la regla 9 para
     * enviarlo a un punto de acopio, y de la regla 12 para evaluar el empaque.
     */
    public void declararCondicionConservacion(CondicionConservacion condicion, Instant momento) {
        Objects.requireNonNull(condicion, "La condicion de conservacion no puede ser nula.");
        this.condicionConservacion = condicion;
        this.cadenaDeFrio = condicion.requiereCadenaFrio()
                ? CadenaDeFrio.iniciar(momento)
                : CadenaDeFrio.noAplica();
        tocar();
    }

    /**
     * Regla 9: un lote perecedero debe tener definida su condicion de conservacion
     * antes de ser enviado a un punto de acopio.
     */
    public void enviarAPuntoAcopio(PuntoAcopioId puntoAcopio, Instant momento) {
        Objects.requireNonNull(puntoAcopio, "El punto de acopio no puede ser nulo.");
        if (esPerecedero() && condicionConservacion == null) {
            throw new DespachoNoPermitidoException("R9",
                    "es perecedero y no tiene declarada su condicion de conservacion.");
        }
        cambiarEstado(EstadoLote.EN_ACOPIO, "enviar a punto de acopio");
        this.puntoAcopioActual = puntoAcopio;
        tocar();
        registrar(new LoteEnviadoAAcopio(id, puntoAcopio, momento));
    }

    /**
     * Emite la guia de despacho y pone el lote en transito.
     *
     * <p>Concentra cuatro reglas que solo pueden comprobarse juntas:</p>
     * <ul>
     *   <li><b>Regla 10:</b> la vida util restante debe cubrir el tiempo de transito.</li>
     *   <li><b>Regla 11:</b> un lote en revision o con la cadena de frio rota no sale.</li>
     *   <li><b>Regla 12:</b> el empaque debe ser compatible con la condicion de conservacion.</li>
     *   <li><b>Regla 17:</b> la certificacion de origen no puede estar vencida.</li>
     * </ul>
     */
    public void despachar(String destino, TipoEmpaque empaque, TiempoTransito tiempoTransito, Instant momento) {
        Objects.requireNonNull(empaque, "El despacho debe declarar el tipo de empaque (regla 12).");
        Objects.requireNonNull(tiempoTransito, "El despacho debe declarar el tiempo de transito (regla 10).");
        LocalDate hoy = fechaDe(momento);

        if (estado == EstadoLote.EN_REVISION) {
            throw new DespachoNoPermitidoException("R11",
                    "esta en revision por "
                            + (motivoRevision == null ? "causa no registrada" : motivoRevision.descripcion())
                            + " y no puede continuar su despacho hasta ser validado.");
        }
        if (estado == EstadoLote.EN_TRANSITO) {
            throw new DespachoNoPermitidoException("R10",
                    "esta en transito y no puede ser modificado.");
        }
        if (!estado.permiteDespacho()) {
            throw new DespachoNoPermitidoException("R9",
                    "debe estar en un punto de acopio para despacharse; su estado es " + estado.etiqueta() + ".");
        }
        if (cadenaDeFrio.estaRota()) {
            throw new DespachoNoPermitidoException("R11",
                    "su cadena de frio esta rota: " + cadenaDeFrio.motivoRuptura());
        }
        if (esPerecedero() && condicionConservacion == null) {
            throw new DespachoNoPermitidoException("R9",
                    "es perecedero y no tiene declarada su condicion de conservacion.");
        }
        if (condicionConservacion != null && !condicionConservacion.esCompatibleCon(empaque)) {
            throw new DespachoNoPermitidoException("R12",
                    condicionConservacion.motivoIncompatibilidad(empaque) + ".");
        }
        if (selloOrigen != null && selloOrigen.estaVencido(hoy)) {
            ponerEnRevision(MotivoRevision.CERTIFICACION_VENCIDA,
                    "El sello " + selloOrigen.tipo().etiqueta() + " vencio el " + selloOrigen.fechaVencimiento(),
                    momento);
            throw new DespachoNoPermitidoException("R17",
                    "su certificacion de origen vencio el " + selloOrigen.fechaVencimiento()
                            + " y debe validarse nuevamente.");
        }
        if (denominacionOrigen != null && !denominacionOrigen.estaVigente(hoy)) {
            throw new DespachoNoPermitidoException("R17",
                    "su denominacion de origen vencio el " + denominacionOrigen.fechaVencimiento() + ".");
        }
        VidaUtilRestante vidaUtil = vidaUtilRestante(momento);
        if (!vidaUtil.alcanzaPara(tiempoTransito.duracion())) {
            throw new DespachoNoPermitidoException("R10",
                    "su vida util restante (" + vidaUtil + ") no cubre el tiempo de transito de "
                            + tiempoTransito + ".");
        }

        String origen = puntoAcopioActual != null ? "Punto de acopio " + puntoAcopioActual : "Finca de origen";
        this.guiaDespacho = GuiaDespacho.emitir(id, origen, destino, empaque, tiempoTransito, momento);
        this.guiaDespacho.registrarHito(HitoDespacho.de(TipoHito.SALIDA_PUNTO_ACOPIO, origen, momento));
        cambiarEstado(EstadoLote.EN_TRANSITO, "despachar");
        tocar();
        registrar(new LoteDespachado(id, guiaDespacho.id(), destino, vidaUtil.diasRestantes(), momento));
    }

    /**
     * Anota un paso del recorrido en la guia de despacho.
     *
     * <p>Si el hito trae lectura de temperatura, la contrasta contra la condicion de
     * conservacion: una lectura fuera de rango rompe la cadena de frio y manda el
     * lote a revision sin que nadie tenga que acordarse de comprobarlo (regla 11).</p>
     */
    public void registrarHitoDespacho(TipoHito tipo, String ubicacion, BigDecimal temperaturaC, Instant momento) {
        exigirGuiaActiva();
        HitoDespacho hito = new HitoDespacho(tipo, ubicacion, momento, temperaturaC, "");
        guiaDespacho.registrarHito(hito);
        tocar();
        if (temperaturaC != null && condicionConservacion != null) {
            this.cadenaDeFrio = cadenaDeFrio.registrarLectura(
                    temperaturaC, condicionConservacion.temperatura(), momento);
            if (cadenaDeFrio.estaRota()) {
                ponerEnRevision(MotivoRevision.RUPTURA_CADENA_FRIO, cadenaDeFrio.motivoRuptura(), momento);
            }
        }
    }

    /** Regla 11: reporte explicito de perdida de las condiciones de conservacion. */
    public void registrarRupturaCadenaFrio(String motivo, Instant momento) {
        this.cadenaDeFrio = cadenaDeFrio.romper(
                (motivo == null || motivo.isBlank()) ? "Ruptura reportada sin detalle" : motivo, momento);
        if (guiaDespacho != null && guiaDespacho.estaAbierta()) {
            guiaDespacho.registrarHito(new HitoDespacho(TipoHito.INCIDENCIA,
                    guiaDespacho.destino(), momento, null, cadenaDeFrio.motivoRuptura()));
        }
        ponerEnRevision(MotivoRevision.RUPTURA_CADENA_FRIO, cadenaDeFrio.motivoRuptura(), momento);
    }

    /**
     * Regla 18: contrasta una medicion real contra el calibre declarado. Si no
     * coincide, el lote queda en revision manual antes de continuar su distribucion.
     *
     * @return {@code true} si la medicion coincide con lo declarado.
     */
    public boolean verificarCalibre(MedicionCalibre medicion, Instant momento) {
        Objects.requireNonNull(medicion, "La medicion de calibre no puede ser nula.");
        if (calibre.admite(medicion)) {
            return true;
        }
        ponerEnRevision(MotivoRevision.CALIBRE_NO_COINCIDE,
                "Peso promedio medido de " + medicion.pesoPromedioGramos()
                        + " g fuera del calibre declarado " + calibre,
                momento);
        return false;
    }

    /**
     * Regla 11: un responsable valida el lote detenido y este vuelve al estado en el
     * que estaba. Los motivos que exigen corregir un dato (calibre, certificacion) no
     * se liberan con una simple validacion.
     */
    public void validarRevision(String responsable, Instant momento) {
        if (estado != EstadoLote.EN_REVISION) {
            throw new ReglaDeNegocioVioladaException("R11",
                    "El lote no esta en revision; su estado es " + estado.etiqueta() + ".");
        }
        if (responsable == null || responsable.isBlank()) {
            throw new ReglaDeNegocioVioladaException("R11",
                    "La validacion de un lote en revision debe registrar quien la realiza.");
        }
        if (motivoRevision != null && !motivoRevision.esResolubleConValidacion()) {
            throw new ReglaDeNegocioVioladaException(motivoRevision.codigoRegla(),
                    "La revision por '" + motivoRevision.descripcion()
                            + "' exige corregir el dato de origen antes de validar el lote.");
        }
        EstadoLote destino = estadoPrevioARevision == null ? EstadoLote.PUBLICADO : estadoPrevioARevision;
        cambiarEstado(destino, "validar revision");
        if (cadenaDeFrio.estaRota()) {
            this.cadenaDeFrio = cadenaDeFrio.restablecer(momento);
        }
        this.motivoRevision = null;
        this.detalleRevision = null;
        this.estadoPrevioARevision = null;
        tocar();
        registrar(new LoteValidado(id, responsable.trim(), destino, momento));
    }

    /** Corrige el calibre declarado y libera la revision abierta por la regla 18. */
    public void corregirCalibre(Calibre calibreReal, String responsable, Instant momento) {
        Objects.requireNonNull(calibreReal, "El calibre corregido no puede ser nulo.");
        this.calibre = calibreReal;
        if (estado == EstadoLote.EN_REVISION && motivoRevision == MotivoRevision.CALIBRE_NO_COINCIDE) {
            this.motivoRevision = null;
            EstadoLote destino = estadoPrevioARevision == null ? EstadoLote.PUBLICADO : estadoPrevioARevision;
            cambiarEstado(destino, "corregir calibre");
            this.estadoPrevioARevision = null;
            this.detalleRevision = null;
            registrar(new LoteValidado(id, responsable == null ? "sistema" : responsable, destino, momento));
        }
        tocar();
    }

    /** Regla 17: renueva la certificacion vencida y libera la revision correspondiente. */
    public void renovarSelloOrigen(SelloOrigen nuevoSello, String responsable, Instant momento) {
        Objects.requireNonNull(nuevoSello, "El sello de origen no puede ser nulo.");
        LocalDate hoy = fechaDe(momento);
        if (!nuevoSello.estaVigente(hoy)) {
            throw new ReglaDeNegocioVioladaException("R17",
                    "El sello con el que se intenta renovar ya esta vencido (" + nuevoSello.fechaVencimiento() + ").");
        }
        this.selloOrigen = nuevoSello;
        if (estado == EstadoLote.EN_REVISION && motivoRevision == MotivoRevision.CERTIFICACION_VENCIDA) {
            EstadoLote destino = estadoPrevioARevision == null ? EstadoLote.EN_ACOPIO : estadoPrevioARevision;
            cambiarEstado(destino, "renovar certificacion");
            this.motivoRevision = null;
            this.estadoPrevioARevision = null;
            this.detalleRevision = null;
            registrar(new LoteValidado(id, responsable == null ? "sistema" : responsable, destino, momento));
        }
        tocar();
    }

    /** Regla 15: cierra el recorrido cuando el destinatario recibe el lote. */
    public void confirmarEntrega(DestinatarioId destinatario, Instant momento) {
        Objects.requireNonNull(destinatario, "La entrega debe registrar al destinatario.");
        exigirGuiaActiva();
        guiaDespacho.registrarHito(
                HitoDespacho.de(TipoHito.ENTREGA_DESTINATARIO, guiaDespacho.destino(), momento));
        cambiarEstado(EstadoLote.ENTREGADO, "confirmar entrega");
        tocar();
        registrar(new LoteEntregado(id, destinatario, momento));
    }

    /** Cierra el lote por vencimiento de su vida util. */
    public void marcarVencido(Instant momento) {
        if (estado == EstadoLote.VENCIDO) {
            return;
        }
        cambiarEstado(EstadoLote.VENCIDO, "marcar vencido");
        if (guiaDespacho != null && guiaDespacho.estaAbierta()) {
            guiaDespacho.anular(momento);
        }
        tocar();
    }

    // =====================================================================
    //  DATOS OPCIONALES DE ORIGEN Y CALIDAD
    // =====================================================================

    public void asignarSelloOrigen(SelloOrigen sello, Instant momento) {
        Objects.requireNonNull(sello, "El sello de origen no puede ser nulo.");
        if (sello.estaVencido(fechaDe(momento))) {
            throw new ReglaDeNegocioVioladaException("R17",
                    "No puede asignarse un sello de origen ya vencido (" + sello.fechaVencimiento() + ").");
        }
        this.selloOrigen = sello;
        tocar();
    }

    public void asignarDenominacionOrigen(DenominacionOrigen denominacion, Instant momento) {
        Objects.requireNonNull(denominacion, "La denominacion de origen no puede ser nula.");
        if (!denominacion.estaVigente(fechaDe(momento))) {
            throw new ReglaDeNegocioVioladaException("R17",
                    "No puede asignarse una denominacion de origen vencida.");
        }
        this.denominacionOrigen = denominacion;
        tocar();
    }

    public void declararExcedenteCosecha(ExcedenteCosecha excedente) {
        this.excedenteCosecha = excedente;
        tocar();
    }

    public void cambiarPrecioFinca(PrecioFinca nuevoPrecio) {
        Objects.requireNonNull(nuevoPrecio, "El precio de finca no puede ser nulo.");
        if (estado.esTerminal()) {
            throw new ReglaDeNegocioVioladaException("INV-PRECIO",
                    "Un lote en estado " + estado.etiqueta() + " ya no admite cambios de precio.");
        }
        this.precioFinca = nuevoPrecio;
        tocar();
    }

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

    public Cantidad cantidadReservada() {
        return reservas.stream()
                .map(ReservaLote::cantidad)
                .reduce(Cantidad.cero(cantidadInicial.unidad()), Cantidad::mas);
    }

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
