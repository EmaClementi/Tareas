package com.tareas.tareas.service;

import com.tareas.tareas.Validacion;
import com.tareas.tareas.domain.tarea.*;
import com.tareas.tareas.domain.usuario.Usuario;
import com.tareas.tareas.domain.usuario.Role;
import com.tareas.tareas.domain.usuario.UsuarioRepository;
import com.tareas.tareas.domain.usuario.UsuarioService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


//Un test sigue la regla AAA (Arrange, Act, Assert):
//Arrange (Given) → preparás los datos.
//Act (When) → ejecutás la acción.
//Assert (Then) → verificás el resultado.

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests del servicio de Tareas")
class TareaServiceTest {

    @Mock
    private TareaRepository tareaRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private UsuarioService usuarioService;

    @InjectMocks
    private TareaService tareaService;


    @Test
    @DisplayName("Crear tarea exitosamente cuando no existe una con el mismo nombre")
    void crearTareaExitosa() {
        // Arrange
        var usuario = new Usuario(1L, "ema", "clementi", "emi@gmail.com", "123456", Role.USER, null);
        var datosCrearTarea = new DatosCrearTarea(
                "Hacer ejercicio",
                "Rutina de piernas",
                Importancia.ALTA,
                5,
                null,
                null
        );

        var tarea = new Tarea(datosCrearTarea, usuario);

        // Simula que la tarea no existe aún
        when(tareaRepository.existsByUsuarioIdAndNombre(usuario.getId(), "Hacer ejercicio"))
                .thenReturn(false);
        when(tareaRepository.save(any(Tarea.class))).thenReturn(tarea);

        // Act
        var resultado = tareaService.crearTarea(datosCrearTarea, usuario);

        // Assert
        assertThat(resultado).isNotNull();
        assertThat(resultado.nombre()).isEqualTo("Hacer ejercicio");
        assertThat(resultado.importancia()).isEqualTo(Importancia.ALTA);
        verify(tareaRepository).save(any(Tarea.class));
    }

    @Test
    @DisplayName("Crear tarea que ya existe debe lanzar excepción de validación")
    void crearTareaQueLanzaValidacion() {
        // Arrange
        var usuario = new Usuario(1L, "ema", "clementi", "emi@gmail.com", "123456", Role.USER, null);
        var datosCrearTarea = new DatosCrearTarea(
                "Tarea existente",
                "Descripción",
                Importancia.MEDIA,
                3,
                null,
                null
        );

        // Simula que la tarea ya existe
        when(tareaRepository.existsByUsuarioIdAndNombre(usuario.getId(), "Tarea existente"))
                .thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> tareaService.crearTarea(datosCrearTarea, usuario))
                .isInstanceOf(Validacion.class)
                .hasMessage("La tarea ya existe");

