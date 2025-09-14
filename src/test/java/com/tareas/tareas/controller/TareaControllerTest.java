package com.tareas.tareas.controller;

import com.tareas.tareas.domain.tarea.*;
import com.tareas.tareas.domain.usuario.Role;
import com.tareas.tareas.domain.usuario.Usuario;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.AutoConfigureJsonTesters;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

//Un test sigue la regla AAA (Arrange, Act, Assert):
//Arrange (Given) → preparás los datos.
//Act (When) → ejecutás la acción.
//Assert (Then) → verificás el resultado.
@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureJsonTesters
public class TareaControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private JacksonTester<DatosCrearTarea> datosCrearTareaJacksonTester;

    @Autowired
    private JacksonTester<DatosRespuestaTarea> datosRespuestaTareaJacksonTester;

    @Autowired
    private JacksonTester<DatosActualizarTarea> datosActualizarTareaJacksonTester;

    @MockBean
    private TareaService tareaService;

    @Autowired
    private EntityManager em;


    @Test // con esta anotacion digo que el metodo es una prueba unitaria
    @DisplayName("Cuando la solicitud vuelva vacia deberia devolver un error 400 bad request")
    @WithMockUser
    void crearTareaSinDatos() throws Exception {
        var response = mvc.perform(post("/tareas"))
                .andReturn().getResponse(); // simulo que hago un post al endpoint /tareas

        assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value()); // verifico que el resultado sea un error 400 bad request

    }


    @Test
    @DisplayName("Cuando se cree la tarea deberia devolver un 200 ok")
    @WithMockUser
    void crearTareaConDatos() throws Exception {
        var nombre = "Hacer ejercicio";
        var descripcion = "Hacer rutina del dia 3, piernas completas";
        var fecha_creacion = LocalDateTime.now();
        var fecha_finalizacion = LocalDateTime.now().plusHours(1);
        var estado = Estado.PENDIENTE;
        var importancia = Importancia.ALTA;
        var datosRespuestaTarea = new DatosRespuestaTarea(nombre,descripcion,estado,importancia);
        when(tareaService.crearTarea(any())).thenReturn(datosRespuestaTarea);

        var response = mvc.perform(post("/tareas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(datosCrearTareaJacksonTester.write(
                        new DatosCrearTarea(nombre,descripcion,fecha_creacion,fecha_finalizacion,estado,importancia,2L)
                ).getJson()
                )
        )
                .andReturn().getResponse();

        var jsonEsperado = datosRespuestaTareaJacksonTester.write(datosRespuestaTarea)
                .getJson();

        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(response.getContentAsString()).isEqualTo(jsonEsperado);
    }

    @Test
    @DisplayName("buscamos una tarea que no existe, debe devolver un 400 bad request")
    @WithMockUser
    void buscarTareaNoExistente() throws Exception {
        when(tareaService.buscarTareaPorId(1L)).thenThrow(new EntityNotFoundException("La tarea no existe"));

        var response = mvc.perform(get("/tareas/1"))
                .andReturn()
                .getResponse();

        assertThat(response.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
    }

    @Test
    @DisplayName("Buscamos una tarea que existe y deberia devolver un 200 ok")
    @WithMockUser
    void buscarTareaExistente() throws Exception {
        var usuario = new Usuario(1L,"ema","clementi","emi@gmail.com","123456", Role.USER,null);
        DatosCrearTarea nuevaTarea = new DatosCrearTarea("Ir al medico", "Ir al medico a controlar resultados", LocalDateTime.now(),LocalDateTime.now().plusHours(2),Estado.PENDIENTE, Importancia.ALTA, 1L);
        var tarea = new Tarea(nuevaTarea,usuario);
        var respuestaTarea = new DatosRespuestaTarea(tarea);
        when(tareaService.buscarTareaPorId(1L)).thenReturn(respuestaTarea); // este metodo hace que cuando alguien llame al metodo de buscarTarea, devuelva automaticamente
        // el dto de respuesta, esto evita que se llame a la base de datos real


        var response = mvc.perform(get("/tareas/1")) // uso mockmvc para simular la solicitud al endpoint
                .andReturn()
                .getResponse(); // obtengo la respuesta que devolveria el servidor

        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value()); // verifico que la respuesta http sea un 200 ok
        assertThat(response.getContentAsString()) // convierto el body de la respuesta en un string
                .isEqualTo(datosRespuestaTareaJacksonTester.write(respuestaTarea).getJson());
        // convierto el dto de la respuesta en json en formato string, y lo comparo con el que recibi, si son iguales, el test pasa

    }
    @Test
    @DisplayName("Actualizamos una tarea y deberia devolver un 200 ok con los datos de la tarea")
    @WithMockUser
    void modificarTarea() throws Exception {
        DatosActualizarTarea actualizarTarea = new DatosActualizarTarea(1L, "Ir al medico", "Ir al medico a controlar resultados", LocalDateTime.now(),LocalDateTime.now().plusHours(2),Estado.PENDIENTE, Importancia.ALTA, 1L);
        var respuestaTarea = new DatosRespuestaTarea(actualizarTarea.nombre(),actualizarTarea.descripcion(),actualizarTarea.estado(),actualizarTarea.importancia());

        when(tareaService.editarTarea(actualizarTarea)).thenReturn(respuestaTarea); // cuando el service reciba el dto actualizarTarea, debe devolver el dto respuestaTarea

        var response = mvc.perform(put("/tareas")
                .contentType(MediaType.APPLICATION_JSON) // decimos que el contenido va a venir en el cuerpo de la solicitud
                .content(datosActualizarTareaJacksonTester.write(actualizarTarea).getJson()))// convertimos el objeto en json con jackson
                .andReturn()
                .getResponse();

        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(response.getContentAsString())
                .isEqualTo(datosRespuestaTareaJacksonTester.write(respuestaTarea).getJson());
    }

    @Test
    @DisplayName("Actualizamos una tarea con campos no validos, deberia devolver un 400 bad request")
    @WithMockUser
    void modificarTareaConCamposInvalidos() throws Exception {
        DatosActualizarTarea actualizarTarea = new DatosActualizarTarea(null, "", "asdasd", LocalDateTime.now(),LocalDateTime.now().plusHours(2),Estado.PENDIENTE, Importancia.ALTA, 1L);
        // no hacemos when(tareaService) por que se corta antes de llegar al servicio

        var response = mvc.perform(put("/tareas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(datosActualizarTareaJacksonTester.write(actualizarTarea).getJson()))
                .andReturn()
                .getResponse();

        assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());

    }

    @Test
    @DisplayName("Actualizamos una tarea que no existe, deberia devolver un 404 not found")
    @WithMockUser
    void modificarTareaInexistente() throws Exception {
        DatosActualizarTarea actualizarTarea = new DatosActualizarTarea(99L, "Ir al medico", "Ir al medico a controlar resultados", LocalDateTime.now(),LocalDateTime.now().plusHours(2),Estado.PENDIENTE, Importancia.ALTA, 1L);

        when(tareaService.editarTarea(actualizarTarea)).thenThrow(new EntityNotFoundException("Tarea no encontrada"));


        var response = mvc.perform(put("/tareas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(datosActualizarTareaJacksonTester.write(actualizarTarea).getJson()))
                .andReturn()
                .getResponse();

        assertThat(response.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
    }

    @Test
    @DisplayName("Eliminamos una tarea, deberia devolver un 204 NO CONTENT")
    @WithMockUser
    void eliminarTarea() throws Exception {

        doNothing().when(tareaService).eliminarTarea(1L);

        var response = mvc.perform(delete("/tareas/1"))
                .andReturn()
                .getResponse();

        assertThat(response.getStatus()).isEqualTo(HttpStatus.NO_CONTENT.value());
    }

    @Test
    @DisplayName("Eliminamos una tarea que no existe, deberia devolver un 400 not foud")
    @WithMockUser
    void eliminarTareaInexistente() throws Exception {

        doThrow(new EntityNotFoundException("Tarea no encontrada"))
                .when(tareaService).eliminarTarea(99L);

        var response = mvc.perform(delete("/tareas/99"))
                .andReturn()
                .getResponse();

        assertThat(response.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
    }

    @Test
    @DisplayName("Acceder a endpoint sin token deberia devolver 403 Forbidden")
    void accederSinToken() throws Exception {
        var response = mvc.perform(get("/tareas/1"))
                .andReturn()
                .getResponse();

        assertThat(response.getStatus()).isEqualTo(HttpStatus.FORBIDDEN.value());
    }

    @Test
    @DisplayName("Acceder con token invalido deberia devolver 403 Forbidden")
    void accederConTokenInvalido() throws Exception {

        mvc.perform(get("/tareas/1")
                        .header("Authorization", "Bearer " + "eJ1c2VyaWn"))
                .andExpect(status().isForbidden());
    }

}
