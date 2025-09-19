# Aplicación de lista de tareas con autenticación, CRUD de tareas y gestión de usuarios.

## Descripción

Esta aplicación permite a los usuarios:
- Registrarse y autenticarse con JWT.
- Crear, actualizar, eliminar y consultar tareas.
- Validar datos usando anotaciones de Spring Validation.

## Tecnologías

- Java 17
- Spring Boot 3
- Spring Security
- Spring Data JPA
- MySQL
- Flyway (migraciones de BD)
- JWT (Auth0)
- JUnit + Mockito (tests)
- Lombok

## Endpoints

### Autenticación
POST /auth/registro  → Registrar usuario
POST /auth/login     → Login y obtención de JWT

### Tareas (requiere token)
GET /tareas          → Listar tareas
GET /tareas/{id}     → Consultar tarea por id
GET /tareas/nombre/{nombre}
POST /tareas         → Crear tarea
PUT /tareas          → Actualizar tarea
DELETE /tareas/{id}  → Eliminar tarea

## Test
Se utilizan tests de integración con MockMvc y Mockito.
### En tareas:
- Creación de tareas con datos válidos e inválidos.
- Búsqueda de tareas existentes y no existentes.
- Actualización de tareas, incluyendo validaciones de campos y tareas inexistentes.
- Eliminación de tareas, incluyendo intentos sobre tareas no existentes.
- Acceso a endpoints protegido por JWT con y sin token, o con token inválido.

### En usuario:
- Registro de usuarios, incluyendo validación de email duplicado y campos inválidos.
- Autenticación de usuarios con credenciales correctas e incorrectas.
- Generación y validación de tokens JWT.
  
