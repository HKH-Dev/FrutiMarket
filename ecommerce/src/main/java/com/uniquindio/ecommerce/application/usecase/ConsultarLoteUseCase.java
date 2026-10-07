package com.uniquindio.ecommerce.application.usecase;

import com.uniquindio.ecommerce.domain.catalogo.Lote;
import com.uniquindio.ecommerce.domain.exception.RecursoNoEncontradoException;
import com.uniquindio.ecommerce.domain.repository.LoteRepository;

import java.util.List;

public class ConsultarLoteUseCase {

    private final LoteRepository loteRepository;

    public ConsultarLoteUseCase(LoteRepository loteRepository) {
        this.loteRepository = loteRepository;
    }

    public Lote ejecutar(String loteId) {
        return loteRepository.buscarPorId(loteId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Lote", loteId));
    }

    public List<Lote> consultarTodos() {
        return loteRepository.buscarTodos();
    }
}
