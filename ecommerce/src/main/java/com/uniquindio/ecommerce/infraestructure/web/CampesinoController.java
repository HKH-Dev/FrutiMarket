package com.uniquindio.ecommerce.infraestructure.web;

import com.uniquindio.ecommerce.application.port.in.AutorizarCampesinoUseCase;
import com.uniquindio.ecommerce.domain.valueobject.identidad.CampesinoId;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/campesinos")
@Tag(name = "Campesinos", description = "Solo ADMIN: autorizar campesinos para ofrecer lotes")
@SecurityRequirement(name = "bearerAuth")
public class CampesinoController {

    private final AutorizarCampesinoUseCase autorizarCampesino;

    public CampesinoController(AutorizarCampesinoUseCase autorizarCampesino) {
        this.autorizarCampesino = autorizarCampesino;
    }

    @PostMapping("/{id}/autorizacion")
    @Operation(summary = "Autorizar a un campesino",
            description = "Regla 1: un campesino recien registrado no puede ofrecer lotes hasta que un ADMIN lo autorice. "
                    + "El id es el perfilId que devuelve el registro.")
    public ResponseEntity<Void> autorizar(@PathVariable UUID id) {
        autorizarCampesino.autorizar(CampesinoId.de(id));
        return ResponseEntity.noContent().build();
    }
}
