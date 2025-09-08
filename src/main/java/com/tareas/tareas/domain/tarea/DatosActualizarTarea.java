package com.tareas.tareas.domain.tarea;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record DatosActualizarTarea(
    @NotNull Long id,
    @NotNull String nombre,
    @NotNull String descripcion,
    @NotNull LocalDateTime fecha_creacion,
    @NotNull LocalDateTime fecha_finalizacion,
    @NotNull Estado estado,
    @NotNull Importancia importancia,
    Long usuarioId
) {
}
