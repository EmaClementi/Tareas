ALTER TABLE tareas
DROP CONSTRAINT tareas_usuario_id_fkey;

ALTER TABLE tareas
ADD CONSTRAINT tareas_usuario_id_fkey
FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE CASCADE;