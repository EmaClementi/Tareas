package com.tareas.tareas.controller;

import com.tareas.tareas.domain.tarea.DatosRespuestaTarea;
import com.tareas.tareas.domain.usuario.*;
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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;


@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureJsonTesters
public class UsuarioControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    AutenticacionController autenticacionController;

    @Autowired
    JacksonTester<DatosCrearUsuario> datosCrearUsuarioJacksonTester;

    @MockBean
    private PasswordEncoder passwordEncoder;

    @MockBean
    private UsuarioRepository usuarioRepository;

    @Test
    @DisplayName("Al registrar un usuario deberia devolver un 200 ok")
    void registrarUsuario() throws Exception {
        var nombre = "Ema";
        var apellido = "Perez";
        var email = "ema@gmail.com";
        var clave = "123456";

        var usuario = new DatosCrearUsuario(nombre,apellido,email,clave,null);

        when(usuarioRepository.findByEmail("ema@test.com")).thenReturn(null);
        when(passwordEncoder.encode("123456")).thenReturn("encoded_pass");

        var response = mvc.perform(post("/auth/registro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(datosCrearUsuarioJacksonTester.write(usuario).getJson()))
                .andReturn()
                .getResponse();

        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(response.getContentAsString()).isEqualTo("Usuario registrado correctamente");
    }
    @Test
    @DisplayName("Registrar un usuario con email ya existente debería devolver 400 Bad Request")
    void registrarUsuarioEmailDuplicado() throws Exception {
        // Arrange
        var nombre = "Ema";
        var apellido = "Perez";
        var email = "ema@gmail.com";
        var clave = "123456";
        var usuario = new DatosCrearUsuario(nombre,apellido,email,clave,null);
        when(usuarioRepository.findByEmail("ema@gmail.com")).thenReturn(new Usuario()); // al buscar el email en la db, le devuelvo un usuario, simulando que ya existe

        var response = mvc.perform(post("/auth/registro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(datosCrearUsuarioJacksonTester.write(usuario).getJson()))// simulo que le mando un json desde el endpoint, lo serializo a json
                .andReturn()
                .getResponse();

        assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value()); // si la respuesta es igual a un error 400 y contiene una respuesta igual a la siquiente linea, el test pasa
        assertThat(response.getContentAsString()).isEqualTo("Email ya registrado");
    }

}
