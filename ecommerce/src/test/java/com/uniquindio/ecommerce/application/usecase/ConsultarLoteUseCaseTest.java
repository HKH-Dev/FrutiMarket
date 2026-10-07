package com.uniquindio.ecommerce.application.usecase;

import com.uniquindio.ecommerce.domain.entity.Campesino;
import com.uniquindio.ecommerce.domain.catalogo.Lote;
import com.uniquindio.ecommerce.domain.entity.Producto;
import com.uniquindio.ecommerce.domain.exception.RecursoNoEncontradoException;
import com.uniquindio.ecommerce.domain.valueobject.UnidadMedida;
import com.uniquindio.ecommerce.infraestructure.persistence.LoteRepositoryEnMemoria;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ConsultarLoteUseCaseTest {

    private LoteRepositoryEnMemoria repositorio;
    private ConsultarLoteUseCase useCase;

    @BeforeEach
    void setUp() {
        repositorio = new LoteRepositoryEnMemoria();
        useCase = new ConsultarLoteUseCase(repositorio);
    }

    private Lote crearLote(String id) {
        Campesino campesino = new Campesino(UUID.randomUUID(), "12345", 5.0);
        Producto producto = new Producto(
                UUID.randomUUID(), "Mango", "Mango Tommy",
                null, null, TipoCultivo.ORGANICO, EstadoLote.REGISTRADO
        );
        return new Lote.Builder()
                .id(id)
                .codigo("COD-" + id)
                .campesino(campesino)
                .producto(producto)
                .tipoCultivo(TipoCultivo.ORGANICO)
                .unidadMedida(UnidadMedida.KILOGRAMO)
                .cantidadInicial(new BigDecimal("100"))
                .precioUnitario(new BigDecimal("3500"))
                .fechaCosecha(LocalDate.of(2026, 9, 1))
                .fechaLimiteConsumo(LocalDate.of(2026, 10, 15))
                .build();
    }

    // ==========================================
    // PRUEBA 1: Consultar un lote existente lo retorna correctamente
    // ==========================================
    @Test
    void ejecutarConIdExistenteRetornaElLote() {
        // Arrange
        Lote lote = crearLote("lote-test-01");
        repositorio.almacenar(lote);

        // Act
        Lote resultado = useCase.ejecutar("lote-test-01");

        // Assert
        assertNotNull(resultado);
        assertEquals("lote-test-01", resultado.getId());
        assertEquals("COD-lote-test-01", resultado.getCodigo());
    }

    // ==========================================
    // PRUEBA 2: Consultar un lote inexistente lanza excepcion
    // ==========================================
    @Test
    void ejecutarConIdInexistenteLanzaRecursoNoEncontrado() {
        // Arrange (repositorio vacio)

        // Act & Assert
        RecursoNoEncontradoException excepcion = assertThrows(
                RecursoNoEncontradoException.class,
                () -> useCase.ejecutar("id-que-no-existe")
        );
        assertTrue(excepcion.getMessage().contains("Lote"),
                "Debe indicar que el recurso no encontrado es un Lote");
    }

    // ==========================================
    // PRUEBA 3: Consultar todos retorna la lista completa
    // ==========================================
    @Test
    void consultarTodosRetornaLosLotesAlmacenados() {
        // Arrange
        repositorio.almacenar(crearLote("lote-A"));
        repositorio.almacenar(crearLote("lote-B"));
        repositorio.almacenar(crearLote("lote-C"));

        // Act
        List<Lote> resultado = useCase.consultarTodos();

        // Assert
        assertEquals(3, resultado.size(),
                "Debe retornar los 3 lotes almacenados");
    }
}
