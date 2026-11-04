package com.uniquindio.ecommerce.infraestructure.persistence.jpa;

import com.uniquindio.ecommerce.domain.catalogo.Lote;
import com.uniquindio.ecommerce.domain.consulta.BusquedaLotes;
import com.uniquindio.ecommerce.domain.consulta.Pagina;
import com.uniquindio.ecommerce.domain.valueobject.catalogo.PrecioFinca;
import com.uniquindio.ecommerce.domain.valueobject.catalogo.UnidadMedida;
import com.uniquindio.ecommerce.domain.valueobject.origenycalidad.TipoCultivo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.uniquindio.ecommerce.LotesDePrueba.AHORA;
import static com.uniquindio.ecommerce.LotesDePrueba.lote;
import static com.uniquindio.ecommerce.LotesDePrueba.mango;
import static com.uniquindio.ecommerce.LotesDePrueba.moraRefrigerada;
import static com.uniquindio.ecommerce.LotesDePrueba.primera;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Listado paginado con busqueda y filtros (JPQL con parametros con nombre), con datos conocidos:
 * <pre>
 *   5 lotes de mango publicados   (precios 2000, 2500, 3000, 3500, 4000)
 *   1 lote de mora publicado      (precio 9000)
 *   1 lote de mango RETIRADO      -> borrado logico: no debe salir
 *   1 lote de mango sin publicar  -> no se puede comprar: no debe salir
 * </pre>
 */
@DataJpaTest
@Import(LoteRepositoryJpa.class)
@TestPropertySource(properties = "spring.sql.init.mode=never")
class BusquedaLotesDataJpaTest {

    @Autowired LoteRepositoryJpa lotes;
    @Autowired TestEntityManager entityManager;

    private Lote retirado;

    @BeforeEach
    void prepararCatalogoConocido() {
        for (String precio : List.of("2000", "2500", "3000", "3500", "4000")) {
            lotes.almacenar(publicado(mango("100").precioFinca(PrecioFinca.de(precio, UnidadMedida.KILOGRAMO)).build()));
        }
        lotes.almacenar(publicado(lote(moraRefrigerada(), "50", primera())
                .precioFinca(PrecioFinca.de("9000", UnidadMedida.KILOGRAMO)).build()));
        retirado = publicado(mango("100").build());
        retirado.retirar();
        lotes.almacenar(retirado);
        lotes.almacenar(mango("100").build());              // registrado, sin publicar
        entityManager.flush();
        entityManager.clear();
    }

    @Test
    void paginaPorTextoConTotalesConocidos() {
        // Ejecutar: "MANGO" en mayusculas, de a 2 por pagina
        Pagina<Lote> primera = lotes.buscarDisponibles(new BusquedaLotes("MANGO", null, null, 0, 2));
        Pagina<Lote> ultima = lotes.buscarDisponibles(new BusquedaLotes("MANGO", null, null, 2, 2));

        // Verificar: 5 mangos (ni el retirado ni el borrador) en 3 paginas: 2 + 2 + 1
        assertEquals(5, primera.totalElementos());
        assertEquals(3, primera.totalPaginas());
        assertEquals(2, primera.contenido().size());
        assertEquals(1, ultima.contenido().size());
        assertTrue(primera.contenido().stream().allMatch(l -> l.nombreProducto().equals("Mango Tommy")));
    }

    @Test
    void lasPaginasNoRepitenNiPierdenLotes() {
        Set<Object> vistos = new HashSet<>();
        for (int pagina = 0; pagina < 3; pagina++) {
            lotes.buscarDisponibles(new BusquedaLotes("mango", null, null, pagina, 2)).contenido()
                    .forEach(lote -> vistos.add(lote.id()));
        }
        assertEquals(5, vistos.size());
    }

    @Test
    void elLoteRetiradoNuncaAparece() {
        Pagina<Lote> todos = lotes.buscarDisponibles(new BusquedaLotes("", null, null, 0, 50));

        assertEquals(6, todos.totalElementos());
        assertTrue(todos.contenido().stream().noneMatch(lote -> lote.id().equals(retirado.id())));
    }

    @Test
    void filtraPorPrecioMaximoYPorTipoDeCultivo() {
        // Precio de finca hasta 3000: los mangos de 2000, 2500 y 3000
        assertEquals(3, lotes.buscarDisponibles(new BusquedaLotes("", null, new BigDecimal("3000"), 0, 10)).totalElementos());
        // Tipo de cultivo que no hay en el catalogo
        assertEquals(0, lotes.buscarDisponibles(new BusquedaLotes("", TipoCultivo.TUBERCULO, null, 0, 10)).totalElementos());
        // Texto + filtro: "mora" entre las frutas
        Pagina<Lote> moras = lotes.buscarDisponibles(new BusquedaLotes("mora", TipoCultivo.FRUTA, null, 0, 10));
        assertEquals(1, moras.totalElementos());
        assertEquals("Mora de castilla", moras.contenido().get(0).nombreProducto());
    }

    private static Lote publicado(Lote lote) {
        lote.publicar(AHORA);
        return lote;
    }
}
