package com.uniquindio.ecommerce;

import com.uniquindio.ecommerce.domain.catalogo.Lote;
import com.uniquindio.ecommerce.domain.catalogo.Producto;
import com.uniquindio.ecommerce.domain.entity.PuntoAlmacenamiento;
import com.uniquindio.ecommerce.domain.valueobject.catalogo.Cantidad;
import com.uniquindio.ecommerce.domain.valueobject.catalogo.PrecioFinca;
import com.uniquindio.ecommerce.domain.valueobject.catalogo.UnidadMedida;
import com.uniquindio.ecommerce.domain.valueobject.identidad.CampesinoId;
import com.uniquindio.ecommerce.domain.valueobject.logistica.Conservacion;
import com.uniquindio.ecommerce.domain.valueobject.origenycalidad.Calidad;
import com.uniquindio.ecommerce.domain.valueobject.origenycalidad.FichaTrazabilidad;
import com.uniquindio.ecommerce.domain.valueobject.origenycalidad.TecnicaProduccion;
import com.uniquindio.ecommerce.domain.valueobject.origenycalidad.TipoCultivo;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

/** Datos de prueba compartidos, a una fecha fija. */
public final class LotesDePrueba {

    public static final Instant AHORA = Instant.parse("2026-09-15T15:00:00Z");
    public static final LocalDate COSECHA = LocalDate.of(2026, 9, 10);

    private LotesDePrueba() {
    }

    public static Cantidad kilos(String valor) {
        return Cantidad.de(valor, UnidadMedida.KILOGRAMO);
    }

    public static Calidad primera() {
        return Calidad.de(Calidad.Categoria.PRIMERA, 5, false);
    }

    public static Producto mangoTommy() {
        return Producto.builder().nombre("Mango Tommy").tipoCultivo(TipoCultivo.FRUTA)
                .unidadVenta(UnidadMedida.KILOGRAMO).vidaUtilDias(30).conservacion(Conservacion.AMBIENTE).build();
    }

    public static Producto moraRefrigerada() {
        return Producto.builder().nombre("Mora de castilla").tipoCultivo(TipoCultivo.FRUTA)
                .unidadVenta(UnidadMedida.KILOGRAMO).vidaUtilDias(10).conservacion(Conservacion.REFRIGERADO).build();
    }

    /** Lote en borrador de un campesino nuevo, con ficha de trazabilidad y precio. */
    public static Lote.Builder lote(Producto producto, String kilosIniciales, Calidad calidad) {
        CampesinoId campesino = CampesinoId.nuevo();
        return Lote.builder()
                .codigo("LOT-" + UUID.randomUUID().toString().substring(0, 6))
                .producto(producto)
                .campesino(campesino)
                .fichaTrazabilidad(FichaTrazabilidad.registrar(
                        campesino, UUID.randomUUID(), TecnicaProduccion.AGROECOLOGICA, COSECHA))
                .calidad(calidad)
                .cantidadInicial(kilos(kilosIniciales))
                .precioFinca(PrecioFinca.de("3500", UnidadMedida.KILOGRAMO))
                .fechaCosecha(COSECHA)
                .momentoRegistro(AHORA);
    }

    public static Lote.Builder mango(String kilosIniciales) {
        return lote(mangoTommy(), kilosIniciales, primera());
    }

    public static Lote mangoPublicado(String kilosIniciales) {
        Lote lote = mango(kilosIniciales).build();
        lote.publicar(AHORA);
        return lote;
    }

    public static PuntoAlmacenamiento almacen(String nombre, PuntoAlmacenamiento.Tipo tipo, int capacidad,
                                              String... municipiosCobertura) {
        return PuntoAlmacenamiento.builder()
                .nombre(nombre).tipo(tipo).municipio("Armenia").capacidadLotes(capacidad)
                .cultivosHabilitados(Set.of(TipoCultivo.FRUTA))
                .conservacionesDisponibles(Set.of(Conservacion.AMBIENTE, Conservacion.REFRIGERADO))
                .municipiosCobertura(Set.of(municipiosCobertura))
                .build();
    }
}
