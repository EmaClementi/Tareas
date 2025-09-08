package com.tareas.tareas.domain.usuario;

import jakarta.validation.constraints.NotNull;

public record DatosListaUsuarios(
        @NotNull Long id,
        @NotNull String nombre,
        @NotNull String email,
        @NotNull String clave
) {
}
