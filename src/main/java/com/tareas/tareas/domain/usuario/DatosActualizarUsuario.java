package com.tareas.tareas.domain.usuario;

import com.tareas.tareas.domain.tarea.Tarea;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record DatosActualizarUsuario(
        @NotNull Long id,
        @NotNull String nombre,
        @NotNull String email,
        @NotNull String clave
) {
}
