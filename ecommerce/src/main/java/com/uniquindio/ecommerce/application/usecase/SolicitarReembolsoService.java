package com.uniquindio.ecommerce.application.usecase;

import com.uniquindio.ecommerce.application.dto.CompraResponse;
import com.uniquindio.ecommerce.application.port.in.SolicitarReembolsoUseCase;
import com.uniquindio.ecommerce.application.port.out.PublicadorEventos;
import com.uniquindio.ecommerce.application.port.out.Reloj;
import com.uniquindio.ecommerce.domain.entity.Compra;
import com.uniquindio.ecommerce.domain.event.EventoDominio;
import com.uniquindio.ecommerce.domain.exception.RecursoNoEncontradoException;
import com.uniquindio.ecommerce.domain.repository.CompraRepository;
import com.uniquindio.ecommerce.domain.valueobject.identidad.CompradorId;
import com.uniquindio.ecommerce.domain.valueobject.identidad.PedidoId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

/** Busca la compra, le pide al agregado que se reembolse (el aplica las reglas) y la guarda. */
@Service
@Transactional
public class SolicitarReembolsoService implements SolicitarReembolsoUseCase {

    private final CompraRepository compraRepository;
    private final Reloj reloj;
    private final PublicadorEventos publicadorEventos;

    public SolicitarReembolsoService(CompraRepository compraRepository, Reloj reloj, PublicadorEventos publicadorEventos) {
        this.compraRepository = compraRepository;
        this.reloj = reloj;
        this.publicadorEventos = publicadorEventos;
    }

    @Override
    public CompraResponse reembolsar(PedidoId compraId, CompradorId solicitante) {
        Instant ahora = reloj.ahora();
        Compra compra = compraRepository.obtenerCompra(compraId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Compra", compraId));
        compra.reembolsar(solicitante, ahora);
        compraRepository.registrar(compra);
        publicadorEventos.publicar(new EventoDominio(EventoDominio.Tipo.COMPRA_REEMBOLSADA,
                compra.getId().toString(), "Total $" + compra.total(), ahora));
        return CompraResponse.desde(compra);
    }
}
