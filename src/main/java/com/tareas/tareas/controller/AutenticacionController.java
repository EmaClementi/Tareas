package com.tareas.tareas.controller;

import com.tareas.tareas.domain.usuario.*;
import com.tareas.tareas.domain.usuario.DatosJWTToken;
import com.tareas.tareas.infra.security.TokenService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AutenticacionController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/registro")
    public ResponseEntity registrarUsuario(@RequestBody @Valid DatosCrearUsuario datos) {
        var usuario = usuarioRepository.findByEmail(datos.email());
        if(usuario != null){
            return ResponseEntity.badRequest().body("Email ya registrado");
        }else{
            Usuario nuevoUsuario = new Usuario();
            nuevoUsuario.setNombre(datos.nombre());
            nuevoUsuario.setApellido(datos.apellido());
            nuevoUsuario.setEmail(datos.email());
            nuevoUsuario.setRole(Role.USER);
            // Encriptar la contraseña antes de guardar
            nuevoUsuario.setClave(passwordEncoder.encode(datos.clave()));

            usuarioRepository.save(nuevoUsuario);
            return ResponseEntity.ok("Usuario registrado correctamente");
        }


    }
    @PostMapping("/login")
    public ResponseEntity autenticarUsuario(@RequestBody DatosAutenticacionUsuario datosAutenticacionUsuario) {
        Authentication authToken = new UsernamePasswordAuthenticationToken(datosAutenticacionUsuario.email(),
                datosAutenticacionUsuario.clave());
        var usuarioAutenticado = authenticationManager.authenticate(authToken);
        var JWTtoken = tokenService.generarToken((Usuario) usuarioAutenticado.getPrincipal());
        return ResponseEntity.ok(new DatosJWTToken(JWTtoken));
    }

}
