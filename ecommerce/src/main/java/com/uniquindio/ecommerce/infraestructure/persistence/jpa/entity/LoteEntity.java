package com.uniquindio.ecommerce.infraestructure.persistence.jpa.entity;

import com.uniquindio.ecommerce.domain.catalogo.EstadoLote;
import com.uniquindio.ecommerce.domain.catalogo.MotivoRevision;
import com.uniquindio.ecommerce.domain.catalogo.Producto;
import com.uniquindio.ecommerce.domain.valueobject.catalogo.UnidadMedida;
import com.uniquindio.ecommerce.domain.valueobject.logistica.Conservacion;
import com.uniquindio.ecommerce.domain.valueobject.origenycalidad.Calidad;
import com.uniquindio.ecommerce.domain.valueobject.origenycalidad.TecnicaProduccion;
import com.uniquindio.ecommerce.domain.valueobject.origenycalidad.TipoCultivo;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Fila del agregado Lote. Los datos del producto (nombre, linea, cultivo, conservacion,
 * temporada) se copian al registrar el lote, igual que en el dominio: si el producto
 * cambia despues, el lote conserva lo que se declaro al cosecharlo.
 */
@Entity
@Table(name = "lotes", indexes = {
        @Index(name = "ix_lotes_producto_estado", columnList = "producto_id, estado"),
        @Index(name = "ix_lotes_campesino", columnList = "campesino_id"),
        @Index(name = "ix_lotes_almacen", columnList = "almacen_actual_id")
})
@Getter
@Setter
@NoArgsConstructor
public class LoteEntity {

    @Id
    private UUID id;

    @Column(nullable = false, unique = true, length = 60)
    private String codigo;

    // --- Producto (copia al registrar) ---
    @Column(name = "producto_id", nullable = false)
    private UUID productoId;

    @Column(name = "nombre_producto", nullable = false, length = 120)
    private String nombreProducto;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Producto.Linea linea;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_cultivo", nullable = false, length = 30)
    private TipoCultivo tipoCultivo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Conservacion conservacion;

    @Column(name = "temporada_inicio", length = 7)
    private String temporadaInicio;

    @Column(name = "temporada_fin", length = 7)
    private String temporadaFin;

    // --- Origen y trazabilidad (regla 8) ---
    @Column(name = "campesino_id", nullable = false)
    private UUID campesinoId;

    @Column(name = "finca_origen", nullable = false)
    private UUID fincaOrigen;

    @Enumerated(EnumType.STRING)
    @Column(name = "tecnica_produccion", nullable = false, length = 30)
    private TecnicaProduccion tecnicaProduccion;

    @Column(name = "ficha_fecha_cosecha", nullable = false)
    private LocalDate fichaFechaCosecha;

    @Column(name = "ficha_observaciones", nullable = false, length = 500)
    private String fichaObservaciones;

    @Column(name = "fecha_cosecha", nullable = false)
    private LocalDate fechaCosecha;

    @Column(name = "fecha_limite_consumo")
    private LocalDate fechaLimiteConsumo;

    // --- Calidad (regla 18) ---
    @Enumerated(EnumType.STRING)
    @Column(name = "calidad_declarada_categoria", nullable = false, length = 20)
    private Calidad.Categoria calidadDeclaradaCategoria;

    @Column(name = "calidad_declarada_defectos", nullable = false)
    private int calidadDeclaradaDefectos;

    @Column(name = "calidad_declarada_organico", nullable = false)
    private boolean calidadDeclaradaOrganico;

    @Enumerated(EnumType.STRING)
    @Column(name = "calidad_verificada_categoria", length = 20)
    private Calidad.Categoria calidadVerificadaCategoria;

    @Column(name = "calidad_verificada_defectos")
    private Integer calidadVerificadaDefectos;

    @Column(name = "calidad_verificada_organico")
    private Boolean calidadVerificadaOrganico;

    // --- Cantidades y precio (reglas 2 y 3); todas en la misma unidad ---
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UnidadMedida unidad;

    @Column(name = "cantidad_inicial", nullable = false, precision = 19, scale = 3)
    private BigDecimal cantidadInicial;

    @Column(name = "cantidad_disponible", nullable = false, precision = 19, scale = 3)
    private BigDecimal cantidadDisponible;

    @Column(name = "precio_finca", precision = 19)
    private BigDecimal precioFinca;

    @Column(name = "merma_porcentaje", nullable = false, precision = 5, scale = 2)
    private BigDecimal mermaPorcentaje;

    @Column(name = "merma_justificacion", nullable = false, length = 300)
    private String mermaJustificacion;

    @Column(name = "merma_aplicada", nullable = false)
    private boolean mermaAplicada;

    // --- Estado y custodia ---
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoLote estado;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_previo_revision", length = 20)
    private EstadoLote estadoPrevioARevision;

    @Enumerated(EnumType.STRING)
    @Column(name = "motivo_revision", length = 30)
    private MotivoRevision motivoRevision;

    @Column(name = "detalle_revision", length = 500)
    private String detalleRevision;

    @Column(name = "almacen_actual_id")
    private UUID almacenActualId;

    @Column(name = "cadena_frio_rota", nullable = false)
    private boolean cadenaFrioRota;

    @Column(name = "fecha_registro", nullable = false)
    private Instant fechaRegistro;

    @Column(name = "fecha_publicacion")
    private Instant fechaPublicacion;

    /** Contador de cambios del dominio; no es el bloqueo optimista de JPA. */
    @Column(nullable = false)
    private long version;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "lote_reservas", joinColumns = @JoinColumn(name = "lote_id"))
    private List<ReservaLoteEmbeddable> reservas = new ArrayList<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "lote_custodia", joinColumns = @JoinColumn(name = "lote_id"))
    @OrderColumn(name = "orden")
    private List<RegistroCustodiaEmbeddable> custodia = new ArrayList<>();
}
