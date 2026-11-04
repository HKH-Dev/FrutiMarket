package com.uniquindio.ecommerce.application.usecase;

import com.uniquindio.ecommerce.application.port.in.PublicarLoteUseCase;
import com.uniquindio.ecommerce.application.port.out.PublicadorEventos;
import com.uniquindio.ecommerce.application.port.out.Reloj;
import com.uniquindio.ecommerce.domain.exception.RecursoNoEncontradoException;
import com.uniquindio.ecommerce.domain.catalogo.Lote;
import com.uniquindio.ecommerce.domain.entity.Campesino;
import com.uniquindio.ecommerce.domain.repository.CampesinoRepository;
import com.uniquindio.ecommerce.domain.repository.LoteRepository;
import com.uniquindio.ecommerce.domain.valueobject.identidad.CampesinoId;
import com.uniquindio.ecommerce.domain.valueobject.identidad.LoteId;

import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
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
        campesinoRepository.obtenerCampesino(solicitante)
                .orElseThrow(() -> new RecursoNoEncontradoException("Campesino", solicitante))
                .exigirAutorizacionParaPublicar();
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
