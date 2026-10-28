package com.uniquindio.ecommerce.infraestructure.persistence.jpa;

import com.uniquindio.ecommerce.domain.catalogo.Lote;
import com.uniquindio.ecommerce.domain.catalogo.Producto;
import com.uniquindio.ecommerce.domain.entity.Compra;
import com.uniquindio.ecommerce.domain.entity.DetalleCompra;
import com.uniquindio.ecommerce.domain.repository.ReporteVentasRepository.ProductoVendido;
import com.uniquindio.ecommerce.domain.valueobject.catalogo.PrecioFinca;
import com.uniquindio.ecommerce.domain.valueobject.catalogo.UnidadMedida;
import com.uniquindio.ecommerce.domain.valueobject.identidad.CompradorId;
import com.uniquindio.ecommerce.domain.valueobject.logistica.Conservacion;
import com.uniquindio.ecommerce.domain.valueobject.origenycalidad.TipoCultivo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;
import java.util.List;

import static com.uniquindio.ecommerce.LotesDePrueba.AHORA;
import static com.uniquindio.ecommerce.LotesDePrueba.kilos;
import static com.uniquindio.ecommerce.LotesDePrueba.lote;
import static com.uniquindio.ecommerce.LotesDePrueba.primera;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Punto 0.2: el ranking de productos mas vendidos (consulta con JOIN y GROUP BY).
 *
 * <p>Los datos los arma la prueba, asi que sabemos de antemano la respuesta:</p>
 * <pre>
 *   Compra 1 (completada): Mango 30 kg + Mora 5 kg
 *   Compra 2 (completada): Mango 20 kg + Cafe 40 kg
 *   Compra 3 (CANCELADA):  Mora 100 kg              -> no debe contar
 *
 *   Ranking esperado: 1. Mango 50 kg (2 compras)  2. Cafe 40 kg (1)  3. Mora 5 kg (1)
 * </pre>
 */
@DataJpaTest
@Import({LoteRepositoryJpa.class, CompraRepositoryJpa.class, ReporteVentasRepositoryJpa.class})
@TestPropertySource(properties = "spring.sql.init.mode=never")
class ReporteVentasRepositoryDataJpaTest {

    private static final PrecioFinca PRECIO = PrecioFinca.de("1000", UnidadMedida.KILOGRAMO);

    @Autowired LoteRepositoryJpa lotes;
    @Autowired CompraRepositoryJpa compras;
    @Autowired ReporteVentasRepositoryJpa reportes;
    @Autowired TestEntityManager entityManager;

    private Lote mango;
    private Lote mora;
    private Lote cafe;

    @BeforeEach
    void prepararVentasConocidas() {
        mango = lote(producto("Mango Tommy"), "500", primera()).build();
        mora = lote(producto("Mora de castilla"), "500", primera()).build();
        cafe = lote(producto("Cafe tostado"), "500", primera()).build();
        lotes.almacenar(mango);
        lotes.almacenar(mora);
        lotes.almacenar(cafe);

        compras.registrar(compraCompletada(detalle(mango, "30"), detalle(mora, "5")));
        compras.registrar(compraCompletada(detalle(mango, "20"), detalle(cafe, "40")));
        Compra cancelada = compra(detalle(mora, "100"));
        cancelada.cancelar();
        compras.registrar(cancelada);

        entityManager.flush();
        entityManager.clear();
    }

    @Test
    void rankingOrdenaPorCantidadVendidaYCuentaSoloComprasCompletadas() {
        // Ejecutar
        List<ProductoVendido> ranking = reportes.productosMasVendidos(10);

        // Verificar: cantidad de filas, orden y totales que ya sabiamos
        assertEquals(3, ranking.size());
        assertEquals(List.of("Mango Tommy", "Cafe tostado", "Mora de castilla"),
                ranking.stream().map(ProductoVendido::nombre).toList());
        assertEquals(0, new BigDecimal("50").compareTo(ranking.get(0).cantidadVendida()));
        assertEquals(2, ranking.get(0).numeroCompras());
        assertEquals(0, new BigDecimal("40").compareTo(ranking.get(1).cantidadVendida()));
        assertEquals(0, new BigDecimal("5").compareTo(ranking.get(2).cantidadVendida()));
        assertEquals(mango.producto(), ranking.get(0).producto());
    }

    @Test
    void rankingRespetaElLimitePedido() {
        // Ejecutar
        List<ProductoVendido> top2 = reportes.productosMasVendidos(2);

        // Verificar
        assertEquals(List.of("Mango Tommy", "Cafe tostado"), top2.stream().map(ProductoVendido::nombre).toList());
    }

    private static Producto producto(String nombre) {
        return Producto.builder().nombre(nombre).tipoCultivo(TipoCultivo.FRUTA).unidadVenta(UnidadMedida.KILOGRAMO)
                .vidaUtilDias(30).conservacion(Conservacion.AMBIENTE).build();
    }

    private static DetalleCompra detalle(Lote lote, String kilos) {
        return DetalleCompra.crear(lote.id(), kilos(kilos), PRECIO);
    }

    private static Compra compra(DetalleCompra... detalles) {
        Compra compra = Compra.builder().compradorId(CompradorId.nuevo()).fecha(AHORA).build();
        for (DetalleCompra detalle : detalles) {
            compra.agregarDetalle(detalle);
        }
        return compra;
    }

    private static Compra compraCompletada(DetalleCompra... detalles) {
        Compra compra = compra(detalles);
        compra.confirmar();
        return compra;
    }
}
