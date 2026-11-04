package com.uniquindio.ecommerce.infraestructure.seguridad;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

/** Usuario que inicia sesion. Solo guarda el hash BCrypt de la clave, nunca la clave. */
@Entity
@Table(name = "usuarios")
public class UsuarioEntity {

    @Id
    private UUID id;

    @Column(nullable = false, unique = true, length = 120)
    private String correo;

    @Column(name = "clave_hash", nullable = false, length = 100)
    private String claveHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Rol rol;

    @Column(nullable = false)
    private boolean activo;

    /**
     * Campesino o comprador del dominio que representa este usuario (CampesinoId o CompradorId).
     * Nulo para ADMIN. Viaja en el token, asi el usuario solo puede actuar en nombre propio.
     */
    @Column(name = "actor_id")
    private UUID actorId;

    protected UsuarioEntity() {
        // requerido por JPA
    }

    public UsuarioEntity(UUID id, String correo, String claveHash, Rol rol, UUID actorId) {
        this.id = id;
        this.correo = correo;
        this.claveHash = claveHash;
        this.rol = rol;
        this.actorId = actorId;
        this.activo = true;
    }

    public UUID getId() { return id; }
    public String getCorreo() { return correo; }
    public String getClaveHash() { return claveHash; }
    public Rol getRol() { return rol; }
    public boolean isActivo() { return activo; }
    public UUID getActorId() { return actorId; }
}