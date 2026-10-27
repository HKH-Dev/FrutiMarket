package com.uniquindio.ecommerce.infraestructure.web.dto;

import com.uniquindio.ecommerce.application.port.in.ConsultarCatalogoUseCase.VistaProducto;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

/**
 * Producto del catalogo.
 *
 * @param unidadVenta valor a enviar en {@code unidadMedida} al registrar un lote (p. ej. KILOGRAMO).
 * @param simboloUnidad para mostrar al usuario (p. ej. "kg").
 */
public record ProductoResponse(UUID id, String nombre, String linea, String tipoCultivo,
                               @Schema(example = "KILOGRAMO") String unidadVenta,
                               @Schema(example = "kg") String simboloUnidad,
                               String conservacion, int vidaUtilDias, String temporada, boolean enTemporada) {

    public static ProductoResponse desde(VistaProducto vista) {
        return new ProductoResponse(vista.id().valor(), vista.nombre(), vista.linea().name(),
                vista.tipoCultivo().name(), vista.unidadVenta().name(), vista.unidadVenta().simbolo(),
                vista.conservacion().name(), vista.vidaUtilDias(), vista.temporada(), vista.enTemporada());
    }
}
