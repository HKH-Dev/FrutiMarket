package com.uniquindio.ecommerce.domain.entity;

import com.uniquindio.ecommerce.domain.valueobject.EstadoReembolso;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class Reembolso {

    private UUID reembolsoId;
    private String motivo;
    private BigDecimal monto;
    private LocalDateTime fechaSolicitud;
    private EstadoReembolso estado;

    public Reembolso(String motivo, BigDecimal monto) {

        if (motivo == null || motivo.isBlank()) {
            throw new IllegalArgumentException(
                    "El motivo del reembolso es obligatorio."
            );
        }

        if (monto == null || monto.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "El monto del reembolso debe ser mayor que cero."
            );
        }

        this.reembolsoId = UUID.randomUUID();
        this.motivo = motivo;
        this.monto = monto;
        this.fechaSolicitud = LocalDateTime.now();
        this.estado = EstadoReembolso.SOLICITADO;
    }

    public void solicitar() {
        if (!puedeSolicitar()) {
            throw new IllegalStateException(
                    "No se puede solicitar el reembolso en el estado actual."
            );
        }

        estado = EstadoReembolso.SOLICITADO;
        fechaSolicitud = LocalDateTime.now();
    }

    public void aprobar() {
        if (estado != EstadoReembolso.SOLICITADO) {
            throw new IllegalStateException(
                    "Solo se puede aprobar un reembolso solicitado."
            );
        }

        estado = EstadoReembolso.APROBADO;
    }

    public void rechazar() {
        if (estado != EstadoReembolso.SOLICITADO) {
            throw new IllegalStateException(
                    "Solo se puede rechazar un reembolso solicitado."
            );
        }

        estado = EstadoReembolso.RECHAZADO;
    }

    public boolean puedeSolicitar() {
        return estado == EstadoReembolso.RECHAZADO;
    }

    public UUID getReembolsoId() {
        return reembolsoId;
    }

    public String getMotivo() {
        return motivo;
    }

    public BigDecimal getMonto() {
        return monto;
    }

    public LocalDateTime getFechaSolicitud() {
        return fechaSolicitud;
    }

    public EstadoReembolso getEstado() {
        return estado;
    }
}