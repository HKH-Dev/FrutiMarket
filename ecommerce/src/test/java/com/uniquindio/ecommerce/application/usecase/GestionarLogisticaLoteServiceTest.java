package com.uniquindio.ecommerce.application.usecase;

import com.uniquindio.ecommerce.application.port.out.Reloj;
import com.uniquindio.ecommerce.domain.catalogo.EstadoLote;
import com.uniquindio.ecommerce.domain.catalogo.Lote;
import com.uniquindio.ecommerce.domain.catalogo.Producto;
import com.uniquindio.ecommerce.domain.catalogo.RegistroCustodia;
import com.uniquindio.ecommerce.domain.entity.Comprador;
import com.uniquindio.ecommerce.domain.entity.PuntoAlmacenamiento;
import com.uniquindio.ecommerce.domain.exception.ReglaDeNegocioVioladaException;
import com.uniquindio.ecommerce.domain.valueobject.catalogo.UnidadMedida;
import com.uniquindio.ecommerce.domain.valueobject.identidad.PedidoId;
import com.uniquindio.ecommerce.domain.valueobject.logistica.Conservacion;
import com.uniquindio.ecommerce.domain.valueobject.origenycalidad.TipoCultivo;
import com.uniquindio.ecommerce.infraestructure.persistence.CompradorRepositoryEnMemoria;
import com.uniquindio.ecommerce.infraestructure.persistence.LoteRepositoryEnMemoria;
import com.uniquindio.ecommerce.infraestructure.persistence.PuntoAlmacenamientoRepositoryEnMemoria;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;
import java.util.Set;

import static com.uniquindio.ecommerce.LotesDePrueba.AHORA;
import static com.uniquindio.ecommerce.LotesDePrueba.almacen;
import static com.uniquindio.ecommerce.LotesDePrueba.kilos;
import static com.uniquindio.ecommerce.LotesDePrueba.lote;
import static com.uniquindio.ecommerce.LotesDePrueba.mangoPublicado;
import static com.uniquindio.ecommerce.LotesDePrueba.primera;
import static org.junit.jupiter.api.Assertions.*;

class GestionarLogisticaLoteServiceTest {

    private final LoteRepositoryEnMemoria lotes = new LoteRepositoryEnMemoria();
    private final PuntoAlmacenamientoRepositoryEnMemoria almacenes = new PuntoAlmacenamientoRepositoryEnMemoria();
    private final CompradorRepositoryEnMemoria compradores = new CompradorRepositoryEnMemoria();
    private final GestionarLogisticaLoteService servicio =
            new GestionarLogisticaLoteService(lotes, Reloj.fijo(AHORA), evento -> { }, almacenes, compradores);

    private PuntoAlmacenamiento acopio;
    private PuntoAlmacenamiento centro;
    private Comprador tienda;

    @BeforeEach
    void setUp() {
        acopio = almacen("Acopio Calarca", PuntoAlmacenamiento.Tipo.PUNTO_ACOPIO, 10);
        centro = almacen("Centro Armenia", PuntoAlmacenamiento.Tipo.CENTRO_REDISTRIBUCION, 10, "Circasia");
        almacenes.registrar(acopio);
        almacenes.registrar(centro);
        tienda = Comprador.builder().nombre("Fruver Circasia").tipo(Comprador.Tipo.TIENDA).municipio("Circasia").build();
        compradores.registrar(tienda);
    }

    private Lote loteVendidoListoParaMover(Producto producto) {
        Lote lote = lote(producto, "50", primera()).build();
        lote.publicar(AHORA);
        lote.reservar(PedidoId.nuevo(), kilos("50"), AHORA);
        return lotes.almacenar(lote);
    }

    @Test
    void elRecorridoFincaAcopioCentroCompradorQuedaEnLaCadenaDeCustodia() {
        // Arrange
        Lote lote = lotes.almacenar(mangoPublicado("50"));

        // Act
        servicio.ingresarAAlmacen(lote.id(), acopio.id(), "Operario acopio");
        servicio.despacharAAlmacen(lote.id(), centro.id(), Duration.ofHours(4), "Transportador");
        servicio.ingresarAAlmacen(lote.id(), centro.id(), "Operario centro");
        servicio.despacharAComprador(lote.id(), tienda.getId(), Duration.ofHours(2), "Domiciliario");
        servicio.confirmarEntrega(lote.id(), "Dueno del fruver", "Domiciliario");

        // Assert
        List<RegistroCustodia.Tipo> recorrido = lote.custodia().stream().map(RegistroCustodia::tipo).toList();
        assertEquals(List.of(
                RegistroCustodia.Tipo.COSECHA,
                RegistroCustodia.Tipo.INGRESO_ALMACEN, RegistroCustodia.Tipo.SALIDA_ALMACEN,
                RegistroCustodia.Tipo.INGRESO_ALMACEN, RegistroCustodia.Tipo.SALIDA_ALMACEN,
                RegistroCustodia.Tipo.ENTREGA), recorrido);
        assertEquals(EstadoLote.ENTREGADO, lote.estado());
        assertTrue(acopio.lotesAlmacenados().isEmpty() && centro.lotesAlmacenados().isEmpty());
    }

