package com.uniquindio.ecommerce.infraestructure.persistence.jpa;

import com.uniquindio.ecommerce.domain.catalogo.EstadoLote;
import com.uniquindio.ecommerce.domain.catalogo.Lote;
import com.uniquindio.ecommerce.domain.catalogo.Producto;
import com.uniquindio.ecommerce.domain.entity.Compra;
import com.uniquindio.ecommerce.domain.entity.Comprador;
import com.uniquindio.ecommerce.domain.entity.DetalleCompra;
import com.uniquindio.ecommerce.domain.entity.PuntoAlmacenamiento;
import com.uniquindio.ecommerce.domain.valueobject.EstadoCompra;
import com.uniquindio.ecommerce.domain.valueobject.catalogo.PrecioFinca;
import com.uniquindio.ecommerce.domain.valueobject.catalogo.UnidadMedida;
import com.uniquindio.ecommerce.domain.valueobject.identidad.PedidoId;
import com.uniquindio.ecommerce.domain.valueobject.origenycalidad.Calidad;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static com.uniquindio.ecommerce.LotesDePrueba.AHORA;
import static com.uniquindio.ecommerce.LotesDePrueba.kilos;
import static com.uniquindio.ecommerce.LotesDePrueba.mango;
import static com.uniquindio.ecommerce.LotesDePrueba.mangoTommy;
import static com.uniquindio.ecommerce.LotesDePrueba.almacen;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Guarda cada agregado en H2, lo vuelve a leer y comprueba que no se perdio nada. */
@SpringBootTest
class RepositoriosJpaTest {

    @Autowired LoteRepositoryJpa lotes;
    @Autowired ProductoRepositoryJpa productos;
    @Autowired CompraRepositoryJpa compras;
    @Autowired CompradorRepositoryJpa compradores;
    @Autowired PuntoAlmacenamientoRepositoryJpa almacenes;

    @Test
    void loteConReservaYCustodiaSobreviveAlGuardado() {
        Lote lote = mango("100").build();
        lote.publicar(AHORA);
        PedidoId pedido = PedidoId.nuevo();
        lote.reservar(pedido, kilos("30"), AHORA);
        lote.inspeccionarCalidad(Calidad.de(Calidad.Categoria.SEGUNDA, 20, false), "Inspector", AHORA);
        lotes.almacenar(lote);

        Lote leido = lotes.obtenerLote(lote.id()).orElseThrow();

        assertEquals(EstadoLote.EN_REVISION, leido.estado());
        assertEquals(lote.cantidadDisponible(), leido.cantidadDisponible());
        assertEquals(lote.reservas(), leido.reservas());
        assertEquals(lote.custodia(), leido.custodia());
        assertEquals(lote.calidadVerificada(), leido.calidadVerificada());
        assertEquals(lote.fichaTrazabilidad(), leido.fichaTrazabilidad());
        assertEquals(lote.version(), leido.version());
        assertTrue(lotes.existeCodigo(lote.codigo().toLowerCase()));

        // R18: al corregir la calidad vuelve a su estado previo, que tambien debe haberse persistido.
        leido.corregirCalidadDeclarada(Calidad.de(Calidad.Categoria.SEGUNDA, 20, false), "Campesino", AHORA);
        assertEquals(EstadoLote.PUBLICADO, leido.estado());
    }

    @Test
    void lotesDisponiblesSoloIncluyePublicadosConCantidad() {
        Lote publicado = mango("50").build();
        publicado.publicar(AHORA);
        Lote borrador = mango("50").build();
        lotes.almacenar(publicado);
        lotes.almacenar(borrador);

        var disponibles = lotes.lotesDisponiblesDe(publicado.producto());

        assertEquals(1, disponibles.size());
        assertEquals(publicado.id(), disponibles.get(0).id());
    }

    @Test
    void productoInactivoNoApareceEnActivos() {
        Producto producto = mangoTommy();
        producto.desactivar();
        productos.registrar(producto);

        assertFalse(productos.obtenerProducto(producto.id()).orElseThrow().estaActivo());
        assertTrue(productos.productosActivos().stream().noneMatch(p -> p.id().equals(producto.id())));
    }

    @Test
    void compraConservaDetallesYEstado() {
        Comprador comprador = Comprador.builder().nombre("Tienda La 14").municipio("Armenia").build();
        compradores.registrar(comprador);
        Compra compra = Compra.builder().compradorId(comprador.getId()).fecha(AHORA).build();
        compra.agregarDetalle(DetalleCompra.crear(mango("10").build().id(), kilos("4"),
                PrecioFinca.de("3500", UnidadMedida.KILOGRAMO)));
        compra.confirmar();
        compras.registrar(compra);

        Compra leida = compras.obtenerCompra(compra.getId()).orElseThrow();

        assertEquals(EstadoCompra.COMPLETADA, leida.getEstado());
        assertEquals(compra.total(), leida.total());
        assertEquals(compra.getDetalles().get(0).getId(), leida.getDetalles().get(0).getId());
        assertEquals(1, compras.comprasDelComprador(comprador.getId()).size());
    }

    @Test
    void almacenSeBuscaPorMunicipioSinImportarMayusculas() {
        PuntoAlmacenamiento acopio = almacen("Acopio Calarca", PuntoAlmacenamiento.Tipo.PUNTO_ACOPIO, 5, "Calarca");
        almacenes.registrar(acopio);

        assertTrue(almacenes.almacenesQueCubren("  CALARCA ").stream().anyMatch(a -> a.id().equals(acopio.id())));
        assertTrue(almacenes.almacenesQueCubren("Pereira").stream().noneMatch(a -> a.id().equals(acopio.id())));
    }
}
