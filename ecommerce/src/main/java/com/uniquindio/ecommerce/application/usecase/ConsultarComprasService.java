package com.uniquindio.ecommerce.application.usecase;

import com.uniquindio.ecommerce.application.dto.CompraResponse;
import com.uniquindio.ecommerce.application.port.in.ConsultarComprasUseCase;
import com.uniquindio.ecommerce.domain.repository.CompraRepository;
import com.uniquindio.ecommerce.domain.valueobject.identidad.CompradorId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class ConsultarComprasService implements ConsultarComprasUseCase {

    private final CompraRepository compraRepository;

    public ConsultarComprasService(CompraRepository compraRepository) {
        this.compraRepository = compraRepository;
    }

    @Override
    public List<CompraResponse> comprasDe(CompradorId comprador) {
        return compraRepository.comprasDelComprador(comprador).stream().map(CompraResponse::desde).toList();
    }
}
