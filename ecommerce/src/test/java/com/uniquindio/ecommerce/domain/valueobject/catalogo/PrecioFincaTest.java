package com.uniquindio.ecommerce.domain.valueobject.catalogo;

import com.uniquindio.ecommerce.domain.exception.ReglaDeNegocioVioladaException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class PrecioFincaTest {

    @Test
    void dosPreciosConMismoValorYUnidadSonIguales() {
        // Arrange
        PrecioFinca precio = PrecioFinca.de("3500", UnidadMedida.KILOGRAMO);
        PrecioFinca mismoPrecio = PrecioFinca.de(new BigDecimal("3500.0"), UnidadMedida.KILOGRAMO);

        // Act
        boolean sonIguales = precio.equals(mismoPrecio);

        // Assert
        assertTrue(sonIguales);
    }

    @Test
    void precioEnCeroLanzaExcepcionDeLaRegla2() {
        // Arrange
        String cero = "0";

        // Act
        ReglaDeNegocioVioladaException excepcion = assertThrows(ReglaDeNegocioVioladaException.class,
                () -> PrecioFinca.de(cero, UnidadMedida.KILOGRAMO));

        // Assert
        assertEquals("R2", excepcion.codigoRegla());
    }

    @Test
    void elTotalEsPrecioPorCantidadEnLaMismaUnidad() {
        // Arrange
        PrecioFinca precio = PrecioFinca.de("2000", UnidadMedida.KILOGRAMO);
        Cantidad cantidad = Cantidad.de("3", UnidadMedida.KILOGRAMO);

        // Act
        BigDecimal total = precio.totalPara(cantidad);

        // Assert
        assertEquals(0, new BigDecimal("6000").compareTo(total));
    }
}
