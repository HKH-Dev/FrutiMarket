package com.uniquindio.ecommerce.domain.entity;

import com.uniquindio.ecommerce.domain.valueobject.CategoriaCalibre;
import com.uniquindio.ecommerce.domain.valueobject.EstadoLote;
import com.uniquindio.ecommerce.domain.valueobject.PrecioFInca;
import com.uniquindio.ecommerce.domain.valueobject.TipoCultivo;
import lombok.AllArgsConstructor;

import java.util.UUID;

@AllArgsConstructor
public class Producto {
    private final UUID productoId;
    private String titulo;
    private String descripcion;
    private PrecioFInca precio;
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
