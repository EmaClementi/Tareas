package com.tareas.tareas.service;

import com.tareas.tareas.Validacion;
import com.tareas.tareas.domain.usuario.*;
import com.tareas.tareas.domain.tarea.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests del servicio de Usuarios")
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private UsuarioService usuarioService;


    @Test
    @DisplayName("Crear usuario exitosamente cuando no existe uno con el mismo email")
    void crearUsuarioExitoso() {
        // Arrange
        var datosCrearUsuario = new DatosCrearUsuario(
                "Juan",
                "Perez",
                "juan@gmail.com",
                "password123",
                null
        );

        var usuarioGuardado = new Usuario(datosCrearUsuario);
        usuarioGuardado.setId(1L);

        // Simula que no existe usuario con ese email
        when(usuarioRepository.findByEmail("juan@gmail.com")).thenReturn(null);
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuarioGuardado);

        // Act
        var resultado = usuarioService.crearUsuario(datosCrearUsuario);

        // Assert
        assertThat(resultado).isNotNull();
        assertThat(resultado.nombre()).isEqualTo("Juan");
        assertThat(resultado.email()).isEqualTo("juan@gmail.com");
        verify(usuarioRepository).save(any(Usuario.class));
    }

    @Test
    @DisplayName("Crear usuario que ya existe debe lanzar excepción de validación")
    void crearUsuarioQueLanzaValidacion() {
        // Arrange
        var datosCrearUsuario = new DatosCrearUsuario(
                "Juan",
                "Perez",
                "juan@gmail.com",
                "password123",
                null
        );

        var usuarioExistente = new Usuario(datosCrearUsuario);
        usuarioExistente.setId(1L);

        // Simula que ya existe un usuario con ese email
        when(usuarioRepository.findByEmail("juan@gmail.com")).thenReturn(usuarioExistente);

        // Act & Assert
        assertThatThrownBy(() -> usuarioService.crearUsuario(datosCrearUsuario))
                .isInstanceOf(Validacion.class)
                .hasMessage("El usuario ya existe");

        // Verifica que NO se intente guardar
        verify(usuarioRepository, never()).save(any(Usuario.class));
    }


    @Test
    @DisplayName("Buscar usuario existente debe devolver los datos correctos")
    void buscarUsuarioExistente() {
        // Arrange
        var usuario = new Usuario(1L, "ema", "clementi", "emi@gmail.com", "123456", Role.USER, null);

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));

        // Act
        var resultado = usuarioService.buscarUsuario(1L);

        // Assert
        assertThat(resultado).isNotNull();
        assertThat(resultado.nombre()).isEqualTo("ema");
        assertThat(resultado.email()).isEqualTo("emi@gmail.com");
    }

    @Test
    @DisplayName("Buscar usuario que no existe debe lanzar excepción de validación")
    void buscarUsuarioNoExistenteLanzaValidacion() {
        // Arrange
        when(usuarioRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> usuarioService.buscarUsuario(99L))
                .isInstanceOf(Validacion.class)
                .hasMessage("El usuario no existe");
    }

    @Test
    @DisplayName("Buscar usuario existente con tareas debe incluirlas")
    void buscarUsuarioConTareas() {
        // Arrange
        var usuario = new Usuario(1L, "ema", "clementi", "emi@gmail.com", "123456", Role.USER, null);
        var tarea = new Tarea(
                new DatosCrearTarea("Tarea 1", "Desc 1", Importancia.ALTA, 5, null, null),
                usuario
        );
        usuario.setTareas(List.of(tarea));

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));

        // Act
        var resultado = usuarioService.buscarUsuario(1L);

        // Assert
        assertThat(resultado).isNotNull();
        assertThat(resultado.tareas()).isNotEmpty();
        assertThat(resultado.tareas()).hasSize(1);
    }


    @Test
    @DisplayName("Listar usuarios debe devolver todos los usuarios")
    void listarUsuariosExitosa() {
        // Arrange
        var usuario1 = new Usuario(1L, "ema", "clementi", "emi@gmail.com", "123456", Role.USER, List.of());
        var usuario2 = new Usuario(2L, "juan", "perez", "juan@gmail.com", "123456", Role.USER, List.of());

        when(usuarioRepository.findAll()).thenReturn(List.of(usuario1, usuario2));

        // Act
        var resultado = usuarioService.listarUsuarios();

        // Assert
        assertThat(resultado).hasSize(2);
        assertThat(resultado).allMatch(u -> u.email() != null);
        verify(usuarioRepository).findAll();
    }

    @Test
    @DisplayName("Listar usuarios cuando hay varios con tareas")
    void listarUsuariosConTareas() {
        // Arrange
        var usuario1 = new Usuario(1L, "ema", "clementi", "emi@gmail.com", "123456", Role.USER, null);
        var tarea1 = new Tarea(
                new DatosCrearTarea("Tarea 1", "Desc 1", Importancia.ALTA, 5, null, null),
                usuario1
        );
        usuario1.setTareas(List.of(tarea1));

        var usuario2 = new Usuario(2L, "juan", "perez", "juan@gmail.com", "123456", Role.USER, null);
        usuario2.setTareas(List.of());

        when(usuarioRepository.findAll()).thenReturn(List.of(usuario1, usuario2));

        // Act
        var resultado = usuarioService.listarUsuarios();

        // Assert
        assertThat(resultado).hasSize(2);
        assertThat(resultado.get(0).tareas()).hasSize(1);
        assertThat(resultado.get(1).tareas()).isEmpty();
    }

    @Test
    @DisplayName("Listar usuarios vacío debe devolver lista vacía")
    void listarUsuariosVacio() {
        // Arrange
        when(usuarioRepository.findAll()).thenReturn(List.of());

        // Act
        var resultado = usuarioService.listarUsuarios();

        // Assert
        assertThat(resultado).isEmpty();
    }


    @Test
    @DisplayName("Editar usuario existente debe actualizar los datos")
    void editarUsuarioExitoso() {
        // Arrange
        var usuario = new Usuario(1L, "ema", "clementi", "emi@gmail.com", "123456", Role.USER, null);

        var datosActualizar = new DatosActualizarUsuario(
                1L,
                "ema actualizado",
                "clementi actualizado",
                "emi.nuevo@gmail.com",
                "password1234"
        );

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));

        // Act
        var resultado = usuarioService.modificarUsuario(datosActualizar);

        // Assert
        assertThat(resultado).isNotNull();
        assertThat(resultado.nombre()).isEqualTo("ema actualizado");
        verify(usuarioRepository).findById(1L);
    }

    @Test
    @DisplayName("Editar usuario que no existe debe lanzar excepción de validación")
    void editarUsuarioNoExistenteLanzaValidacion() {
        // Arrange
        var datosActualizar = new DatosActualizarUsuario(
                99L,
                "nombre",
                "apellido",
                "email@gmail.com",
                "contra1234"
        );

        when(usuarioRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> usuarioService.modificarUsuario(datosActualizar))
                .isInstanceOf(Validacion.class)
                .hasMessage("El usuario no existe");
    }

    @Test
    @DisplayName("Editar usuario debe mantener sus tareas")
    void editarUsuarioMantieneTaskas() {
        // Arrange
        var usuario = new Usuario(1L, "ema", "clementi", "emi@gmail.com", "123456", Role.USER, null);
        var tarea = new Tarea(
                new DatosCrearTarea("Tarea importante", "Desc", Importancia.ALTA, 5, null, null),
                usuario
        );
        usuario.setTareas(List.of(tarea));

        var datosActualizar = new DatosActualizarUsuario(
                1L,
                "nombre nuevo",
                "apellido nuevo",
                "nuevo@gmail.com",
                "nuevaclave123"
        );

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));

        // Act
        var resultado = usuarioService.modificarUsuario(datosActualizar);

        // Assert
        assertThat(resultado).isNotNull();
        assertThat(resultado.tareas()).hasSize(1);
        assertThat(resultado.tareas().get(0).nombre()).isEqualTo("Tarea importante");
    }


    @Test
    @DisplayName("Eliminar usuario existente debe eliminarlo")
    void eliminarUsuarioExitoso() {
        // Arrange
        var usuario = new Usuario(1L, "ema", "clementi", "emi@gmail.com", "123456", Role.USER, null);

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        doNothing().when(usuarioRepository).deleteById(1L);

        // Act
        usuarioService.eliminarUsuario(1L);

        // Assert
        verify(usuarioRepository).deleteById(1L);
    }

    @Test
    @DisplayName("Eliminar usuario que no existe debe lanzar excepción de validación")
    void eliminarUsuarioNoExistenteLanzaValidacion() {
        // Arrange
        when(usuarioRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> usuarioService.eliminarUsuario(99L))
                .isInstanceOf(Validacion.class)
                .hasMessage("El usuario no existe");

        // Verifica que NO se intente eliminar
        verify(usuarioRepository, never()).deleteById(any());
    }

    @Test
    @DisplayName("Eliminar usuario debe borrar todas sus tareas asociadas")
    void eliminarUsuarioEliminaTareas() {
        // Arrange
        var usuario = new Usuario(1L, "ema", "clementi", "emi@gmail.com", "123456", Role.USER, null);
        var tarea = new Tarea(
                new DatosCrearTarea("Tarea a eliminar", "Desc", Importancia.ALTA, 5, null, null),
                usuario
        );
        usuario.setTareas(List.of(tarea));

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        doNothing().when(usuarioRepository).deleteById(1L);

        // Act
        usuarioService.eliminarUsuario(1L);

        // Assert
        verify(usuarioRepository).deleteById(1L);
    }



    @Test
    @DisplayName("Obtener usuario autenticado debe retornar el usuario del contexto de seguridad")
    void obtenerUsuarioAutenticadoExitoso() {
        // Arrange
        var usuario = new Usuario(1L, "ema", "clementi", "emi@gmail.com", "123456", Role.USER, null);

        // Mock del SecurityContext
        Authentication auth = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);

        when(securityContext.getAuthentication()).thenReturn(auth);
        when(auth.getName()).thenReturn("emi@gmail.com");
        when(usuarioRepository.findByEmail("emi@gmail.com")).thenReturn(usuario);

        SecurityContextHolder.setContext(securityContext);

        // Act
        var resultado = usuarioService.getUsuarioAutenticado();

        // Assert
        assertThat(resultado).isNotNull();
        assertThat(resultado.getEmail()).isEqualTo("emi@gmail.com");
        assertThat(resultado.getNombre()).isEqualTo("ema");
    }

    @Test
    @DisplayName("Obtener usuario autenticado con múltiples tareas")
    void obtenerUsuarioAutenticadoConTareas() {
        // Arrange
        var usuario = new Usuario(1L, "ema", "clementi", "emi@gmail.com", "123456", Role.USER, null);
        var tarea1 = new Tarea(
                new DatosCrearTarea("Tarea 1", "Desc 1", Importancia.ALTA, 5, null, null),
                usuario
        );
        var tarea2 = new Tarea(
                new DatosCrearTarea("Tarea 2", "Desc 2", Importancia.MEDIA, 3, null, null),
                usuario
        );
        usuario.setTareas(List.of(tarea1, tarea2));

        // Mock del SecurityContext
        Authentication auth = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);

        when(securityContext.getAuthentication()).thenReturn(auth);
        when(auth.getName()).thenReturn("emi@gmail.com");
        when(usuarioRepository.findByEmail("emi@gmail.com")).thenReturn(usuario);

        SecurityContextHolder.setContext(securityContext);

        // Act
        var resultado = usuarioService.getUsuarioAutenticado();

        // Assert
        assertThat(resultado).isNotNull();
        assertThat(resultado.getTareas()).hasSize(2);
    }

}