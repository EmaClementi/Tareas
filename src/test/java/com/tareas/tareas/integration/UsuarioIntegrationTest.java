package com.tareas.tareas.integration;

import com.tareas.tareas.Validacion;
import com.tareas.tareas.domain.usuario.*;
import com.tareas.tareas.domain.tarea.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureTestEntityManager;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Commit;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Transactional
@AutoConfigureTestEntityManager
@DisplayName("Tests de Integración - Usuarios")
class UsuarioIntegrationTest {

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private TareaRepository tareaRepository;


    @Test
    @DisplayName("Integración: Crear usuario y verificar que se guarda en BD")
    void crearUsuarioFlujCompleto() {
        // Arrange
        var datosCrearUsuario = new DatosCrearUsuario(
                "Juan",
                "Pérez",
                "juan@gmail.com",
                "password123",
                List.of()
        );

        // Act
        var resultado = usuarioService.crearUsuario(datosCrearUsuario);

        // Assert - Verificar en BD real
        var usuarioEnBD = usuarioRepository.findByEmail("juan@gmail.com");
        assertThat(usuarioEnBD).isNotNull();
        assertThat(usuarioEnBD.getNombre()).isEqualTo("Juan");
        assertThat(usuarioEnBD.getEmail()).isEqualTo("juan@gmail.com");
    }

    @Test
    @DisplayName("Integración: No se puede crear usuario con email duplicado")
    void crearDosUsuariosIgualesLanzaError() {
        // Arrange
        var datosCrearUsuario = new DatosCrearUsuario(
                "Juan",
                "Pérez",
                "juan.duplicado@gmail.com",
                "password123",
                List.of()
        );

        // Act - Crear el primer usuario
        usuarioService.crearUsuario(datosCrearUsuario);

        // Act & Assert - Intentar crear el segundo usuario con el mismo email
        assertThatThrownBy(() -> usuarioService.crearUsuario(datosCrearUsuario))
                .isInstanceOf(Validacion.class)
                .hasMessage("El usuario ya existe");
    }

    @Test
    @DisplayName("Integración: Usuario creado tiene lista de tareas vacía")
    void usuarioNuevoTieneTareasVacia() {
        // Arrange
        var datosCrearUsuario = new DatosCrearUsuario(
                "María",
                "Garcia",
                "maria@gmail.com",
                "password123",
                List.of()
        );

        // Act
        var resultado = usuarioService.crearUsuario(datosCrearUsuario);

        // Assert
        assertThat(resultado.tareas()).isEmpty();
    }



    @Test
    @DisplayName("Integración: Buscar usuario por ID y verificar datos")
    void buscarUsuarioFlujCompleto() {
        // Arrange
        var datosCrearUsuario = new DatosCrearUsuario(
                "Carlos",
                "Lopez",
                "carlos@gmail.com",
                "password123",
                List.of()
        );
        var usuarioCreado = usuarioService.crearUsuario(datosCrearUsuario);

        // Act
        var resultado = usuarioService.buscarUsuario(usuarioCreado.id());

        // Assert
        assertThat(resultado).isNotNull();
        assertThat(resultado.nombre()).isEqualTo("Carlos");
        assertThat(resultado.email()).isEqualTo("carlos@gmail.com");
    }

    @Test
    @DisplayName("Integración: Buscar usuario inexistente lanza error")
    void buscarUsuarioInexistenteLanzaError() {
        // Act & Assert
        assertThatThrownBy(() -> usuarioService.buscarUsuario(99999L))
                .isInstanceOf(Validacion.class)
                .hasMessage("El usuario no existe");
    }

    @Test
    @DisplayName("Integración: Buscar usuario con tareas devuelve todas sus tareas")
    void buscarUsuarioConTareasFlujoCompleto() {
        // Arrange
        var datosCrearUsuario = new DatosCrearUsuario(
                "Ana",
                "Martinez",
                "ana@gmail.com",
                "password123",
                new ArrayList<>()
        );
        var usuarioCreado = usuarioService.crearUsuario(datosCrearUsuario);
        var usuarioEnBD = usuarioRepository.findById(usuarioCreado.id()).get();

        // Crear tareas asociadas
        var tarea1 = new Tarea(
                new DatosCrearTarea("Tarea 1", "Desc 1", Importancia.ALTA, 5, null, null),
                usuarioEnBD
        );
        var tarea2 = new Tarea(
                new DatosCrearTarea("Tarea 2", "Desc 2", Importancia.MEDIA, 3, null, null),
                usuarioEnBD
        );
        usuarioEnBD.agregarTarea(tarea1);
        usuarioEnBD.agregarTarea(tarea2);
        tareaRepository.save(tarea1);
        tareaRepository.save(tarea2);

        // Act
        var resultado = usuarioService.buscarUsuario(usuarioCreado.id());

        // Assert
        assertThat(resultado.tareas()).hasSize(2);
        assertThat(resultado.tareas()).allMatch(t -> t.nombre().startsWith("Tarea"));
    }


