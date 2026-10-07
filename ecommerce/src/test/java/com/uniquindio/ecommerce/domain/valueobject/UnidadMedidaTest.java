package com.uniquindio.ecommerce.domain.valueobject;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UnidadMedidaTest {

    // ==========================================
    // PRUEBA 1: Igualdad por valor
    // Los enums en Java son Value Objects naturales:
    // dos referencias al mismo valor son iguales.
    // ==========================================
    @Test
    void dosReferenciasAlMismoKilogramoSonIguales() {
        // Arrange
        UnidadMedida unidad1 = UnidadMedida.KILOGRAMO;
        UnidadMedida unidad2 = UnidadMedida.KILOGRAMO;

        // Act & Assert
        assertEquals(unidad1, unidad2,
                "Dos referencias a KILOGRAMO deben ser iguales por valor");
        assertSame(unidad1, unidad2,
                "Los enums garantizan identidad unica por valor");
    }

    // ==========================================
    // PRUEBA 2: Dos unidades distintas NO son iguales
    // ==========================================
    @Test
    void kilogramoYLibraNOsonIguales() {
        // Arrange
        UnidadMedida kg = UnidadMedida.KILOGRAMO;
        UnidadMedida lb = UnidadMedida.LIBRA;

        // Act
        boolean sonIguales = kg.equals(lb);

        // Assert
        assertFalse(sonIguales,
                "KILOGRAMO y LIBRA son unidades distintas");
        assertNotEquals(kg.getSimbolo(), lb.getSimbolo(),
                "Sus simbolos deben ser diferentes");
    }

    // ==========================================
    // PRUEBA 3: El simbolo corresponde a la unidad
    // ==========================================
    @Test
    void cadaUnidadTieneSuSimboloCorrecto() {
        // Arrange & Act & Assert
        assertEquals("kg", UnidadMedida.KILOGRAMO.getSimbolo());
        assertEquals("lb", UnidadMedida.LIBRA.getSimbolo());
        assertEquals("t", UnidadMedida.TONELADA.getSimbolo());
        assertEquals("und", UnidadMedida.UNIDAD.getSimbolo());
        assertEquals("can", UnidadMedida.CANASTILLA.getSimbolo());
    }
}
