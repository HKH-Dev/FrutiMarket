package com.uniquindio.ecommerce.domain.catalogo;

import com.uniquindio.ecommerce.domain.exception.ReglaDeNegocioVioladaException;
import com.uniquindio.ecommerce.domain.valueobject.catalogo.UnidadMedida;
import com.uniquindio.ecommerce.domain.valueobject.origenycalidad.TemporadaCosecha;
import com.uniquindio.ecommerce.domain.valueobject.origenycalidad.TipoCultivo;
import org.junit.jupiter.api.Test;

import java.time.MonthDay;

import static com.uniquindio.ecommerce.LotesDePrueba.mango;
import static com.uniquindio.ecommerce.LotesDePrueba.mangoTommy;
import static org.junit.jupiter.api.Assertions.*;

class ProductoTest {

    @Test
    void unProductoTransformadoNoPuedeDependerDeTemporada() {
        // Arrange
        Producto.Builder mermelada = Producto.builder()
                .nombre("Mermelada de mora").linea(Producto.Linea.TRANSFORMADO)
                .tipoCultivo(TipoCultivo.DERIVADO_PROCESADO).unidadVenta(UnidadMedida.UNIDAD)
                .temporada(TemporadaCosecha.de(MonthDay.of(4, 1), MonthDay.of(6, 30)));

        // Act
        ReglaDeNegocioVioladaException excepcion = assertThrows(ReglaDeNegocioVioladaException.class, mermelada::build);

        // Assert
        assertEquals("INV-PRODUCTO", excepcion.codigoRegla());
    }

    @Test
    void unProductoInactivoNoAdmiteLotesNuevos() {
        // Arrange
        Producto mango = mangoTommy();
        mango.desactivar();
        Lote.Builder lote = mango("100").producto(mango);

        // Act
        ReglaDeNegocioVioladaException excepcion = assertThrows(ReglaDeNegocioVioladaException.class, lote::build);

        // Assert
        assertEquals("INV-PRODUCTO", excepcion.codigoRegla());
    }

    @Test
    void actualizarLaVidaUtilANegativoLanzaExcepcionYConservaLaAnterior() {
        // Arrange
        Producto mango = mangoTommy();

        // Act
        assertThrows(ReglaDeNegocioVioladaException.class, () -> mango.actualizarVidaUtil(-1));

        // Assert
        assertEquals(30, mango.vidaUtilDias());
    }
}