    @Test
    @DisplayName("Integración: Listar todos los usuarios")
    void listarUsuariosFlujCompleto() {
        // Arrange
        var datos1 = new DatosCrearUsuario("Usuario 1", "apellido", "usuario1@gmail.com", "pass123", List.of());
        var datos2 = new DatosCrearUsuario("Usuario 2", "apellido", "usuario2@gmail.com", "pass123", List.of());
        var datos3 = new DatosCrearUsuario("Usuario 3", "apellido", "usuario3@gmail.com", "pass123", List.of());

        usuarioService.crearUsuario(datos1);
        usuarioService.crearUsuario(datos2);
        usuarioService.crearUsuario(datos3);

        // Act
        var resultado = usuarioService.listarUsuarios();

        // Assert
        assertThat(resultado.size()).isGreaterThanOrEqualTo(3);
        assertThat(resultado).allMatch(u -> u.email() != null);
    }

    @Test
    void listarUsuariosConTareasFlujoCompleto() {
        // Arrange
        var datosCrearUsuario = new DatosCrearUsuario(
                "Usuario con tareas",
                "apellido",
                "usuario.tareas@gmail.com",
                "pass123",
                new ArrayList<>()
        );
        usuarioService.crearUsuario(datosCrearUsuario);

        // Buscar el usuario creado por email

        var usuarioEnBD =  usuarioRepository.findByEmail("usuario.tareas@gmail.com");
        // Crear una tarea
        var tarea = new Tarea(
                new DatosCrearTarea("Tarea importante", "Desc", Importancia.ALTA, 5, null, null),
                usuarioEnBD
        );
        usuarioEnBD.agregarTarea(tarea);
        tareaRepository.save(tarea);

        // Act
        var resultado = usuarioService.listarUsuarios();

        // Assert
        var usuarioConTareas = resultado.stream()
                .filter(u -> u.email().equals("usuario.tareas@gmail.com"))
                .findFirst();

        assertThat(usuarioConTareas).isPresent();
        assertThat(usuarioConTareas.get().tareas()).hasSize(1);
    }



    @Test
    @DisplayName("Integración: Editar usuario y verificar cambios en BD")
    void editarUsuarioFlujoCompleto() {
        // Arrange
        var datosCrearUsuario = new DatosCrearUsuario(
                "Usuario Original",
                "Usuario apellido",
                "usuario.original@gmail.com",
                "password123",
                List.of()
        );
        var usuarioCreado = usuarioService.crearUsuario(datosCrearUsuario);

        var datosActualizar = new DatosActualizarUsuario(
                usuarioCreado.id(),
                "Usuario Actualizado",
                "Nuevo Apellido",
                "nuevo.email@gmail.com",
                "password123"
        );

        // Act
        var resultado = usuarioService.modificarUsuario(datosActualizar);

        // Assert - Verificar en BD real
        var usuarioEnBD = usuarioRepository.findById(usuarioCreado.id());
        assertThat(usuarioEnBD).isPresent();
        assertThat(usuarioEnBD.get().getNombre()).isEqualTo("Usuario Actualizado");
    }

    @Test
    @DisplayName("Integración: Editar usuario inexistente lanza error")
    void editarUsuarioInexistenteLanzaError() {
        // Arrange
        var datosActualizar = new DatosActualizarUsuario(
                99999L,
                "Nombre",
                "Apellido",
                "email@gmail.com",
                "password123"
        );

        // Act & Assert
        assertThatThrownBy(() -> usuarioService.modificarUsuario(datosActualizar))
                .isInstanceOf(Validacion.class)
                .hasMessage("El usuario no existe");
    }

    @Test
    @DisplayName("Integración: Editar usuario mantiene sus tareas")
    void editarUsuarioMantieneTareasFlujCompleto() {
        // Arrange
        var datosCrearUsuario = new DatosCrearUsuario(
                "Usuario E2E",
                "Usuario Apellido",
                "usuario.e2e@gmail.com",
                "password123",
                new ArrayList<>()
        );
        var usuarioCreado = usuarioService.crearUsuario(datosCrearUsuario);
        var usuarioEnBD = usuarioRepository.findById(usuarioCreado.id()).get();

        // Crear tareas
        var tarea1 = new Tarea(
                new DatosCrearTarea("Tarea 1", "Desc 1", Importancia.ALTA, 5, null, null),
                usuarioEnBD
        );
        var tarea2 = new Tarea(
                new DatosCrearTarea("Tarea 2", "Desc 2", Importancia.MEDIA, 3, null, null),
                usuarioEnBD
        );
        usuarioEnBD.agregarTarea(tarea1);
        usuarioEnBD.agregarTarea(tarea1);
        tareaRepository.save(tarea1);
        tareaRepository.save(tarea2);



        // Editar usuario
        var datosActualizar = new DatosActualizarUsuario(
                usuarioCreado.id(),
                "Usuario Modificado",
                "Apellido Nuevo",
                "nuevo@gmail.com",
                "password123"
        );

        // Act
        usuarioService.modificarUsuario(datosActualizar);

        // Assert - Verificar que las tareas siguen asociadas
        var resultado = usuarioService.buscarUsuario(usuarioCreado.id());
        assertThat(resultado.tareas()).hasSize(2);
    }


