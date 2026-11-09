package com.uniquindio.ecommerce.infraestructure.persistence.jpa;

import com.uniquindio.ecommerce.domain.chat.Conversacion;
import com.uniquindio.ecommerce.domain.chat.Mensaje;
import com.uniquindio.ecommerce.domain.comercial.Cupon;
import com.uniquindio.ecommerce.domain.comercial.DescuentoTemporada;
import com.uniquindio.ecommerce.domain.comercial.Pago;
import com.uniquindio.ecommerce.domain.repository.ConversacionRepository;
import com.uniquindio.ecommerce.domain.repository.CuponRepository;
import com.uniquindio.ecommerce.domain.repository.DescuentoTemporadaRepository;
import com.uniquindio.ecommerce.domain.repository.FavoritoRepository;
import com.uniquindio.ecommerce.domain.repository.PagoRepository;
import com.uniquindio.ecommerce.domain.valueobject.identidad.CampesinoId;
import com.uniquindio.ecommerce.domain.valueobject.identidad.CompradorId;
import com.uniquindio.ecommerce.domain.valueobject.identidad.PedidoId;
import com.uniquindio.ecommerce.domain.valueobject.identidad.ProductoId;
import com.uniquindio.ecommerce.infraestructure.persistence.jpa.entity.FavoritoEntity;
import com.uniquindio.ecommerce.infraestructure.persistence.jpa.mapper.ComercialMapper;
import com.uniquindio.ecommerce.infraestructure.persistence.jpa.springdata.ConversacionJpaRepository;
import com.uniquindio.ecommerce.infraestructure.persistence.jpa.springdata.CuponJpaRepository;
import com.uniquindio.ecommerce.infraestructure.persistence.jpa.springdata.DescuentoJpaRepository;
import com.uniquindio.ecommerce.infraestructure.persistence.jpa.springdata.FavoritoJpaRepository;
import com.uniquindio.ecommerce.infraestructure.persistence.jpa.springdata.MensajeJpaRepository;
import com.uniquindio.ecommerce.infraestructure.persistence.jpa.springdata.PagoJpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/** Adaptadores JPA de los repositorios comerciales y del chat. */
public final class ComercialRepositoriosJpa {

    private ComercialRepositoriosJpa() {
    }

    @Repository
    @Transactional
    public static class Descuentos implements DescuentoTemporadaRepository {
        private final DescuentoJpaRepository jpa;

        public Descuentos(DescuentoJpaRepository jpa) {
            this.jpa = jpa;
        }

        @Override
        public void guardar(DescuentoTemporada descuento) {
            jpa.save(ComercialMapper.aEntidad(descuento));
        }

        @Override
        @Transactional(readOnly = true)
        public Optional<DescuentoTemporada> obtener(UUID id) {
            return jpa.findById(id).map(ComercialMapper::aDominio);
        }

        @Override
        @Transactional(readOnly = true)
        public List<DescuentoTemporada> todos() {
            return jpa.findAll().stream().map(ComercialMapper::aDominio).toList();
        }
    }

    @Repository
    @Transactional
    public static class Cupones implements CuponRepository {
        private final CuponJpaRepository jpa;

        public Cupones(CuponJpaRepository jpa) {
            this.jpa = jpa;
        }

        @Override
        public void guardar(Cupon cupon) {
            jpa.save(ComercialMapper.aEntidad(cupon));
        }

        @Override
        @Transactional(readOnly = true)
        public Optional<Cupon> porCodigo(String codigo) {
            return jpa.findByCodigo(codigo).map(ComercialMapper::aDominio);
        }

        @Override
        @Transactional(readOnly = true)
        public List<Cupon> delComprador(CompradorId comprador) {
            return jpa.findByCompradorIdOrderByVenceElAsc(comprador.valor()).stream().map(ComercialMapper::aDominio).toList();
        }

        @Override
        @Transactional(readOnly = true)
        public List<Cupon> todos() {
            return jpa.findAll().stream().map(ComercialMapper::aDominio).toList();
        }
    }

    @Repository
    @Transactional
    public static class Pagos implements PagoRepository {
        private final PagoJpaRepository jpa;

        public Pagos(PagoJpaRepository jpa) {
            this.jpa = jpa;
        }

        @Override
        public void guardar(Pago pago) {
            jpa.save(ComercialMapper.aEntidad(pago));
        }

        @Override
        @Transactional(readOnly = true)
        public Optional<Pago> deLaCompra(PedidoId compra) {
            return jpa.findByCompraId(compra.valor()).map(ComercialMapper::aDominio);
        }

        @Override
        @Transactional(readOnly = true)
        public ResumenPagos resumen() {
            var fila = jpa.resumenAprobados();
            return new ResumenPagos(fila.ingresos(), fila.descuentos(), fila.aprobados(), jpa.countByEstado(Pago.Estado.REEMBOLSADO.name()));
        }
    }

    @Repository
    @Transactional
    public static class Favoritos implements FavoritoRepository {
        private final FavoritoJpaRepository jpa;

        public Favoritos(FavoritoJpaRepository jpa) {
            this.jpa = jpa;
        }

        @Override
        public void agregar(CompradorId comprador, ProductoId producto, Instant momento) {
            if (jpa.existsByCompradorIdAndProductoId(comprador.valor(), producto.valor())) {
                return;
            }
            FavoritoEntity e = new FavoritoEntity();
            e.setId(UUID.randomUUID());
            e.setCompradorId(comprador.valor());
            e.setProductoId(producto.valor());
            e.setAgregado(momento);
            jpa.save(e);
        }

        @Override
        public void quitar(CompradorId comprador, ProductoId producto) {
            jpa.quitar(comprador.valor(), producto.valor());
        }

        @Override
        @Transactional(readOnly = true)
        public Set<ProductoId> delComprador(CompradorId comprador) {
            return jpa.findByCompradorId(comprador.valor()).stream().map(f -> ProductoId.de(f.getProductoId())).collect(Collectors.toSet());
        }
    }

    @Repository
    @Transactional
    public static class Conversaciones implements ConversacionRepository {
        private final ConversacionJpaRepository jpa;
        private final MensajeJpaRepository mensajes;

        public Conversaciones(ConversacionJpaRepository jpa, MensajeJpaRepository mensajes) {
            this.jpa = jpa;
            this.mensajes = mensajes;
        }

        @Override
        public void guardar(Conversacion conversacion) {
            jpa.save(ComercialMapper.aEntidad(conversacion));
        }

        @Override
        @Transactional(readOnly = true)
        public Optional<Conversacion> obtener(UUID id) {
            return jpa.findById(id).map(ComercialMapper::aDominio);
        }

        @Override
        @Transactional(readOnly = true)
        public Optional<Conversacion> entre(CompradorId comprador, CampesinoId campesino) {
            return jpa.findByCompradorIdAndCampesinoId(comprador.valor(), campesino.valor()).map(ComercialMapper::aDominio);
        }

        @Override
        @Transactional(readOnly = true)
        public List<Conversacion> delActor(UUID actor) {
            return jpa.findByCompradorIdOrCampesinoId(actor, actor).stream().map(ComercialMapper::aDominio).toList();
        }

        @Override
        public void guardarMensaje(Mensaje mensaje) {
            mensajes.save(ComercialMapper.aEntidad(mensaje));
        }

        @Override
        @Transactional(readOnly = true)
        public List<Mensaje> mensajes(UUID conversacion) {
            return mensajes.findByConversacionIdOrderByMomentoAsc(conversacion).stream().map(ComercialMapper::aDominio).toList();
        }
    }
}
