package com.tareas.tareas.controller;

import com.tareas.tareas.domain.tarea.*;
import com.tareas.tareas.domain.usuario.Usuario;
import com.tareas.tareas.domain.usuario.UsuarioService;
import jakarta.validation.ReportAsSingleViolation;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/tareas")
public class TareaController {

    @Autowired
    TareaService tareaService;

    @Autowired
    UsuarioService usuarioService;


    @PostMapping
    @Transactional
    public ResponseEntity crearTarea(@RequestBody @Valid DatosCrearTarea datos, @AuthenticationPrincipal Usuario usuario){

        var tarea = tareaService.crearTarea(datos,usuario);

        return ResponseEntity.ok(tarea);

    }
    @GetMapping
    @Transactional
    public ResponseEntity listarMisTareas() {
        Usuario usuario = usuarioService.getUsuarioAutenticado();
        var tareas = tareaService.obtenerTareasPorUsuario(usuario);
        return ResponseEntity.ok(tareas);
    }


    @GetMapping("/{id}")
    @Transactional
    public ResponseEntity buscarTarea(@PathVariable Long id, @AuthenticationPrincipal Usuario usuario){
        var tarea = tareaService.buscarTareaPorId(id,usuario);

        return ResponseEntity.ok(tarea);
    }

    @GetMapping("/nombre/{nombre}")
    @Transactional
    public ResponseEntity buscarTareaPorNombre(@PathVariable String nombre, @AuthenticationPrincipal Usuario usuario){
        List<DatosRespuestaTarea> tareas = tareaService.buscarTareaPorNombre(nombre,usuario);

        return ResponseEntity.ok(tareas);
    }

    @PutMapping("/{idTarea}")
    @Transactional
    public ResponseEntity modificarTarea(@RequestBody @Valid DatosActualizarTarea datos,@PathVariable Long idTarea, @AuthenticationPrincipal Usuario usuario){
        DatosRespuestaTarea tarea = tareaService.editarTarea(datos, usuario, idTarea);

        return ResponseEntity.ok(tarea);

    }
    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity eliminarTarea(@PathVariable Long id, @AuthenticationPrincipal Usuario usuario){
        tareaService.eliminarTarea(id, usuario);

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/filtrar")
    @Transactional
    public ResponseEntity<List<DatosRespuestaTarea>> filtrarTareas(@RequestBody DatosFiltroTarea datos, @AuthenticationPrincipal Usuario usuario) {
        var tareas = tareaService.filtrarTareas(datos, usuario);
        return ResponseEntity.ok(tareas);
    }
}
