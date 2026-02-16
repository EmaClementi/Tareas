package com.tareas.tareas.domain.usuario;

import com.tareas.tareas.domain.tarea.DatosRespuestaTarea;
import com.tareas.tareas.domain.tarea.Tarea;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record DatosRespuestaUsuario(
        Long id,
        @NotNull String nombre,
        @NotNull String email,
        List<DatosRespuestaTarea> tareas
) {
    public DatosRespuestaUsuario(Usuario usuario) {
        this(usuario.getId(),usuario.getNombre(), usuario.getEmail(), usuario.tareas());
    }
}
