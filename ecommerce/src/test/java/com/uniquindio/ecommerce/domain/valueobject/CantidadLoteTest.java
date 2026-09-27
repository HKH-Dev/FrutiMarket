package com.uniquindio.ecommerce.domain.valueobject;

import com.uniquindio.ecommerce.domain.exception.ReglaNegocioException;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class CantidadLoteTest {

    @Test
    void dosCantidadesConElMismoValorDebenSerIguales() {
        // Arrange
        CantidadLote c1 = new CantidadLote(new BigDecimal("100.0"));
        CantidadLote c2 = new CantidadLote(new BigDecimal("100.0"));

        // Act & Assert (Igualdad por VALOR)
        assertEquals(c1, c2);
    }

    @Test
    void debeLanzarExcepcionCuandoLaCantidadEsMenorOIgualACero() {
        // Arrange, Act & Assert (Validación que lanza la excepción)
        assertThrows(ReglaNegocioException.class, () -> {
            new CantidadLote(new BigDecimal("0.0"));
        });
    }
}