package com.uniquindio.ecommerce.application.usecase;

import com.uniquindio.ecommerce.application.dto.CompraResponse;
import com.uniquindio.ecommerce.application.port.out.Reloj;
import com.uniquindio.ecommerce.domain.entity.Compra;
import com.uniquindio.ecommerce.domain.entity.DetalleCompra;
import com.uniquindio.ecommerce.domain.event.EventoDominio;
import com.uniquindio.ecommerce.domain.exception.RecursoNoEncontradoException;
import com.uniquindio.ecommerce.domain.exception.ReglaDeNegocioVioladaException;
import com.uniquindio.ecommerce.domain.valueobject.EstadoCompra;
import com.uniquindio.ecommerce.domain.valueobject.catalogo.PrecioFinca;
import com.uniquindio.ecommerce.domain.valueobject.catalogo.UnidadMedida;
import com.uniquindio.ecommerce.domain.valueobject.identidad.CompradorId;
import com.uniquindio.ecommerce.domain.valueobject.identidad.LoteId;
import com.uniquindio.ecommerce.domain.valueobject.identidad.PedidoId;
import com.uniquindio.ecommerce.infraestructure.persistence.CompraRepositoryEnMemoria;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static com.uniquindio.ecommerce.LotesDePrueba.kilos;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Caso de uso con repositorio en memoria y reloj fijo: se prueba el plazo sin esperar dias. */
class SolicitarReembolsoServiceTest {

    private static final Instant FECHA_COMPRA = Instant.parse("2026-11-01T10:00:00Z");

    private final CompraRepositoryEnMemoria compras = new CompraRepositoryEnMemoria();
    private final List<EventoDominio> eventos = new ArrayList<>();
    private final CompradorId comprador = CompradorId.nuevo();
    private Compra compra;

    @BeforeEach
    void registrarCompraCompletada() {
        compra = Compra.builder().compradorId(comprador).fecha(FECHA_COMPRA).build();
        compra.agregarDetalle(DetalleCompra.crear(LoteId.nuevo(), kilos("4"), PrecioFinca.de("3500", UnidadMedida.KILOGRAMO)));
        compra.confirmar();
        compras.registrar(compra);
    }

    private SolicitarReembolsoService servicioEn(Instant ahora) {
        return new SolicitarReembolsoService(compras, Reloj.fijo(ahora), eventos::add);
    }

    @Test
    void dentroDelPlazoLaCompraQuedaReembolsadaYSePublicaElEvento() {
        // Arrange
        SolicitarReembolsoService servicio = servicioEn(FECHA_COMPRA.plus(Duration.ofDays(1)));

        // Act
        CompraResponse respuesta = servicio.reembolsar(compra.getId(), comprador);

        // Assert
        assertEquals("REEMBOLSADA", respuesta.estado());
        assertEquals(EstadoCompra.REEMBOLSADA, compras.obtenerCompra(compra.getId()).orElseThrow().getEstado());
        assertEquals(EventoDominio.Tipo.COMPRA_REEMBOLSADA, eventos.get(0).tipo());
    }

    @Test
    void fueraDelPlazoFallaConR20YNoPublicaNada() {
        // Arrange
        SolicitarReembolsoService servicio = servicioEn(FECHA_COMPRA.plus(Duration.ofDays(4)));

        // Act
        ReglaDeNegocioVioladaException excepcion = assertThrows(ReglaDeNegocioVioladaException.class,
                () -> servicio.reembolsar(compra.getId(), comprador));

        // Assert
        assertEquals("R20", excepcion.codigoRegla());
        assertEquals(EstadoCompra.COMPLETADA, compras.obtenerCompra(compra.getId()).orElseThrow().getEstado());
        assertTrue(eventos.isEmpty());
    }

    @Test
    void unaCompraQueNoExisteResponde404() {
        SolicitarReembolsoService servicio = servicioEn(FECHA_COMPRA);

        assertThrows(RecursoNoEncontradoException.class, () -> servicio.reembolsar(PedidoId.nuevo(), comprador));
    }
}
