package com.uniquindio.ecommerce.domain.entity;

import com.uniquindio.ecommerce.domain.catalogo.EstadoLote;
import com.uniquindio.ecommerce.domain.valueobject.catalogo.CategoriaCalibre;
import com.uniquindio.ecommerce.domain.valueobject.catalogo.PrecioFinca;
import com.uniquindio.ecommerce.domain.valueobject.origenycalidad.TipoCultivo;
import lombok.AllArgsConstructor;

import java.util.UUID;

@AllArgsConstructor
public class Producto {
    private final UUID productoId;
    private String titulo;
    private String descripcion;
    private PrecioFinca precio;
    private CategoriaCalibre categoria;
    private TipoCultivo tipo;
    private EstadoLote estado;

    private void publicar(){}

    private void actualizar(){}
    private void eliminar(){}
    private void restaurar(){}
    private void cambiarPrecio(){}
    private boolean estaDisponible(){return false;}
    private boolean puedeVender(){return false;}
    private double obenerCalficacionPromedio(){return 0;}

}
