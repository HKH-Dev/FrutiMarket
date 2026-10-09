package com.uniquindio.ecommerce.application.usecase;

import com.uniquindio.ecommerce.application.dto.CompraResponse;
import com.uniquindio.ecommerce.application.dto.RealizarCompraRequest;
import com.uniquindio.ecommerce.application.port.out.Reloj;
import com.uniquindio.ecommerce.domain.catalogo.Lote;
import com.uniquindio.ecommerce.domain.entity.Comprador;
import com.uniquindio.ecommerce.domain.exception.RecursoNoEncontradoException;
import com.uniquindio.ecommerce.domain.exception.ReglaDeNegocioVioladaException;
import com.uniquindio.ecommerce.infraestructure.persistence.CompraRepositoryEnMemoria;
import com.uniquindio.ecommerce.infraestructure.persistence.CompradorRepositoryEnMemoria;
import com.uniquindio.ecommerce.infraestructure.persistence.LoteRepositoryEnMemoria;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static com.uniquindio.ecommerce.LotesDePrueba.AHORA;
import static com.uniquindio.ecommerce.LotesDePrueba.kilos;
import static com.uniquindio.ecommerce.LotesDePrueba.mangoPublicado;
import static org.junit.jupiter.api.Assertions.*;

class RealizarCompraServiceTest {

    private final LoteRepositoryEnMemoria lotes = new LoteRepositoryEnMemoria();
    private final CompraRepositoryEnMemoria compras = new CompraRepositoryEnMemoria();
    private final CompradorRepositoryEnMemoria compradores = new CompradorRepositoryEnMemoria();
    private final RealizarCompraService servicio =
            new RealizarCompraService(lotes, Reloj.fijo(AHORA), evento -> { }, compras, compradores);

    private Comprador restaurante;

    @BeforeEach
    void setUp() {
        restaurante = Comprador.builder().nombre("Restaurante La Fonda").tipo(Comprador.Tipo.RESTAURANTE)
                .municipio("Armenia").build();
        compradores.registrar(restaurante);
    }

    private static RealizarCompraRequest.Item item(Lote lote, String kilos) {
        return new RealizarCompraRequest.Item(lote.id().valor(), new BigDecimal(kilos));
    }

    @Test
    void unCompradorPuedeComprarLotesDeVariosCampesinosEnUnaSolaCompra() {
        // Arrange
        Lote deAna = lotes.almacenar(mangoPublicado("50"));
        Lote deLuis = lotes.almacenar(mangoPublicado("30"));
        RealizarCompraRequest request = new RealizarCompraRequest(restaurante.getId().valor(),
                List.of(item(deAna, "5"), item(deLuis, "10")));

        // Act
        CompraResponse respuesta = servicio.comprar(request);

        // Assert
        assertEquals("COMPLETADA", respuesta.estado());
        assertEquals(2, respuesta.detalles().size());
        assertEquals(0, new BigDecimal("52500").compareTo(respuesta.total()), "15 kg x 3500");
        assertEquals(kilos("45"), deAna.cantidadDisponible());
        assertEquals(kilos("20"), deLuis.cantidadDisponible());
        assertEquals(1, compras.comprasDelComprador(restaurante.getId()).size());
    }

    @Test
    void comprarMasDeLoDisponibleNoRegistraLaCompraNiDescuentaElLote() {
        // Arrange
        Lote lote = lotes.almacenar(mangoPublicado("50"));
        RealizarCompraRequest request = new RealizarCompraRequest(restaurante.getId().valor(), List.of(item(lote, "80")));

        // Act
        ReglaDeNegocioVioladaException excepcion = assertThrows(ReglaDeNegocioVioladaException.class,
                () -> servicio.comprar(request));

        // Assert
        assertEquals("R3", excepcion.codigoRegla());
        assertTrue(compras.comprasDelComprador(restaurante.getId()).isEmpty());
        assertEquals(kilos("50"), lote.cantidadDisponible());
    }

    @Test
    void unCompradorQueNoExisteNoPuedeComprar() {
        // Arrange
        Lote lote = lotes.almacenar(mangoPublicado("50"));
        RealizarCompraRequest request = new RealizarCompraRequest(UUID.randomUUID(), List.of(item(lote, "5")));

        // Act & Assert
        assertThrows(RecursoNoEncontradoException.class, () -> servicio.comprar(request));
        assertEquals(kilos("50"), lote.cantidadDisponible());
    }
}
