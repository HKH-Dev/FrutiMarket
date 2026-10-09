package com.uniquindio.ecommerce.domain.entity;

import com.uniquindio.ecommerce.domain.exception.ReglaDeNegocioVioladaException;
import com.uniquindio.ecommerce.domain.valueobject.identidad.LoteId;
import com.uniquindio.ecommerce.domain.valueobject.logistica.Conservacion;
import com.uniquindio.ecommerce.domain.valueobject.origenycalidad.TipoCultivo;
import org.junit.jupiter.api.Test;

import static com.uniquindio.ecommerce.LotesDePrueba.almacen;
import static org.junit.jupiter.api.Assertions.*;

class PuntoAlmacenamientoTest {

    // ---------- Invariante INV-CAPACIDAD ----------

    @Test
    void recibirUnLoteConElAlmacenLlenoLanzaExcepcionYNoLoGuarda() {
        // Arrange
        PuntoAlmacenamiento acopio = almacen("Acopio Calarca", PuntoAlmacenamiento.Tipo.PUNTO_ACOPIO, 1);
        acopio.recibirLote(LoteId.nuevo(), TipoCultivo.FRUTA, Conservacion.AMBIENTE);
        LoteId otroLote = LoteId.nuevo();

        // Act
        ReglaDeNegocioVioladaException excepcion = assertThrows(ReglaDeNegocioVioladaException.class,
                () -> acopio.recibirLote(otroLote, TipoCultivo.FRUTA, Conservacion.AMBIENTE));

        // Assert
        assertEquals("INV-CAPACIDAD", excepcion.codigoRegla());
        assertFalse(acopio.guarda(otroLote));
        assertEquals(1, acopio.lotesAlmacenados().size());
    }

    // ---------- Invariante R13 ----------

    @Test
    void recibirUnCultivoNoHabilitadoLanzaExcepcionYNoLoGuarda() {
        // Arrange
        PuntoAlmacenamiento acopio = almacen("Acopio Calarca", PuntoAlmacenamiento.Tipo.PUNTO_ACOPIO, 5);
        LoteId papa = LoteId.nuevo();

        // Act
        ReglaDeNegocioVioladaException excepcion = assertThrows(ReglaDeNegocioVioladaException.class,
                () -> acopio.recibirLote(papa, TipoCultivo.TUBERCULO, Conservacion.AMBIENTE));

        // Assert
        assertEquals("R13", excepcion.codigoRegla());
        assertEquals(5, acopio.espacioDisponible());
    }

    @Test
    void cubreSuMunicipioYLosDeSuZonaSinImportarMayusculas() {
        // Arrange
        PuntoAlmacenamiento centro = almacen("Centro Armenia", PuntoAlmacenamiento.Tipo.CENTRO_REDISTRIBUCION, 10,
                "Circasia", "Montenegro");

        // Act & Assert
        assertTrue(centro.cubre("armenia"));
        assertTrue(centro.cubre(" CIRCASIA "));
        assertFalse(centro.cubre("Pereira"));
    }
}
