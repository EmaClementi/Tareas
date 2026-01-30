package com.tareas.tareas.domain.tarea;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record DatosListaTarea(
        @NotNull Long id,
        @NotNull String nombre,
        @NotNull String descripcion,
        @NotNull LocalDateTime fechaCreacion,
        LocalDate fechaInicio,
        LocalDate fechaVencimiento,
        LocalDateTime fechaFinalizacion,
        @NotNull Estado estado,
        @NotNull Importancia importancia,
        Boolean estaVencida,
        Long diasRestantes
) {
}
