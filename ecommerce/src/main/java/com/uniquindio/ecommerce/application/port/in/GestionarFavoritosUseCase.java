package com.uniquindio.ecommerce.application.port.in;

import com.uniquindio.ecommerce.domain.valueobject.identidad.CompradorId;
import com.uniquindio.ecommerce.domain.valueobject.identidad.ProductoId;

import java.util.Set;

public interface GestionarFavoritosUseCase {

    void agregar(CompradorId comprador, ProductoId producto);

    void quitar(CompradorId comprador, ProductoId producto);

    Set<ProductoId> favoritosDe(CompradorId comprador);
}
