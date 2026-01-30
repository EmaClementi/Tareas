package com.tareas.tareas.domain.tarea;

import java.time.LocalDate;

public record DatosFiltroTarea(
        String busqueda,              // Búsqueda en nombre/descripción
        Estado estado,                // Filtrar por estado
        Importancia importancia,      // Filtrar por importancia
        LocalDate fechaDesde,         // Rango de fechas - desde
        LocalDate fechaHasta,         // Rango de fechas - hasta
        Boolean soloVencidas,         // Solo mostrar vencidas
        Integer diasDuracion,          // Dias de duracion de la tarea
        String ordenarPor,            // Campo por el cual ordenar
        String direccion              // ASC o DESC
) {
}