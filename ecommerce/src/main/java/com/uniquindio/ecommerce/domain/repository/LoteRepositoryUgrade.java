package com.uniquindio.ecommerce.domain.repository;

import com.uniquindio.eccommerce.dominio.entity.catalogo.EstadoLote;
import com.uniquindio.eccommerce.dominio.entity.catalogo.Lote;
import com.uniquindio.eccommerce.dominio.valueobject.identidad.CampesinoId;
import com.uniquindio.eccommerce.dominio.valueobject.identidad.LoteId;
import com.uniquindio.eccommerce.dominio.valueobject.identidad.ProductoId;
import com.uniquindio.eccommerce.dominio.valueobject.identidad.PuntoAcopioId;
import com.uniquindio.eccommerce.dominio.valueobject.origenycalidad.TipoCultivo;

import java.util.List;
import java.util.Optional;

/**
 * Puerto de salida para persistir y recuperar lotes.
 *
 * <p><b>La interfaz vive en el dominio; la implementacion, en infraestructura.</b>
 * Esa inversion es lo que hace hexagonal a la arquitectura: el dominio declara lo
 * que necesita y no se entera de si detras hay MariaDB, memoria o un archivo.</p>
 *
 * <p>Todos los metodos trabajan con el agregado completo. No hay "guardar la guia
 * de despacho" por separado: se guarda el lote, y con el todo lo que vive dentro de
 * su frontera. Es la traduccion tecnica de que el agregado es la unidad de
 * consistencia transaccional.</p>
 */
public interface LoteRepositoryUgrade {

    /** Guarda el lote completo, con sus reservas y su guia de despacho. */
    Lote guardar(Lote lote);

    Optional<Lote> buscarPorId(LoteId id);

    Optional<Lote> buscarPorCodigo(String codigo);

    boolean existeCodigo(String codigo);

    /** Lotes publicados y con cantidad disponible de un producto del catalogo. */
    List<Lote> buscarDisponiblesPorProducto(ProductoId producto);

    List<Lote> buscarPorCampesino(CampesinoId campesino);

    List<Lote> buscarPorEstado(EstadoLote estado);

    /** Regla 16: candidatos a despacho desde un punto de acopio, para ordenar por FEFO. */
    List<Lote> buscarEnAcopioPorCultivo(PuntoAcopioId puntoAcopio, TipoCultivo tipoCultivo);

    /** Lotes que ya superaron su fecha limite de consumo y siguen activos. */
    List<Lote> buscarVencidosNoCerrados();

    void eliminar(LoteId id);
}
