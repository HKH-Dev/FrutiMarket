package com.uniquindio.ecommerce.domain.entity;

import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@AllArgsConstructor
public class FincaOrigen {
    private final UUID fincaId;
    private String nombre;
//    private Direccion ubicacion;
    private BigDecimal extensionHectareas;

    private void actualizarDato(){}
}
