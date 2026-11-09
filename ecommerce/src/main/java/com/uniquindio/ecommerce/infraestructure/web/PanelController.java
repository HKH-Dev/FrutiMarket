package com.uniquindio.ecommerce.infraestructure.web;

import com.uniquindio.ecommerce.application.port.in.ConsultarPanelesUseCase;
import com.uniquindio.ecommerce.infraestructure.seguridad.UsuarioActual;
import com.uniquindio.ecommerce.infraestructure.web.dto.ComercialDtos.CampesinoResponse;
import com.uniquindio.ecommerce.infraestructure.web.dto.ComercialDtos.CompradorResponse;
import com.uniquindio.ecommerce.infraestructure.web.dto.ComercialDtos.ResumenPagosResponse;
import com.uniquindio.ecommerce.infraestructure.web.dto.ComercialDtos.VentaResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/paneles")
@Tag(name = "Paneles", description = "Datos de los paneles de administrador y campesino")
@SecurityRequirement(name = "bearerAuth")
public class PanelController {

    private final ConsultarPanelesUseCase paneles;

    public PanelController(ConsultarPanelesUseCase paneles) {
        this.paneles = paneles;
    }

    @GetMapping("/admin/resumen")
    @Operation(summary = "Ingresos, descuentos y pagos (ADMIN)")
    public ResumenPagosResponse resumen() {
        return ResumenPagosResponse.desde(paneles.resumenPagos());
    }

    @GetMapping("/admin/campesinos")
    @Operation(summary = "Campesinos y su autorizacion (ADMIN)")
    public List<CampesinoResponse> campesinos() {
        return paneles.campesinos().stream().map(CampesinoResponse::desde).toList();
    }

    @GetMapping("/admin/compradores")
    @Operation(summary = "Compradores (ADMIN)")
    public List<CompradorResponse> compradores() {
        return paneles.compradores().stream().map(CompradorResponse::desde).toList();
    }

    @GetMapping("/campesino/ventas")
    @Operation(summary = "Mis ventas por producto (CAMPESINO)")
    public List<VentaResponse> ventas(@Parameter(hidden = true) @AuthenticationPrincipal Jwt token) {
        return paneles.ventasDe(UsuarioActual.campesino(token)).stream().map(VentaResponse::desde).toList();
    }
}
