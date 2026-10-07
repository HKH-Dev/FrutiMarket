package ecommerce.domain.entity;

import com.uniquindio.ecommerce.domain.entity.Campesino;
import com.uniquindio.ecommerce.domain.exception.ReglaDeNegocioVioladaException;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class CampesinoTest {

    @Test
    void dosCampesinosConMismoIdYDatosDistintosSonLaMismaEntidad() {
        // Arrange
        UUID mismoId = UUID.randomUUID();
        Campesino registrado = new Campesino(mismoId, "1094000111", 2.0);
        Campesino actualizado = new Campesino(mismoId, "1094999999", 15.0);

        // Act
        boolean sonIguales = registrado.equals(actualizado);

        // Assert
        assertTrue(sonIguales, "Una entidad se compara por identidad, no por sus datos");
        assertEquals(registrado.hashCode(), actualizado.hashCode());
    }

    @Test
    void actualizarEscalaProduccionNegativaLanzaExcepcionYConservaLaAnterior() {
        // Arrange
        Campesino campesino = new Campesino(UUID.randomUUID(), "1094000111", 5.0);

        // Act
        ReglaDeNegocioVioladaException excepcion = assertThrows(
                ReglaDeNegocioVioladaException.class,
                () -> campesino.actualizarEscalaProduccion(-3.0));

        // Assert
        assertEquals("INV-CAMPESINO", excepcion.codigoRegla());
        assertEquals(5.0, campesino.getEscalaProduccion(),
                "La escala no debe cambiar tras el rechazo");
    }
}
