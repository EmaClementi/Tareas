package com.tareas.tareas.domain.usuario;

import com.tareas.tareas.domain.tarea.Tarea;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.awt.geom.NoninvertibleTransformException;
import java.util.List;

public record DatosCrearUsuario(
        @NotNull(message = "El nombre es obligatorio") String nombre,
        @NotNull(message = "El apellido es obligatorio") String apellido,

        @NotNull(message = "El email es obligatorio")
        @Email(message = "Debe ser un email válido")
        String email,
        @NotNull(message = "La clave es obligatoria")
        @NotBlank
        @Size(min = 6, max = 10, message = "La clave debe tener al menos 6 caracteres")
        String clave,
        List<Tarea> tareas
) {
}
