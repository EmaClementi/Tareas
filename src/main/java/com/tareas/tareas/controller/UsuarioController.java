package com.tareas.tareas.controller;

import com.tareas.tareas.domain.tarea.DatosRespuestaTarea;
import com.tareas.tareas.domain.usuario.*;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/usuarios")
public class UsuarioController {

    @Autowired
    UsuarioService usuarioService;


    @PostMapping
    @Transactional
    public ResponseEntity crearUsuario(@RequestBody @Valid DatosCrearUsuario datos){
        var usuario = usuarioService.crearUsuario(datos);

        return ResponseEntity.ok(usuario);
    }

    @GetMapping
    @Transactional
    public ResponseEntity listarUsuarios(){

        List<DatosRespuestaUsuario> usuarios = usuarioService.listarUsuarios();

        return ResponseEntity.ok(usuarios);

    }

    @GetMapping("/{id}")
    @Transactional
    public ResponseEntity buscarUsuario(@PathVariable Long id){
        var usuario = usuarioService.buscarUsuario(id);

        return ResponseEntity.ok(usuario);
    }
    @PutMapping
    @Transactional
    public ResponseEntity editarUsuario(@RequestBody DatosActualizarUsuario datos){

        var usuario = usuarioService.modificarUsuario(datos);

        return ResponseEntity.ok(usuario);
    }

    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity eliminarUsuario(@PathVariable Long id){

        usuarioService.eliminarUsuario(id);

        return ResponseEntity.noContent().build();
    }
}
