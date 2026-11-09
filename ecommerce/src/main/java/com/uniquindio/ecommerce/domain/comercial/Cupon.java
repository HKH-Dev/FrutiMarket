package com.uniquindio.ecommerce.domain.comercial;

import com.uniquindio.ecommerce.domain.exception.ReglaDeNegocioVioladaException;
import com.uniquindio.ecommerce.domain.valueobject.identidad.CompradorId;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

/**
 * Cupon personalizado: pertenece a un solo comprador y se usa una sola vez.
 *
 * <p>Invariantes: <b>R23</b> solo lo redime su dueno; <b>R24</b> debe estar sin usar y vigente.
 * Si la compra se reembolsa, el cupon se libera.</p>
 */
public final class Cupon {

    private final UUID id;
    private final String codigo;
    private final CompradorId comprador;
    private final int porcentaje;
    private final LocalDate venceEl;
    private Instant usadoEn;

    private Cupon(UUID id, String codigo, CompradorId comprador, int porcentaje, LocalDate venceEl, Instant usadoEn) {
        this.id = id;
        this.codigo = codigo;
        this.comprador = comprador;
        this.porcentaje = porcentaje;
        this.venceEl = venceEl;
        this.usadoEn = usadoEn;
    }

    public static Cupon crear(String codigo, CompradorId comprador, int porcentaje, LocalDate venceEl, LocalDate hoy) {
        ReglaDeNegocioVioladaException.validar(codigo != null && codigo.trim().matches("[A-Za-z0-9-]{4,30}"), "R24",
                "El codigo debe tener de 4 a 30 letras, numeros o guiones.");
        ReglaDeNegocioVioladaException.validar(comprador != null, "R23", "El cupon debe tener un comprador.");
        ReglaDeNegocioVioladaException.validar(porcentaje >= 1 && porcentaje <= 50, "R24", "El cupon debe estar entre 1% y 50%.");
        ReglaDeNegocioVioladaException.validar(venceEl != null && !venceEl.isBefore(hoy), "R24", "El cupon no puede nacer vencido.");
        return new Cupon(UUID.randomUUID(), normalizar(codigo), comprador, porcentaje, venceEl, null);
    }

    public static Cupon reconstituir(UUID id, String codigo, CompradorId comprador, int porcentaje, LocalDate venceEl, Instant usadoEn) {
        return new Cupon(id, codigo, comprador, porcentaje, venceEl, usadoEn);
    }

    public static String normalizar(String codigo) {
        return codigo.trim().toUpperCase(Locale.ROOT);
    }

    /** Comprueba R23 y R24 sin consumir el cupon: lo usa la cotizacion. */
    public void exigirRedimible(CompradorId solicitante, LocalDate hoy) {
        ReglaDeNegocioVioladaException.validar(comprador.equals(solicitante), "R23", "Este cupon pertenece a otro comprador.");
        ReglaDeNegocioVioladaException.validar(usadoEn == null, "R24", "Este cupon ya fue usado.");
        ReglaDeNegocioVioladaException.validar(!hoy.isAfter(venceEl), "R24", "Este cupon vencio el " + venceEl + ".");
    }

    public void redimir(CompradorId solicitante, Instant momento, LocalDate hoy) {
        exigirRedimible(solicitante, hoy);
        this.usadoEn = momento;
    }

    public void liberar() {
        this.usadoEn = null;
    }

    public UUID id() { return id; }
    public String codigo() { return codigo; }
    public CompradorId comprador() { return comprador; }
    public int porcentaje() { return porcentaje; }
    public LocalDate venceEl() { return venceEl; }
    public Optional<Instant> usadoEn() { return Optional.ofNullable(usadoEn); }
}
