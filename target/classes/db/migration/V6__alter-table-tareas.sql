UPDATE tareas
SET fecha_vencimiento = fecha_creacion::DATE + duracion_dias
WHERE fecha_vencimiento IS NULL AND duracion_dias IS NOT NULL;