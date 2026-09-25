package com.uniquindio.ecommerce.domain.valueobject;

import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

public record FichaTrazabilidad() {
    private static final UUID trazabilidadId = null;
    private Object tecnicaSiembra(){
        return null;
    }
    private LocalDate fechaCosecha(){
        return LocalDate.now();
    }
    private String observaciones(){
        return "text";
    }
    private LocalDate fechaRegistro(){
        return fechaCosecha();
    }

    private boolean estaCompleta(){
        return Objects.nonNull(tecnicaSiembra());
    }

    private boolean validarOrigen(){
        return Objects.nonNull(tecnicaSiembra());
    }


}
