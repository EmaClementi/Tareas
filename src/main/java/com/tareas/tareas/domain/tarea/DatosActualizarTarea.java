package com.tareas.tareas.domain.tarea;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record DatosActualizarTarea(
    @NotNull Long id,
    @NotNull String nombre,
    @NotNull String descripcion,
    @NotNull LocalDateTime fechaCreacion,
    @NotNull LocalDateTime fechaFinalizacion,
    @NotNull Estado estado,
    @NotNull Importancia importancia,
    Long usuarioId
) {
}
