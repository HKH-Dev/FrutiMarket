package com.uniquindio.ecommerce.domain.valueobject;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EstadoLoteTest {

    // ==========================================
    // PRUEBA 1: Igualdad por valor entre enums
    // ==========================================
    @Test
    void dosReferenciasAlMismoEstadoSonIguales() {
        // Arrange
        EstadoLote estado1 = EstadoLote.PUBLICADO;
        EstadoLote estado2 = EstadoLote.PUBLICADO;

        // Act & Assert
        assertEquals(estado1, estado2,
                "Dos referencias a PUBLICADO deben ser iguales");
    }

    // ==========================================
    // PRUEBA 2: Validacion de regla - solo PUBLICADO y EN_ACOPIO permiten venta
    // ==========================================
    @Test
    void soloPublicadoYEnAcopioPermitenVenta() {
        // Arrange & Act & Assert
        assertTrue(EstadoLote.PUBLICADO.permiteVenta(),
                "Un lote PUBLICADO debe permitir venta");
        assertTrue(EstadoLote.EN_ACOPIO.permiteVenta(),
                "Un lote EN_ACOPIO debe permitir venta");

        assertFalse(EstadoLote.REGISTRADO.permiteVenta(),
                "Un lote REGISTRADO no debe permitir venta");
        assertFalse(EstadoLote.AGOTADO.permiteVenta(),
                "Un lote AGOTADO no debe permitir venta");
        assertFalse(EstadoLote.VENCIDO.permiteVenta(),
                "Un lote VENCIDO no debe permitir venta");
        assertFalse(EstadoLote.CERRADO.permiteVenta(),
                "Un lote CERRADO no debe permitir venta");
    }

    // ==========================================
    // PRUEBA 3: Estados activos vs inactivos
    // ==========================================
    @Test
    void estadosCerradoYVencidoNoSonActivos() {
        // Arrange & Act & Assert
        assertTrue(EstadoLote.REGISTRADO.esActivo());
        assertTrue(EstadoLote.PUBLICADO.esActivo());
        assertTrue(EstadoLote.EN_ACOPIO.esActivo());
        assertTrue(EstadoLote.AGOTADO.esActivo());

        assertFalse(EstadoLote.VENCIDO.esActivo(),
                "VENCIDO no es un estado activo");
        assertFalse(EstadoLote.CERRADO.esActivo(),
                "CERRADO no es un estado activo");
    }
}
