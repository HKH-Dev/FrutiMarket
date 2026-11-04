package com.uniquindio.ecommerce.infraestructure.web;

import com.uniquindio.ecommerce.application.port.in.BuscarCatalogoLotesUseCase;
import com.uniquindio.ecommerce.domain.valueobject.origenycalidad.TipoCultivo;
import com.uniquindio.ecommerce.infraestructure.web.dto.LoteResponses.LoteResumenResponse;
import com.uniquindio.ecommerce.infraestructure.web.dto.PaginaResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/catalogo")
@Tag(name = "Catalogo", description = "Busqueda paginada de lotes publicados")
public class CatalogoController {

    private final BuscarCatalogoLotesUseCase buscarCatalogo;

    public CatalogoController(BuscarCatalogoLotesUseCase buscarCatalogo) {
        this.buscarCatalogo = buscarCatalogo;
    }

    @GetMapping("/lotes")
    @Operation(summary = "Buscar lotes del catalogo (paginado)",
            description = "Lotes publicados y con existencias. Busca por texto en el producto o el codigo y "
                    + "filtra por tipo de cultivo. No muestra lotes retirados. Responde con el contenido de la "
                    + "pagina, el total de elementos y el total de paginas. "
                    + "Errores: 400 si la pagina es negativa o el tamano no esta entre 1 y 50.")
    public PaginaResponse<LoteResumenResponse> buscar(
            @Parameter(description = "Parte del nombre del producto o del codigo", example = "mango")
            @RequestParam(required = false, defaultValue = "") String q,
            @Parameter(description = "Filtro opcional: FRUTA, HORTALIZA_HOJA, HORTALIZA_RAIZ, TUBERCULO, LEGUMINOSA, CEREAL, ...")
            @RequestParam(required = false) TipoCultivo tipoCultivo,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "10") int tamano) {
        return PaginaResponse.desde(buscarCatalogo.buscar(q, tipoCultivo, pagina, tamano), LoteResumenResponse::desde);
    }
}