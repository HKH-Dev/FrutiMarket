package com.uniquindio.ecommerce.application.usecase;

import com.uniquindio.ecommerce.application.port.in.ConsultarLoteUseCase;
import com.uniquindio.ecommerce.application.port.out.PublicadorEventos;
import com.uniquindio.ecommerce.application.port.out.Reloj;
import com.uniquindio.ecommerce.domain.catalogo.Lote;
import com.uniquindio.ecommerce.domain.repository.LoteRepository;
import com.uniquindio.ecommerce.domain.service.SeleccionLotes;
import com.uniquindio.ecommerce.domain.valueobject.identidad.AlmacenId;
import com.uniquindio.ecommerce.domain.valueobject.identidad.CampesinoId;
import com.uniquindio.ecommerce.domain.valueobject.identidad.LoteId;
import com.uniquindio.ecommerce.domain.valueobject.identidad.ProductoId;
import com.uniquindio.ecommerce.domain.valueobject.origenycalidad.TipoCultivo;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public class ConsultarLoteService extends ServicioDeAplicacion implements ConsultarLoteUseCase {

    public ConsultarLoteService(LoteRepository loteRepository, Reloj reloj, PublicadorEventos publicadorEventos) {
        super(loteRepository, reloj, publicadorEventos);
    }

    @Override
    public VistaTrazabilidad consultarTrazabilidad(LoteId loteId) {
        Lote lote = cargar(loteId);
        return new VistaTrazabilidad(
                lote.id(),
                lote.codigo(),
                lote.nombreProducto(),
                lote.fichaTrazabilidad(),
                lote.calidadDeclarada(),
                lote.calidadVerificada(),
                lote.fechaCosecha(),
                lote.fechaLimiteConsumo(),
                lote.diasParaVencer(reloj.ahora()),
                lote.custodia());
    }

    @Override
    public List<VistaLote> consultarDisponiblesDe(ProductoId producto) {
        Instant ahora = reloj.ahora();
        return aVistas(SeleccionLotes.mejoresPrimero(loteRepository.lotesDisponiblesDe(producto), ahora), ahora);
    }

    @Override
    public Optional<VistaLote> mejorLoteDe(ProductoId producto) {
        Instant ahora = reloj.ahora();
        return SeleccionLotes.mejorLote(loteRepository.lotesDisponiblesDe(producto), ahora)
                .map(lote -> aVista(lote, ahora));
    }

    @Override
    public List<VistaLote> consultarPorCampesino(CampesinoId campesino) {
        return aVistas(loteRepository.lotesDelCampesino(campesino), reloj.ahora());
    }

    @Override
    public List<VistaLote> consultarOrdenDeDespacho(AlmacenId almacen, TipoCultivo tipoCultivo) {
        Instant ahora = reloj.ahora();
        return aVistas(SeleccionLotes.ordenDeDespacho(loteRepository.lotesEnAlmacen(almacen, tipoCultivo), ahora), ahora);
    }

    private List<VistaLote> aVistas(List<Lote> lotes, Instant ahora) {
        return lotes.stream().map(lote -> aVista(lote, ahora)).toList();
    }

    private VistaLote aVista(Lote lote, Instant ahora) {
        return new VistaLote(lote.id(), lote.codigo(), lote.nombreProducto(), lote.campesinoResponsable(),
                lote.cantidadDisponible(), lote.precioFinca(), lote.puntajeCalidad(),
                lote.diasParaVencer(ahora), lote.estado());
    }
}
