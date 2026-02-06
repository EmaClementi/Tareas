CREATE TABLE tareas (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(255) NOT NULL UNIQUE,
    descripcion VARCHAR(255) NOT NULL,
    fecha_creacion DATETIME NOT NULL,
    fecha_finalizacion DATETIME NOT NULL,
    estado VARCHAR(50) NOT NULL,
    importancia VARCHAR(50) NOT NULL,
    usuario_id BIGINT NOT NULL,
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id)
)