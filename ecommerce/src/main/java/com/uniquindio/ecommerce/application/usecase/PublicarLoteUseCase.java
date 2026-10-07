package com.uniquindio.ecommerce.application.usecase;

import com.uniquindio.ecommerce.application.ServicioDeAplicacion;
import com.uniquindio.ecommerce.application.port.in.PublicarLoteUseCase;
import com.uniquindio.ecommerce.application.port.out.AutorizacionCampesinoPort;
import com.uniquindio.ecommerce.application.port.out.PublicadorEventos;
import com.uniquindio.ecommerce.application.port.out.Reloj;
import com.uniquindio.ecommerce.domain.catalogo.Lote;
import com.uniquindio.ecommerce.domain.exception.ReglaDeNegocioVioladaException;
import com.uniquindio.ecommerce.domain.repository.LoteRepository;
import com.uniquindio.ecommerce.domain.valueobject.identidad.CampesinoId;
import com.uniquindio.ecommerce.domain.valueobject.identidad.LoteId;

import java.util.Objects;
import java.util.Optional;

public class PublicarLoteService extends ServicioDeAplicacion implements PublicarLoteUseCase {

    private final AutorizacionCampesinoPort autorizacionCampesino;

    public PublicarLoteService(LoteRepository loteRepository,
                               Reloj reloj,
                               PublicadorEventos publicadorEventos,
                               AutorizacionCampesinoPort autorizacionCampesino) {
        super(loteRepository, reloj, publicadorEventos);
        this.autorizacionCampesino = Objects.requireNonNull(autorizacionCampesino,
                "El puerto de autorizacion de campesinos es obligatorio.");
    }

    @Override
    public void publicar(PublicarLoteUseCase.PublicarLoteCommand comando) {
        Objects.requireNonNull(comando, "El comando de publicacion es obligatorio.");
        Lote lote = cargar(comando.lote());
        exigirPropiedad(comando.solicitante(), lote);
        if (!autorizacionCampesino.estaAutorizadoParaPublicar(comando.solicitante())) {
            throw new ReglaDeNegocioVioladaException("R1",
                    "El campesino " + comando.solicitante()
                            + " no esta autorizado para publicar en la plataforma.");
        }
        lote.publicar(reloj.ahora());
        persistirYPublicar(lote);
    }

    @Override
    public void desactivar(DesactivarLoteCommand comando) {
        Objects.requireNonNull(comando, "El comando de desactivacion es obligatorio.");
        Lote lote = cargar(comando.lote());
        exigirPropiedad(comando.solicitante(), lote);
        lote.desactivar(comando.motivo(), reloj.ahora());
        persistirYPublicar(lote);
    }

    @Override
    public void reactivar(LoteId lote, CampesinoId solicitante) {
        Lote agregado = cargar(lote);
        exigirPropiedad(solicitante, agregado);
        agregado.reactivar(reloj.ahora());
        persistirYPublicar(agregado);
    }

    /**
     * Regla 5: el retiro definitivo falla si hay pedidos activos. La comprobacion
     * la hace el agregado; aqui solo se verifica la propiedad del lote.
     */
    @Override
    public void retirar(LoteId lote, CampesinoId solicitante) {
        Lote agregado = cargar(lote);
        exigirPropiedad(solicitante, agregado);
        agregado.retirar(reloj.ahora());
        persistirYPublicar(agregado);
    }

    @Override
    public Optional<String> diagnosticarPublicacion(LoteId lote) {
        return cargar(lote).motivoNoPublicable(reloj.ahora());
    }

    private void exigirPropiedad(CampesinoId solicitante, Lote lote) {
        if (!autorizacionCampesino.esResponsableDelLote(solicitante, lote.campesinoResponsable())) {
            throw new ReglaDeNegocioVioladaException("R5",
                    "El campesino " + solicitante + " no es responsable del lote " + lote.codigo() + ".");
        }
    }
}
