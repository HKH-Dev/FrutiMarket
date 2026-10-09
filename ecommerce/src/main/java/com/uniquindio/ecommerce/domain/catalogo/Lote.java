package com.uniquindio.ecommerce.domain.catalogo;

import com.uniquindio.ecommerce.domain.event.EventoDominio;
import com.uniquindio.ecommerce.domain.exception.ReglaDeNegocioVioladaException;
import com.uniquindio.ecommerce.domain.valueobject.catalogo.Cantidad;
import com.uniquindio.ecommerce.domain.valueobject.catalogo.Merma;
import com.uniquindio.ecommerce.domain.valueobject.catalogo.PrecioFinca;
import com.uniquindio.ecommerce.domain.valueobject.identidad.AlmacenId;
import com.uniquindio.ecommerce.domain.valueobject.identidad.CampesinoId;
import com.uniquindio.ecommerce.domain.valueobject.identidad.LoteId;
import com.uniquindio.ecommerce.domain.valueobject.identidad.PedidoId;
import com.uniquindio.ecommerce.domain.valueobject.identidad.ProductoId;
import com.uniquindio.ecommerce.domain.valueobject.logistica.Conservacion;
import com.uniquindio.ecommerce.domain.valueobject.origenycalidad.Calidad;
import com.uniquindio.ecommerce.domain.valueobject.origenycalidad.FichaTrazabilidad;
import com.uniquindio.ecommerce.domain.valueobject.origenycalidad.TemporadaCosecha;
import com.uniquindio.ecommerce.domain.valueobject.origenycalidad.TipoCultivo;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Agregado raiz: cantidad de un producto que un campesino ofrece, con su calidad,
 * su vida util y su cadena de custodia (todo lo que le paso desde la finca).
 *
 * <p>Invariantes:</p>
 * <ul>
 *   <li><b>R3:</b> nunca puede reservarse mas cantidad de la disponible.</li>
 *   <li><b>R5:</b> nunca puede retirarse mientras tenga reservas activas; debe desactivarse.</li>
 *   <li><b>R2/R8:</b> nunca puede publicarse sin precio, cantidad y ficha de trazabilidad completa.</li>
 *   <li><b>R10:</b> nunca puede despacharse si su vida util no cubre el tiempo de transito.</li>
 *   <li><b>R11:</b> nunca se mueve un lote en revision o con la cadena de frio rota.</li>
 *   <li><b>INV-CUSTODIA:</b> la cadena de custodia siempre crece en orden cronologico; nunca se corrige.</li>
 *   <li><b>INV-ESTADO:</b> siempre cambia de estado segun las transiciones de {@link EstadoLote}.</li>
 * </ul>
 */
public class Lote {

    private static final ZoneId ZONA_OPERACION = ZoneId.of("America/Bogota");
    private static final int TOLERANCIA_CALIDAD = 10;

    private final LoteId id;
    private final String codigo;
    private final ProductoId producto;
    private final String nombreProducto;
    private final Producto.Linea linea;
    private final TipoCultivo tipoCultivo;
    private final Conservacion conservacion;
    private final TemporadaCosecha temporada;
    private final CampesinoId campesinoResponsable;
    private final FichaTrazabilidad fichaTrazabilidad;
    private final LocalDate fechaCosecha;
    private final LocalDate fechaLimiteConsumo;
    private final Cantidad cantidadInicial;
    private final Instant fechaRegistro;
    private final List<ReservaLote> reservas;
    private final List<RegistroCustodia> custodia;
    private final List<EventoDominio> eventos;

    private Calidad calidadDeclarada;
    private Calidad calidadVerificada;
    private Cantidad cantidadDisponible;
    private PrecioFinca precioFinca;
    private Merma merma;
    private boolean mermaAplicada;
    private EstadoLote estado;
    private AlmacenId almacenActual;
    private boolean cadenaFrioRota;
    private EstadoLote estadoPrevioARevision;
    private MotivoRevision motivoRevision;
    private String detalleRevision;
    private Instant fechaPublicacion;
    private long version;

