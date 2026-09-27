package com.uniquindio.ecommerce.domain.entity;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class LoteTest {

    @Test
    void dosLotesConElMismoIdSonElMismoLote() {
        // Arrange: Mismo ID pero atributos distintos
        Lote lote1 = new Lote.Builder().id("LOT-001").codigo("COD-A").build();
        Lote lote2 = new Lote.Builder().id("LOT-001").codigo("COD-B").build();

        // Act & Assert (Igualdad por IDENTIDAD)
        assertEquals(lote1, lote2);
    }
}