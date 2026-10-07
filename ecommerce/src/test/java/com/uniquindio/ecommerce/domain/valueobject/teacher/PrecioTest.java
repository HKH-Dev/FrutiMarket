package com.uniquindio.ecommerce.domain.valueobject.teacher;

import com.uniquindio.ecommerce.domain.exception.ReglaNegocioException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PrecioTest {

    // ==========================================
    // PRUEBA 1: Igualdad por valor (records en Java)
    // Dos Precio con mismo monto y moneda son iguales.
    // ==========================================
    @Test
    void dosPreciosConMismoMontoYMonedaSonIguales() {
        // Arrange
        Precio precio1 = new Precio(100.0, "COP");
        Precio precio2 = new Precio(100.0, "COP");

        // Act & Assert
        assertEquals(precio1, precio2,
                "Dos Value Objects con los mismos valores deben ser iguales");
        assertEquals(precio1.hashCode(), precio2.hashCode(),
                "Si son iguales, su hashCode tambien debe serlo");
    }

    // ==========================================
    // PRUEBA 2: Validacion que lanza ReglaNegocioException
    // Un precio negativo viola la regla de negocio.
    // ==========================================
    @Test
    void precioNegativoLanzaReglaNegocioException() {
        // Arrange
        double montoNegativo = -50.0;

        // Act & Assert
        ReglaNegocioException excepcion = assertThrows(
                ReglaNegocioException.class,
                () -> new Precio(montoNegativo, "COP")
        );
        assertTrue(excepcion.getMessage().contains("negativo"),
                "El mensaje debe indicar que el precio no puede ser negativo");
    }

    // ==========================================
    // PRUEBA 3: Precio con licencia multiplica correctamente
    // ==========================================
    @Test
    void precioConLicenciaComercialMultiplicaPorFactorTres() {
        // Arrange
        Precio precioBase = new Precio(1000.0, "COP");

        // Act
        Precio precioComercial = precioBase.conLicencia(Licencia.COMERCIAL);

        // Assert
        assertEquals(3000.0, precioComercial.monto(),
                "Licencia COMERCIAL tiene factor 3, asi que 1000 * 3 = 3000");
        assertEquals("COP", precioComercial.moneda(),
                "La moneda no debe cambiar al aplicar licencia");
    }
}
