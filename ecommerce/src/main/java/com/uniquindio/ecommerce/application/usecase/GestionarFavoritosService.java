package com.uniquindio.ecommerce.application.usecase;

import com.uniquindio.ecommerce.application.port.in.GestionarFavoritosUseCase;
import com.uniquindio.ecommerce.application.port.out.Reloj;
import com.uniquindio.ecommerce.domain.exception.RecursoNoEncontradoException;
import com.uniquindio.ecommerce.domain.repository.FavoritoRepository;
import com.uniquindio.ecommerce.domain.repository.ProductoRepository;
import com.uniquindio.ecommerce.domain.valueobject.identidad.CompradorId;
import com.uniquindio.ecommerce.domain.valueobject.identidad.ProductoId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
@Transactional
public class GestionarFavoritosService implements GestionarFavoritosUseCase {

    private final FavoritoRepository favoritos;
    private final ProductoRepository productos;
    private final Reloj reloj;

    public GestionarFavoritosService(FavoritoRepository favoritos, ProductoRepository productos, Reloj reloj) {
        this.favoritos = favoritos;
        this.productos = productos;
        this.reloj = reloj;
    }

    @Override
    public void agregar(CompradorId comprador, ProductoId producto) {
        productos.obtenerProducto(producto).orElseThrow(() -> new RecursoNoEncontradoException("Producto", producto));
        favoritos.agregar(comprador, producto, reloj.ahora());
    }

    @Override
    public void quitar(CompradorId comprador, ProductoId producto) {
        favoritos.quitar(comprador, producto);
    }

    @Override
    @Transactional(readOnly = true)
    public Set<ProductoId> favoritosDe(CompradorId comprador) {
        return favoritos.delComprador(comprador);
    }
}
