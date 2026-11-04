package com.uniquindio.ecommerce.infraestructure.persistence;

import com.uniquindio.ecommerce.domain.catalogo.EstadoLote;
import com.uniquindio.ecommerce.domain.repository.Pagina;

import com.uniquindio.ecommerce.domain.catalogo.Lote;
import com.uniquindio.ecommerce.domain.repository.LoteRepository;
import com.uniquindio.ecommerce.domain.valueobject.identidad.AlmacenId;
import com.uniquindio.ecommerce.domain.valueobject.identidad.CampesinoId;
import com.uniquindio.ecommerce.domain.valueobject.identidad.LoteId;
import com.uniquindio.ecommerce.domain.valueobject.identidad.ProductoId;
import com.uniquindio.ecommerce.domain.valueobject.origenycalidad.TipoCultivo;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class LoteRepositoryEnMemoria implements LoteRepository {

    private final Map<LoteId, Lote> lotes = new HashMap<>();

    @Override
    public Lote almacenar(Lote lote) {
        lotes.put(lote.id(), lote);
        return lote;
    }

    @Override
    public Optional<Lote> obtenerLote(LoteId id) {
        return Optional.ofNullable(lotes.get(id));
    }

    @Override
    public boolean existeCodigo(String codigo) {
        return lotes.values().stream().anyMatch(lote -> lote.codigo().equalsIgnoreCase(codigo));
    }

    @Override
    public List<Lote> lotesDisponiblesDe(ProductoId producto) {
        return lotes.values().stream()
                .filter(lote -> lote.producto().equals(producto))
                .filter(Lote::estaDisponible)
                .toList();
    }

    @Override
    public List<Lote> lotesDelCampesino(CampesinoId campesino) {
        return lotes.values().stream().filter(lote -> lote.esDelCampesino(campesino)).toList();
    }

    @Override
    public List<Lote> lotesEnAlmacen(AlmacenId almacen, TipoCultivo tipoCultivo) {
        return lotes.values().stream()
                .filter(lote -> lote.estado() == EstadoLote.EN_ACOPIO)
                .filter(lote -> lote.almacenActual().filter(almacen::equals).isPresent())
                .filter(lote -> lote.tipoCultivo() == tipoCultivo)
                .toList();
    }

    @Override
    public List<Lote> lotesVencidosSinCerrar(LocalDate hoy) {
        return lotes.values().stream()
                .filter(lote -> lote.estaVencido(hoy))
                .filter(lote -> !lote.estado().esTerminal())
                .toList();
    }
    @Override
    public Pagina<Lote> buscarEnCatalogo(String texto, TipoCultivo tipoCultivo, int pagina, int tamano) {
        String buscado = texto == null ? "" : texto.trim().toLowerCase();
        List<Lote> coincidencias = lotes.values().stream()
                .filter(lote -> lote.estado() == EstadoLote.PUBLICADO)
                .filter(Lote::estaDisponible)
                .filter(lote -> tipoCultivo == null || lote.tipoCultivo() == tipoCultivo)
                .filter(lote -> lote.nombreProducto().toLowerCase().contains(buscado)
                        || lote.codigo().toLowerCase().contains(buscado))
                .sorted(java.util.Comparator.comparing((Lote lote) -> lote.nombreProducto().toLowerCase())
                        .thenComparing(Lote::codigo))
                .toList();
        int desde = Math.min(pagina * tamano, coincidencias.size());
        int hasta = Math.min(desde + tamano, coincidencias.size());
        int totalPaginas = (int) Math.ceil(coincidencias.size() / (double) tamano);
        return new Pagina<>(coincidencias.subList(desde, hasta), pagina, tamano, coincidencias.size(), totalPaginas);
    }
}
