package com.tareas.tareas.domain.tarea;

import java.time.LocalDateTime;

public record DatosCrearTarea(
        String nombre,
        String descripcion,
        LocalDateTime fecha_creacion,
        LocalDateTime fecha_finalizacion,
        Estado estado,
        Importancia importancia,
        Long usuarioId
) {
}
