package com.tareas.tareas.domain.tarea;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record DatosCrearTarea(
        @NotBlank @NotNull(message = "El nombre es obligatorio") String nombre,
        @NotNull(message = "La descripcion es obligatoria") String descripcion,
        LocalDateTime fecha_creacion,
        LocalDateTime fecha_finalizacion,
        Estado estado,
        Importancia importancia,
        Long usuarioId
) {
}
