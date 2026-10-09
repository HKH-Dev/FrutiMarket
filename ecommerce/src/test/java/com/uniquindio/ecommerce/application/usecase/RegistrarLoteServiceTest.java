package com.uniquindio.ecommerce.application.usecase;

import com.uniquindio.ecommerce.application.dto.RegistrarLoteRequest;
import com.uniquindio.ecommerce.application.port.out.Reloj;
import com.uniquindio.ecommerce.domain.catalogo.EstadoLote;
import com.uniquindio.ecommerce.domain.catalogo.Lote;
import com.uniquindio.ecommerce.domain.catalogo.Producto;
import com.uniquindio.ecommerce.domain.entity.Campesino;
import com.uniquindio.ecommerce.domain.event.EventoDominio;
import com.uniquindio.ecommerce.domain.exception.RecursoNoEncontradoException;
import com.uniquindio.ecommerce.domain.exception.ReglaDeNegocioVioladaException;
import com.uniquindio.ecommerce.domain.valueobject.catalogo.UnidadMedida;
import com.uniquindio.ecommerce.domain.valueobject.identidad.LoteId;
import com.uniquindio.ecommerce.domain.valueobject.origenycalidad.Calidad;
import com.uniquindio.ecommerce.domain.valueobject.origenycalidad.TecnicaProduccion;
import com.uniquindio.ecommerce.infraestructure.persistence.CampesinoRepositoryEnMemoria;
import com.uniquindio.ecommerce.infraestructure.persistence.LoteRepositoryEnMemoria;
import com.uniquindio.ecommerce.infraestructure.persistence.ProductoRepositoryEnMemoria;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.uniquindio.ecommerce.LotesDePrueba.AHORA;
import static com.uniquindio.ecommerce.LotesDePrueba.COSECHA;
import static com.uniquindio.ecommerce.LotesDePrueba.mangoTommy;
import static org.junit.jupiter.api.Assertions.*;

class RegistrarLoteServiceTest {

    private final LoteRepositoryEnMemoria lotes = new LoteRepositoryEnMemoria();
    private final CampesinoRepositoryEnMemoria campesinos = new CampesinoRepositoryEnMemoria();
    private final ProductoRepositoryEnMemoria productos = new ProductoRepositoryEnMemoria();
    private final List<EventoDominio> eventosPublicados = new ArrayList<>();
    private final RegistrarLoteService servicio =
            new RegistrarLoteService(lotes, Reloj.fijo(AHORA), eventosPublicados::add, campesinos, productos);

    private Campesino ana;
    private Producto mango;

    @BeforeEach
    void setUp() {
        ana = Campesino.builder().nombre("Ana Ruiz").numeroIdentificacion("1094000111").build();
        ana.autorizarParaPublicar();
        campesinos.registrar(ana);
        mango = mangoTommy();
        productos.registrar(mango);
    }

    private RegistrarLoteRequest formulario(String codigo, UUID campesino) {
        return new RegistrarLoteRequest(codigo, mango.id().valor(), campesino, UUID.randomUUID(),
                TecnicaProduccion.AGROECOLOGICA, Calidad.Categoria.PRIMERA, 10, false,
                new BigDecimal("200"), UnidadMedida.KILOGRAMO, new BigDecimal("3200"), COSECHA, null);
    }

    @Test
    void registrarUnLoteLoGuardaEnBorradorConSuCalidadYPublicaElEvento() {
        // Arrange
        RegistrarLoteRequest request = formulario("LOT-MANGO-01", ana.getId().valor());

        // Act
        LoteId id = servicio.registrar(request.aComando());

        // Assert
        Lote guardado = lotes.obtenerLote(id).orElseThrow();
        assertEquals(EstadoLote.REGISTRADO, guardado.estado());
        assertEquals(80, guardado.puntajeCalidad(), "PRIMERA (85) - 10/2");
        assertEquals("Mango Tommy", guardado.nombreProducto());
        assertEquals(EventoDominio.Tipo.LOTE_REGISTRADO, eventosPublicados.get(0).tipo());
    }

    @Test
    void unCampesinoNoAutorizadoNoPuedeRegistrarLotes() {
        // Arrange
        ana.revocarAutorizacion();

        // Act
        ReglaDeNegocioVioladaException excepcion = assertThrows(ReglaDeNegocioVioladaException.class,
                () -> servicio.registrar(formulario("LOT-MANGO-02", ana.getId().valor()).aComando()));

        // Assert
        assertEquals("R1", excepcion.codigoRegla());
        assertFalse(lotes.existeCodigo("LOT-MANGO-02"));
    }

    @Test
    void noSePuedenRegistrarDosLotesConElMismoCodigo() {
        // Arrange
        servicio.registrar(formulario("LOT-MANGO-03", ana.getId().valor()).aComando());

        // Act
        ReglaDeNegocioVioladaException excepcion = assertThrows(ReglaDeNegocioVioladaException.class,
                () -> servicio.registrar(formulario("LOT-MANGO-03", ana.getId().valor()).aComando()));

        // Assert
        assertEquals("INV-CODIGO", excepcion.codigoRegla());
    }

    @Test
    void unCampesinoQueNoExisteNoPuedeRegistrar() {
        // Arrange
        UUID desconocido = UUID.randomUUID();

        // Act & Assert
        assertThrows(RecursoNoEncontradoException.class,
                () -> servicio.registrar(formulario("LOT-MANGO-04", desconocido).aComando()));
    }
}
