package com.tareas.tareas.controller;

import com.tareas.tareas.domain.tarea.*;
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
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import org.springframework.boot.test.mock.mockito.MockBean;


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

    @MockBean
    private TareaService tareaService;


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
}
