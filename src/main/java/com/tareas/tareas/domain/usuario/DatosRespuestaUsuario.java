package com.tareas.tareas.domain.usuario;

import com.tareas.tareas.domain.tarea.Tarea;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record DatosRespuestaUsuario(
        @NotNull String nombre,
        @NotNull String email
) {
    public DatosRespuestaUsuario(Usuario usuario) {
        this(usuario.getNombre(), usuario.getEmail());
    }
}
