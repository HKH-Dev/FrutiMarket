package com.uniquindio.ecommerce.application.usecase;

import com.uniquindio.ecommerce.domain.entity.Compra;
import com.uniquindio.ecommerce.domain.repository.CompraRepository;

public class RealizarCompraUseCase {

    private final CompraRepository repository;

    public RealizarCompraUseCase(CompraRepository repository){
        this.repository = repository;

    }

//    public Compra ejecutar(String id, String modeloId, String compradorId, Precio precioActual){
//        Compra compra = Compra.realizar(id, modeloId, compradorId, precioActual);
//        repository.guardar(compra);
//        return compra;
//    }

}


