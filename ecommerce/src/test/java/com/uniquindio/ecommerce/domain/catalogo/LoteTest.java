package com.uniquindio.ecommerce.domain.catalogo;

import com.uniquindio.ecommerce.domain.exception.ReglaDeNegocioVioladaException;
import com.uniquindio.ecommerce.domain.valueobject.catalogo.Cantidad;
import com.uniquindio.ecommerce.domain.valueobject.catalogo.UnidadMedida;
import com.uniquindio.ecommerce.domain.valueobject.identidad.AlmacenId;
import com.uniquindio.ecommerce.domain.valueobject.identidad.PedidoId;
import com.uniquindio.ecommerce.domain.valueobject.origenycalidad.Calidad;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;

import static com.uniquindio.ecommerce.LotesDePrueba.AHORA;
import static com.uniquindio.ecommerce.LotesDePrueba.kilos;
import static com.uniquindio.ecommerce.LotesDePrueba.lote;
import static com.uniquindio.ecommerce.LotesDePrueba.mango;
import static com.uniquindio.ecommerce.LotesDePrueba.mangoPublicado;
import static com.uniquindio.ecommerce.LotesDePrueba.moraRefrigerada;
import static com.uniquindio.ecommerce.LotesDePrueba.primera;
import static org.junit.jupiter.api.Assertions.*;

class LoteTest {

    private static final Instant MAS_TARDE = AHORA.plusSeconds(3600);

    // ---------- Invariante R3: nunca se reserva mas de lo disponible ----------

    @Test
    void reservarMasDeLoDisponibleLanzaExcepcionYNoAlteraCantidadNiReservas() {
        // Arrange
        Lote lote = mangoPublicado("100");

        // Act
        ReglaDeNegocioVioladaException excepcion = assertThrows(ReglaDeNegocioVioladaException.class,
                () -> lote.reservar(PedidoId.nuevo(), kilos("150"), AHORA));

        // Assert
        assertEquals("R3", excepcion.codigoRegla());
        assertEquals(kilos("100"), lote.cantidadDisponible(), "La cantidad disponible no debe cambiar");
        assertTrue(lote.reservas().isEmpty(), "No debe quedar ninguna reserva");
        assertEquals(EstadoLote.PUBLICADO, lote.estado());
    }

    // ---------- Invariante R5: nunca se retira un lote con reservas activas ----------

    @Test
    void retirarLoteConReservasActivasLanzaExcepcionYConservaEstadoYReserva() {
        // Arrange
        Lote lote = mangoPublicado("100");
        lote.reservar(PedidoId.nuevo(), kilos("20"), AHORA);

        // Act
        ReglaDeNegocioVioladaException excepcion = assertThrows(ReglaDeNegocioVioladaException.class, lote::retirar);

        // Assert
        assertEquals("R5", excepcion.codigoRegla());
        assertEquals(EstadoLote.PUBLICADO, lote.estado());
        assertTrue(lote.tieneReservasActivas());
        assertEquals(kilos("80"), lote.cantidadDisponible());
    }

    // ---------- Invariante R9: solo se despacha desde un almacenamiento ----------

    @Test
    void despacharUnLoteQueNoEstaEnUnAlmacenLanzaExcepcionYNoAgregaCustodia() {
        // Arrange
        Lote lote = mangoPublicado("100");
        int registrosAntes = lote.custodia().size();

        // Act
        ReglaDeNegocioVioladaException excepcion = assertThrows(ReglaDeNegocioVioladaException.class,
                () -> lote.despachar("Centro Armenia", Duration.ofHours(12), "Transportador", AHORA));

        // Assert
        assertEquals("R9", excepcion.codigoRegla());
        assertEquals(registrosAntes, lote.custodia().size());
        assertEquals(EstadoLote.PUBLICADO, lote.estado());
    }

    // ---------- Invariante R11: temperatura fuera de rango detiene el lote ----------

