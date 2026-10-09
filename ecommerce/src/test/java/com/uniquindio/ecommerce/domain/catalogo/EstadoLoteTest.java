package com.uniquindio.ecommerce.domain.catalogo;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EstadoLoteTest {

    @Test
    void unEstadoTerminalNoPuedePasarANingunOtroEstado() {
        // Arrange
        EstadoLote retirado = EstadoLote.RETIRADO;

        // Act
        boolean puedeVolverAPublicarse = retirado.puedePasarA(EstadoLote.PUBLICADO);

        // Assert
        assertTrue(retirado.esTerminal());
        assertFalse(puedeVolverAPublicarse);
    }

    @Test
    void soloUnLotePublicadoAdmitePedidos() {
        // Arrange & Act & Assert
        assertTrue(EstadoLote.PUBLICADO.admitePedidos());
        assertFalse(EstadoLote.REGISTRADO.admitePedidos());
        assertFalse(EstadoLote.AGOTADO.admitePedidos());
        assertFalse(EstadoLote.EN_ACOPIO.admitePedidos());
    }
}
