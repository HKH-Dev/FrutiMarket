package com.uniquindio.ecommerce.application.usecase;

import com.uniquindio.ecommerce.application.port.in.ControlarCalidadLoteUseCase;
import com.uniquindio.ecommerce.application.port.out.PublicadorEventos;
import com.uniquindio.ecommerce.application.port.out.Reloj;
import com.uniquindio.ecommerce.domain.catalogo.Lote;
import com.uniquindio.ecommerce.domain.repository.LoteRepository;
import com.uniquindio.ecommerce.domain.valueobject.catalogo.Calibre;
import com.uniquindio.ecommerce.domain.valueobject.catalogo.MedicionCalibre;
import com.uniquindio.ecommerce.domain.valueobject.identidad.LoteId;
import com.uniquindio.ecommerce.domain.valueobject.origenycalidad.SelloOrigen;

import java.util.Objects;

public class ControlarCalidadLoteService extends ServicioDeAplicacion
        implements ControlarCalidadLoteUseCase {

    public ControlarCalidadLoteService(LoteRepository loteRepository,
                                       Reloj reloj,
                                       PublicadorEventos publicadorEventos) {
        super(loteRepository, reloj, publicadorEventos);
    }

    @Override
    public void registrarHito(RegistrarHitoCommand comando) {
        Objects.requireNonNull(comando, "El comando de hito es obligatorio.");
        ejecutarSobre(comando.lote(), lote -> lote.registrarHitoDespacho(
                comando.tipo(), comando.ubicacion(), comando.temperaturaC(), reloj.ahora()));
    }

    @Override
    public void reportarRupturaCadenaFrio(LoteId lote, String motivo) {
        ejecutarSobre(lote, agregado -> agregado.registrarRupturaCadenaFrio(motivo, reloj.ahora()));
    }

    @Override
    public boolean verificarCalibre(LoteId lote, MedicionCalibre medicion) {
        Lote agregado = cargar(lote);
        boolean coincide = agregado.verificarCalibre(medicion, reloj.ahora());
        persistirYPublicar(agregado);
        return coincide;
    }

    @Override
    public void validarLoteEnRevision(LoteId lote, String responsable) {
        ejecutarSobre(lote, agregado -> agregado.validarRevision(responsable, reloj.ahora()));
    }

    @Override
    public void corregirCalibre(LoteId lote, Calibre calibreReal, String responsable) {
        ejecutarSobre(lote, agregado -> agregado.corregirCalibre(calibreReal, responsable, reloj.ahora()));
    }

    @Override
    public void renovarCertificacion(LoteId lote, SelloOrigen nuevoSello, String responsable) {
        ejecutarSobre(lote, agregado -> agregado.renovarSelloOrigen(nuevoSello, responsable, reloj.ahora()));
    }
}