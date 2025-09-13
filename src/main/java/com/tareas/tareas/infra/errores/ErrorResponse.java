package com.tareas.tareas.infra.errores;

import java.time.LocalDateTime;
import java.util.List;

public record ErrorResponse<T>(
        int status,
        String error,
        List<?> detalles,
        LocalDateTime timestamp

) {
}
