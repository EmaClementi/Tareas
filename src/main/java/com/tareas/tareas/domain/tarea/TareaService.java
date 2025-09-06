package com.tareas.tareas.domain.tarea;

import com.tareas.tareas.Validacion;
import com.tareas.tareas.domain.usuario.UsuarioRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TareaService {

    @Autowired
    TareaRepository tareaRepository;

    @Autowired
    UsuarioRepository usuarioRepository;

    public DatosRespuestaTarea crearTarea(@Valid DatosCrearTarea datos) {
        var tarea = tareaRepository.findByNombre(datos.nombre());
        var usuario = usuarioRepository.findById(datos.usuarioId());

        if(tarea.isPresent()){
            throw new Validacion("La tarea ya existe");
        }
        if(usuario.isPresent()){
            var usuarioEncontrado = usuario.get();
            var nuevaTarea = new Tarea(datos, usuarioEncontrado);
            tareaRepository.save(nuevaTarea);

            return new DatosRespuestaTarea(nuevaTarea);
        }else{
            throw new Validacion("El usuario no existe");
        }

    }

    public List<DatosListaTarea> listar() {
        List<DatosListaTarea> tareas = tareaRepository.findAll()
                .stream()
                .map(tarea -> new DatosListaTarea(tarea.getId(),tarea.getNombre(),tarea.getDescripcion(),tarea.getFechaCreacion(),tarea.getFechaFinalizacion(),tarea.getEstado(),tarea.getImportancia(),tarea.getUsuario().getId()))
                .toList();
        return tareas;

    }
    public DatosRespuestaTarea editarTarea(DatosActualizarTarea datos) {
        var tarea = tareaRepository.findByNombre(datos.nombre());

        if(tarea.isPresent()){
            var tareaEncontrada = tarea.get();

            return new DatosRespuestaTarea(tareaEncontrada.getNombre(),tareaEncontrada.getDescripcion(),tareaEncontrada.getEstado(),tareaEncontrada.getImportancia(),tareaEncontrada.getUsuario().getId());
        }else{
            throw new Validacion("No existe una tarea con ese nombre");
        }

    }
}
