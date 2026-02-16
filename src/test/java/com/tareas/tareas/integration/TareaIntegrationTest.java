package com.tareas.tareas.integration;

import com.tareas.tareas.Validacion;
import com.tareas.tareas.domain.tarea.*;
import com.tareas.tareas.domain.usuario.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureTestEntityManager;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Transactional
@AutoConfigureTestEntityManager
@DisplayName("Tests de Integración - Tareas")
class TareaIntegrationTest {

    @Autowired
    private TareaService tareaService;

    @Autowired
    private TareaRepository tareaRepository;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    private Usuario usuarioTest;

    @BeforeEach
    void setup() {
        // Crear un usuario de prueba antes de cada test
        usuarioTest = new Usuario(new DatosCrearUsuario("Test Usuario",
                "Usuario Test",
                "test@gmail.com",
                "password123",
                List.of()
        ));
        usuarioRepository.save(usuarioTest);
    }

    @Test
    @DisplayName("Integración: Crear tarea y verificar que se guarda en BD")
    void crearTareaFlujCompleto() {
        // Arrange
        var datosCrearTarea = new DatosCrearTarea(
                "Tarea de integración",
                "Esta tarea se guarda en BD real",
                Importancia.ALTA,
                5,
                null,
                null
        );

        // Act
        var resultado = tareaService.crearTarea(datosCrearTarea, usuarioTest);

        // Assert - Verificar en BD real
        var tareaEnBD = tareaRepository.findById(resultado.id());
        assertThat(tareaEnBD).isPresent();
        assertThat(tareaEnBD.get().getNombre()).isEqualTo("Tarea de integración");
        assertThat(tareaEnBD.get().getUsuario().getId()).isEqualTo(usuarioTest.getId());
    }

    @Test
    @DisplayName("Integración: No se puede crear dos tareas con el mismo nombre")
    void crearDosTargetasIgualesLanzaError() {
        // Arrange
        var datosCrearTarea = new DatosCrearTarea(
                "Tarea duplicada",
                "Descripción",
                Importancia.MEDIA,
                3,
                null,
                null
        );

        // Act - Crear la primera tarea
        tareaService.crearTarea(datosCrearTarea, usuarioTest);

        // Act & Assert - Intentar crear la segunda tarea con el mismo nombre
        assertThatThrownBy(() -> tareaService.crearTarea(datosCrearTarea, usuarioTest))
                .isInstanceOf(Validacion.class)
                .hasMessage("La tarea ya existe");
    }

    @Test
    @DisplayName("Integración: Dos usuarios pueden crear tareas con el mismo nombre")
    void dosusuariosConTareasIguales() {
        // Arrange
        var usuario2 = new Usuario(new DatosCrearUsuario("Otro Usuario",
                "Otro Apellido",
                "otro2@gmail.com",
                "password123",
                List.of())
        );
        usuarioRepository.save(usuario2);

        var datos = new DatosCrearTarea(
                "Tarea compartida",
                "Descripción",
                Importancia.ALTA,
                5,
                null,
                null
        );

        // Act
        var resultado1 = tareaService.crearTarea(datos, usuarioTest);
        var resultado2 = tareaService.crearTarea(datos, usuario2);

        // Assert
        assertThat(resultado1.id()).isNotEqualTo(resultado2.id());
        assertThat(tareaRepository.findById(resultado1.id())).isPresent();
        assertThat(tareaRepository.findById(resultado2.id())).isPresent();
    }

    @Test
    @DisplayName("Integración: Buscar tarea y verificar que devuelve datos correctos de BD")
    void buscarTareaFlujCompleto() {
        // Arrange
        var datosCrearTarea = new DatosCrearTarea(
                "Buscar esta tarea",
                "Descripción importante",
                Importancia.ALTA,
                7,
                null,
                null
        );
        var tareaCreada = tareaService.crearTarea(datosCrearTarea, usuarioTest);

        // Act
        var resultado = tareaService.buscarTareaPorId(tareaCreada.id(), usuarioTest);

        // Assert
        assertThat(resultado).isNotNull();
        assertThat(resultado.nombre()).isEqualTo("Buscar esta tarea");
        assertThat(resultado.descripcion()).isEqualTo("Descripción importante");
        assertThat(resultado.duracionDias()).isEqualTo(7);
    }

