package com.tareas.tareas.domain.tarea;

import com.tareas.tareas.domain.usuario.Usuario;
import jakarta.validation.constraints.NotNull;

public record DatosRespuestaTarea(
        @NotNull String nombre,
        @NotNull String descripcion,
        @NotNull Estado estado,
        @NotNull Importancia importancia,
        @NotNull Long usuarioId
) {
    public DatosRespuestaTarea(Tarea nuevaTarea) {
        this(nuevaTarea.getNombre(),nuevaTarea.getDescripcion(), nuevaTarea.getEstado(), nuevaTarea.getImportancia(), nuevaTarea.getUsuario().getId());

    }
}
