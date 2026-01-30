package com.tareas.tareas.domain.tarea;

import com.tareas.tareas.domain.usuario.Usuario;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record DatosRespuestaTarea(
        Long id,
        @NotNull String nombre,
        @NotNull String descripcion,
        @NotNull Estado estado,
        @NotNull LocalDateTime fechaCreacion,
        LocalDate fechaInicio,
        LocalDate fechaVencimiento,
        LocalDateTime fechaFinalizacion,
        @NotNull Importancia importancia,
        Integer duracionDias,
        Boolean estaVencida, // Indicador si está vencida
        Long diasRestantes // Días restantes hasta vencimiento
) {
    public DatosRespuestaTarea(Tarea tarea) {
        this(
                tarea.getId(),
                tarea.getNombre(),
                tarea.getDescripcion(),
                tarea.getEstado(),
                tarea.getFechaCreacion(),
                tarea.getFechaInicio(),
                tarea.getFechaVencimiento(),
                tarea.getFechaFinalizacion(),
                tarea.getImportancia(),
                tarea.getDuracionDias(),
                tarea.estaVencida(),
                tarea.diasRestantes()
        );
    }
}
