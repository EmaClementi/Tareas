package com.tareas.tareas.domain.tarea;

import java.util.Map;

public record DatosEstadisticasTarea(
        Long totalTareas,
        Long tareasCompletadas,
        Long tareasPendientes,
        Long tareasEnProgreso,
        Long tareasCanceladas,
        Long tareasVencidas,
        Long tareasCompletadasHoy,
        Long tareasCompletadasEstaSemana,
        Map<String, Long> tareasPorEstado,
        Map<String, Long> tareasPorImportancia,
        Double porcentajeCompletado,
        Double porcentajePendiente,
        Double porcentajeEnProgreso,
        Double porcentajeCancelado,
        Double porcentajeVencido
) {
}
