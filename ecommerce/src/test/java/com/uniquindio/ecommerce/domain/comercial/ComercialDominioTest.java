package com.uniquindio.ecommerce.domain.comercial;

import com.uniquindio.ecommerce.domain.chat.Conversacion;
import com.uniquindio.ecommerce.domain.exception.ReglaDeNegocioVioladaException;
import com.uniquindio.ecommerce.domain.service.CalculadoraPrecios;
import com.uniquindio.ecommerce.domain.valueobject.identidad.CampesinoId;
import com.uniquindio.ecommerce.domain.valueobject.identidad.CompradorId;
import com.uniquindio.ecommerce.domain.valueobject.identidad.ProductoId;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ComercialDominioTest {

    private static final LocalDate HOY = LocalDate.of(2026, 11, 9);
    private static final Instant AHORA = Instant.parse("2026-11-09T15:00:00Z");
    private final ProductoId mango = ProductoId.nuevo();
    private final CompradorId ana = CompradorId.nuevo();

    private static void assertRegla(String codigo, Runnable accion) {
        assertEquals(codigo, assertThrows(ReglaDeNegocioVioladaException.class, accion::run).codigoRegla());
    }

    private DescuentoTemporada campana(int porcentaje) {
        return DescuentoTemporada.crear("Cosecha", mango, porcentaje, HOY.minusDays(1), HOY.plusDays(1));
    }

    private Cupon cupon(int porcentaje) {
        return Cupon.crear("clave-10", ana, porcentaje, HOY.plusDays(5), HOY);
    }

    @Test
    void lasCampanasNoSeAcumulanYElCuponSeCalculaSobreLoQueQueda() {
        var linea = new CalculadoraPrecios.Linea(mango, new BigDecimal("1000"));

        Liquidacion l = CalculadoraPrecios.liquidar(List.of(linea), List.of(campana(10), campana(20)), cupon(50), HOY);

        assertEquals(new BigDecimal("200.00"), l.descuentoTemporada());
        assertEquals(new BigDecimal("400.00"), l.descuentoCupon());
        assertEquals(new BigDecimal("400.00"), l.total());
    }

    @Test
    void unaCampanaDeOtroProductoOVencidaNoDescuenta() {
        var linea = new CalculadoraPrecios.Linea(ProductoId.nuevo(), new BigDecimal("1000"));
        assertEquals(new BigDecimal("1000.00"), CalculadoraPrecios.liquidar(List.of(linea), List.of(campana(20)), null, HOY).total());
        assertTrue(!campana(20).vigenteEl(HOY.plusDays(2)));
    }

    @Test
    void campanaConPorcentajeOFechasInvalidasSeRechaza() {
        assertRegla("R21", () -> campana(71));
        assertRegla("R22", () -> DescuentoTemporada.crear("X", mango, 10, HOY, HOY.minusDays(1)));
    }

    @Test
    void elCuponSoloLoRedimeSuDuenoYUnaVez() {
        Cupon cupon = cupon(10);
        assertRegla("R23", () -> cupon.redimir(CompradorId.nuevo(), AHORA, HOY));
        cupon.redimir(ana, AHORA, HOY);
        assertRegla("R24", () -> cupon.redimir(ana, AHORA, HOY));
    }

    @Test
    void cuponLiberadoSePuedeVolverAUsarYVencidoNo() {
        Cupon cupon = cupon(10);
        cupon.redimir(ana, AHORA, HOY);
        cupon.liberar();
        cupon.redimir(ana, AHORA, HOY);

        assertRegla("R24", () -> cupon(10).redimir(ana, AHORA, HOY.plusDays(6)));
    }

    @Test
    void soloLosParticipantesEscribenYElMensajeNoVaVacio() {
        CampesinoId maria = CampesinoId.nuevo();
        Conversacion chat = Conversacion.iniciar(ana, maria, AHORA);

        assertEquals("Hola", chat.escribir(maria.valor(), " Hola ", AHORA).texto());
        assertRegla("R26", () -> chat.escribir(UUID.randomUUID(), "Hola", AHORA));
        assertRegla("R27", () -> chat.escribir(ana.valor(), "  ", AHORA));
        assertRegla("R27", () -> chat.escribir(ana.valor(), "x".repeat(1001), AHORA));
    }

    @Test
    void unPagoNoSeReembolsaDosVeces() {
        Pago pago = Pago.aprobado(com.uniquindio.ecommerce.domain.valueobject.identidad.PedidoId.nuevo(), ana, Pago.Metodo.PSE, null, "R",
                new Liquidacion(BigDecimal.TEN, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.TEN), null, AHORA);
        pago.reembolsar();
        assertRegla("INV-PAGO", pago::reembolsar);
    }
}