    private Lote(Builder builder) {
        this.id = builder.id;
        this.codigo = builder.codigo;
        this.producto = builder.producto.id();
        this.nombreProducto = builder.producto.nombre();
        this.linea = builder.producto.linea();
        this.tipoCultivo = builder.producto.tipoCultivo();
        this.conservacion = builder.producto.conservacion();
        this.temporada = builder.producto.temporada();
        this.campesinoResponsable = builder.campesino;
        this.fichaTrazabilidad = builder.fichaTrazabilidad;
        this.fechaCosecha = builder.fechaCosecha;
        this.fechaLimiteConsumo = builder.fechaLimiteConsumo;
        this.cantidadInicial = builder.cantidadInicial;
        this.cantidadDisponible = builder.cantidadInicial;
        this.calidadDeclarada = builder.calidad;
        this.precioFinca = builder.precioFinca;
        this.merma = builder.merma;
        this.fechaRegistro = builder.momento;
        this.reservas = new ArrayList<>();
        this.custodia = new ArrayList<>();
        this.eventos = new ArrayList<>();
        this.estado = EstadoLote.REGISTRADO;
    }

    public static Builder builder() {
        return new Builder();
    }

    // =====================================================================
    //  CATALOGO Y DISPONIBILIDAD
    // =====================================================================

    /** Reglas 2 y 8. Aplica la merma antes, para no ofrecer producto que se va a perder. */
    public void publicar(Instant momento) {
        Optional<String> impedimento = motivoNoPublicable(momento);
        ReglaDeNegocioVioladaException.validar(impedimento.isEmpty(), "R2/R8",
                "El lote no puede publicarse: " + impedimento.orElse(""));
        aplicarMerma(momento);
        ReglaDeNegocioVioladaException.validar(!cantidadDisponible.esCero(), "R2",
                "El lote no puede publicarse: tras aplicar la merma no queda cantidad disponible.");
        cambiarEstado(EstadoLote.PUBLICADO, "publicar");
        this.fechaPublicacion = momento;
        registrar(EventoDominio.Tipo.LOTE_PUBLICADO, "Disponible: " + cantidadDisponible, momento);
    }