    @Test
    void temperaturaFueraDeRangoEnUnLoteRefrigeradoLoPoneEnRevisionYBloqueaElDespacho() {
        // Arrange
        Lote mora = lote(moraRefrigerada(), "40", primera()).build();
        mora.publicar(AHORA);
        mora.ingresarAAlmacen(AlmacenId.nuevo(), "Acopio Calarca", "Operario", AHORA);

        // Act
        mora.registrarTemperatura(new BigDecimal("15"), "Operario", MAS_TARDE);

        // Assert
        assertEquals(EstadoLote.EN_REVISION, mora.estado());
        assertTrue(mora.cadenaFrioRota());
        ReglaDeNegocioVioladaException excepcion = assertThrows(ReglaDeNegocioVioladaException.class,
                () -> mora.despachar("Centro Armenia", Duration.ofHours(6), "Transportador", MAS_TARDE));
        assertEquals("R11", excepcion.codigoRegla());
    }

    // ---------- Invariante R18: la calidad real no coincide con la declarada ----------

    @Test
    void unaInspeccionConCalidadMuyInferiorPoneElLoteEnRevisionYSoloSeLiberaCorrigiendo() {
        // Arrange
        Lote lote = mango("100").calidad(Calidad.de(Calidad.Categoria.EXTRA, 0, false)).build();
        lote.publicar(AHORA);
        Calidad observada = Calidad.de(Calidad.Categoria.SEGUNDA, 20, false);

        // Act
        boolean coincide = lote.inspeccionarCalidad(observada, "Inspector", MAS_TARDE);

        // Assert
        assertFalse(coincide);
        assertEquals(EstadoLote.EN_REVISION, lote.estado());
        assertThrows(ReglaDeNegocioVioladaException.class, () -> lote.validarRevision("Inspector", MAS_TARDE),
                "Una revision de calidad no se libera solo validando");
        lote.corregirCalidadDeclarada(observada, "Campesino", MAS_TARDE);
        assertEquals(EstadoLote.PUBLICADO, lote.estado());
        assertEquals(observada.puntaje(), lote.puntajeCalidad());
    }

    // ---------- Invariante INV-CUSTODIA: la cadena solo crece en orden cronologico ----------

    @Test
    void unRegistroDeCustodiaAnteriorAlUltimoLanzaExcepcionYNoSeAgrega() {
        // Arrange
        Lote lote = mangoPublicado("100");
        lote.ingresarAAlmacen(AlmacenId.nuevo(), "Acopio Calarca", "Operario", MAS_TARDE);
        int registrosAntes = lote.custodia().size();

        // Act
        ReglaDeNegocioVioladaException excepcion = assertThrows(ReglaDeNegocioVioladaException.class,
                () -> lote.registrarTemperatura(new BigDecimal("20"), "Operario", AHORA));

        // Assert
        assertEquals("INV-CUSTODIA", excepcion.codigoRegla());
        assertEquals(registrosAntes, lote.custodia().size());
    }

    @Test
    void elRegistroIniciaLaCadenaDeCustodiaConLaCosecha() {
        // Arrange & Act
        Lote lote = mango("100").build();

        // Assert
        assertEquals(1, lote.custodia().size());
        assertEquals(RegistroCustodia.Tipo.COSECHA, lote.custodia().get(0).tipo());
    }

    @Test
    void publicarUnLoteYaPublicadoLanzaExcepcionYNoCambiaSuFechaDePublicacion() {
        // Arrange
        Lote lote = mangoPublicado("100");
        Instant fechaOriginal = lote.fechaPublicacion().orElseThrow();

        // Act
        assertThrows(ReglaDeNegocioVioladaException.class, () -> lote.publicar(MAS_TARDE));

        // Assert
        assertEquals(fechaOriginal, lote.fechaPublicacion().orElseThrow());
    }

    @Test
    void laCantidadDebeEstarEnLaUnidadDeVentaDelProducto() {
        // Arrange
        Lote.Builder enLibras = mango("100").cantidadInicial(Cantidad.de("200", UnidadMedida.LIBRA));

        // Act
        ReglaDeNegocioVioladaException excepcion = assertThrows(ReglaDeNegocioVioladaException.class, enLibras::build);

        // Assert
        assertEquals("INV-UNIDAD", excepcion.codigoRegla());
    }

    @Test
    void reservarTodaLaCantidadDisponibleAgotaElLote() {
        // Arrange
        Lote lote = mangoPublicado("100");

        // Act
        lote.reservar(PedidoId.nuevo(), kilos("100"), AHORA);

        // Assert
        assertTrue(lote.cantidadDisponible().esCero());
        assertEquals(EstadoLote.AGOTADO, lote.estado());
    }
}
