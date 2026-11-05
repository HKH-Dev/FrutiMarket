package com.uniquindio.ecommerce.infraestructure.persistence;

import java.util.Locale;
import java.util.Comparator;
import java.time.Instant;
import com.uniquindio.ecommerce.domain.consulta.Pagina;
import com.uniquindio.ecommerce.domain.consulta.BusquedaLotes;
import com.uniquindio.ecommerce.domain.catalogo.EstadoLote;
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
    public Pagina<Lote> buscarDisponibles(BusquedaLotes criterios) {
        String texto = criterios.texto().toLowerCase(Locale.ROOT);
        List<Lote> todos = lotes.values().stream()
                .filter(Lote::estaDisponible)
                .filter(lote -> lote.nombreProducto().toLowerCase(Locale.ROOT).contains(texto)
                        || lote.codigo().toLowerCase(Locale.ROOT).contains(texto))
                .filter(lote -> criterios.tipoCultivo() == null || lote.tipoCultivo() == criterios.tipoCultivo())
                .filter(lote -> criterios.precioMaximo() == null || (lote.precioFinca() != null
                        && lote.precioFinca().valorPorUnidad().compareTo(criterios.precioMaximo()) <= 0))
                .sorted(Comparator.comparing((Lote lote) -> lote.fechaPublicacion().orElse(Instant.MIN)).reversed()
                        .thenComparing(Lote::codigo))
                .toList();
        int desde = Math.min(criterios.pagina() * criterios.tamano(), todos.size());
        int hasta = Math.min(desde + criterios.tamano(), todos.size());
        int totalPaginas = (int) Math.ceil((double) todos.size() / criterios.tamano());
        return new Pagina<>(todos.subList(desde, hasta), criterios.pagina(), criterios.tamano(), todos.size(), totalPaginas);
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
}
