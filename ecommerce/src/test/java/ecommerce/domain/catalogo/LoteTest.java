package ecommerce.domain.catalogo;

import com.uniquindio.ecommerce.domain.catalogo.EstadoLote;
import com.uniquindio.ecommerce.domain.catalogo.Lote;
import com.uniquindio.ecommerce.domain.exception.CantidadInsuficienteException;
import com.uniquindio.ecommerce.domain.exception.LoteNoPublicableException;
import com.uniquindio.ecommerce.domain.exception.ReglaDeNegocioVioladaException;
import com.uniquindio.ecommerce.domain.valueobject.catalogo.*;
import com.uniquindio.ecommerce.domain.valueobject.identidad.CampesinoId;
import com.uniquindio.ecommerce.domain.valueobject.identidad.FincaOrigenId;
import com.uniquindio.ecommerce.domain.valueobject.identidad.PedidoId;
import com.uniquindio.ecommerce.domain.valueobject.identidad.ProductoId;
import com.uniquindio.ecommerce.domain.valueobject.origenycalidad.FichaTrazabilidad;
import com.uniquindio.ecommerce.domain.valueobject.origenycalidad.TecnicaProduccion;
import com.uniquindio.ecommerce.domain.valueobject.origenycalidad.TemporadaCosecha;
import com.uniquindio.ecommerce.domain.valueobject.origenycalidad.TipoCultivo;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class LoteTest {

    private static final Instant AHORA = Instant.parse("2026-09-15T15:00:00Z");
    private static final LocalDate FECHA_COSECHA = LocalDate.of(2026, 9, 10);

    private static Cantidad kilos(String valor) {
        return Cantidad.de(valor, UnidadMedida.KILOGRAMO);
    }

    private static Lote loteDeMangoPublicado(String kilosIniciales) {
        CampesinoId campesino = CampesinoId.nuevo();
        FichaTrazabilidad ficha = FichaTrazabilidad.registrar(
                campesino, FincaOrigenId.nuevo(), TecnicaProduccion.AGROECOLOGICA, FECHA_COSECHA);
        Lote lote = Lote.registrarMateriaPrima(
                "LOT-MANGO-01", ProductoId.nuevo(), campesino, ficha, TipoCultivo.FRUTA,
                kilos(kilosIniciales), PrecioFinca.de("3500", Moneda.COP, UnidadMedida.KILOGRAMO),
                null, TemporadaCosecha.permanente(), FECHA_COSECHA, 30, Merma.ninguna(), AHORA);
        lote.publicar(AHORA);
        return lote;
    }

    // ---------- Invariante R3: nunca se reserva mas de lo disponible ----------

    @Test
    void reservarMasDeLoDisponibleLanzaExcepcionYNoAlteraCantidadNiReservas() {
        // Arrange
        Lote lote = loteDeMangoPublicado("100");

        // Act
        CantidadInsuficienteException excepcion = assertThrows(
                CantidadInsuficienteException.class,
                () -> lote.reservar(PedidoId.nuevo(), kilos("150"), AHORA));

        // Assert
        assertEquals("R3", excepcion.codigoRegla());
        assertEquals(kilos("100"), lote.cantidadDisponible(), "La cantidad disponible no debe cambiar");
        assertTrue(lote.reservas().isEmpty(), "No debe quedar ninguna reserva registrada");
        assertEquals(EstadoLote.PUBLICADO, lote.estado());
    }

    // ---------- Invariante R5: nunca se retira un lote con reservas activas ----------

    @Test
    void retirarLoteConReservasActivasLanzaExcepcionYConservaEstadoYReserva() {
        // Arrange
        Lote lote = loteDeMangoPublicado("100");
        lote.reservar(PedidoId.nuevo(), kilos("20"), AHORA);

        // Act
        ReglaDeNegocioVioladaException excepcion = assertThrows(
                ReglaDeNegocioVioladaException.class,
                () -> lote.retirar(AHORA));

        // Assert
        assertEquals("R5", excepcion.codigoRegla());
        assertEquals(EstadoLote.PUBLICADO, lote.estado(), "El lote debe seguir publicado");
        assertTrue(lote.tieneReservasActivas(), "La reserva del pedido debe seguir activa");
        assertEquals(kilos("80"), lote.cantidadDisponible());
    }

    // ---------- Invariante INV-ESTADO: no se publica dos veces ----------

    @Test
    void publicarUnLoteYaPublicadoLanzaExcepcionYNoCambiaSuFechaDePublicacion() {
        // Arrange
        Lote lote = loteDeMangoPublicado("100");
        Instant fechaPublicacionOriginal = lote.fechaPublicacion().orElseThrow();
        Instant despues = AHORA.plusSeconds(3600);

        // Act
        assertThrows(LoteNoPublicableException.class, () -> lote.publicar(despues));

        // Assert
        assertEquals(EstadoLote.PUBLICADO, lote.estado());
        assertEquals(fechaPublicacionOriginal, lote.fechaPublicacion().orElseThrow());
    }

    @Test
    void reservarTodaLaCantidadDisponibleAgotaElLote() {
        // Arrange
        Lote lote = loteDeMangoPublicado("100");

        // Act
        lote.reservar(PedidoId.nuevo(), kilos("100"), AHORA);

        // Assert
        assertTrue(lote.cantidadDisponible().esCero());
        assertEquals(EstadoLote.AGOTADO, lote.estado());
    }
}
