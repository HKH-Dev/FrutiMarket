package com.uniquindio.ecommerce.application.usecase;

import com.uniquindio.ecommerce.application.port.in.ConsultarLoteUseCase.VistaLote;
import com.uniquindio.ecommerce.application.port.in.ConsultarLoteUseCase.VistaTrazabilidad;
import com.uniquindio.ecommerce.application.port.out.Reloj;
import com.uniquindio.ecommerce.domain.catalogo.Lote;
import com.uniquindio.ecommerce.domain.catalogo.Producto;
import com.uniquindio.ecommerce.domain.catalogo.RegistroCustodia;
import com.uniquindio.ecommerce.domain.valueobject.identidad.AlmacenId;
import com.uniquindio.ecommerce.domain.valueobject.origenycalidad.Calidad;
import com.uniquindio.ecommerce.infraestructure.persistence.LoteRepositoryEnMemoria;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static com.uniquindio.ecommerce.LotesDePrueba.AHORA;
import static com.uniquindio.ecommerce.LotesDePrueba.lote;
import static com.uniquindio.ecommerce.LotesDePrueba.mangoTommy;
import static org.junit.jupiter.api.Assertions.*;

class ConsultarLoteServiceTest {

    private final LoteRepositoryEnMemoria lotes = new LoteRepositoryEnMemoria();
    private final ConsultarLoteService servicio = new ConsultarLoteService(lotes, Reloj.fijo(AHORA), evento -> { });

    private Lote publicado(Producto producto, Calidad calidad) {
        Lote lote = lote(producto, "100", calidad).build();
        lote.publicar(AHORA);
        return lotes.almacenar(lote);
    }

    @Test
    void elCompradorVeLosLotesDeVariosCampesinosDelMejorAlPeor() {
        // Arrange
        Producto mango = mangoTommy();
        Lote segunda = publicado(mango, Calidad.de(Calidad.Categoria.SEGUNDA, 10, false));
        Lote extra = publicado(mango, Calidad.de(Calidad.Categoria.EXTRA, 5, true));
        Lote primera = publicado(mango, Calidad.de(Calidad.Categoria.PRIMERA, 0, false));

        // Act
        List<VistaLote> catalogo = servicio.consultarDisponiblesDe(mango.id());

        // Assert
        assertEquals(List.of(extra.id(), primera.id(), segunda.id()), catalogo.stream().map(VistaLote::lote).toList());
        assertEquals(extra.id(), servicio.mejorLoteDe(mango.id()).orElseThrow().lote());
    }

    @Test
    void laTrazabilidadMuestraOrigenCalidadYCadaRegistroDeCustodia() {
        // Arrange
        Lote lote = publicado(mangoTommy(), Calidad.de(Calidad.Categoria.PRIMERA, 0, false));
        lote.ingresarAAlmacen(AlmacenId.nuevo(), "Acopio Calarca", "Operario", AHORA.plusSeconds(60));
        lote.registrarTemperatura(new BigDecimal("18"), "Operario", AHORA.plusSeconds(120));

        // Act
        VistaTrazabilidad trazabilidad = servicio.consultarTrazabilidad(lote.id());

        // Assert
        assertEquals(lote.fichaTrazabilidad(), trazabilidad.ficha());
        assertEquals(85, trazabilidad.calidadDeclarada().puntaje());
        assertEquals(List.of(RegistroCustodia.Tipo.COSECHA, RegistroCustodia.Tipo.INGRESO_ALMACEN,
                        RegistroCustodia.Tipo.CONTROL_TEMPERATURA),
                trazabilidad.custodia().stream().map(RegistroCustodia::tipo).toList());
    }
}
