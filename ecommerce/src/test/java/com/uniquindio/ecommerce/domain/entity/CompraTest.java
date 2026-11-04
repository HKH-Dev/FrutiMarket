package com.uniquindio.ecommerce.domain.entity;

import com.uniquindio.ecommerce.domain.exception.ReglaDeNegocioVioladaException;
import com.uniquindio.ecommerce.domain.valueobject.EstadoCompra;
import com.uniquindio.ecommerce.domain.valueobject.catalogo.Cantidad;
import com.uniquindio.ecommerce.domain.valueobject.catalogo.PrecioFinca;
import com.uniquindio.ecommerce.domain.valueobject.catalogo.UnidadMedida;
import com.uniquindio.ecommerce.domain.valueobject.identidad.CompradorId;
import com.uniquindio.ecommerce.domain.valueobject.identidad.LoteId;
import com.uniquindio.ecommerce.domain.valueobject.identidad.PedidoId;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class CompraTest {

    private static Compra nuevaCompra() {
        return Compra.builder().compradorId(CompradorId.nuevo()).fecha(Instant.now()).build();
    }

    private static DetalleCompra detalle(String kilos, String precio) {
        return DetalleCompra.crear(LoteId.nuevo(),
                Cantidad.de(kilos, UnidadMedida.KILOGRAMO),
                PrecioFinca.de(precio, UnidadMedida.KILOGRAMO));
    }

    // ---------- Invariante INV-COMPRA-VACIA ----------

    @Test
    void confirmarCompraSinDetallesLanzaExcepcionYSigueEnPendiente() {
        // Arrange
        Compra compra = nuevaCompra();

        // Act
        ReglaDeNegocioVioladaException excepcion = assertThrows(ReglaDeNegocioVioladaException.class, compra::confirmar);

        // Assert
        assertEquals("INV-COMPRA-VACIA", excepcion.codigoRegla());
        assertEquals(EstadoCompra.PENDIENTE, compra.getEstado(), "El estado no debe cambiar");
    }

    // ---------- Invariante INV-COMPRA-ESTADO ----------

    @Test
    void agregarDetalleAUnaCompraConfirmadaLanzaExcepcionYNoCambiaDetallesNiTotal() {
        // Arrange
        Compra compra = nuevaCompra();
        compra.agregarDetalle(detalle("2", "5000"));
        compra.confirmar();
        BigDecimal totalConfirmado = compra.total();

        // Act
        ReglaDeNegocioVioladaException excepcion = assertThrows(ReglaDeNegocioVioladaException.class,
                () -> compra.agregarDetalle(detalle("1", "9000")));

        // Assert
        assertEquals("INV-COMPRA-ESTADO", excepcion.codigoRegla());
        assertEquals(1, compra.getDetalles().size(), "No debe agregarse el detalle rechazado");
        assertEquals(totalConfirmado, compra.total(), "El total no debe cambiar");
        assertEquals(EstadoCompra.COMPLETADA, compra.getEstado());
    }

    // ---------- Invariante INV-COMPRA-REEMBOLSO ----------

    @Test
    void reembolsarUnaCompraPendienteLanzaExcepcionYSigueEnPendiente() {
        // Arrange
        Compra compra = nuevaCompra();
        compra.agregarDetalle(detalle("1", "3000"));

        // Act
        ReglaDeNegocioVioladaException excepcion = assertThrows(ReglaDeNegocioVioladaException.class,
                () -> compra.reembolsar(compra.getCompradorId(), compra.getFecha()));

        // Assert
        assertEquals("INV-COMPRA-REEMBOLSO", excepcion.codigoRegla());
        assertEquals(EstadoCompra.PENDIENTE, compra.getEstado());
    }

    // ---------- Reglas 19 y 20: reembolso ----------

    private static Compra compraCompletada(CompradorId comprador, Instant fecha) {
        Compra compra = Compra.builder().compradorId(comprador).fecha(fecha).build();
        compra.agregarDetalle(detalle("2", "3000"));
        compra.confirmar();
        return compra;
    }

    @Test
    void elCompradorPuedeReembolsarDentroDelPlazo() {
        // Arrange
        CompradorId comprador = CompradorId.nuevo();
        Instant fecha = Instant.parse("2026-11-01T10:00:00Z");
        Compra compra = compraCompletada(comprador, fecha);

        // Act: justo en el ultimo instante del plazo de 3 dias
        compra.reembolsar(comprador, fecha.plus(Compra.PLAZO_REEMBOLSO));

        // Assert
        assertEquals(EstadoCompra.REEMBOLSADA, compra.getEstado());
    }

    @Test
    void reembolsarFueraDelPlazoLanzaR20YLaCompraSigueCompletada() {
        // Arrange
        CompradorId comprador = CompradorId.nuevo();
        Instant fecha = Instant.parse("2026-11-01T10:00:00Z");
        Compra compra = compraCompletada(comprador, fecha);

        // Act
        ReglaDeNegocioVioladaException excepcion = assertThrows(ReglaDeNegocioVioladaException.class,
                () -> compra.reembolsar(comprador, fecha.plus(Compra.PLAZO_REEMBOLSO).plusSeconds(1)));

        // Assert
        assertEquals("R20", excepcion.codigoRegla());
        assertEquals(EstadoCompra.COMPLETADA, compra.getEstado());
    }

    @Test
    void otroCompradorNoPuedeReembolsarLaCompraLanzaR19() {
        // Arrange
        Instant fecha = Instant.parse("2026-11-01T10:00:00Z");
        Compra compra = compraCompletada(CompradorId.nuevo(), fecha);

        // Act
        ReglaDeNegocioVioladaException excepcion = assertThrows(ReglaDeNegocioVioladaException.class,
                () -> compra.reembolsar(CompradorId.nuevo(), fecha));

        // Assert
        assertEquals("R19", excepcion.codigoRegla());
        assertEquals(EstadoCompra.COMPLETADA, compra.getEstado());
    }

    // ---------- Invariante INV-COMPRA-TOTAL ----------

    @Test
    void elTotalEsLaSumaDeLosSubtotales() {
        // Arrange
        Compra compra = nuevaCompra();

        // Act
        compra.agregarDetalle(detalle("3", "2000"));
        compra.agregarDetalle(detalle("1", "4500"));

        // Assert
        assertEquals(0, new BigDecimal("10500").compareTo(compra.total()), "3x2000 + 1x4500 = 10500");
    }

    @Test
    void dosComprasConMismoIdSonLaMismaEntidadAunqueTenganDetallesDistintos() {
        // Arrange
        PedidoId mismoId = PedidoId.nuevo();
        CompradorId comprador = CompradorId.nuevo();
        Instant fecha = Instant.now();
        Compra original = Compra.builder().id(mismoId).compradorId(comprador).fecha(fecha).build();
        Compra otraCopia = Compra.builder().id(mismoId).compradorId(comprador).fecha(fecha).build();
        otraCopia.agregarDetalle(detalle("5", "1000"));

        // Act
        boolean sonIguales = original.equals(otraCopia);

        // Assert
        assertTrue(sonIguales);
    }
}