        // Verifica que NO se intente guardar
        verify(tareaRepository, never()).save(any(Tarea.class));
    }


    @Test
    @DisplayName("Buscar tarea existente debe devolver los datos correctos")
    void buscarTareaExistente() {
        // Arrange
        var usuario = new Usuario(1L, "ema", "clementi", "emi@gmail.com", "123456", Role.USER, null);
        var datosCrearTarea = new DatosCrearTarea("Ir al médico", "Controlar resultados", Importancia.ALTA, 5, null, null);
        var tarea = new Tarea(datosCrearTarea, usuario);
        tarea.setId(1L);

        when(tareaRepository.findByIdAndUsuario(1L, usuario)).thenReturn(Optional.of(tarea));

        // Act
        var resultado = tareaService.buscarTareaPorId(1L, usuario);

        // Assert
        assertThat(resultado).isNotNull();
        assertThat(resultado.nombre()).isEqualTo("Ir al médico");
        assertThat(resultado.descripcion()).isEqualTo("Controlar resultados");
    }

    @Test
    @DisplayName("Buscar tarea que no existe debe lanzar excepción de validación")
    void buscarTareaNoExistenteLanzaValidacion() {
        // Arrange
        var usuario = new Usuario(1L, "ema", "clementi", "emi@gmail.com", "123456", Role.USER, null);

        when(tareaRepository.findByIdAndUsuario(99L, usuario)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> tareaService.buscarTareaPorId(99L, usuario))
                .isInstanceOf(Validacion.class)
                .hasMessage("La tarea buscada no existe");
    }

    @Test
    @DisplayName("Buscar tarea de otro usuario debe lanzar excepción")
    void buscarTareaDeOtroUsuarioLanzaExcepcion() {
        var usuario1 = new Usuario(1L, "ema", "clementi", "emi@gmail.com", "123456", Role.USER, null);
        var usuario2 = new Usuario(2L, "juan", "perez", "juan@gmail.com", "123456", Role.USER, null);

        // Usuario2 intenta buscar pero la BD devuelve vacío (porque el repository verifica usuario)
        when(tareaRepository.findByIdAndUsuario(1L, usuario2)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> tareaService.buscarTareaPorId(1L, usuario2))
                .isInstanceOf(Validacion.class);
    }


    @Test
    @DisplayName("Buscar tareas por nombre debe devolver las coincidencias")
    void buscarTareaPorNombreExitosa() {
        // Arrange
        var usuario = new Usuario(1L, "ema", "clementi", "emi@gmail.com", "123456", Role.USER, null);
        var tarea1 = new Tarea(
                new DatosCrearTarea("Ir al médico", "Check", Importancia.ALTA, 5, null, null),
                usuario
        );
        var tarea2 = new Tarea(
                new DatosCrearTarea("Llamar al médico", "Agendar", Importancia.MEDIA, 4, null, null),
                usuario
        );

        when(tareaRepository.findByNombreContainingIgnoreCaseAndUsuario("médico", usuario))
                .thenReturn(List.of(tarea1, tarea2));

        // Act
        var resultado = tareaService.buscarTareaPorNombre("médico", usuario);

        // Assert
        assertThat(resultado).hasSize(2);
        assertThat(resultado).allMatch(t -> t.nombre().contains("médico") || t.nombre().contains("Médico"));
    }

    @Test
    @DisplayName("Buscar tareas por nombre sin coincidencias debe devolver lista vacía")
    void buscarTareaPorNombreSinResultados() {
        // Arrange
        var usuario = new Usuario(1L, "ema", "clementi", "emi@gmail.com", "123456", Role.USER, null);

        when(tareaRepository.findByNombreContainingIgnoreCaseAndUsuario("inexistente", usuario))
                .thenReturn(List.of());

        // Act
        var resultado = tareaService.buscarTareaPorNombre("inexistente", usuario);

        // Assert
        assertThat(resultado).isEmpty();
    }


    @Test
    @DisplayName("Obtener tareas por usuario debe devolver todas sus tareas")
    void obtenerTareasPorUsuarioExitosa() {
        // Arrange
        var usuario = new Usuario(1L, "ema", "clementi", "emi@gmail.com", "123456", Role.USER, null);
        var tarea1 = new Tarea(
                new DatosCrearTarea("Tarea 1", "Desc 1", Importancia.ALTA, 5, null, null),
                usuario
        );
        var tarea2 = new Tarea(
                new DatosCrearTarea("Tarea 2", "Desc 2", Importancia.MEDIA, 4, null, null),
                usuario
        );

        when(tareaRepository.findByUsuario(usuario))
                .thenReturn(List.of(tarea1, tarea2));

        // Act
        var resultado = tareaService.obtenerTareasPorUsuario(usuario);

        // Assert
        assertThat(resultado).hasSize(2);
        verify(tareaRepository).findByUsuario(usuario);
    }

    @Test
    @DisplayName("Usuario sin tareas debe lanzar RuntimeException")
    void obtenerTareasPorUsuarioSinTareas() {
        // Arrange
        var usuario = new Usuario(1L, "ema", "clementi", "emi@gmail.com", "123456", Role.USER, null);

        when(tareaRepository.findByUsuario(usuario))
                .thenReturn(List.of());

        // Act & Assert
        assertThatThrownBy(() -> tareaService.obtenerTareasPorUsuario(usuario))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("El usuario no tiene tareas asignadas");
    }


    @Test
    @DisplayName("Editar tarea existente debe actualizar los datos")
    void editarTareaExitosa() {
        // Arrange
        var usuario = new Usuario(1L, "ema", "clementi", "emi@gmail.com", "123456", Role.USER, null);
        var tarea = new Tarea(
                new DatosCrearTarea("Tarea original", "Desc original", Importancia.BAJA, 3, LocalDate.now(), null),
                usuario
        );
        tarea.setId(1L);

        tarea.setFechaCreacion(LocalDateTime.now());
        var datosActualizar = new DatosActualizarTarea(
                "Tarea actualizada",
                "Nueva descripción",
                Estado.EN_PROGRESO,
                Importancia.ALTA,
                7,
                LocalDate.now(),
                null
        );

        when(tareaRepository.findByIdAndUsuario(1L, usuario)).thenReturn(Optional.of(tarea));

        // Act
        var resultado = tareaService.editarTarea(datosActualizar, usuario, 1L);

        // Assert
        assertThat(resultado).isNotNull();
        verify(tareaRepository).findByIdAndUsuario(1L, usuario);
    }

    @Test
    @DisplayName("Editar tarea que no existe debe lanzar excepción de validación")
    void editarTareaInexistenteLanzaValidacion() {
        // Arrange
        var usuario = new Usuario(1L, "ema", "clementi", "emi@gmail.com", "123456", Role.USER, null);
        var datosActualizar = new DatosActualizarTarea("Nombre", "Desc", Estado.PENDIENTE, Importancia.ALTA, 5, null, null);

        when(tareaRepository.findByIdAndUsuario(99L, usuario)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> tareaService.editarTarea(datosActualizar, usuario, 99L))
                .isInstanceOf(Validacion.class)
                .hasMessage("No existe la tarea o el usuario");
    }


    @Test
    @DisplayName("Eliminar tarea existente debe eliminarla")
    void eliminarTareaExitosa() {
        // Arrange
        var usuario = new Usuario(1L, "ema", "clementi", "emi@gmail.com", "123456", Role.USER, null);
        var tarea = new Tarea(
                new DatosCrearTarea("Tarea a eliminar", "Desc", Importancia.ALTA, 5, null, null),
                usuario
        );
        tarea.setId(1L);

        when(tareaRepository.findByIdAndUsuario(1L, usuario)).thenReturn(Optional.of(tarea));
        doNothing().when(tareaRepository).delete(any(Tarea.class));

        // Act
        tareaService.eliminarTarea(1L, usuario);

        // Assert
        verify(tareaRepository).delete(tarea);
    }

    @Test
    @DisplayName("Eliminar tarea que no existe debe lanzar excepción de validación")
    void eliminarTareaInexistenteLanzaValidacion() {
        // Arrange
        var usuario = new Usuario(1L, "ema", "clementi", "emi@gmail.com", "123456", Role.USER, null);

        when(tareaRepository.findByIdAndUsuario(99L, usuario)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> tareaService.eliminarTarea(99L, usuario))
                .isInstanceOf(Validacion.class)
                .hasMessage("La tarea no existe");
    }


    // ==================== FILTRAR TAREAS ====================

    @Test
    @DisplayName("Filtrar tareas por nombre debe devolver coincidencias")
    void filtrarTareasPorNombre() {
        // Arrange
        var usuario = new Usuario(1L, "ema", "clementi", "emi@gmail.com", "123456", Role.USER, null);
        var tarea1 = new Tarea(
                new DatosCrearTarea("Ir al médico", "Check", Importancia.ALTA, 5, null, null),
                usuario
        );
        var tarea2 = new Tarea(
                new DatosCrearTarea("Comprar leche", "Supermercado", Importancia.BAJA, 1, null, null),
                usuario
        );

        var filtro = new DatosFiltroTarea(
                "médico",
                null,
                null,
                null,
                null,
                false,
                null,
                null,
                null
        );

        when(tareaRepository.findByUsuario(usuario))
                .thenReturn(List.of(tarea1, tarea2));

        // Act
        var resultado = tareaService.filtrarTareas(filtro, usuario);

        // Assert
        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).nombre()).containsIgnoringCase("médico");
    }

    @Test
    @DisplayName("Filtrar tareas por estado debe devolver solo tareas con ese estado")
    void filtrarTareasPorEstado() {
        // Arrange
        var usuario = new Usuario(1L, "ema", "clementi", "emi@gmail.com", "123456", Role.USER, null);
        var tarea1 = new Tarea(
                new DatosCrearTarea("Tarea 1", "Desc 1", Importancia.ALTA, 5, null, null),
                usuario
        );
        tarea1.setEstado(Estado.PENDIENTE);

        var tarea2 = new Tarea(
                new DatosCrearTarea("Tarea 2", "Desc 2", Importancia.MEDIA, 4, null, null),
                usuario
        );
        tarea2.setEstado(Estado.COMPLETADA);

        var filtro = new DatosFiltroTarea(
                null,
                Estado.PENDIENTE,
                null,
                null,
                null,
                false,
                null,
                null,
                null
        );

        when(tareaRepository.findByUsuario(usuario))
                .thenReturn(List.of(tarea1, tarea2));

        // Act
        var resultado = tareaService.filtrarTareas(filtro, usuario);

        // Assert
        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).estado()).isEqualTo(Estado.PENDIENTE);
    }

    @Test
    @DisplayName("Filtrar tareas por importancia debe devolver solo tareas con esa importancia")
    void filtrarTareasPorImportancia() {
        // Arrange
        var usuario = new Usuario(1L, "ema", "clementi", "emi@gmail.com", "123456", Role.USER, null);
        var tarea1 = new Tarea(
                new DatosCrearTarea("Tarea 1", "Desc 1", Importancia.ALTA, 5, null, null),
                usuario
        );
        var tarea2 = new Tarea(
                new DatosCrearTarea("Tarea 2", "Desc 2", Importancia.BAJA, 4, null, null),
                usuario
        );

        var filtro = new DatosFiltroTarea(
                null,
                null,
                Importancia.ALTA,
                null,
                null,
                false,
                null,
                null,
                null
        );

        when(tareaRepository.findByUsuario(usuario))
                .thenReturn(List.of(tarea1, tarea2));

        // Act
        var resultado = tareaService.filtrarTareas(filtro, usuario);

        // Assert
        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).importancia()).isEqualTo(Importancia.ALTA);
    }

    @Test
    @DisplayName("Filtrar tareas vencidas debe devolver solo tareas vencidas activas")
    void filtrarTareasVencidas() {
        // Arrange
        var usuario = new Usuario(1L, "ema", "clementi", "emi@gmail.com", "123456", Role.USER, null);
        LocalDate haceDias = LocalDate.now().minusDays(5);
        LocalDate proximos = LocalDate.now().plusDays(5);

        var tareaVencida = new Tarea(
                new DatosCrearTarea("Tarea vencida", "Desc", Importancia.ALTA, 5, null, null),
                usuario
        );
        tareaVencida.setEstado(Estado.PENDIENTE);

        var tareaActiva = new Tarea(
                new DatosCrearTarea("Tarea activa", "Desc", Importancia.MEDIA, 4, null, null),
                usuario
        );
        tareaActiva.setEstado(Estado.PENDIENTE);

        var filtro = new DatosFiltroTarea(
                null,
                null,
                null,
                null,
                null,
                true,  // soloVencidas = true
                null,
                null,
                null
        );

        when(tareaRepository.findByUsuario(usuario))
                .thenReturn(List.of(tareaVencida, tareaActiva));

        // Act
        var resultado = tareaService.filtrarTareas(filtro, usuario);

        // Assert
        verify(tareaRepository).findByUsuario(usuario);
    }

    @Test
    @DisplayName("Filtrar tareas sin resultados debe devolver lista vacía")
    void filtrarTareasSinResultados() {
        // Arrange
        var usuario = new Usuario(1L, "ema", "clementi", "emi@gmail.com", "123456", Role.USER, null);
        var filtro = new DatosFiltroTarea(
                "inexistente",
                null,
                null,
                null,
                null,
                false,
                null,
                null,
                null
        );

        when(tareaRepository.findByUsuario(usuario))
                .thenReturn(List.of());

        // Act
        var resultado = tareaService.filtrarTareas(filtro, usuario);

        // Assert
        assertThat(resultado).isEmpty();
    }

    @Test
    @DisplayName("Filtrar tareas con múltiples criterios debe aplicar todos")
    void filtrarTareasConMultiplesCriterios() {
        // Arrange
        var usuario = new Usuario(1L, "ema", "clementi", "emi@gmail.com", "123456", Role.USER, null);
        var tarea1 = new Tarea(
                new DatosCrearTarea("Ir al médico", "Check", Importancia.ALTA, 5, null, null),
                usuario
        );
        tarea1.setEstado(Estado.PENDIENTE);

        var tarea2 = new Tarea(
                new DatosCrearTarea("Ir al dentista", "Check", Importancia.MEDIA, 5, null, null),
                usuario
        );
        tarea2.setEstado(Estado.PENDIENTE);

        var filtro = new DatosFiltroTarea(
                "médico",           // busqueda
                Estado.PENDIENTE,   // estado
                Importancia.ALTA,   // importancia
                null,
                null,
                false,
                null,
                null,
                null
        );

        when(tareaRepository.findByUsuario(usuario))
                .thenReturn(List.of(tarea1, tarea2));

        // Act
        var resultado = tareaService.filtrarTareas(filtro, usuario);

        // Assert
        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).nombre()).containsIgnoringCase("médico");
        assertThat(resultado.get(0).estado()).isEqualTo(Estado.PENDIENTE);
        assertThat(resultado.get(0).importancia()).isEqualTo(Importancia.ALTA);
    }


    // ==================== OBTENER ESTADÍSTICAS ====================

    @Test
    @DisplayName("Obtener estadísticas sin tareas debe devolver valores en cero")
    void obtenerEstadisticasSinTareas() {
        // Arrange
        var usuario = new Usuario(1L, "ema", "clementi", "emi@gmail.com", "123456", Role.USER, null);

        when(tareaRepository.findByUsuario(usuario))
                .thenReturn(List.of());

        // Act
        var resultado = tareaService.obtenerEstadisticas(usuario);

        // Assert
        assertThat(resultado.totalTareas()).isEqualTo(0);
        assertThat(resultado.tareasCompletadas()).isEqualTo(0);
        assertThat(resultado.tareasPendientes()).isEqualTo(0);
        assertThat(resultado.porcentajeCompletado()).isEqualTo(0.0);
    }

    @Test
    @DisplayName("Obtener estadísticas debe contar correctamente por estado")
    void obtenerEstadisticasConTareas() {
        // Arrange
        var usuario = new Usuario(1L, "ema", "clementi", "emi@gmail.com", "123456", Role.USER, null);

        var tarea1 = new Tarea(
                new DatosCrearTarea("Tarea 1", "Desc 1", Importancia.ALTA, 5, null, null),
                usuario
        );
        tarea1.setEstado(Estado.COMPLETADA);
        tarea1.setId(1L);

        var tarea2 = new Tarea(
                new DatosCrearTarea("Tarea 2", "Desc 2", Importancia.MEDIA, 4, null, null),
                usuario
        );
        tarea2.setEstado(Estado.PENDIENTE);
        tarea2.setId(2L);

        var tarea3 = new Tarea(
                new DatosCrearTarea("Tarea 3", "Desc 3", Importancia.BAJA, 3, null, null),
                usuario
        );
        tarea3.setEstado(Estado.EN_PROGRESO);
        tarea3.setId(3L);

        when(tareaRepository.findByUsuario(usuario))
                .thenReturn(List.of(tarea1, tarea2, tarea3));

        // Act
        var resultado = tareaService.obtenerEstadisticas(usuario);

        // Assert
        assertThat(resultado.totalTareas()).isEqualTo(3);
        assertThat(resultado.tareasCompletadas()).isEqualTo(1);
        assertThat(resultado.tareasPendientes()).isEqualTo(1);
        assertThat(resultado.tareasEnProgreso()).isEqualTo(1);
        assertThat(resultado.porcentajeCompletado()).isEqualTo(33.33);
    }

    @Test
    @DisplayName("Obtener estadísticas debe contar tareas vencidas correctamente")
    void obtenerEstadisticasConTareasVencidas() {
        // Arrange
        var usuario = new Usuario(1L, "ema", "clementi", "emi@gmail.com", "123456", Role.USER, null);
        LocalDate haceDias = LocalDate.now().minusDays(5);

        var tareaVencida = new Tarea(
                new DatosCrearTarea("Tarea vencida", "Desc", Importancia.ALTA, 5, null, null),
                usuario
        );
        tareaVencida.setEstado(Estado.PENDIENTE);
        tareaVencida.setId(1L);

        when(tareaRepository.findByUsuario(usuario))
                .thenReturn(List.of(tareaVencida));

        // Act
        var resultado = tareaService.obtenerEstadisticas(usuario);

        // Assert
        assertThat(resultado.totalTareas()).isEqualTo(1);
        verify(tareaRepository).findByUsuario(usuario);
    }

    @Test
    @DisplayName("Obtener estadísticas debe agrupar correctamente por estado e importancia")
    void obtenerEstadisticasAgrupamientos() {
        // Arrange
        var usuario = new Usuario(1L, "ema", "clementi", "emi@gmail.com", "123456", Role.USER, null);

        var tarea1 = new Tarea(
                new DatosCrearTarea("Tarea 1", "Desc 1", Importancia.ALTA, 5, null, null),
                usuario
        );
        tarea1.setEstado(Estado.COMPLETADA);
        tarea1.setId(1L);

        var tarea2 = new Tarea(
                new DatosCrearTarea("Tarea 2", "Desc 2", Importancia.ALTA, 4, null, null),
                usuario
        );
        tarea2.setEstado(Estado.PENDIENTE);
        tarea2.setId(2L);

        when(tareaRepository.findByUsuario(usuario))
                .thenReturn(List.of(tarea1, tarea2));

        // Act
        var resultado = tareaService.obtenerEstadisticas(usuario);

        // Assert
        assertThat(resultado.tareasPorEstado()).containsKeys("COMPLETADA", "PENDIENTE");
        assertThat(resultado.tareasPorImportancia()).containsKey("ALTA");
    }

}
