package com.tareas.tareas.controller;

import com.tareas.tareas.domain.tarea.*;
import jakarta.validation.ReportAsSingleViolation;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tareas")
public class TareaController {

    @Autowired
    TareaService tareaService;

    @PostMapping
    @Transactional
    public ResponseEntity crearTarea(@RequestBody @Valid DatosCrearTarea datos){

        var tarea = tareaService.crearTarea(datos);

        return ResponseEntity.ok(tarea);

    }

    @GetMapping
    @Transactional
    public ResponseEntity listarTareas(){
        List<DatosListaTarea> tareas = tareaService.listar();

        return ResponseEntity.ok(tareas);
    }
    @GetMapping("/{id}")
    @Transactional
    public ResponseEntity buscarTarea(@PathVariable Long id){
        var tarea = tareaService.buscarTareaPorId(id);

        return ResponseEntity.ok(tarea);
    }

    @GetMapping("/nombre/{nombre}")
    @Transactional
    public ResponseEntity buscarTareaPorNombre(@PathVariable String nombre){
        List<DatosRespuestaTarea> tareas = tareaService.buscarTareaPorNombre(nombre);

        return ResponseEntity.ok(tareas);
    }

    @PutMapping
    @Transactional
    public ResponseEntity modificarTarea(@RequestBody @Valid DatosActualizarTarea datos){
        DatosRespuestaTarea tarea = tareaService.editarTarea(datos);

        return ResponseEntity.ok(tarea);

    }
    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity eliminarTarea(@PathVariable Long id){
        tareaService.eliminarTarea(id);

        return ResponseEntity.noContent().build();
    }
}