    @Test
    @DisplayName("Integración: Eliminar usuario lo remueve de BD")
    void eliminarUsuarioFlujCompleto() {
        // Arrange
        var datosCrearUsuario = new DatosCrearUsuario(
                "Usuario E2E",
                "Usuario Apellido",
                "usuario.e2e@gmail.com",
                "password123",
                List.of()
        );
        var usuarioCreado = usuarioService.crearUsuario(datosCrearUsuario);

        // Act
        usuarioService.eliminarUsuario(usuarioCreado.id());

        // Assert
        var usuarioEnBD = usuarioRepository.findById(usuarioCreado.id());
        assertThat(usuarioEnBD).isEmpty();
    }

    @Test
    @DisplayName("Integración: Eliminar usuario inexistente lanza error")
    void eliminarUsuarioInexistenteLanzaError() {
        // Act & Assert
        assertThatThrownBy(() -> usuarioService.eliminarUsuario(99999L))
                .isInstanceOf(Validacion.class)
                .hasMessage("El usuario no existe");
    }

    @Test
    @DisplayName("Integración: Eliminar usuario también elimina sus tareas")
    void eliminarUsuarioEliminaSusTareasFlujCompleto() {
        // Arrange
        var datosCrearUsuario = new DatosCrearUsuario(
                "Usuario E2E",
                "Usuario Apellido",
                "usuario.e2e@gmail.com",
                "password123",
                new ArrayList<>()
        );
        var usuarioCreado = usuarioService.crearUsuario(datosCrearUsuario);
        var usuarioEnBD = usuarioRepository.findById(usuarioCreado.id()).get();

        // Crear tareas
        var tarea1 = new Tarea(
                new DatosCrearTarea("Tarea a eliminar 1", "Desc", Importancia.ALTA, 5, null, null),
                usuarioEnBD
        );
        var tarea2 = new Tarea(
                new DatosCrearTarea("Tarea a eliminar 2", "Desc", Importancia.MEDIA, 3, null, null),
                usuarioEnBD
        );
        usuarioEnBD.agregarTarea(tarea1);
        usuarioEnBD.agregarTarea(tarea2);

        tareaRepository.save(tarea1);
        tareaRepository.save(tarea2);

        // Verificar que las tareas existen
        var tareasAntesDelEliminado = tareaRepository.findByUsuario(usuarioEnBD);
        assertThat(tareasAntesDelEliminado).hasSize(2);

        // Act
        usuarioService.eliminarUsuario(usuarioCreado.id());

        // Assert
        var usuarioEnBD_despues = usuarioRepository.findById(usuarioCreado.id());
        assertThat(usuarioEnBD_despues).isEmpty();
        assertThat(tareaRepository.findById(tarea1.getId())).isEmpty();
        assertThat(tareaRepository.findById(tarea2.getId())).isEmpty();
    }


    @Test
    @DisplayName("Integración E2E: Usuario crea, edita y elimina tareas")
    void flujoCompletoUsuarioConTareasE2E() {
        // Arrange - Crear usuario
        var datosCrearUsuario = new DatosCrearUsuario(
                "Usuario E2E",
                "Usuario Apellido",
                "usuario.e2e@gmail.com",
                "password123",
                List.of()
        );
        var usuarioCreado = usuarioService.crearUsuario(datosCrearUsuario);
        var usuarioEnBD = usuarioRepository.findById(usuarioCreado.id()).get();

        // Act 1 - Crear una tarea
        var datosCrearTarea = new DatosCrearTarea(
                "Tarea E2E",
                "Esta es una tarea de flujo E2E",
                Importancia.ALTA,
                7,
                null,
                null
        );
        var tarea = new Tarea(datosCrearTarea, usuarioEnBD);
        tareaRepository.save(tarea);

        // Assert 1 - Verificar que la tarea se creó
        var tareasDelUsuario1 = tareaRepository.findByUsuario(usuarioEnBD);
        assertThat(tareasDelUsuario1).hasSize(1);

        // Act 2 - Editar la tarea
        tarea.actualizarTarea(
                new DatosActualizarTarea(
                        "Tarea E2E Editada",
                        "Descripción editada",
                        Estado.EN_PROGRESO,
                        Importancia.ALTA,
                        10,
                        null,
                        null
                ),
                usuarioEnBD
        );
        tareaRepository.save(tarea);

        // Assert 2 - Verificar los cambios
        var tareaEditada = tareaRepository.findById(tarea.getId());
        assertThat(tareaEditada).isPresent();
        assertThat(tareaEditada.get().getNombre()).isEqualTo("Tarea E2E Editada");
        assertThat(tareaEditada.get().getEstado()).isEqualTo(Estado.EN_PROGRESO);

        // Act 3 - Eliminar la tarea
        tareaRepository.delete(tarea);

        // Assert 3 - Verificar que se eliminó
        var tareasDelUsuario2 = tareaRepository.findByUsuario(usuarioEnBD);
        assertThat(tareasDelUsuario2).isEmpty();

        // Act 4 - Verificar que el usuario sigue existiendo
        var usuarioFinal = usuarioRepository.findById(usuarioCreado.id());
        assertThat(usuarioFinal).isPresent();
    }

}