    @Test
    @DisplayName("Integración: Usuario no puede ver tareas de otro usuario")
    void usuarioNoVeTareasDeOtro() {
        // Arrange
        var usuario2 = new Usuario(new DatosCrearUsuario("Otro Usuario",
                "Otro Apellido",
                "otro2@gmail.com",
                "password123",
                List.of())
        );
        usuarioRepository.save(usuario2);

        var datos = new DatosCrearTarea("Tarea secreta", "Desc", Importancia.ALTA, 5, null, null);
        var tareaCreada = tareaService.crearTarea(datos, usuarioTest);

        // Act & Assert
        assertThatThrownBy(() -> tareaService.buscarTareaPorId(tareaCreada.id(), usuario2))
                .isInstanceOf(Validacion.class)
                .hasMessage("La tarea buscada no existe");
    }



    @Test
    @DisplayName("Integración: Editar tarea y verificar cambios en BD")
    void editarTareaFlujCompleto() {
        // Arrange
        var datosCrearTarea = new DatosCrearTarea(
                "Tarea original",
                "Descripción original",
                Importancia.BAJA,
                3,
                null,
                null
        );
        var tareaCreada = tareaService.crearTarea(datosCrearTarea, usuarioTest);

        var datosActualizar = new DatosActualizarTarea(
                "Tarea actualizada",
                "Descripción nueva",
                Estado.EN_PROGRESO,
                Importancia.ALTA,
                10,
                null,
                null
        );

        // Act
        var resultado = tareaService.editarTarea(datosActualizar, usuarioTest, tareaCreada.id());

        // Assert - Verificar en BD real
        var tareaEnBD = tareaRepository.findById(tareaCreada.id());
        assertThat(tareaEnBD).isPresent();
        assertThat(tareaEnBD.get().getNombre()).isEqualTo("Tarea actualizada");
        assertThat(tareaEnBD.get().getEstado()).isEqualTo(Estado.EN_PROGRESO);
        assertThat(tareaEnBD.get().getImportancia()).isEqualTo(Importancia.ALTA);
    }

    @Test
    @DisplayName("Integración: Editar tarea con duracionDias recalcula fechaVencimiento")
    void editarTareaActualizaDuracionYFecha() {
        // Arrange
        var datosCrearTarea = new DatosCrearTarea(
                "Tarea con fecha",
                "Desc",
                Importancia.MEDIA,
                5,
                null,
                null
        );
        var tareaCreada = tareaService.crearTarea(datosCrearTarea, usuarioTest);

        var datosActualizar = new DatosActualizarTarea(
                "Tarea actualizada",
                "Desc",
                Estado.PENDIENTE,
                Importancia.ALTA,
                15,  // Cambiar duración
                null,
                null
        );

        // Act
        tareaService.editarTarea(datosActualizar, usuarioTest, tareaCreada.id());

        // Assert
        var tareaEnBD = tareaRepository.findById(tareaCreada.id());
        assertThat(tareaEnBD).isPresent();
        assertThat(tareaEnBD.get().getDuracionDias()).isEqualTo(15);
        assertThat(tareaEnBD.get().getFechaVencimiento()).isNotNull();
    }

    @Test
    @DisplayName("Integración: Marcar tarea como COMPLETADA setea fechaFinalizacion")
    void marcarTareaComoCompletada() {
        // Arrange
        var datosCrearTarea = new DatosCrearTarea(
                "Tarea a completar",
                "Desc",
                Importancia.ALTA,
                5,
                null,
                null
        );
        var tareaCreada = tareaService.crearTarea(datosCrearTarea, usuarioTest);

        var datosActualizar = new DatosActualizarTarea(
                "Tarea completada",
                "Desc",
                Estado.COMPLETADA,  // Marcar como completada
                Importancia.ALTA,
                5,
                null,
                null
        );

        // Act
        tareaService.editarTarea(datosActualizar, usuarioTest, tareaCreada.id());

        // Assert
        var tareaEnBD = tareaRepository.findById(tareaCreada.id());
        assertThat(tareaEnBD).isPresent();
        assertThat(tareaEnBD.get().getEstado()).isEqualTo(Estado.COMPLETADA);
        assertThat(tareaEnBD.get().getFechaFinalizacion()).isNotNull();
    }


    @Test
    @DisplayName("Integración: Eliminar tarea la remueve de BD")
    void eliminarTareaFlujCompleto() {
        // Arrange
        var datosCrearTarea = new DatosCrearTarea(
                "Tarea a eliminar",
                "Desc",
                Importancia.MEDIA,
                3,
                null,
                null
        );
        var tareaCreada = tareaService.crearTarea(datosCrearTarea, usuarioTest);

        // Act
        tareaService.eliminarTarea(tareaCreada.id(), usuarioTest);

        // Assert
        var tareaEnBD = tareaRepository.findById(tareaCreada.id());
        assertThat(tareaEnBD).isEmpty();
    }


