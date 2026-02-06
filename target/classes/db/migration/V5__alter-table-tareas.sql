ALTER TABLE tareas
ADD COLUMN fecha_inicio DATE,
ADD COLUMN fecha_vencimiento DATE;

UPDATE tareas
SET fecha_vencimiento = DATE_ADD(DATE(fecha_creacion), INTERVAL duracion_dias DAY)
WHERE fecha_vencimiento IS NULL AND duracion_dias IS NOT NULL;
