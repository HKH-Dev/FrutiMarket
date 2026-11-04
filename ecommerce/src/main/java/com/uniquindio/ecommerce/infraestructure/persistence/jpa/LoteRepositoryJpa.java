package com.uniquindio.ecommerce.infraestructure.persistence.jpa;

import java.util.Locale;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Page;
import com.uniquindio.ecommerce.domain.consulta.Pagina;
import com.uniquindio.ecommerce.domain.consulta.BusquedaLotes;
import com.uniquindio.ecommerce.domain.catalogo.EstadoLote;
import com.uniquindio.ecommerce.domain.catalogo.Lote;
import com.uniquindio.ecommerce.domain.repository.LoteRepository;
import com.uniquindio.ecommerce.domain.valueobject.identidad.AlmacenId;
import com.uniquindio.ecommerce.domain.valueobject.identidad.CampesinoId;
import com.uniquindio.ecommerce.domain.valueobject.identidad.LoteId;
import com.uniquindio.ecommerce.domain.valueobject.identidad.ProductoId;
import com.uniquindio.ecommerce.domain.valueobject.origenycalidad.TipoCultivo;
import com.uniquindio.ecommerce.infraestructure.persistence.jpa.entity.LoteEntity;
import com.uniquindio.ecommerce.infraestructure.persistence.jpa.mapper.LoteMapper;
import com.uniquindio.ecommerce.infraestructure.persistence.jpa.springdata.LoteJpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
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
        jpa.save(LoteMapper.aEntidad(lote));
        return lote;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Lote> obtenerLote(LoteId id) {
        return jpa.findById(id.valor()).map(LoteMapper::aDominio);
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

    /** Aqui, y solo aqui, se traduce entre la pagina del dominio y la de Spring Data. */
    @Override
    @Transactional(readOnly = true)
    public Pagina<Lote> buscarDisponibles(BusquedaLotes criterios) {
        Pageable pagina = PageRequest.of(criterios.pagina(), criterios.tamano(),
                Sort.by(Sort.Order.desc("fechaPublicacion"), Sort.Order.asc("codigo")));
        Page<LoteEntity> resultado = jpa.buscarDisponibles(EstadoLote.PUBLICADO,
                criterios.texto().toLowerCase(Locale.ROOT), criterios.tipoCultivo(), criterios.precioMaximo(), pagina);
        return new Pagina<>(aDominio(resultado.getContent()), resultado.getNumber(), resultado.getSize(),
                resultado.getTotalElements(), resultado.getTotalPages());
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

    private static List<Lote> aDominio(List<LoteEntity> entidades) {
        return entidades.stream().map(LoteMapper::aDominio).toList();
    }

    private static Set<EstadoLote> estadosDonde(boolean admitenPedidos, boolean noTerminales) {
        return Arrays.stream(EstadoLote.values())
                .filter(estado -> !admitenPedidos || estado.admitePedidos())
                .filter(estado -> !noTerminales || !estado.esTerminal())
                .collect(Collectors.toSet());
    }
}
