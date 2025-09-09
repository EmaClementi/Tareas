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
        var tarea = tareaRepository.existsByUsuarioIdAndNombre(datos.usuarioId(), datos.nombre());
        var usuario = usuarioRepository.findById(datos.usuarioId());

        if(usuario.isPresent()){
            var usuarioEncontrado = usuario.get();
            if(tarea){
                throw new Validacion("La tarea ya existe");
            }else{
                var nuevaTarea = new Tarea(datos, usuarioEncontrado);
                tareaRepository.save(nuevaTarea);
                return new DatosRespuestaTarea(nuevaTarea);
            }
        }else{
            throw new Validacion("El usuario no existe");
        }

    }

    public List<DatosListaTarea> listar() {
        List<DatosListaTarea> tareas = tareaRepository.findAll()
                .stream()
                .map(tarea -> new DatosListaTarea(tarea.getId(),tarea.getNombre(),tarea.getDescripcion(),tarea.getFechaCreacion(),tarea.getFechaFinalizacion(),tarea.getEstado(),tarea.getImportancia()))
                .toList();
        return tareas;

    }
    public DatosRespuestaTarea editarTarea(DatosActualizarTarea datos) {
        var tarea = tareaRepository.findById(datos.id());
        var usuario = usuarioRepository.findById(datos.usuarioId());


        if(tarea.isPresent() && usuario.isPresent()){
            var tareaEncontrada = tarea.get();
            var usuarioEncontrado = usuario.get();


            tareaEncontrada.actualizarTarea(datos,usuarioEncontrado);

            return new DatosRespuestaTarea(tareaEncontrada.getNombre(),tareaEncontrada.getDescripcion(),tareaEncontrada.getEstado(),tareaEncontrada.getImportancia());
        }else{
            throw new Validacion("No existe la tarea o el usuario");
        }

    }

    public void eliminarTarea(Long id) {
        var tarea = tareaRepository.findById(id);

        if (tarea.isPresent()){
            tareaRepository.deleteById(id);
        }else{
            throw new Validacion("La tarea no existe");
        }
    }

    public DatosRespuestaTarea buscarTareaPorId(Long id) {
        var tarea = tareaRepository.findById(id);

        if(tarea.isPresent()){
            var tareaEncontrada = tarea.get();

            return new DatosRespuestaTarea(tareaEncontrada);
        }else{
            throw new Validacion("La tarea buscada no existe");
        }
    }

    public List<DatosRespuestaTarea> buscarTareaPorNombre(String nombre) {
        List<DatosRespuestaTarea> tareas = tareaRepository.findByNombreContainsIgnoreCase(nombre).stream()
                .map(t-> new DatosRespuestaTarea(t))
                .toList();

        return tareas;
    }
}
