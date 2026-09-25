package com.uniquindio.ecommerce.domain.entity;

import lombok.AllArgsConstructor;

import java.util.List;
import java.util.UUID;

@AllArgsConstructor

public class Campesino {
    private final UUID campesinoId;
    private final String numeroIdentificacion;
    private double escalaProduccion;

    private void publicarProducto(){

    }

    private  void actualizarProducto(){}

    private  void eliminarProducto(){}

    private List<Compra> consultarVentas(){
        return null;
    }

    private double obtenerCalificacionPromedio(){
        return 0;
    }

    private void responderComentario(){}




}
