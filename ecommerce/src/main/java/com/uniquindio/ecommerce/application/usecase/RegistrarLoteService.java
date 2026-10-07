package com.uniquindio.ecommerce.application.usecase;

import com.uniquindio.ecommerce.application.port.in.RegistrarLoteUseCase;
import com.uniquindio.ecommerce.application.port.out.AutorizacionCampesinoPort;
import com.uniquindio.ecommerce.application.port.out.PublicadorEventos;
import com.uniquindio.ecommerce.application.port.out.Reloj;
import com.uniquindio.ecommerce.domain.catalogo.Lote;
import com.uniquindio.ecommerce.domain.exception.ReglaDeNegocioVioladaException;
import com.uniquindio.ecommerce.domain.repository.LoteRepository;
import com.uniquindio.ecommerce.domain.valueobject.identidad.CampesinoId;
import com.uniquindio.ecommerce.domain.valueobject.identidad.LoteId;

import java.util.Objects;

/**
 * Implementacion del caso de uso <b>Registrar lote</b>.
 *
 * <p>Reparto de responsabilidades, que es lo que hace util a esta capa:</p>
 * <ul>
 *   <li><b>Regla 1</b> (campesino autorizado) se resuelve aqui, porque exige
 *       consultar otro agregado.</li>
 *   <li><b>Unicidad del codigo</b> se resuelve aqui, porque exige mirar toda la
 *       coleccion de lotes y ningun lote puede saber si otro comparte su codigo.</li>
 *   <li><b>Reglas 2 y 8</b> y todas las invariantes estructurales se resuelven en
 *       la factoria del agregado, no aqui. El servicio no repite esas validaciones.</li>
 * </ul>
 */
public class RegistrarLoteService extends ServicioDeAplicacion implements RegistrarLoteUseCase {

    private final AutorizacionCampesinoPort autorizacionCampesino;

    public RegistrarLoteService(LoteRepository loteRepository, Reloj reloj, PublicadorEventos publicadorEventos, AutorizacionCampesinoPort autorizacionCampesino) {
        super(loteRepository, reloj, publicadorEventos);
        this.autorizacionCampesino = Objects.requireNonNull(autorizacionCampesino,
                "El puerto de autorizacion de campesinos es obligatorio.");
    }

    @Override
    public LoteId registrarMateriaPrima(RegistrarMateriaPrimaCommand comando) {
        Objects.requireNonNull(comando, "El comando de registro es obligatorio.");
        exigirCampesinoAutorizado(comando.campesino());
        exigirCodigoDisponible(comando.codigo());

        Lote lote = Lote.registrarMateriaPrima(
                comando.codigo(),
                comando.producto(),
                comando.campesino(),
                comando.fichaTrazabilidad(),
                comando.tipoCultivo(),
                comando.cantidadInicial(),
                comando.precioFinca(),
                comando.calibre(),
                comando.temporada(),
                comando.fechaCosecha(),
                comando.vidaUtilDias(),
                comando.merma(),
                reloj.ahora());

        return persistirYPublicar(lote).id();
    }

    @Override
    public LoteId registrarTransformado(RegistrarTransformadoCommand comando) {
        Objects.requireNonNull(comando, "El comando de registro es obligatorio.");
        exigirCampesinoAutorizado(comando.campesino());
        exigirCodigoDisponible(comando.codigo());

        Lote lote = Lote.registrarTransformado(
                comando.codigo(),
                comando.producto(),
                comando.campesino(),
                comando.fichaTrazabilidad(),
                comando.tipoCultivo(),
                comando.cantidadInicial(),
                comando.precioFinca(),
                comando.fichaTransformacion(),
                comando.merma(),
                reloj.ahora());

        return persistirYPublicar(lote).id();
    }

    /** Regla 1: solo un campesino registrado y autorizado puede publicar en la plataforma. */
    private void exigirCampesinoAutorizado(CampesinoId campesino) {
        if (!autorizacionCampesino.estaAutorizadoParaPublicar(campesino)) {
            throw new ReglaDeNegocioVioladaException("R1",
                    "El campesino " + campesino + " no esta registrado y autorizado para publicar en la plataforma.");
        }
    }

    private void exigirCodigoDisponible(String codigo) {
        if (codigo != null && loteRepository.existeCodigo(codigo.trim())) {
            throw new ReglaDeNegocioVioladaException("INV-CODIGO",
                    "Ya existe un lote con el codigo '" + codigo.trim() + "'.");
        }
    }
}
