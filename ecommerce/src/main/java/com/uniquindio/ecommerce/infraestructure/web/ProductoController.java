package com.uniquindio.ecommerce.infraestructure.web;

import com.uniquindio.ecommerce.application.port.in.ConsultarCatalogoUseCase;
import com.uniquindio.ecommerce.domain.valueobject.identidad.ProductoId;
import com.uniquindio.ecommerce.infraestructure.web.dto.ProductoResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/productos")
@Tag(name = "Productos", description = "Catalogo de productos que pueden ofrecerse")
public class ProductoController {

    private final ConsultarCatalogoUseCase catalogo;

    public ProductoController(ConsultarCatalogoUseCase catalogo) {
        this.catalogo = catalogo;
    }

    @GetMapping
    @Operation(summary = "Productos activos", description = "Ordenados por nombre. Use el id para registrar o buscar lotes.")
    public List<ProductoResponse> listar() {
        return catalogo.productosActivos().stream().map(ProductoResponse::desde).toList();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Detalle de un producto")
    public ProductoResponse obtener(@PathVariable UUID id) {
        return ProductoResponse.desde(catalogo.producto(ProductoId.de(id)));
    }
}
