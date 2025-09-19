package com.tareas.tareas.controller;

import com.tareas.tareas.domain.tarea.DatosRespuestaTarea;
import com.tareas.tareas.domain.usuario.*;
import com.tareas.tareas.infra.security.TokenService;
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
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import static com.tareas.tareas.domain.usuario.Role.ADMIN;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
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

    @MockBean
    private TokenService tokenService;

    @Autowired
    AutenticacionController autenticacionController;

    @Autowired
    JacksonTester<DatosCrearUsuario> datosCrearUsuarioJacksonTester;

    @Autowired
    JacksonTester<DatosAutenticacionUsuario> datosAutenticacionUsuarioJacksonTester;

    @Autowired
    private JacksonTester<DatosJWTToken> datosJWTTokenJacksonTester;

    @MockBean
    private PasswordEncoder passwordEncoder;

    @MockBean
    private UsuarioRepository usuarioRepository;

    @MockBean
    private AuthenticationManager authenticationManager;

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

    @Test
    @DisplayName("Al registrar un usuario con un campo vacio o invalido deberia devolver un 400 bad reques")
    void registrarUsuarioConCamposNoValidos() throws Exception {
        var nombre = "";
        var apellido = "Perez";
        var email = "ema789gmail7com";
        var clave = "123456";
        var usuario = new DatosCrearUsuario(nombre,apellido,email,clave,null);

        var response = mvc.perform(post("/auth/registro")
                .contentType(MediaType.APPLICATION_JSON)
                .content(datosCrearUsuarioJacksonTester.write(usuario).getJson()))
                .andReturn()
                .getResponse();

        assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    @DisplayName("Al logearse con campos validos deberia devolver un 200 ok")
    void loginDeUsuario() throws Exception {

        // creamos los datos del usuario
        var email = "ema@gmail.com";
        var clave = "123456";

        // creamos una instancia de los datos que van a venir en el cuerpo de la solicitud
        var loginDTO = new DatosAutenticacionUsuario(email, clave);

        // creamos un usuario con los datos para logearse y su rol
        var usuario = new Usuario();
        usuario.setEmail(email);
        usuario.setClave(clave);
        usuario.setRole(Role.ADMIN);

        //simulamos la creacion de un usuario autenticado con sus datos
        Authentication authentication = new UsernamePasswordAuthenticationToken(usuario, null, usuario.getAuthorities());

        // mockeamos la autenticacion, que normalmente hace la validacion de los datos, recibiendo un objeto autenticacion, en este caso no importa que objeto es, lo importante es que nos va a devolver un objeto del tipo autenticacion, como el que instanciamos arriba
        when(authenticationManager.authenticate(any(Authentication.class)))
                .thenReturn(authentication);

        // simulamos la creacion del token con el usuario, que nos debe devolver el token
        var tokenFalso = "jwt.falso.token";
        when(tokenService.generarToken(usuario))
                .thenReturn(tokenFalso);


        // inicializamos la respuesta del logeo, que es el token, representado por el dto
        DatosJWTToken tokenEsperado = new DatosJWTToken(tokenFalso);

        // simulamos la solicitud
        var response = mvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(datosAutenticacionUsuarioJacksonTester.write(loginDTO).getJson()))
                        .andReturn()
                        .getResponse();

        // nos debe devolver un 200 ok, y el token
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(response.getContentAsString())
                .isEqualTo(datosJWTTokenJacksonTester.write(tokenEsperado).getJson());
    }

    @Test
    @DisplayName("Al logearnos con un email que no existe o clave incorrecta deberia devolver un 401")
    void loginConDatosIncorrectos () throws Exception {
        var email = "ema@gmail.com";
        var clave = "123456";

        var loginUsuario = new DatosAutenticacionUsuario(email,clave);

        when(authenticationManager.authenticate(any(Authentication.class)))
                .thenThrow(new BadCredentialsException("Usuario no encontrado"));

        var response = mvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(datosAutenticacionUsuarioJacksonTester.write(loginUsuario).getJson()))
                .andReturn()
                .getResponse();

        assertThat(response.getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED.value());

    }

}