    @Test
    @DisplayName("Integración: Obtener todas las tareas del usuario")
    void obtenerTareasPorUsuarioFlujCompleto() {
        // Arrange
        var datos1 = new DatosCrearTarea("Tarea 1", "Desc 1", Importancia.ALTA, 5, null, null);
        var datos2 = new DatosCrearTarea("Tarea 2", "Desc 2", Importancia.MEDIA, 3, null, null);
        var datos3 = new DatosCrearTarea("Tarea 3", "Desc 3", Importancia.BAJA, 1, null, null);

        tareaService.crearTarea(datos1, usuarioTest);
        tareaService.crearTarea(datos2, usuarioTest);
        tareaService.crearTarea(datos3, usuarioTest);

        // Act
        var resultado = tareaService.obtenerTareasPorUsuario(usuarioTest);

        // Assert
        assertThat(resultado).hasSize(3);
        assertThat(resultado).allMatch(t -> t.nombre().startsWith("Tarea"));
    }

    @Test
    @DisplayName("Integración: Usuario sin tareas lanza excepción")
    void usuarioSinTareasLanzaError() {
        // Arrange - Usuario sin tareas

        // Act & Assert
        assertThatThrownBy(() -> tareaService.obtenerTareasPorUsuario(usuarioTest))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("El usuario no tiene tareas asignadas");
    }


    @Test
    @DisplayName("Integración: Buscar tareas por nombre")
    void buscarTareasPorNombreFlujCompleto() {
        // Arrange
        var datos1 = new DatosCrearTarea("Ir al médico", "Check", Importancia.ALTA, 5, null, null);
        var datos2 = new DatosCrearTarea("Llamar al médico", "Agendar", Importancia.MEDIA, 3, null, null);
        var datos3 = new DatosCrearTarea("Comprar leche", "Supermercado", Importancia.BAJA, 1, null, null);

        tareaService.crearTarea(datos1, usuarioTest);
        tareaService.crearTarea(datos2, usuarioTest);
        tareaService.crearTarea(datos3, usuarioTest);

        // Act
        var resultado = tareaService.buscarTareaPorNombre("médico", usuarioTest);

        // Assert
        assertThat(resultado).hasSize(2);
        assertThat(resultado).allMatch(t -> t.nombre().toLowerCase().contains("médico"));
    }

    @Test
    @DisplayName("Integración: Búsqueda case-insensitive")
    void buscarTareasCaseInsensitive() {
        // Arrange
        var datos = new DatosCrearTarea("TAREA IMPORTANTE", "Desc", Importancia.ALTA, 5, null, null);
        tareaService.crearTarea(datos, usuarioTest);

        // Act
        var resultado1 = tareaService.buscarTareaPorNombre("importante", usuarioTest);
        var resultado2 = tareaService.buscarTareaPorNombre("IMPORTANTE", usuarioTest);
        var resultado3 = tareaService.buscarTareaPorNombre("ImPoRtAnTe", usuarioTest);

        // Assert
        assertThat(resultado1).hasSize(1);
        assertThat(resultado2).hasSize(1);
        assertThat(resultado3).hasSize(1);
    }