    @Test
    void unAlmacenQueNoGuardaRefrigeradosRechazaElLoteYNingunoCambia() {
        // Arrange
        PuntoAlmacenamiento sinFrio = PuntoAlmacenamiento.builder().nombre("Bodega rural")
                .tipo(PuntoAlmacenamiento.Tipo.PUNTO_ACOPIO).municipio("Calarca").capacidadLotes(5)
                .cultivosHabilitados(Set.of(TipoCultivo.FRUTA))
                .conservacionesDisponibles(Set.of(Conservacion.AMBIENTE)).build();
        almacenes.registrar(sinFrio);
        Producto mora = Producto.builder().nombre("Mora").tipoCultivo(TipoCultivo.FRUTA)
                .unidadVenta(UnidadMedida.KILOGRAMO).vidaUtilDias(10).conservacion(Conservacion.REFRIGERADO).build();
        Lote lote = loteVendidoListoParaMover(mora);

        // Act
        ReglaDeNegocioVioladaException excepcion = assertThrows(ReglaDeNegocioVioladaException.class,
                () -> servicio.ingresarAAlmacen(lote.id(), sinFrio.id(), "Operario"));

        // Assert
        assertEquals("R9", excepcion.codigoRegla());
        assertTrue(sinFrio.lotesAlmacenados().isEmpty());
        assertEquals(EstadoLote.AGOTADO, lote.estado());
    }

    @Test
    void noSeDespachaUnLoteSiOtroDelMismoCultivoEnElAlmacenVenceAntes() {
        // Arrange
        Producto mangoDuradero = Producto.builder().nombre("Mango").tipoCultivo(TipoCultivo.FRUTA)
                .unidadVenta(UnidadMedida.KILOGRAMO).vidaUtilDias(30).build();
        Producto guayabaDelicada = Producto.builder().nombre("Guayaba").tipoCultivo(TipoCultivo.FRUTA)
                .unidadVenta(UnidadMedida.KILOGRAMO).vidaUtilDias(10).build();
        Lote venceMasTarde = loteVendidoListoParaMover(mangoDuradero);
        Lote venceAntes = loteVendidoListoParaMover(guayabaDelicada);
        servicio.ingresarAAlmacen(venceMasTarde.id(), acopio.id(), "Operario");
        servicio.ingresarAAlmacen(venceAntes.id(), acopio.id(), "Operario");

        // Act
        ReglaDeNegocioVioladaException excepcion = assertThrows(ReglaDeNegocioVioladaException.class,
                () -> servicio.despacharAAlmacen(venceMasTarde.id(), centro.id(), Duration.ofHours(4), "Transportador"));

        // Assert
        assertEquals("R16", excepcion.codigoRegla());
        assertEquals(EstadoLote.EN_ACOPIO, venceMasTarde.estado());
        assertTrue(acopio.guarda(venceMasTarde.id()));
    }

    @Test
    void noSeDespachaAUnCompradorFueraDeLaCoberturaDelAlmacen() {
        // Arrange
        Lote lote = lotes.almacenar(mangoPublicado("50"));
        servicio.ingresarAAlmacen(lote.id(), acopio.id(), "Operario");
        Comprador lejano = Comprador.builder().nombre("Cliente Pereira").municipio("Pereira").build();
        compradores.registrar(lejano);

        // Act
        ReglaDeNegocioVioladaException excepcion = assertThrows(ReglaDeNegocioVioladaException.class,
                () -> servicio.despacharAComprador(lote.id(), lejano.getId(), Duration.ofHours(3), "Domiciliario"));

        // Assert
        assertEquals("R15", excepcion.codigoRegla());
        assertEquals(EstadoLote.EN_ACOPIO, lote.estado());
    }
}
