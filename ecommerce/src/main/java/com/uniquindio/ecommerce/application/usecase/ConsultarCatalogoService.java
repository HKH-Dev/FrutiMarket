package com.uniquindio.ecommerce.application.usecase;

import com.uniquindio.ecommerce.application.port.in.ConsultarCatalogoUseCase;
import com.uniquindio.ecommerce.application.port.out.Reloj;
import com.uniquindio.ecommerce.domain.catalogo.Producto;
import com.uniquindio.ecommerce.domain.exception.RecursoNoEncontradoException;
import com.uniquindio.ecommerce.domain.repository.ProductoRepository;
import com.uniquindio.ecommerce.domain.valueobject.identidad.ProductoId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class ConsultarCatalogoService implements ConsultarCatalogoUseCase {

    private static final ZoneId ZONA_OPERACION = ZoneId.of("America/Bogota");

    private final ProductoRepository productoRepository;
    private final Reloj reloj;

    public ConsultarCatalogoService(ProductoRepository productoRepository, Reloj reloj) {
        this.productoRepository = productoRepository;
        this.reloj = reloj;
    }

    @Override
    public List<VistaProducto> productosActivos() {
        LocalDate hoy = hoy();
        return productoRepository.productosActivos().stream()
                .sorted(Comparator.comparing(Producto::nombre, String.CASE_INSENSITIVE_ORDER))
                .map(producto -> vista(producto, hoy))
                .toList();
    }

    @Override
    public VistaProducto producto(ProductoId id) {
        return productoRepository.obtenerProducto(id)
                .map(producto -> vista(producto, hoy()))
                .orElseThrow(() -> new RecursoNoEncontradoException("Producto", id));
    }

    private LocalDate hoy() {
        return LocalDate.ofInstant(reloj.ahora(), ZONA_OPERACION);
    }

    private static VistaProducto vista(Producto producto, LocalDate hoy) {
        return new VistaProducto(producto.id(), producto.nombre(), producto.linea(), producto.tipoCultivo(),
                producto.unidadVenta(), producto.conservacion(), producto.vidaUtilDias(),
                producto.temporada().toString(), producto.estaEnTemporada(hoy));
    }
}
