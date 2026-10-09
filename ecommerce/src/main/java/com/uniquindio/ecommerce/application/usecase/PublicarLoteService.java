package com.uniquindio.ecommerce.application.usecase;

import com.uniquindio.ecommerce.application.port.in.PublicarLoteUseCase;
import com.uniquindio.ecommerce.application.port.out.PublicadorEventos;
import com.uniquindio.ecommerce.application.port.out.Reloj;
import com.uniquindio.ecommerce.domain.catalogo.Lote;
import com.uniquindio.ecommerce.domain.entity.Campesino;
import com.uniquindio.ecommerce.domain.exception.ReglaDeNegocioVioladaException;
import com.uniquindio.ecommerce.domain.repository.CampesinoRepository;
import com.uniquindio.ecommerce.domain.repository.LoteRepository;
import com.uniquindio.ecommerce.domain.valueobject.identidad.CampesinoId;
import com.uniquindio.ecommerce.domain.valueobject.identidad.LoteId;

import java.util.Optional;

public class PublicarLoteService extends ServicioDeAplicacion implements PublicarLoteUseCase {

    private final CampesinoRepository campesinoRepository;

    public PublicarLoteService(LoteRepository loteRepository, Reloj reloj, PublicadorEventos publicadorEventos,
                               CampesinoRepository campesinoRepository) {
        super(loteRepository, reloj, publicadorEventos);
        this.campesinoRepository = campesinoRepository;
    }

    @Override
    public void publicar(LoteId loteId, CampesinoId solicitante) {
        Lote lote = cargarComoResponsable(loteId, solicitante);
        boolean autorizado = campesinoRepository.obtenerCampesino(solicitante)
                .map(Campesino::estaAutorizadoParaPublicar).orElse(false);
        ReglaDeNegocioVioladaException.validar(autorizado, "R1",
                "El campesino " + solicitante + " no esta autorizado para publicar en la plataforma.");
        lote.publicar(reloj.ahora());
        persistirYPublicar(lote);
    }

    @Override
    public void desactivar(LoteId loteId, CampesinoId solicitante, String motivo) {
        Lote lote = cargarComoResponsable(loteId, solicitante);
        lote.desactivar(motivo);
        persistirYPublicar(lote);
    }

    @Override
    public void reactivar(LoteId loteId, CampesinoId solicitante) {
        Lote lote = cargarComoResponsable(loteId, solicitante);
        lote.reactivar(reloj.ahora());
        persistirYPublicar(lote);
    }

    @Override
    public void retirar(LoteId loteId, CampesinoId solicitante) {
        Lote lote = cargarComoResponsable(loteId, solicitante);
        lote.retirar();
        persistirYPublicar(lote);
    }

    @Override
    public Optional<String> diagnosticarPublicacion(LoteId loteId) {
        return cargar(loteId).motivoNoPublicable(reloj.ahora());
    }
}
