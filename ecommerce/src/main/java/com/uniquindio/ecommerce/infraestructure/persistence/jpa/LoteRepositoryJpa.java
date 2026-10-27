package com.uniquindio.ecommerce.infraestructure.persistence.jpa;

import com.uniquindio.ecommerce.domain.catalogo.EstadoLote;
import com.uniquindio.ecommerce.domain.catalogo.Lote;
import com.uniquindio.ecommerce.domain.catalogo.RegistroCustodia;
import com.uniquindio.ecommerce.domain.catalogo.ReservaLote;
import com.uniquindio.ecommerce.domain.repository.LoteRepository;
import com.uniquindio.ecommerce.domain.valueobject.catalogo.Cantidad;
import com.uniquindio.ecommerce.domain.valueobject.catalogo.Merma;
import com.uniquindio.ecommerce.domain.valueobject.catalogo.PrecioFinca;
import com.uniquindio.ecommerce.domain.valueobject.catalogo.UnidadMedida;
import com.uniquindio.ecommerce.domain.valueobject.identidad.AlmacenId;
import com.uniquindio.ecommerce.domain.valueobject.identidad.CampesinoId;
import com.uniquindio.ecommerce.domain.valueobject.identidad.GuiaDespachoId;
import com.uniquindio.ecommerce.domain.valueobject.identidad.LoteId;
import com.uniquindio.ecommerce.domain.valueobject.identidad.PedidoId;
import com.uniquindio.ecommerce.domain.valueobject.identidad.ProductoId;
import com.uniquindio.ecommerce.domain.valueobject.origenycalidad.Calidad;
import com.uniquindio.ecommerce.domain.valueobject.origenycalidad.FichaTrazabilidad;
import com.uniquindio.ecommerce.domain.valueobject.origenycalidad.TipoCultivo;
import com.uniquindio.ecommerce.infraestructure.persistence.jpa.entity.LoteEntity;
import com.uniquindio.ecommerce.infraestructure.persistence.jpa.entity.RegistroCustodiaEmbeddable;
import com.uniquindio.ecommerce.infraestructure.persistence.jpa.entity.ReservaLoteEmbeddable;
import com.uniquindio.ecommerce.infraestructure.persistence.jpa.springdata.LoteJpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.MonthDay;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
@Transactional
public class LoteRepositoryJpa implements LoteRepository {

    private static final Set<EstadoLote> ADMITEN_PEDIDOS = estadosDonde(true, false);
    private static final Set<EstadoLote> NO_TERMINALES = estadosDonde(false, true);

    private final LoteJpaRepository jpa;

