ALTER TABLE tareas DROP CONSTRAINT tareas_nombre_key;

ALTER TABLE tareas ADD CONSTRAINT uk_tarea_usuario_nombre
  UNIQUE (usuario_id, nombre);