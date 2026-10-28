package com.uniquindio.ecommerce.infraestructure.persistence.jpa;

import com.uniquindio.ecommerce.domain.catalogo.EstadoLote;
import com.uniquindio.ecommerce.domain.catalogo.Lote;
import com.uniquindio.ecommerce.domain.catalogo.RegistroCustodia;
import com.uniquindio.ecommerce.domain.valueobject.catalogo.Cantidad;
import com.uniquindio.ecommerce.domain.valueobject.catalogo.Merma;
import com.uniquindio.ecommerce.domain.valueobject.catalogo.PrecioFinca;
import com.uniquindio.ecommerce.domain.valueobject.catalogo.UnidadMedida;
import com.uniquindio.ecommerce.domain.valueobject.identidad.PedidoId;
import com.uniquindio.ecommerce.domain.valueobject.origenycalidad.Calidad;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;

import static com.uniquindio.ecommerce.LotesDePrueba.AHORA;
import static com.uniquindio.ecommerce.LotesDePrueba.kilos;
import static com.uniquindio.ecommerce.LotesDePrueba.mango;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Punto 0.1 y punto 2: prueba del repositorio con {@code @DataJpaTest}.
 *
 * <p>Solo levanta la capa JPA (entidades, Spring Data y una base H2 de pruebas); los adaptadores
 * propios se agregan con {@code @Import}. Cada prueba corre en una transaccion que se deshace al final.</p>
 */
@DataJpaTest
@Import(LoteRepositoryJpa.class)
@TestPropertySource(properties = "spring.sql.init.mode=never") // sin data.sql: solo los datos de la prueba
class LoteRepositoryDataJpaTest {

    @Autowired
    LoteRepositoryJpa repositorio;

    @Autowired
    TestEntityManager entityManager;

    @Test
    void guardarYRecuperarUnLoteDevuelveLosValueObjectsIntactos() {
        // Preparar: un lote publicado, con merma aplicada, una reserva y un registro de calidad
        Lote original = mango("100")
                .merma(Merma.de("10", "Golpes en el transporte"))
                .build();
        original.publicar(AHORA);
        PedidoId pedido = PedidoId.nuevo();
        original.reservar(pedido, kilos("25"), AHORA);
        original.inspeccionarCalidad(Calidad.de(Calidad.Categoria.PRIMERA, 8, false), "Inspector ICA", AHORA);

        // Ejecutar: guardar, vaciar la cache de JPA y volver a leer desde la base de datos
        repositorio.almacenar(original);
        entityManager.flush();
        entityManager.clear();
        Lote recuperado = repositorio.obtenerLote(original.id()).orElseThrow();

        // Verificar: es otro objeto, pero con exactamente los mismos datos
        assertNotSame(original, recuperado);
        assertEquals(original.codigo(), recuperado.codigo());
        assertEquals(EstadoLote.PUBLICADO, recuperado.estado());
        assertEquals(Cantidad.de("100", UnidadMedida.KILOGRAMO), recuperado.cantidadInicial());
        assertEquals(Cantidad.de("65", UnidadMedida.KILOGRAMO), recuperado.cantidadDisponible()); // 100 - 10% - 25
        assertEquals(PrecioFinca.de("3500", UnidadMedida.KILOGRAMO), recuperado.precioFinca());
        assertEquals(Merma.de(new BigDecimal("10"), "Golpes en el transporte"), recuperado.merma());
        assertTrue(recuperado.mermaAplicada());
        assertEquals(original.calidadDeclarada(), recuperado.calidadDeclarada());
        assertEquals(original.calidadVerificada(), recuperado.calidadVerificada());
        assertEquals(original.fichaTrazabilidad(), recuperado.fichaTrazabilidad());
        assertEquals(original.fechaLimiteConsumo(), recuperado.fechaLimiteConsumo());
        assertEquals(original.reservas(), recuperado.reservas());
        assertEquals(original.custodia(), recuperado.custodia());
        assertEquals(RegistroCustodia.Tipo.CONTROL_CALIDAD, recuperado.custodia().get(1).tipo());
    }
}
