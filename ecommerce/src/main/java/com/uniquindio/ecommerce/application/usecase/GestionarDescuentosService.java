package com.uniquindio.ecommerce.application.usecase;

import com.uniquindio.ecommerce.application.port.in.GestionarDescuentosUseCase;
import com.uniquindio.ecommerce.application.port.out.Reloj;
import com.uniquindio.ecommerce.domain.comercial.DescuentoTemporada;
import com.uniquindio.ecommerce.domain.exception.RecursoNoEncontradoException;
import com.uniquindio.ecommerce.domain.repository.DescuentoTemporadaRepository;
import com.uniquindio.ecommerce.domain.repository.ProductoRepository;
import com.uniquindio.ecommerce.domain.valueobject.identidad.ProductoId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class GestionarDescuentosService implements GestionarDescuentosUseCase {

    private final DescuentoTemporadaRepository descuentos;
    private final ProductoRepository productos;
    private final Reloj reloj;

    public GestionarDescuentosService(DescuentoTemporadaRepository descuentos, ProductoRepository productos, Reloj reloj) {
        this.descuentos = descuentos;
        this.productos = productos;
        this.reloj = reloj;
    }

    @Override
    public DescuentoTemporada crear(String nombre, ProductoId producto, int porcentaje, LocalDate inicio, LocalDate fin) {
        if (producto != null) {
            productos.obtenerProducto(producto).orElseThrow(() -> new RecursoNoEncontradoException("Producto", producto));
        }
        DescuentoTemporada descuento = DescuentoTemporada.crear(nombre, producto, porcentaje, inicio, fin);
        descuentos.guardar(descuento);
        return descuento;
    }

    @Override
    public void desactivar(UUID id) {
        DescuentoTemporada descuento = descuentos.obtener(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Campana", id));
        descuento.desactivar();
        descuentos.guardar(descuento);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DescuentoTemporada> todos() {
        return descuentos.todos();
    }

    @Override
    @Transactional(readOnly = true)
    public List<DescuentoTemporada> vigentes() {
        LocalDate hoy = LocalDate.ofInstant(reloj.ahora(), ZoneId.systemDefault());
        return descuentos.todos().stream().filter(d -> d.vigenteEl(hoy)).toList();
    }
}