    public LoteRepositoryJpa(LoteJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Lote almacenar(Lote lote) {
        jpa.save(aEntidad(lote));
        return lote;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Lote> obtenerLote(LoteId id) {
        return jpa.findById(id.valor()).map(LoteRepositoryJpa::aDominio);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existeCodigo(String codigo) {
        return codigo != null && jpa.existsByCodigoIgnoreCase(codigo.trim());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Lote> lotesDisponiblesDe(ProductoId producto) {
        return aDominio(jpa.findByProductoIdAndEstadoInAndCantidadDisponibleGreaterThan(
                producto.valor(), ADMITEN_PEDIDOS, BigDecimal.ZERO));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Lote> lotesDelCampesino(CampesinoId campesino) {
        return aDominio(jpa.findByCampesinoId(campesino.valor()));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Lote> lotesEnAlmacen(AlmacenId almacen, TipoCultivo tipoCultivo) {
        return aDominio(jpa.findByEstadoAndAlmacenActualIdAndTipoCultivo(EstadoLote.EN_ACOPIO, almacen.valor(), tipoCultivo));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Lote> lotesVencidosSinCerrar(LocalDate hoy) {
        return aDominio(jpa.findByFechaLimiteConsumoBeforeAndEstadoIn(hoy, NO_TERMINALES));
    }

    // =====================================================================
    //  MAPEO DOMINIO <-> TABLA
    // =====================================================================

    static LoteEntity aEntidad(Lote lote) {
        LoteEntity e = new LoteEntity();
        e.setId(lote.id().valor());
        e.setCodigo(lote.codigo());
        e.setProductoId(lote.producto().valor());
        e.setNombreProducto(lote.nombreProducto());
        e.setLinea(lote.linea());
        e.setTipoCultivo(lote.tipoCultivo());
        e.setConservacion(lote.conservacion());
        e.setTemporadaInicio(lote.temporada().inicio().map(MonthDay::toString).orElse(null));
        e.setTemporadaFin(lote.temporada().fin().map(MonthDay::toString).orElse(null));

        FichaTrazabilidad ficha = lote.fichaTrazabilidad();
        e.setCampesinoId(lote.campesinoResponsable().valor());
        e.setFincaOrigen(ficha.fincaOrigen());
        e.setTecnicaProduccion(ficha.tecnica());
        e.setFichaFechaCosecha(ficha.fechaCosecha());
        e.setFichaObservaciones(ficha.observaciones());
        e.setFechaCosecha(lote.fechaCosecha());
        e.setFechaLimiteConsumo(lote.fechaLimiteConsumo().orElse(null));

        Calidad declarada = lote.calidadDeclarada();
        e.setCalidadDeclaradaCategoria(declarada.categoria());
        e.setCalidadDeclaradaDefectos(declarada.porcentajeDefectos());
        e.setCalidadDeclaradaOrganico(declarada.certificadoOrganico());
        lote.calidadVerificada().ifPresent(v -> {
            e.setCalidadVerificadaCategoria(v.categoria());
            e.setCalidadVerificadaDefectos(v.porcentajeDefectos());
            e.setCalidadVerificadaOrganico(v.certificadoOrganico());
        });

        e.setUnidad(lote.cantidadInicial().unidad());
        e.setCantidadInicial(lote.cantidadInicial().valor());
        e.setCantidadDisponible(lote.cantidadDisponible().valor());
        e.setPrecioFinca(lote.precioFinca() == null ? null : lote.precioFinca().valorPorUnidad());
        e.setMermaPorcentaje(lote.merma().porcentaje());
        e.setMermaJustificacion(lote.merma().justificacion());
        e.setMermaAplicada(lote.mermaAplicada());

        e.setEstado(lote.estado());
        e.setEstadoPrevioARevision(lote.estadoPrevioARevision().orElse(null));
        e.setMotivoRevision(lote.motivoRevision().orElse(null));
        e.setDetalleRevision(lote.detalleRevision().orElse(null));
        e.setAlmacenActualId(lote.almacenActual().map(AlmacenId::valor).orElse(null));
        e.setCadenaFrioRota(lote.cadenaFrioRota());
        e.setFechaRegistro(lote.fechaRegistro());
        e.setFechaPublicacion(lote.fechaPublicacion().orElse(null));
        e.setVersion(lote.version());

        e.setReservas(new ArrayList<>(lote.reservas().stream().map(r -> new ReservaLoteEmbeddable(
                r.pedido().valor(), r.cantidad().valor(), r.momento())).toList()));
        e.setCustodia(new ArrayList<>(lote.custodia().stream().map(c -> new RegistroCustodiaEmbeddable(
                c.tipo(), c.almacen().map(AlmacenId::valor).orElse(null), c.guia().map(GuiaDespachoId::valor).orElse(null),
                c.lugar(), c.responsable(), c.momento(), c.temperaturaC().orElse(null), c.observacion())).toList()));
        return e;
    }

    static Lote aDominio(LoteEntity e) {
        UnidadMedida unidad = e.getUnidad();
        CampesinoId campesino = CampesinoId.de(e.getCampesinoId());
        Calidad verificada = e.getCalidadVerificadaCategoria() == null ? null : Calidad.de(
                e.getCalidadVerificadaCategoria(), e.getCalidadVerificadaDefectos(), e.getCalidadVerificadaOrganico());

        List<ReservaLote> reservas = e.getReservas().stream().map(r -> ReservaLote.reconstituir(
                PedidoId.de(r.getPedidoId()), Cantidad.de(r.getCantidad(), unidad), r.getMomento())).toList();
        List<RegistroCustodia> custodia = e.getCustodia().stream().map(c -> RegistroCustodia.reconstituir(
                c.getTipo(), c.getAlmacenId() == null ? null : AlmacenId.de(c.getAlmacenId()),
                c.getGuiaId() == null ? null : new GuiaDespachoId(c.getGuiaId()), c.getLugar(), c.getResponsable(),
                c.getMomento(), c.getTemperaturaC(), c.getObservacion())).toList();

        return Lote.reconstituir(new Lote.Datos(
                LoteId.de(e.getId()),
                e.getCodigo(),
                ProductoId.de(e.getProductoId()),
                e.getNombreProducto(),
                e.getLinea(),
                e.getTipoCultivo(),
                e.getConservacion(),
                ProductoRepositoryJpa.temporada(e.getTemporadaInicio(), e.getTemporadaFin()),
                campesino,
                FichaTrazabilidad.registrar(campesino, e.getFincaOrigen(), e.getTecnicaProduccion(),
                        e.getFichaFechaCosecha(), e.getFichaObservaciones()),
                e.getFechaCosecha(),
                e.getFechaLimiteConsumo(),
                Cantidad.de(e.getCantidadInicial(), unidad),
                e.getFechaRegistro(),
                reservas,
                custodia,
                Calidad.de(e.getCalidadDeclaradaCategoria(), e.getCalidadDeclaradaDefectos(), e.isCalidadDeclaradaOrganico()),
                verificada,
                Cantidad.de(e.getCantidadDisponible(), unidad),
                e.getPrecioFinca() == null ? null : PrecioFinca.de(e.getPrecioFinca(), unidad),
                Merma.de(e.getMermaPorcentaje(), e.getMermaJustificacion()),
                e.isMermaAplicada(),
                e.getEstado(),
                e.getAlmacenActualId() == null ? null : AlmacenId.de(e.getAlmacenActualId()),
                e.isCadenaFrioRota(),
                e.getEstadoPrevioARevision(),
                e.getMotivoRevision(),
                e.getDetalleRevision(),
                e.getFechaPublicacion(),
                e.getVersion()));
    }

    private static List<Lote> aDominio(List<LoteEntity> entidades) {
        return entidades.stream().map(LoteRepositoryJpa::aDominio).toList();
    }

    private static Set<EstadoLote> estadosDonde(boolean admitenPedidos, boolean noTerminales) {
        return Arrays.stream(EstadoLote.values())
                .filter(estado -> !admitenPedidos || estado.admitePedidos())
                .filter(estado -> !noTerminales || !estado.esTerminal())
                .collect(Collectors.toSet());
    }
}
