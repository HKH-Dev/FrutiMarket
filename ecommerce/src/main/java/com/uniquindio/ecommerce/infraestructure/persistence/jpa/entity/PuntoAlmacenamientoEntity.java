package com.uniquindio.ecommerce.infraestructure.persistence.jpa.entity;

import com.uniquindio.ecommerce.domain.entity.PuntoAlmacenamiento;
import com.uniquindio.ecommerce.domain.valueobject.logistica.Conservacion;
import com.uniquindio.ecommerce.domain.valueobject.origenycalidad.TipoCultivo;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "puntos_almacenamiento")
@Getter
@Setter
@NoArgsConstructor
public class PuntoAlmacenamientoEntity {

    @Id
    private UUID id;

    @Column(nullable = false, length = 120)
    private String nombre;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private PuntoAlmacenamiento.Tipo tipo;

    @Column(nullable = false, length = 80)
    private String municipio;

    @Column(name = "capacidad_lotes", nullable = false)
    private int capacidadLotes;

    /** Regla 15: municipios normalizados (minusculas, sin espacios extremos). */
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "almacen_municipios_cobertura", joinColumns = @JoinColumn(name = "almacen_id"))
    @Column(name = "municipio", nullable = false, length = 80)
    private Set<String> municipiosCobertura = new HashSet<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "almacen_cultivos_habilitados", joinColumns = @JoinColumn(name = "almacen_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_cultivo", nullable = false, length = 30)
    private Set<TipoCultivo> cultivosHabilitados = new HashSet<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "almacen_conservaciones", joinColumns = @JoinColumn(name = "almacen_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "conservacion", nullable = false, length = 20)
    private Set<Conservacion> conservacionesDisponibles = new HashSet<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "almacen_lotes", joinColumns = @JoinColumn(name = "almacen_id"))
    @Column(name = "lote_id", nullable = false)
    private Set<UUID> lotesAlmacenados = new HashSet<>();
}
