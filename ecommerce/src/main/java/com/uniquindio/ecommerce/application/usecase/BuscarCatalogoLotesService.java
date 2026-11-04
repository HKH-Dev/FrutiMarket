package com.uniquindio.ecommerce.application.usecase;

import com.uniquindio.ecommerce.application.port.in.BuscarCatalogoLotesUseCase;
import com.uniquindio.ecommerce.application.port.in.ConsultarLoteUseCase.VistaLote;
import com.uniquindio.ecommerce.application.port.out.Reloj;
import com.uniquindio.ecommerce.domain.catalogo.Lote;
import com.uniquindio.ecommerce.domain.exception.ReglaDeNegocioVioladaException;
import com.uniquindio.ecommerce.domain.repository.LoteRepository;
import com.uniquindio.ecommerce.domain.repository.Pagina;
import com.uniquindio.ecommerce.domain.valueobject.origenycalidad.TipoCultivo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

/**
 * Buscar en el catalogo. Solo valida la entrada (pagina y tamano) y delega la busqueda al repositorio;
 * filtrar, ordenar, cortar la pagina y contar lo hace la base de datos.
 */
@Service
@Transactional(readOnly = true)
public class BuscarCatalogoLotesService implements BuscarCatalogoLotesUseCase {

    public static final int TAMANO_MAXIMO = 50;

    private final LoteRepository loteRepository;
    private final Reloj reloj;

    public BuscarCatalogoLotesService(LoteRepository loteRepository, Reloj reloj) {
        this.loteRepository = loteRepository;
        this.reloj = reloj;
    }

    @Override
    public Pagina<VistaLote> buscar(String texto, TipoCultivo tipoCultivo, int pagina, int tamano) {
        // Validar la entrada si puede estar aqui: no es una regla del negocio.
        ReglaDeNegocioVioladaException.validar(pagina >= 0, "INV-PAGINA", "La pagina no puede ser negativa.");
        ReglaDeNegocioVioladaException.validar(tamano >= 1 && tamano <= TAMANO_MAXIMO, "INV-PAGINA",
                "El tamano de pagina debe estar entre 1 y " + TAMANO_MAXIMO + ".");
        Instant ahora = reloj.ahora();
        return loteRepository.buscarEnCatalogo(texto, tipoCultivo, pagina, tamano)
                .mapear(lote -> aVista(lote, ahora));
    }

    private static VistaLote aVista(Lote lote, Instant ahora) {
        return new VistaLote(lote.id(), lote.codigo(), lote.nombreProducto(), lote.campesinoResponsable(),
                lote.cantidadDisponible(), lote.precioFinca(), lote.puntajeCalidad(),
                lote.diasParaVencer(ahora), lote.estado());
    }
}