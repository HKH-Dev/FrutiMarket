package com.uniquindio.ecommerce.domain.valueobject.origenycalidad;

import com.uniquindio.ecommerce.domain.exception.ReglaDeNegocioVioladaException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CalidadTest {

    @Test
    void dosCalidadesConLosMismosDatosSonIguales() {
        // Arrange
        Calidad una = Calidad.de(Calidad.Categoria.PRIMERA, 10, true);
        Calidad otra = Calidad.de(Calidad.Categoria.PRIMERA, 10, true);

        // Act
        boolean sonIguales = una.equals(otra);

        // Assert
        assertTrue(sonIguales);
        assertEquals(una.hashCode(), otra.hashCode());
    }

    @Test
    void porcentajeDeDefectosMayorA100LanzaExcepcion() {
        // Arrange
        int defectosImposibles = 120;

        // Act
        ReglaDeNegocioVioladaException excepcion = assertThrows(ReglaDeNegocioVioladaException.class,
                () -> Calidad.de(Calidad.Categoria.SEGUNDA, defectosImposibles, false));

        // Assert
        assertEquals("INV-CALIDAD", excepcion.codigoRegla());
    }

    @Test
    void elPuntajePremiaLaCategoriaYLaCertificacionYCastigaLosDefectos() {
        // Arrange
        Calidad extraOrganica = Calidad.de(Calidad.Categoria.EXTRA, 0, true);
        Calidad primeraConDefectos = Calidad.de(Calidad.Categoria.PRIMERA, 20, false);
        Calidad segunda = Calidad.de(Calidad.Categoria.SEGUNDA, 0, false);

        // Act
        int puntajeExtra = extraOrganica.puntaje();
        int puntajePrimera = primeraConDefectos.puntaje();
        int puntajeSegunda = segunda.puntaje();

        // Assert
        assertEquals(100, puntajeExtra, "100 + 5 de bono queda topado en 100");
        assertEquals(75, puntajePrimera, "85 - 20/2");
        assertEquals(65, puntajeSegunda);
    }
}
