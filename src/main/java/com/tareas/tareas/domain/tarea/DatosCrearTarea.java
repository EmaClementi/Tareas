package com.tareas.tareas.domain.tarea;

import com.tareas.tareas.Validacion;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record DatosCrearTarea(
        @NotBlank
        @NotNull(message = "El nombre es obligatorio")
        String nombre,

        @NotNull(message = "La descripcion es obligatoria")
        String descripcion,

        @NotNull
        Importancia importancia,

        Integer duracionDias, // opcional (nullable)

        LocalDate fechaInicio, // opcional

        LocalDate fechaVencimiento //opcional
) {
    //al menos uno debe estar presente
    public DatosCrearTarea {
        if (duracionDias == null && fechaVencimiento == null) {
            throw new Validacion(
                    "Debe proporcionar duracionDias o fechaVencimiento"
            );
        }
    }
}
