package com.uniquindio.ecommerce.application.usecase;

import com.uniquindio.ecommerce.application.port.in.RegistrarLoteUseCase;
import com.uniquindio.ecommerce.application.port.out.PublicadorEventos;
import com.uniquindio.ecommerce.application.port.out.Reloj;
import com.uniquindio.ecommerce.domain.catalogo.Lote;
import com.uniquindio.ecommerce.domain.catalogo.Producto;
import com.uniquindio.ecommerce.domain.entity.Campesino;
import com.uniquindio.ecommerce.domain.exception.RecursoNoEncontradoException;
import com.uniquindio.ecommerce.domain.exception.ReglaDeNegocioVioladaException;
import com.uniquindio.ecommerce.domain.repository.CampesinoRepository;
import com.uniquindio.ecommerce.domain.repository.LoteRepository;
import com.uniquindio.ecommerce.domain.repository.ProductoRepository;
import com.uniquindio.ecommerce.domain.valueobject.identidad.LoteId;
import com.uniquindio.ecommerce.domain.valueobject.origenycalidad.FichaTrazabilidad;

/**
 * <b>Registrar lote</b>. Aqui solo se resuelve lo que exige mirar otros agregados: que el
 * campesino este autorizado (regla 1), que el producto exista y que el codigo no se repita.
 * Las reglas del propio lote las valida su {@code Builder}.
 */
public class RegistrarLoteService extends ServicioDeAplicacion implements RegistrarLoteUseCase {

    private final CampesinoRepository campesinoRepository;
    private final ProductoRepository productoRepository;

    public RegistrarLoteService(LoteRepository loteRepository, Reloj reloj, PublicadorEventos publicadorEventos,
                                CampesinoRepository campesinoRepository, ProductoRepository productoRepository) {
        super(loteRepository, reloj, publicadorEventos);
        this.campesinoRepository = campesinoRepository;
        this.productoRepository = productoRepository;
    }

    @Override
    public LoteId registrar(RegistrarLoteCommand comando) {
        Campesino campesino = campesinoRepository.obtenerCampesino(comando.campesino())
                .orElseThrow(() -> new RecursoNoEncontradoException("Campesino", comando.campesino()));
        ReglaDeNegocioVioladaException.validar(campesino.estaAutorizadoParaPublicar(), "R1",
                "El campesino " + campesino.getNombre() + " no esta autorizado para publicar en la plataforma.");
        ReglaDeNegocioVioladaException.validar(comando.codigo() == null || !loteRepository.existeCodigo(comando.codigo().trim()),
                "INV-CODIGO", "Ya existe un lote con el codigo '" + comando.codigo() + "'.");
        Producto producto = productoRepository.obtenerProducto(comando.producto())
                .orElseThrow(() -> new RecursoNoEncontradoException("Producto", comando.producto()));

        Lote lote = Lote.builder()
                .codigo(comando.codigo())
                .producto(producto)
                .campesino(campesino.getId())
                .fichaTrazabilidad(FichaTrazabilidad.registrar(
                        campesino.getId(), comando.fincaOrigen(), comando.tecnica(), comando.fechaCosecha()))
                .calidad(comando.calidad())
                .cantidadInicial(comando.cantidad())
                .precioFinca(comando.precioFinca())
                .fechaCosecha(comando.fechaCosecha())
                .merma(comando.merma())
                .momentoRegistro(reloj.ahora())
                .build();

        return persistirYPublicar(lote).id();
    }
}