    /** Explica por que el lote no puede publicarse, o vacio si si puede. */
    public Optional<String> motivoNoPublicable(Instant momento) {
        LocalDate hoy = fechaDe(momento);
        if (estado == EstadoLote.EN_REVISION) {
            return Optional.of("esta en revision por " + motivoRevision.descripcion() + ".");
        }
        if (estado != EstadoLote.REGISTRADO && estado != EstadoLote.DESACTIVADO) {
            return Optional.of("esta en estado " + estado.etiqueta() + " y no puede volver a publicarse.");
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
        if (linea == Producto.Linea.MATERIA_PRIMA && !temporada.contiene(hoy)) {
            return Optional.of("la fecha " + hoy + " esta fuera de la temporada de cosecha " + temporada + ".");
        }
        if (estaVencido(hoy)) {
            return Optional.of("su vida util vencio el " + fechaLimiteConsumo + ".");
        }
        return Optional.empty();
    }

    /** Descuenta la merma una sola vez; volver a llamarla no descuenta de nuevo. */
    public void aplicarMerma(Instant momento) {
        if (mermaAplicada || merma.esNula()) {
            return;
        }
        Cantidad perdida = merma.perdidaSobre(cantidadDisponible);
        this.cantidadDisponible = cantidadDisponible.menos(perdida);
        this.mermaAplicada = true;
        tocar();
        registrar(EventoDominio.Tipo.MERMA_APLICADA, "Perdida de " + perdida, momento);
    }

    public void redefinirMerma(Merma nuevaMerma) {
        ReglaDeNegocioVioladaException.validar(nuevaMerma != null, "INV-MERMA", "La merma no puede ser nula.");
        ReglaDeNegocioVioladaException.validar(estado == EstadoLote.REGISTRADO, "INV-MERMA",
                "La merma solo puede redefinirse mientras el lote esta en borrador.");
        this.merma = nuevaMerma;
        this.mermaAplicada = false;
        tocar();
    }

    /** Regla 3. */
    public void reservar(PedidoId pedido, Cantidad cantidad, Instant momento) {
        ReglaDeNegocioVioladaException.validar(pedido != null && cantidad != null, "R3",
                "La reserva debe indicar pedido y cantidad.");
        ReglaDeNegocioVioladaException.validar(estado.admitePedidos(), "R3",
                "El lote esta en estado " + estado.etiqueta() + " y no admite pedidos nuevos.");
        ReglaDeNegocioVioladaException.validar(!cantidad.esCero(), "R3", "Un pedido no puede reservar cantidad cero.");
        ReglaDeNegocioVioladaException.validar(!cantidad.esMayorQue(cantidadDisponible), "R3",
                "La cantidad solicitada (" + cantidad + ") supera la cantidad disponible del lote ("
                        + cantidadDisponible + ").");
        ReglaDeNegocioVioladaException.validar(buscarReserva(pedido).isEmpty(), "R6",
                "El pedido " + pedido + " ya tiene una reserva activa sobre este lote.");
        this.cantidadDisponible = cantidadDisponible.menos(cantidad);
        this.reservas.add(ReservaLote.crear(pedido, cantidad, momento));
        tocar();
        registrar(EventoDominio.Tipo.CANTIDAD_RESERVADA, "Pedido " + pedido + ": " + cantidad, momento);
        if (cantidadDisponible.esCero()) {
            cambiarEstado(EstadoLote.AGOTADO, "agotar");
            registrar(EventoDominio.Tipo.LOTE_AGOTADO, "Sin cantidad disponible", momento);
        }
    }

    public void liberarReserva(PedidoId pedido, Instant momento) {
        ReservaLote reserva = exigirReserva(pedido);
        reservas.remove(reserva);
        this.cantidadDisponible = cantidadDisponible.mas(reserva.cantidad());
        tocar();
        registrar(EventoDominio.Tipo.RESERVA_LIBERADA, "Pedido " + pedido + ": " + reserva.cantidad(), momento);
        if (estado == EstadoLote.AGOTADO) {
            cambiarEstado(EstadoLote.PUBLICADO, "liberar reserva");
        }
    }

    public void confirmarSalida(PedidoId pedido) {
        reservas.remove(exigirReserva(pedido));
        tocar();
    }

    /** Regla 5: retira el lote de la venta sin borrarlo. */
    public void desactivar(String motivo) {
        cambiarEstado(EstadoLote.DESACTIVADO, "desactivar");
        this.detalleRevision = (motivo == null || motivo.isBlank()) ? "Desactivado por el campesino" : motivo.trim();
        tocar();
    }

    public void reactivar(Instant momento) {
        ReglaDeNegocioVioladaException.validar(estado == EstadoLote.DESACTIVADO, "INV-ESTADO",
                "Solo puede reactivarse un lote desactivado.");
        Optional<String> impedimento = motivoNoPublicable(momento);
        ReglaDeNegocioVioladaException.validar(impedimento.isEmpty(), "R2/R8",
                "El lote no puede volver al catalogo: " + impedimento.orElse(""));
        cambiarEstado(EstadoLote.PUBLICADO, "reactivar");
        this.detalleRevision = null;
        tocar();
    }

    /** Regla 5: baja definitiva, rechazada mientras existan reservas activas. */
    public void retirar() {
        ReglaDeNegocioVioladaException.validar(!tieneReservasActivas(), "R5",
                "El lote tiene " + reservas.size() + " pedido(s) activo(s) y no puede retirarse. "
                        + "Debe desactivarse para evitar ventas nuevas.");
        cambiarEstado(EstadoLote.RETIRADO, "retirar");
        tocar();
    }

    public void cambiarPrecioFinca(PrecioFinca nuevoPrecio) {
        ReglaDeNegocioVioladaException.validar(nuevoPrecio != null && nuevoPrecio.unidadReferencia() == cantidadInicial.unidad(),
                "INV-PRECIO", "El precio debe expresarse en la unidad del lote.");
        ReglaDeNegocioVioladaException.validar(!estado.esTerminal(), "INV-PRECIO",
                "Un lote " + estado.etiqueta() + " ya no admite cambios de precio.");
        this.precioFinca = nuevoPrecio;
        tocar();
    }

    // =====================================================================
    //  CADENA DE CUSTODIA
    // =====================================================================

    /** El lote entra a un punto de acopio o centro de redistribucion (desde la finca o desde un transito). */
    public void ingresarAAlmacen(AlmacenId almacen, String nombreAlmacen, String responsable, Instant momento) {
        ReglaDeNegocioVioladaException.validar(almacen != null, "INV-CUSTODIA", "El almacenamiento es obligatorio.");
        exigirQueNoEsteDetenido();
        cambiarEstado(EstadoLote.EN_ACOPIO, "ingresar a almacenamiento");
        this.almacenActual = almacen;
        agregarCustodia(RegistroCustodia.Tipo.INGRESO_ALMACEN, almacen, nombreAlmacen, responsable, momento, null, "");
        registrar(EventoDominio.Tipo.LOTE_ALMACENADO, nombreAlmacen, momento);
    }

    /** Reglas 9, 10 y 11: sale del almacenamiento actual hacia otro almacen o hacia el comprador. */
    public void despachar(String destino, Duration tiempoTransito, String responsable, Instant momento) {
        exigirQueNoEsteDetenido();
        ReglaDeNegocioVioladaException.validar(estado.permiteDespacho(), "R9",
                "El lote no puede despacharse: debe estar en un almacenamiento; su estado es " + estado.etiqueta() + ".");
        ReglaDeNegocioVioladaException.validar(destino != null && !destino.isBlank(), "INV-CUSTODIA",
                "El despacho debe indicar el destino.");
        ReglaDeNegocioVioladaException.validar(tiempoTransito != null && !tiempoTransito.isNegative()
                && !tiempoTransito.isZero(), "R10", "El despacho debe declarar un tiempo de transito positivo.");
        ReglaDeNegocioVioladaException.validar(vidaUtilAlcanzaPara(tiempoTransito, momento), "R10",
                "El lote no puede despacharse: su vida util restante (" + diasParaVencer(momento)
                        + " dias) no cubre el transito de " + tiempoTransito.toHours() + " h.");
        agregarCustodia(RegistroCustodia.Tipo.SALIDA_ALMACEN, almacenActual, "Hacia " + destino.trim(),
                responsable, momento, null, "Transito estimado " + tiempoTransito.toHours() + " h");
        cambiarEstado(EstadoLote.EN_TRANSITO, "despachar");
        this.almacenActual = null;
        registrar(EventoDominio.Tipo.LOTE_DESPACHADO, "Hacia " + destino, momento);
    }

    /** Regla 11: una lectura fuera del rango de su conservacion rompe la cadena de frio. */
    public void registrarTemperatura(BigDecimal temperaturaC, String responsable, Instant momento) {
        ReglaDeNegocioVioladaException.validar(temperaturaC != null, "R11", "La lectura de temperatura es obligatoria.");
        agregarCustodia(RegistroCustodia.Tipo.CONTROL_TEMPERATURA, almacenActual, lugarActual(), responsable,
                momento, temperaturaC, "");
        if (conservacion.requiereCadenaFrio() && !conservacion.admiteTemperatura(temperaturaC)) {
            this.cadenaFrioRota = true;
            ponerEnRevision(MotivoRevision.RUPTURA_CADENA_FRIO,
                    "Lectura de " + temperaturaC + " C fuera del rango " + conservacion.rango(), momento);
        }
    }

    /** Regla 11: cualquier novedad reportada detiene el lote hasta que se valide. */
    public void reportarIncidencia(String detalle, String responsable, Instant momento) {
        String texto = (detalle == null || detalle.isBlank()) ? "Incidencia sin detalle" : detalle.trim();
        agregarCustodia(RegistroCustodia.Tipo.INCIDENCIA, almacenActual, lugarActual(), responsable, momento, null, texto);
        ponerEnRevision(MotivoRevision.INCIDENCIA, texto, momento);
    }

    /** Regla 15 (la cobertura la valida el caso de uso): cierra la cadena de custodia. */
    public void confirmarEntrega(String receptor, String responsable, Instant momento) {
        ReglaDeNegocioVioladaException.validar(estado == EstadoLote.EN_TRANSITO, "R15",
                "Solo puede entregarse un lote en transito; su estado es " + estado.etiqueta() + ".");
        agregarCustodia(RegistroCustodia.Tipo.ENTREGA, null, "Destino del comprador", responsable, momento, null,
                "Recibido por " + (receptor == null || receptor.isBlank() ? "comprador" : receptor.trim()));
        cambiarEstado(EstadoLote.ENTREGADO, "confirmar entrega");
        registrar(EventoDominio.Tipo.LOTE_ENTREGADO, "Recibido por " + receptor, momento);
    }

    public void marcarVencido(Instant momento) {
        ReglaDeNegocioVioladaException.validar(estaVencido(fechaDe(momento)), "INV-VIDA-UTIL",
                "El lote " + codigo + " aun no vence.");
        cambiarEstado(EstadoLote.VENCIDO, "marcar vencido");
        tocar();
    }

    // =====================================================================
    //  CALIDAD
    // =====================================================================

    /**
     * Regla 18: un inspector registra la calidad que observa. Si queda por debajo de la
     * declarada mas alla de la tolerancia, el lote queda en revision.
     *
     * @return {@code true} si la calidad observada coincide con la declarada.
     */
    public boolean inspeccionarCalidad(Calidad observada, String responsable, Instant momento) {
        ReglaDeNegocioVioladaException.validar(observada != null, "R18", "La calidad observada es obligatoria.");
        agregarCustodia(RegistroCustodia.Tipo.CONTROL_CALIDAD, almacenActual, lugarActual(), responsable, momento,
                null, "Observado: " + observada);
        this.calidadVerificada = observada;
        if (calidadDeclarada.superaPorMasDe(observada, TOLERANCIA_CALIDAD)) {
            ponerEnRevision(MotivoRevision.CALIDAD_NO_COINCIDE,
                    "Declarada " + calidadDeclarada.puntaje() + " pts, observada " + observada.puntaje() + " pts", momento);
            return false;
        }
        return true;
    }

    /** Regla 18: el campesino corrige la calidad declarada y se libera la revision. */
    public void corregirCalidadDeclarada(Calidad nueva, String responsable, Instant momento) {
        ReglaDeNegocioVioladaException.validar(nueva != null, "R18", "La calidad corregida es obligatoria.");
        this.calidadDeclarada = nueva;
        tocar();
        if (estado == EstadoLote.EN_REVISION && motivoRevision == MotivoRevision.CALIDAD_NO_COINCIDE) {
            salirDeRevision(responsable, "corregir calidad", momento);
        }
    }

    /** Regla 11: un responsable valida el lote detenido y este vuelve a su estado anterior. */
    public void validarRevision(String responsable, Instant momento) {
        ReglaDeNegocioVioladaException.validar(estado == EstadoLote.EN_REVISION, "R11",
                "El lote no esta en revision; su estado es " + estado.etiqueta() + ".");
        ReglaDeNegocioVioladaException.validar(responsable != null && !responsable.isBlank(), "R11",
                "La validacion debe registrar quien la realiza.");
        ReglaDeNegocioVioladaException.validar(motivoRevision.esResolubleConValidacion(), motivoRevision.codigoRegla(),
                "La revision por '" + motivoRevision.descripcion() + "' exige corregir el dato antes de validar.");
        this.cadenaFrioRota = false;
        salirDeRevision(responsable.trim(), "validar revision", momento);
    }

    // =====================================================================
    //  CONSULTAS
    // =====================================================================

    public boolean estaDisponible() {
        return estado.admitePedidos() && !cantidadDisponible.esCero();
    }

    public boolean esDelCampesino(CampesinoId campesino) {
        return campesinoResponsable.equals(campesino);
    }

    /** Puntaje de calidad para comparar lotes: el verificado por inspeccion si existe, si no el declarado. */
    public int puntajeCalidad() {
        return (calidadVerificada != null ? calidadVerificada : calidadDeclarada).puntaje();
    }

    public boolean esPerecedero() {
        return fechaLimiteConsumo != null;
    }

    public boolean estaVencido(LocalDate hoy) {
        return fechaLimiteConsumo != null && hoy.isAfter(fechaLimiteConsumo);
    }

    /** Dias de vida util; {@code Long.MAX_VALUE} si el lote no es perecedero. */
    public long diasParaVencer(Instant momento) {
        return fechaLimiteConsumo == null ? Long.MAX_VALUE : ChronoUnit.DAYS.between(fechaDe(momento), fechaLimiteConsumo);
    }

    /** Regla 10: las horas sueltas del transito cuentan como un dia completo. */
    public boolean vidaUtilAlcanzaPara(Duration tiempoTransito, Instant momento) {
        if (fechaLimiteConsumo == null) {
            return true;
        }
        long diasTransito = tiempoTransito.toDays() + (tiempoTransito.toHoursPart() > 0 ? 1 : 0);
        return diasParaVencer(momento) > diasTransito;
    }

    /** Regla 16: el lote que vence antes se despacha primero. */
     public boolean tieneMasPrioridadDeDespachoQue(Lote otro, Instant momento) {
        return diasParaVencer(momento) < otro.diasParaVencer(momento);
    }

    public boolean tieneReservasActivas() {
        return !reservas.isEmpty();
    }

    /** Ultimo almacenamiento del que salio el lote; lo usa la regla 15 para validar la cobertura. */
    public Optional<AlmacenId> ultimoAlmacenDeSalida() {
        for (int i = custodia.size() - 1; i >= 0; i--) {
            RegistroCustodia registro = custodia.get(i);
            if (registro.tipo() == RegistroCustodia.Tipo.SALIDA_ALMACEN) {
                return registro.almacen();
            }
        }
        return Optional.empty();
    }

    public List<EventoDominio> eventos() {
        return List.copyOf(eventos);
    }

    public void limpiarEventos() {
        eventos.clear();
    }

    // =====================================================================
    //  INTERNOS
    // =====================================================================

    private void cambiarEstado(EstadoLote destino, String operacion) {
        if (estado == destino) {
            return;
        }
        ReglaDeNegocioVioladaException.validar(estado.puedePasarA(destino), "INV-ESTADO",
                "No se puede " + operacion + ": un lote " + estado.etiqueta()
                        + " no puede pasar a " + destino.etiqueta() + ".");
        this.estado = destino;
    }

    private void exigirQueNoEsteDetenido() {
        ReglaDeNegocioVioladaException.validar(estado != EstadoLote.EN_REVISION, "R11",
                "El lote esta en revision por " + (motivoRevision == null ? "" : motivoRevision.descripcion())
                        + " y no puede moverse hasta ser validado.");
        ReglaDeNegocioVioladaException.validar(!cadenaFrioRota, "R11", "El lote tiene la cadena de frio rota.");
    }

    private void agregarCustodia(RegistroCustodia.Tipo tipo, AlmacenId almacen, String lugar, String responsable,
                                 Instant momento, BigDecimal temperaturaC, String observacion) {
        RegistroCustodia nuevo = RegistroCustodia.crear(tipo, almacen, lugar, responsable, momento, temperaturaC, observacion);
        ReglaDeNegocioVioladaException.validar(custodia.isEmpty()
                        || !momento.isBefore(custodia.get(custodia.size() - 1).momento()), "INV-CUSTODIA",
                "Un registro de custodia no puede ser anterior al ultimo registrado.");
        custodia.add(nuevo);
        tocar();
    }

    private String lugarActual() {
        return almacenActual != null ? "Almacenamiento " + almacenActual : estado.etiqueta();
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
        registrar(EventoDominio.Tipo.LOTE_PUESTO_EN_REVISION, motivo.descripcion() + ": " + detalle, momento);
    }

    private void salirDeRevision(String responsable, String operacion, Instant momento) {
        EstadoLote destino = estadoPrevioARevision == null ? EstadoLote.PUBLICADO : estadoPrevioARevision;
        cambiarEstado(destino, operacion);
        this.motivoRevision = null;
        this.detalleRevision = null;
        this.estadoPrevioARevision = null;
        tocar();
        registrar(EventoDominio.Tipo.LOTE_VALIDADO, "Validado por " + (responsable == null ? "sistema" : responsable), momento);
    }

    private Optional<ReservaLote> buscarReserva(PedidoId pedido) {
        return reservas.stream().filter(r -> r.pedido().equals(pedido)).findFirst();
    }

    private ReservaLote exigirReserva(PedidoId pedido) {
        return buscarReserva(pedido).orElseThrow(() -> new ReglaDeNegocioVioladaException("INV-RESERVA",
                "El pedido " + pedido + " no tiene reserva activa sobre este lote."));
    }

    private void registrar(EventoDominio.Tipo tipo, String detalle, Instant momento) {
        eventos.add(new EventoDominio(tipo, id.toString(), detalle, momento));
    }

    private void tocar() {
        this.version++;
    }

    private static LocalDate fechaDe(Instant momento) {
        return LocalDate.ofInstant(momento, ZONA_OPERACION);
    }

    // =====================================================================
    //  ACCESORES
    // =====================================================================

    public LoteId id() { return id; }
    public String codigo() { return codigo; }
    public ProductoId producto() { return producto; }
    public String nombreProducto() { return nombreProducto; }
    public Producto.Linea linea() { return linea; }
    public TipoCultivo tipoCultivo() { return tipoCultivo; }
    public Conservacion conservacion() { return conservacion; }
    public CampesinoId campesinoResponsable() { return campesinoResponsable; }
    public FichaTrazabilidad fichaTrazabilidad() { return fichaTrazabilidad; }
    public Calidad calidadDeclarada() { return calidadDeclarada; }
    public Optional<Calidad> calidadVerificada() { return Optional.ofNullable(calidadVerificada); }
    public LocalDate fechaCosecha() { return fechaCosecha; }
    public Optional<LocalDate> fechaLimiteConsumo() { return Optional.ofNullable(fechaLimiteConsumo); }
    public Cantidad cantidadInicial() { return cantidadInicial; }
    public Cantidad cantidadDisponible() { return cantidadDisponible; }
    public PrecioFinca precioFinca() { return precioFinca; }
    public Merma merma() { return merma; }
    public EstadoLote estado() { return estado; }
    public Optional<AlmacenId> almacenActual() { return Optional.ofNullable(almacenActual); }
    public boolean cadenaFrioRota() { return cadenaFrioRota; }
    public Optional<MotivoRevision> motivoRevision() { return Optional.ofNullable(motivoRevision); }
    public Optional<String> detalleRevision() { return Optional.ofNullable(detalleRevision); }
    public List<ReservaLote> reservas() { return List.copyOf(reservas); }
    public List<RegistroCustodia> custodia() { return List.copyOf(custodia); }
    public Instant fechaRegistro() { return fechaRegistro; }
    public Optional<Instant> fechaPublicacion() { return Optional.ofNullable(fechaPublicacion); }
    public long version() { return version; }

    @Override
    public boolean equals(Object o) {
        return o instanceof Lote otro && id.equals(otro.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return "Lote[" + codigo + ", " + nombreProducto + ", " + estado.etiqueta() + ", disponible " + cantidadDisponible + "]";
    }

    // =====================================================================
    //  BUILDER: aqui viven las validaciones de creacion, no en el constructor
    // =====================================================================

    public static final class Builder {
        private final LoteId id = LoteId.nuevo();
        private String codigo;
        private Producto producto;
        private CampesinoId campesino;
        private FichaTrazabilidad fichaTrazabilidad;
        private Calidad calidad;
        private Cantidad cantidadInicial;
        private PrecioFinca precioFinca;
        private LocalDate fechaCosecha;
        private Merma merma = Merma.ninguna();
        private Instant momento;
        private LocalDate fechaLimiteConsumo;

        private Builder() {
        }

        public Builder codigo(String codigo) { this.codigo = codigo; return this; }
        public Builder producto(Producto producto) { this.producto = producto; return this; }
        public Builder campesino(CampesinoId campesino) { this.campesino = campesino; return this; }
        public Builder fichaTrazabilidad(FichaTrazabilidad ficha) { this.fichaTrazabilidad = ficha; return this; }
        public Builder calidad(Calidad calidad) { this.calidad = calidad; return this; }
        public Builder cantidadInicial(Cantidad cantidad) { this.cantidadInicial = cantidad; return this; }
        public Builder precioFinca(PrecioFinca precio) { this.precioFinca = precio; return this; }
        /** Fecha de cosecha, o de elaboracion si el producto es transformado. */
        public Builder fechaCosecha(LocalDate fecha) { this.fechaCosecha = fecha; return this; }
        public Builder merma(Merma merma) { this.merma = merma; return this; }
        public Builder momentoRegistro(Instant momento) { this.momento = momento; return this; }

        public Lote build() {
            ReglaDeNegocioVioladaException.validar(codigo != null && !codigo.isBlank(), "INV-CODIGO",
                    "Todo lote debe tener un codigo con el que el campesino lo identifique.");
            ReglaDeNegocioVioladaException.validar(producto != null && producto.estaActivo(), "INV-PRODUCTO",
                    "El lote debe referirse a un producto activo del catalogo.");
            ReglaDeNegocioVioladaException.validar(campesino != null, "R8", "El lote debe tener campesino responsable.");
            ReglaDeNegocioVioladaException.validar(fichaTrazabilidad != null
                    && fichaTrazabilidad.campesinoResponsable().equals(campesino), "R8",
                    "El lote debe tener ficha de trazabilidad del mismo campesino responsable.");
            ReglaDeNegocioVioladaException.validar(calidad != null, "R18", "El lote debe declarar su calidad.");
            ReglaDeNegocioVioladaException.validar(cantidadInicial != null && !cantidadInicial.esCero(), "R2",
                    "Un lote no puede registrarse sin cantidad.");
            ReglaDeNegocioVioladaException.validar(cantidadInicial.unidad() == producto.unidadVenta(), "INV-UNIDAD",
                    "El producto se vende en " + producto.unidadVenta().simbolo() + " y la cantidad viene en "
                            + cantidadInicial.unidad().simbolo() + ".");
            ReglaDeNegocioVioladaException.validar(precioFinca == null
                    || precioFinca.unidadReferencia() == producto.unidadVenta(), "INV-UNIDAD",
                    "El precio debe expresarse en la unidad de venta del producto.");
            ReglaDeNegocioVioladaException.validar(merma != null, "INV-MERMA", "La merma no puede ser nula.");
            ReglaDeNegocioVioladaException.validar(fechaCosecha != null && momento != null, "INV-COSECHA",
                    "El lote debe declarar fecha de cosecha y momento de registro.");

            LocalDate hoy = fechaDe(momento);
            fichaTrazabilidad.validarContra(hoy);
            ReglaDeNegocioVioladaException.validar(!fechaCosecha.isAfter(hoy), "INV-COSECHA",
                    "La fecha de cosecha (" + fechaCosecha + ") no puede ser futura.");
            fechaLimiteConsumo = producto.esPerecedero() ? fechaCosecha.plusDays(producto.vidaUtilDias()) : null;
            ReglaDeNegocioVioladaException.validar(fechaLimiteConsumo == null || !fechaLimiteConsumo.isBefore(hoy),
                    "INV-VIDA-UTIL", "El lote ya estaria vencido al registrarse: su limite seria " + fechaLimiteConsumo + ".");
            codigo = codigo.trim();

            Lote lote = new Lote(this);
            lote.agregarCustodia(RegistroCustodia.Tipo.COSECHA, null, "Finca " + fichaTrazabilidad.fincaOrigen(),
                    "Campesino " + campesino, momento, null, "Calidad declarada: " + calidad);
            lote.registrar(EventoDominio.Tipo.LOTE_REGISTRADO, "Registro " + cantidadInicial + " de " + producto.nombre(), momento);
            return lote;
        }
    }
}
