package com.uniquindio.ecommerce.domain.entity;

import com.uniquindio.ecommerce.domain.exception.ReglaDeNegocioVioladaException;
import com.uniquindio.ecommerce.domain.valueobject.identidad.CampesinoId;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CampesinoTest {

    @Test
    void dosCampesinosConMismoIdYDatosDistintosSonLaMismaEntidad() {
        // Arrange
        CampesinoId mismoId = CampesinoId.nuevo();
        Campesino registrado = Campesino.builder()
                .id(mismoId).nombre("Ana Ruiz").numeroIdentificacion("1094000111").escalaProduccion(2.0)
                .build();
        Campesino actualizado = Campesino.builder()
                .id(mismoId).nombre("Ana Maria Ruiz").numeroIdentificacion("1094000111").escalaProduccion(15.0)
                .build();

        // Act
        boolean sonIguales = registrado.equals(actualizado);

        // Assert
        assertTrue(sonIguales, "Una entidad se compara por identidad, no por sus datos");
        assertEquals(registrado.hashCode(), actualizado.hashCode());
    }

    @Test
    void actualizarEscalaProduccionNegativaLanzaExcepcionYConservaLaAnterior() {
        // Arrange
        Campesino campesino = Campesino.builder()
                .nombre("Luis Gomez").numeroIdentificacion("1094222333").escalaProduccion(5.0)
                .build();

        // Act
        ReglaDeNegocioVioladaException excepcion = assertThrows(ReglaDeNegocioVioladaException.class,
                () -> campesino.actualizarEscalaProduccion(-3.0));

        // Assert
        assertEquals("INV-CAMPESINO", excepcion.codigoRegla());
        assertEquals(5.0, campesino.getEscalaProduccion(), "La escala no debe cambiar tras el rechazo");
    }

    @Test
    void construirCampesinoSinIdentificacionLanzaExcepcion() {
        // Arrange
        Campesino.Builder builder = Campesino.builder().nombre("Sin documento");

        // Act
        ReglaDeNegocioVioladaException excepcion = assertThrows(ReglaDeNegocioVioladaException.class, builder::build);

        // Assert
        assertTrue(excepcion.getMessage().contains("identificacion"));
    }
}
