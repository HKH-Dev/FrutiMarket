package com.uniquindio.ecommerce.application.port.in;

import com.uniquindio.ecommerce.domain.comercial.DescuentoTemporada;
import com.uniquindio.ecommerce.domain.valueobject.identidad.ProductoId;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface GestionarDescuentosUseCase {

    DescuentoTemporada crear(String nombre, ProductoId producto, int porcentaje, LocalDate inicio, LocalDate fin);

    void desactivar(UUID id);

    List<DescuentoTemporada> todos();

    List<DescuentoTemporada> vigentes();
}
