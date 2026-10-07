package com.uniquindio.ecommerce.domain.entity;

import com.uniquindio.ecommerce.domain.valueobject.EstadoCompra;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class CompraTest {

    // ==========================================
    // PRUEBA 1: Igualdad por identidad
    // Dos compras con datos distintos pero mismo id son la misma entidad.
    // ==========================================
    @Test
    void dosComprasDistintasSonDiferentesPorIdentidad() {
        // Arrange
        Compra compra1 = new Compra();
        Compra compra2 = new Compra();

        // Act & Assert
        // Cada Compra genera un UUID diferente, asi que son entidades distintas
        assertNotEquals(compra1.getCompraId(), compra2.getCompraId(),
                "Dos compras nuevas deben tener ids diferentes");
    }

    // ==========================================
    // PRUEBA 2: Regla protegida - no se puede confirmar sin productos
    // ==========================================
    @Test
    void confirmarCompraSinProductosLanzaExcepcion() {
        // Arrange
        Compra compra = new Compra();

        // Act & Assert
        IllegalStateException excepcion = assertThrows(
                IllegalStateException.class,
                () -> compra.confirmar()
        );
        assertTrue(excepcion.getMessage().contains("sin productos"),
                "Debe indicar que no se puede confirmar sin productos");
        assertEquals(EstadoCompra.PENDIENTE, compra.getEstado(),
                "El estado debe seguir siendo PENDIENTE tras el rechazo");
    }

    // ==========================================
    // PRUEBA 3: Regla protegida - no se puede cancelar una compra ya completada
    // ==========================================
    @Test
    void cancelarCompraCompletadaLanzaExcepcion() {
        // Arrange
        Compra compra = new Compra();
        compra.agregarProducto(new DetalleCompra(2, new BigDecimal("5000")));
        compra.confirmar();

        // Act & Assert
        IllegalStateException excepcion = assertThrows(
                IllegalStateException.class,
                () -> compra.cancelar()
        );
        assertTrue(excepcion.getMessage().contains("pendiente"),
                "Debe indicar que solo se cancela una compra pendiente");
        assertEquals(EstadoCompra.COMPLETADA, compra.getEstado(),
                "El estado debe seguir siendo COMPLETADA tras el intento fallido");
    }

    // ==========================================
    // PRUEBA 4: Agregar producto calcula total correctamente
    // ==========================================
    @Test
    void agregarProductoRecalculaElTotal() {
        // Arrange
        Compra compra = new Compra();
        DetalleCompra detalle = new DetalleCompra(3, new BigDecimal("2000"));

        // Act
        compra.agregarProducto(detalle);

        // Assert
        assertEquals(new BigDecimal("6000"), compra.getTotal(),
                "3 unidades x $2000 = $6000");
    }
}
