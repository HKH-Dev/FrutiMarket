package com.uniquindio.ecommerce.domain.entity;

import com.uniquindio.ecommerce.domain.exception.ReglaNegocioException;
import com.uniquindio.ecommerce.domain.valueobject.EstadoLote;
import com.uniquindio.ecommerce.domain.valueobject.TipoCultivo;
import com.uniquindio.ecommerce.domain.valueobject.UnidadMedida;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class LoteTest {

    // ====================================================
    // Metodo auxiliar para crear un Lote valido en los tests
    // ====================================================
    private Lote crearLoteValido() {
        return crearLoteConId("lote-001");
    }

    private Lote crearLoteConId(String id) {
        Campesino campesino = new Campesino(
                UUID.randomUUID(), "1234567890", 5.0
        );
        Producto producto = new Producto(
                UUID.randomUUID(), "Mango Tommy", "Mango de exportacion",
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
    // PRUEBA DE ENTIDAD 1: Igualdad por identidad
    // Dos lotes con mismo id son iguales, aunque tengan datos distintos.
    // ==========================================
    @Test
    void dosLotesConMismoIdSonIgualesAunConDatosDistintos() {
        // Arrange
        String mismoId = "lote-compartido";
        Campesino campesino1 = new Campesino(UUID.randomUUID(), "111", 2.0);
        Campesino campesino2 = new Campesino(UUID.randomUUID(), "222", 8.0);
        Producto producto = new Producto(
                UUID.randomUUID(), "Naranja", "Naranja Valencia",
                null, null, TipoCultivo.CONVENCIONAL, EstadoLote.REGISTRADO
        );

        Lote lote1 = new Lote.Builder()
                .id(mismoId).codigo("COD-A").campesino(campesino1).producto(producto)
                .tipoCultivo(TipoCultivo.ORGANICO).unidadMedida(UnidadMedida.KILOGRAMO)
                .cantidadInicial(new BigDecimal("50")).precioUnitario(new BigDecimal("2000"))
                .fechaCosecha(LocalDate.of(2026, 8, 1))
                .fechaLimiteConsumo(LocalDate.of(2026, 9, 1))
                .build();

        Lote lote2 = new Lote.Builder()
                .id(mismoId).codigo("COD-B").campesino(campesino2).producto(producto)
                .tipoCultivo(TipoCultivo.CONVENCIONAL).unidadMedida(UnidadMedida.LIBRA)
                .cantidadInicial(new BigDecimal("200")).precioUnitario(new BigDecimal("5000"))
                .fechaCosecha(LocalDate.of(2026, 7, 1))
                .fechaLimiteConsumo(LocalDate.of(2026, 12, 1))
                .build();

        // Act & Assert
        assertEquals(lote1, lote2,
                "Dos Lotes con el mismo id deben ser iguales (identidad de Entidad)");
        assertEquals(lote1.hashCode(), lote2.hashCode(),
                "El hashCode debe basarse en el id");
    }

    // ==========================================
    // PRUEBA DE ENTIDAD 2: Regla protegida
    // No se puede crear un lote sin id (invariante).
    // ==========================================
    @Test
    void crearLoteSinIdLanzaReglaNegocioException() {
        // Arrange
        Campesino campesino = new Campesino(UUID.randomUUID(), "999", 1.0);
        Producto producto = new Producto(
                UUID.randomUUID(), "Papaya", "Papaya hawaiana",
                null, null, TipoCultivo.ORGANICO, EstadoLote.REGISTRADO
        );

        // Act & Assert
        ReglaNegocioException excepcion = assertThrows(
                ReglaNegocioException.class,
                () -> new Lote.Builder()
                        .codigo("COD-X")
                        .campesino(campesino)
                        .producto(producto)
                        .tipoCultivo(TipoCultivo.ORGANICO)
                        .unidadMedida(UnidadMedida.KILOGRAMO)
                        .cantidadInicial(new BigDecimal("10"))
                        .precioUnitario(new BigDecimal("1000"))
                        .fechaCosecha(LocalDate.of(2026, 9, 1))
                        .fechaLimiteConsumo(LocalDate.of(2026, 10, 1))
                        .build()  // NO se paso .id(...)
        );
        assertTrue(excepcion.getMessage().contains("id"),
                "Debe indicar que el lote requiere id");
    }

    // ==========================================
    // PRUEBA DE AGREGADO 1: Invariante - descontar mas de lo disponible
    // lanza excepcion Y el estado no cambia.
    // ==========================================
    @Test
    void descontarMasDeLaDisponibilidadLanzaExcepcionYEstadoNoCambia() {
        // Arrange
        Lote lote = crearLoteValido();
        lote.publicar();
        BigDecimal cantidadOriginal = lote.getCantidadDisponible();
        BigDecimal cantidadExcesiva = cantidadOriginal.add(BigDecimal.ONE);

        // Act & Assert
        ReglaNegocioException excepcion = assertThrows(
                ReglaNegocioException.class,
                () -> lote.descontar(cantidadExcesiva)
        );
        assertTrue(excepcion.getMessage().contains("supera"),
                "Debe indicar que la cantidad supera la disponible");

        // Verificar que el estado NO cambio tras el rechazo
        assertEquals(cantidadOriginal, lote.getCantidadDisponible(),
                "La cantidad disponible no debe cambiar tras un descuento rechazado");
        assertEquals(EstadoLote.PUBLICADO, lote.getEstado(),
                "El estado debe seguir siendo PUBLICADO");
    }

    // ==========================================
    // PRUEBA DE AGREGADO 2: Invariante - publicar lote ya publicado
    // lanza excepcion Y el estado se mantiene.
    // ==========================================
    @Test
    void publicarLoteYaPublicadoLanzaExcepcionYEstadoNoCambia() {
        // Arrange
        Lote lote = crearLoteValido();
        lote.publicar();
        lote.descontar(lote.getCantidadDisponible()); // agotar el lote

        // Act & Assert
        assertEquals(EstadoLote.AGOTADO, lote.getEstado(),
                "Precondicion: el lote debe estar AGOTADO");

        ReglaNegocioException excepcion = assertThrows(
                ReglaNegocioException.class,
                () -> lote.publicar()
        );
        assertTrue(excepcion.getMessage().contains("Solo un lote registrado"),
                "Debe indicar que solo se puede publicar un lote REGISTRADO o EN_ACOPIO");

        assertEquals(EstadoLote.AGOTADO, lote.getEstado(),
                "El estado debe seguir siendo AGOTADO despues del rechazo");
    }

    // ==========================================
    // PRUEBA DE AGREGADO 3: Invariante - fecha limite anterior a cosecha
    // ==========================================
    @Test
    void fechaLimiteAnteriorACosechaLanzaExcepcion() {
        // Arrange
        Campesino campesino = new Campesino(UUID.randomUUID(), "555", 3.0);
        Producto producto = new Producto(
                UUID.randomUUID(), "Guayaba", "Guayaba pera",
                null, null, TipoCultivo.ORGANICO, EstadoLote.REGISTRADO
        );

        // Act & Assert
        ReglaNegocioException excepcion = assertThrows(
                ReglaNegocioException.class,
                () -> new Lote.Builder()
                        .id("lote-fechas")
                        .codigo("COD-F")
                        .campesino(campesino)
                        .producto(producto)
                        .tipoCultivo(TipoCultivo.ORGANICO)
                        .unidadMedida(UnidadMedida.KILOGRAMO)
                        .cantidadInicial(new BigDecimal("50"))
                        .precioUnitario(new BigDecimal("1500"))
                        .fechaCosecha(LocalDate.of(2026, 10, 1))
                        .fechaLimiteConsumo(LocalDate.of(2026, 9, 1)) // ANTES de cosecha
                        .build()
        );
        assertTrue(excepcion.getMessage().contains("posterior"),
                "Debe indicar que la fecha limite debe ser posterior a la cosecha");
    }

    // ==========================================
    // PRUEBA DE AGREGADO 4: Descontar toda la cantidad pasa a AGOTADO
    // ==========================================
    @Test
    void descontarTodaLaCantidadCambiaEstadoAAgotado() {
        // Arrange
        Lote lote = crearLoteValido();
        lote.publicar();

        // Act
        lote.descontar(new BigDecimal("100"));

        // Assert
        assertEquals(BigDecimal.ZERO, lote.getCantidadDisponible(),
                "La cantidad disponible debe ser cero");
        assertEquals(EstadoLote.AGOTADO, lote.getEstado(),
                "El estado debe cambiar a AGOTADO al llegar a cero");
    }
}
