package com.uniquindio.ecommerce.domain.valueobject.catalogo;

import com.uniquindio.ecommerce.domain.exception.ReglaDeNegocioVioladaException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CantidadTest {

    @Test
    void dosCantidadesConMismoValorYUnidadSonIgualesAunqueSeEscribanDistinto() {
        // Arrange
        Cantidad diezKilos = Cantidad.de("10", UnidadMedida.KILOGRAMO);
        Cantidad diezKilosConDecimales = Cantidad.de("10.000", UnidadMedida.KILOGRAMO);

        // Act
        boolean sonIguales = diezKilos.equals(diezKilosConDecimales);

        // Assert
        assertTrue(sonIguales, "Un Value Object se compara por valor, no por referencia");
        assertEquals(diezKilos.hashCode(), diezKilosConDecimales.hashCode());
    }

    @Test
    void cantidadNegativaLanzaReglaDeNegocioVioladaException() {
        // Arrange
        String valorNegativo = "-5";

        // Act
        ReglaDeNegocioVioladaException excepcion = assertThrows(ReglaDeNegocioVioladaException.class,
                () -> Cantidad.de(valorNegativo, UnidadMedida.KILOGRAMO));

        // Assert
        assertEquals("INV-CANTIDAD", excepcion.codigoRegla());
    }

    @Test
    void unaUnidadDiscretaNoAdmiteFracciones() {
        // Arrange
        String mediaCanastilla = "2.5";

        // Act
        ReglaDeNegocioVioladaException excepcion = assertThrows(ReglaDeNegocioVioladaException.class,
                () -> Cantidad.de(mediaCanastilla, UnidadMedida.CANASTILLA));

        // Assert
        assertTrue(excepcion.getMessage().contains("no admite fracciones"));
    }
}
