package com.uniquindio.ecommerce.domain.catalogo;

import com.uniquindio.ecommerce.domain.exception.ReglaDeNegocioVioladaException;
import com.uniquindio.ecommerce.domain.valueobject.identidad.AlmacenId;
import com.uniquindio.ecommerce.domain.valueobject.identidad.LoteId;
import com.uniquindio.ecommerce.domain.valueobject.logistica.Conservacion;
import com.uniquindio.ecommerce.domain.valueobject.logistica.TipoEmpaque;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Duration;

import static com.uniquindio.ecommerce.LotesDePrueba.AHORA;
import static org.junit.jupiter.api.Assertions.*;

class GuiaDespachoTest {

    private static GuiaDespacho guiaAmbiente() {
        return GuiaDespacho.emitir(LoteId.nuevo(), AlmacenId.nuevo(), "Acopio Calarca", "Centro Armenia",
                TipoEmpaque.CANASTILLA_PLASTICA, Conservacion.AMBIENTE, Duration.ofHours(4), "Transportador", AHORA);
    }

    @Test
    void alEmitirseAnotaLaSalidaComoPrimerRegistroConElIdDeLaGuia() {
        // Arrange & Act
        GuiaDespacho guia = guiaAmbiente();

        // Assert
        RegistroCustodia salida = guia.ultimoRegistro().orElseThrow();
        assertEquals(RegistroCustodia.Tipo.SALIDA_ALMACEN, salida.tipo());
        assertTrue(salida.esDeLaGuia(guia.id()));
        assertEquals(GuiaDespacho.Estado.EMITIDA, guia.estado());
    }

    // ---------- Invariante R12 ----------

    @Test
    void unLoteRefrigeradoNoPuedeViajarEnEmpaqueNoAislante() {
        // Arrange
        LoteId lote = LoteId.nuevo();

        // Act
        ReglaDeNegocioVioladaException excepcion = assertThrows(ReglaDeNegocioVioladaException.class,
                () -> GuiaDespacho.emitir(lote, AlmacenId.nuevo(), "Acopio", "Centro", TipoEmpaque.CAJA_CARTON,
                        Conservacion.REFRIGERADO, Duration.ofHours(4), "Transportador", AHORA));

        // Assert
        assertEquals("R12", excepcion.codigoRegla());
    }

    // ---------- Invariante INV-GUIA: una guia cerrada no admite registros ----------

    @Test
    void laEntregaCierraLaGuiaYDespuesNoSeAdmitenRegistros() {
        // Arrange
        GuiaDespacho guia = guiaAmbiente();
        guia.registrar(RegistroCustodia.Tipo.CONTROL_TEMPERATURA, "Via Armenia", "Transportador",
                AHORA.plusSeconds(600), new BigDecimal("21"), "");
        guia.registrar(RegistroCustodia.Tipo.ENTREGA, "Centro Armenia", "Transportador", AHORA.plusSeconds(1200), null, "");
        int registrosAntes = guia.registros().size();

        // Act
        ReglaDeNegocioVioladaException excepcion = assertThrows(ReglaDeNegocioVioladaException.class,
                () -> guia.registrar(RegistroCustodia.Tipo.INCIDENCIA, "Centro", "Operario",
                        AHORA.plusSeconds(1800), null, "Tarde"));

        // Assert
        assertEquals("INV-GUIA", excepcion.codigoRegla());
        assertEquals(GuiaDespacho.Estado.CERRADA, guia.estado());
        assertEquals(registrosAntes, guia.registros().size());
    }

    // ---------- Invariante INV-GUIA: orden cronologico ----------

    @Test
    void unRegistroAnteriorAlUltimoLanzaExcepcionYNoSeAnota() {
        // Arrange
        GuiaDespacho guia = guiaAmbiente();

        // Act
        assertThrows(ReglaDeNegocioVioladaException.class,
                () -> guia.registrar(RegistroCustodia.Tipo.INCIDENCIA, "Via", "Transportador",
                        AHORA.minusSeconds(60), null, ""));

        // Assert
        assertEquals(1, guia.registros().size());
        assertEquals(GuiaDespacho.Estado.EMITIDA, guia.estado());
    }
}
