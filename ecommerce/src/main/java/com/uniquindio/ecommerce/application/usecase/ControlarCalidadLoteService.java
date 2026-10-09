package com.uniquindio.ecommerce.application.usecase;

import com.uniquindio.ecommerce.application.port.in.ControlarCalidadLoteUseCase;
import com.uniquindio.ecommerce.application.port.out.PublicadorEventos;
import com.uniquindio.ecommerce.application.port.out.Reloj;
import com.uniquindio.ecommerce.domain.catalogo.Lote;
import com.uniquindio.ecommerce.domain.repository.LoteRepository;
import com.uniquindio.ecommerce.domain.valueobject.identidad.CampesinoId;
import com.uniquindio.ecommerce.domain.valueobject.identidad.LoteId;
import com.uniquindio.ecommerce.domain.valueobject.origenycalidad.Calidad;

import java.math.BigDecimal;

public class ControlarCalidadLoteService extends ServicioDeAplicacion implements ControlarCalidadLoteUseCase {

    public ControlarCalidadLoteService(LoteRepository loteRepository, Reloj reloj, PublicadorEventos publicadorEventos) {
        super(loteRepository, reloj, publicadorEventos);
    }

    @Override
    public boolean inspeccionarCalidad(LoteId loteId, Calidad observada, String inspector) {
        Lote lote = cargar(loteId);
        boolean coincide = lote.inspeccionarCalidad(observada, inspector, reloj.ahora());
        persistirYPublicar(lote);
        return coincide;
    }

    @Override
    public void registrarTemperatura(LoteId lote, BigDecimal temperaturaC, String responsable) {
        ejecutarSobre(lote, agregado -> agregado.registrarTemperatura(temperaturaC, responsable, reloj.ahora()));
    }

    @Override
    public void reportarIncidencia(LoteId lote, String detalle, String responsable) {
        ejecutarSobre(lote, agregado -> agregado.reportarIncidencia(detalle, responsable, reloj.ahora()));
    }

    @Override
    public void validarRevision(LoteId lote, String responsable) {
        ejecutarSobre(lote, agregado -> agregado.validarRevision(responsable, reloj.ahora()));
    }

    @Override
    public void corregirCalidadDeclarada(LoteId loteId, CampesinoId solicitante, Calidad calidadReal) {
        Lote lote = cargarComoResponsable(loteId, solicitante);
        lote.corregirCalidadDeclarada(calidadReal, "Campesino " + solicitante, reloj.ahora());
        persistirYPublicar(lote);
    }
}
