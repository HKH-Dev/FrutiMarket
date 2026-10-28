package com.uniquindio.ecommerce.infraestructure.web;

import com.uniquindio.ecommerce.application.port.in.ConsultarReportesUseCase;
import com.uniquindio.ecommerce.domain.repository.ReporteVentasRepository.ProductoVendido;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

/** Reportes para el administrador. Ruta protegida: requiere token con rol ADMIN. */
@RestController
@RequestMapping("/api/reportes")
@Tag(name = "Reportes", description = "Solo ADMIN: requiere token (boton Authorize)")
@SecurityRequirement(name = "bearerAuth")
public class ReporteController {

    private final ConsultarReportesUseCase reportes;

    public ReporteController(ConsultarReportesUseCase reportes) {
        this.reportes = reportes;
    }

    @GetMapping("/productos-mas-vendidos")
    @Operation(summary = "Ranking de productos mas vendidos",
            description = "Suma lo vendido por producto en compras completadas, de mayor a menor. Solo ADMIN.")
    public List<ProductoVendidoResponse> productosMasVendidos(@RequestParam(defaultValue = "10") int limite) {
        List<ProductoVendido> ranking = reportes.productosMasVendidos(limite);
        return IntStream.range(0, ranking.size())
                .mapToObj(i -> ProductoVendidoResponse.desde(i + 1, ranking.get(i)))
                .toList();
    }

    public record ProductoVendidoResponse(int posicion, UUID productoId, String nombre, BigDecimal cantidadVendida,
                                          String unidad, long numeroCompras) {

        static ProductoVendidoResponse desde(int posicion, ProductoVendido vendido) {
            return new ProductoVendidoResponse(posicion, vendido.producto().valor(), vendido.nombre(),
                    vendido.cantidadVendida(), vendido.unidad().simbolo(), vendido.numeroCompras());
        }
    }
}
