package com.tareas.tareas.domain.tarea;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record DatosActualizarTarea(
        @NotBlank(message = "El nombre es obligatorio")
        String nombre,

        @NotBlank(message = "La descripcion es obligatoria")
        String descripcion,

        @NotNull
        Estado estado,

        @NotNull
        Importancia importancia,

        @Min(value = 1, message = "La duración debe ser al menos 1 día")
        Integer duracionDias, // opcional

        LocalDate fechaInicio, // opcional

        LocalDate fechaVencimiento // opcional
) {
}
