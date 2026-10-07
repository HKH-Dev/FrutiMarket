package com.uniquindio.ecommerce.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor


public class Campesino {
    private final UUID campesinoId;
    private final String numeroIdentificacion;
    private double escalaProduccion;

    public Campesino(UUID campesinoId, String numeroIdentificacion) {
        this.campesinoId = campesinoId;
        this.numeroIdentificacion = numeroIdentificacion;
    }


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
