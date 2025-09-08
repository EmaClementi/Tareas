package com.tareas.tareas.domain.usuario;

import com.tareas.tareas.domain.tarea.Tarea;
import jakarta.validation.constraints.NotNull;

import java.awt.geom.NoninvertibleTransformException;
import java.util.List;

public record DatosCrearUsuario(
        @NotNull String nombre,
        @NotNull String email,
        @NotNull String clave,
        List<Tarea> tareas
) {
}
