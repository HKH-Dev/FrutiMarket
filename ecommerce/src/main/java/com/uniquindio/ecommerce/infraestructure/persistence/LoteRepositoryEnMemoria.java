package com.uniquindio.ecommerce.infraestructure.persistence;

import com.uniquindio.ecommerce.domain.catalogo.EstadoLote;
import com.uniquindio.ecommerce.domain.catalogo.Lote;
import com.uniquindio.ecommerce.domain.repository.LoteRepository;
import com.uniquindio.ecommerce.domain.valueobject.identidad.CampesinoId;
import com.uniquindio.ecommerce.domain.valueobject.identidad.LoteId;
import com.uniquindio.ecommerce.domain.valueobject.identidad.ProductoId;
import com.uniquindio.ecommerce.domain.valueobject.identidad.PuntoAcopioId;
import com.uniquindio.ecommerce.domain.valueobject.origenycalidad.TipoCultivo;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class LoteRepositoryEnMemoria implements LoteRepository {

    private static final ZoneId ZONA_OPERACION = ZoneId.of("America/Bogota");

    private final Map<LoteId, Lote> lotes = new HashMap<>();

    @Override
    public Lote almacenar(Lote lote) {
        lotes.put(lote.id(), lote);
        return lote;
    }

    @Override
    public Optional<Lote> buscarPorId(LoteId id) {
        return Optional.ofNullable(lotes.get(id));
    }

    @Override
    public List<Lote> buscarTodos() {
        return List.copyOf(lotes.values());
    }

    @Override
    public boolean existeCodigo(String codigo) {
        return lotes.values().stream()
                .anyMatch(lote -> lote.codigo().equals(codigo));
    }

    @Override
    public List<Lote> buscarDisponiblesPorProducto(ProductoId producto) {
        return lotes.values().stream()
                .filter(lote -> lote.producto().equals(producto))
                .filter(Lote::estaDisponible)
                .toList();
    }

    @Override
    public List<Lote> consultarPorCampesino(CampesinoId campesino) {
        return lotes.values().stream()
                .filter(lote -> lote.campesinoResponsable().equals(campesino))
                .toList();
    }

    @Override
    public List<Lote> buscarPorEstado(EstadoLote estado) {
        return lotes.values().stream()
                .filter(lote -> lote.estado() == estado)
                .toList();
    }

    @Override
    public List<Lote> buscarEnAcopioPorCultivo(PuntoAcopioId puntoAcopio, TipoCultivo tipoCultivo) {
        return lotes.values().stream()
                .filter(lote -> lote.estado() == EstadoLote.EN_ACOPIO)
                .filter(lote -> lote.puntoAcopioActual().filter(puntoAcopio::equals).isPresent())
                .filter(lote -> lote.tipoCultivo() == tipoCultivo)
                .toList();
    }

    @Override
    public List<Lote> buscarVencidosNoCerrados() {
        LocalDate hoy = LocalDate.now(ZONA_OPERACION);
        return lotes.values().stream()
                .filter(lote -> lote.estaVencido(hoy))
                .filter(lote -> !lote.estado().esTerminal())
                .toList();
    }

    @Override
    public void eliminar(Lote lote) {
        lotes.remove(lote.id());
    }
}