    @Test
    @DisplayName("Integración: Filtrar tareas por estado")
    void filtrarTareasPorEstadoFlujCompleto() {
        // Arrange
        var datos1 = new DatosCrearTarea("Tarea pendiente", "Desc", Importancia.ALTA, 5, null, null);
        var datos2 = new DatosCrearTarea("Tarea completada", "Desc", Importancia.MEDIA, 3, null, null);

        var tarea1 = tareaService.crearTarea(datos1, usuarioTest);
        var tarea2 = tareaService.crearTarea(datos2, usuarioTest);

        // Marcar la segunda como completada
        var datosActualizar = new DatosActualizarTarea(
                "Tarea completada",
                "Desc",
                Estado.COMPLETADA,
                Importancia.MEDIA,
                3,
                null,
                null
        );
        tareaService.editarTarea(datosActualizar, usuarioTest, tarea2.id());

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

        // Act
        var resultado = tareaService.filtrarTareas(filtro, usuarioTest);

        // Assert
        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).estado()).isEqualTo(Estado.PENDIENTE);
    }

    @Test
    @DisplayName("Integración: Filtrar tareas por importancia")
    void filtrarTareasPorImportanciaFlujCompleto() {
        // Arrange
        var datos1 = new DatosCrearTarea("Urgente", "Desc", Importancia.ALTA, 5, null, null);
        var datos2 = new DatosCrearTarea("Normal", "Desc", Importancia.MEDIA, 3, null, null);
        var datos3 = new DatosCrearTarea("Baja", "Desc", Importancia.BAJA, 1, null, null);

        tareaService.crearTarea(datos1, usuarioTest);
        tareaService.crearTarea(datos2, usuarioTest);
        tareaService.crearTarea(datos3, usuarioTest);

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

        // Act
        var resultado = tareaService.filtrarTareas(filtro, usuarioTest);

        // Assert
        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).importancia()).isEqualTo(Importancia.ALTA);
    }

    @Test
    @DisplayName("Integración: Filtrar con múltiples criterios")
    void filtrarTareasConMultiplesCriteriosFlujCompleto() {
        // Arrange
        var datos1 = new DatosCrearTarea("Ir al médico", "Check", Importancia.ALTA, 5, null, null);
        var datos2 = new DatosCrearTarea("Ir al dentista", "Check", Importancia.MEDIA, 3, null, null);
        var datos3 = new DatosCrearTarea("Comprar medicina", "Farmacia", Importancia.ALTA, 2, null, null);

        var tarea1 = tareaService.crearTarea(datos1, usuarioTest);
        tareaService.crearTarea(datos2, usuarioTest);
        tareaService.crearTarea(datos3, usuarioTest);

        var filtro = new DatosFiltroTarea(
                "médico",
                Estado.PENDIENTE,
                Importancia.ALTA,
                null,
                null,
                false,
                null,
                null,
                null
        );

        // Act
        var resultado = tareaService.filtrarTareas(filtro, usuarioTest);

        // Assert
        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).nombre()).contains("médico");
        assertThat(resultado.get(0).importancia()).isEqualTo(Importancia.ALTA);
    }


    @Test
    @DisplayName("Integración: Obtener estadísticas de tareas")
    void obtenerEstadisticasFlujCompleto() {
        // Arrange
        var datos1 = new DatosCrearTarea("Tarea 1", "Desc", Importancia.ALTA, 5, null, null);
        var datos2 = new DatosCrearTarea("Tarea 2", "Desc", Importancia.MEDIA, 3, null, null);
        var datos3 = new DatosCrearTarea("Tarea 3", "Desc", Importancia.BAJA, 1, null, null);

        var tarea1 = tareaService.crearTarea(datos1, usuarioTest);
        var tarea2 = tareaService.crearTarea(datos2, usuarioTest);
        tareaService.crearTarea(datos3, usuarioTest);

        // Completar una tarea
        var datosActualizar = new DatosActualizarTarea(
                "Tarea 1 completada",
                "Desc",
                Estado.COMPLETADA,
                Importancia.ALTA,
                5,
                null,
                null
        );
        tareaService.editarTarea(datosActualizar, usuarioTest, tarea1.id());

        // Act
        var resultado = tareaService.obtenerEstadisticas(usuarioTest);

        // Assert
        assertThat(resultado.totalTareas()).isEqualTo(3);
        assertThat(resultado.tareasCompletadas()).isEqualTo(1);
        assertThat(resultado.tareasPendientes()).isEqualTo(2);
        assertThat(resultado.porcentajeCompletado()).isEqualTo(33.33);
    }

    @Test
    @DisplayName("Integración: Estadísticas incluyen conteos por estado e importancia")
    void estadisticasConAgrupamientos() {
        // Arrange
        var datos1 = new DatosCrearTarea("Alta 1", "Desc", Importancia.ALTA, 5, null, null);
        var datos2 = new DatosCrearTarea("Alta 2", "Desc", Importancia.ALTA, 3, null, null);
        var datos3 = new DatosCrearTarea("Media 1", "Desc", Importancia.MEDIA, 1, null, null);

        tareaService.crearTarea(datos1, usuarioTest);
        tareaService.crearTarea(datos2, usuarioTest);
        tareaService.crearTarea(datos3, usuarioTest);

        // Act
        var resultado = tareaService.obtenerEstadisticas(usuarioTest);

        // Assert
        assertThat(resultado.tareasPorEstado()).containsKey("PENDIENTE");
        assertThat(resultado.tareasPorImportancia()).containsKeys("ALTA", "MEDIA");
        assertThat(resultado.tareasPorImportancia().get("ALTA")).isEqualTo(2);
        assertThat(resultado.tareasPorImportancia().get("MEDIA")).isEqualTo(1);
    }

}