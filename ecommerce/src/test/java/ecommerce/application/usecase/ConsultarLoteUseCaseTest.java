package ecommerce.application.usecase;

import com.uniquindio.ecommerce.application.usecase.ConsultarLoteUseCase;
import com.uniquindio.ecommerce.domain.catalogo.Lote;
import com.uniquindio.ecommerce.domain.exception.RecursoNoEncontradoException;
import com.uniquindio.ecommerce.domain.valueobject.catalogo.*;
import com.uniquindio.ecommerce.domain.valueobject.identidad.CampesinoId;
import com.uniquindio.ecommerce.domain.valueobject.identidad.FincaOrigenId;
import com.uniquindio.ecommerce.domain.valueobject.identidad.LoteId;
import com.uniquindio.ecommerce.domain.valueobject.identidad.ProductoId;
import com.uniquindio.ecommerce.domain.valueobject.origenycalidad.FichaTrazabilidad;
import com.uniquindio.ecommerce.domain.valueobject.origenycalidad.TecnicaProduccion;
import com.uniquindio.ecommerce.domain.valueobject.origenycalidad.TemporadaCosecha;
import com.uniquindio.ecommerce.domain.valueobject.origenycalidad.TipoCultivo;
import com.uniquindio.ecommerce.infraestructure.persistence.LoteRepositoryEnMemoria;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class ConsultarLoteUseCaseTest {

    private static final Instant AHORA = Instant.parse("2026-09-15T15:00:00Z");

    private LoteRepositoryEnMemoria repositorio;
    private ConsultarLoteUseCase useCase;

    @BeforeEach
    void setUp() {
        repositorio = new LoteRepositoryEnMemoria();
        useCase = new ConsultarLoteUseCase(repositorio);
    }

    private static Lote registrarLote(String codigo) {
        CampesinoId campesino = CampesinoId.nuevo();
        LocalDate cosecha = LocalDate.of(2026, 9, 10);
        FichaTrazabilidad ficha = FichaTrazabilidad.registrar(
                campesino, FincaOrigenId.nuevo(), TecnicaProduccion.TRADICIONAL, cosecha);
        return Lote.registrarMateriaPrima(
                codigo, ProductoId.nuevo(), campesino, ficha, TipoCultivo.FRUTA,
                Cantidad.de("50", UnidadMedida.KILOGRAMO),
                PrecioFinca.de("2800", Moneda.COP, UnidadMedida.KILOGRAMO),
                null, TemporadaCosecha.permanente(), cosecha, 20, Merma.ninguna(), AHORA);
    }

    @Test
    void ejecutarConIdDeUnLoteAlmacenadoLoRetorna() {
        // Arrange
        Lote lote = registrarLote("LOT-GUAYABA-01");
        repositorio.almacenar(lote);

        // Act
        Lote resultado = useCase.ejecutar(lote.id());

        // Assert
        assertEquals(lote, resultado);
        assertEquals("LOT-GUAYABA-01", resultado.codigo());
    }

    @Test
    void ejecutarConIdInexistenteLanzaRecursoNoEncontrado() {
        // Arrange
        LoteId idInexistente = LoteId.nuevo();

        // Act
        RecursoNoEncontradoException excepcion = assertThrows(
                RecursoNoEncontradoException.class,
                () -> useCase.ejecutar(idInexistente));

        // Assert
        assertTrue(excepcion.getMessage().contains(idInexistente.toString()));
    }
}
