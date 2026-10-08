package ecommerce.domain.entity;

import com.uniquindio.ecommerce.domain.entity.Compra;
import com.uniquindio.ecommerce.domain.entity.DetalleCompra;
import com.uniquindio.ecommerce.domain.exception.ReglaDeNegocioVioladaException;
import com.uniquindio.ecommerce.domain.valueobject.EstadoCompra;
import com.uniquindio.ecommerce.domain.valueobject.identidad.LoteId;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class CompraTest {

    private static DetalleCompra detalle(int cantidad, String precio) {
        return new DetalleCompra(LoteId.nuevo(), cantidad, new BigDecimal(precio));
    }

    // ---------- Invariante INV-COMPRA-VACIA ----------

    @Test
    void confirmarCompraSinDetallesLanzaExcepcionYSigueEnPendiente() {
        // Arrange
        Compra compra = new Compra();

        // Act
        ReglaDeNegocioVioladaException excepcion = assertThrows(
                ReglaDeNegocioVioladaException.class,
                compra::confirmar);

        // Assert
        assertEquals("INV-COMPRA-VACIA", excepcion.codigoRegla());
        assertEquals(EstadoCompra.PENDIENTE, compra.getEstado(), "El estado no debe cambiar");
        assertEquals(0, compra.getTotal().compareTo(BigDecimal.ZERO), "El total debe seguir en cero");
    }

    // ---------- Invariante INV-COMPRA-ESTADO ----------

    @Test
    void agregarProductoAUnaCompraConfirmadaLanzaExcepcionYNoAlteraDetallesNiTotal() {
        // Arrange
        Compra compra = new Compra();
        compra.agregarProducto(detalle(2, "5000"));
        compra.confirmar();
        BigDecimal totalConfirmado = compra.getTotal();

        // Act
        ReglaDeNegocioVioladaException excepcion = assertThrows(
                ReglaDeNegocioVioladaException.class,
                () -> compra.agregarProducto(detalle(1, "9000")));

        // Assert
        assertEquals("INV-COMPRA-ESTADO", excepcion.codigoRegla());
        assertEquals(1, compra.getDetalles().size(), "No debe agregarse el detalle rechazado");
        assertEquals(totalConfirmado, compra.getTotal(), "El total confirmado no debe cambiar");
        assertEquals(EstadoCompra.COMPLETADA, compra.getEstado());
    }

    // ---------- Invariante INV-COMPRA-TOTAL ----------

    @Test
    void elTotalSiempreEsLaSumaDeLosSubtotalesDeSusDetalles() {
        // Arrange
        Compra compra = new Compra();

        // Act
        compra.agregarProducto(detalle(3, "2000"));
        compra.agregarProducto(detalle(1, "4500"));

        // Assert
        assertEquals(0, new BigDecimal("10500").compareTo(compra.getTotal()), "3x2000 + 1x4500 = 10500");
    }

    @Test
    void dosComprasConMismoIdSonLaMismaEntidadAunqueTenganDetallesDistintos() {
        // Arrange
        UUID mismoId = UUID.randomUUID();
        Compra original = new Compra(mismoId);
        Compra otraCopia = new Compra(mismoId);
        otraCopia.agregarProducto(detalle(5, "1000"));

        // Act
        boolean sonIguales = original.equals(otraCopia);

        // Assert
        assertTrue(sonIguales);
    }
}
