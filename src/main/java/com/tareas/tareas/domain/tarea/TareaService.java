package com.tareas.tareas.domain.tarea;

import com.tareas.tareas.Validacion;
import com.tareas.tareas.domain.usuario.Usuario;
import com.tareas.tareas.domain.usuario.UsuarioRepository;
import com.tareas.tareas.domain.usuario.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.springframework.data.jpa.domain.AbstractPersistable_.id;

@Service
public class TareaService {

    @Autowired
    TareaRepository tareaRepository;

    @Autowired
    UsuarioRepository usuarioRepository;

    @Autowired
    UsuarioService usuarioService;

    public DatosRespuestaTarea crearTarea(@Valid DatosCrearTarea datos, @AuthenticationPrincipal Usuario usuario) {
        var tarea = tareaRepository.existsByUsuarioIdAndNombre(usuario.getId(), datos.nombre());
        // var usuario = usuarioRepository.findById(datos.usuarioId());

        if(tarea){
            throw new Validacion("La tarea ya existe");
        }else{
            var nuevaTarea = new Tarea(datos, usuario);
            tareaRepository.save(nuevaTarea);
            return new DatosRespuestaTarea(nuevaTarea);
        }

    }

    public List<DatosRespuestaTarea> obtenerTareasPorUsuario(Usuario usuario) {
        var tareas = tareaRepository.findByUsuario(usuario);

        if (tareas.isEmpty()) {
            throw new RuntimeException("El usuario no tiene tareas asignadas");
        }
        List<DatosRespuestaTarea> tareasUsuario = tareas.stream()
                .map(t-> new DatosRespuestaTarea(t.getId(),t.getNombre(),t.getDescripcion(),t.getEstado(),t.getFechaCreacion(),t.getFechaInicio(),t.getFechaVencimiento(),t.getFechaFinalizacion(),t.getImportancia(),t.getDuracionDias(),t.estaVencida(),t.diasRestantes()))
                .collect(Collectors.toList());
        return tareasUsuario;
    }

    public DatosRespuestaTarea editarTarea(DatosActualizarTarea datos, @AuthenticationPrincipal Usuario usuario, Long idTarea) {
        Optional<Tarea> tarea = tareaRepository.findByIdAndUsuario(idTarea, usuario);

        if(tarea.isPresent()){
            var tareaEncontrada = tarea.get();

            tareaEncontrada.actualizarTarea(datos, usuario);

            return new DatosRespuestaTarea(tareaEncontrada);
        }else{
            throw new Validacion("No existe la tarea o el usuario");
        }
    }

    public void eliminarTarea(Long id, @AuthenticationPrincipal Usuario usuario) {
        Optional<Tarea> tarea = tareaRepository.findByIdAndUsuario(id, usuario);

        if (tarea.isPresent()){
            var tareaEncontrada = tarea.get();
            tareaRepository.delete(tareaEncontrada);
        }else{
            throw new Validacion("La tarea no existe");
        }
    }

    public DatosRespuestaTarea buscarTareaPorId(Long id, @AuthenticationPrincipal Usuario usuario) {
        Optional<Tarea> tarea = tareaRepository.findByIdAndUsuario(id, usuario);


        if(tarea.isPresent()){
            var tareaEncontrada = tarea.get();

            return new DatosRespuestaTarea(tareaEncontrada);
        }else{
            throw new Validacion("La tarea buscada no existe");
        }
    }

    public List<DatosRespuestaTarea> buscarTareaPorNombre(String nombre, @AuthenticationPrincipal Usuario usuario) {
        List<DatosRespuestaTarea> tareas = tareaRepository.findByNombreContainingIgnoreCaseAndUsuario(nombre,usuario).stream()
                .map(t-> new DatosRespuestaTarea(t))
                .toList();

        return tareas;
    }

    // Este comparador sirve para ordenar los resultados de manera ascendente o descendente
    private Comparator<Tarea> obtenerComparador(String campo) {
        return switch (campo) {
            case "fechaVencimiento" -> Comparator.comparing(
                    Tarea::getFechaVencimiento,
                    Comparator.nullsLast(Comparator.naturalOrder())
            );
            case "fechaCreacion" -> Comparator.comparing(Tarea::getFechaCreacion);
            case "importancia" -> Comparator.comparing(Tarea::getImportancia);
            case "nombre" -> Comparator.comparing(
                    Tarea::getNombre,
                    String.CASE_INSENSITIVE_ORDER
            );
            default -> Comparator.comparing(Tarea::getId);
        };
    }

    public List<DatosRespuestaTarea> filtrarTareas(DatosFiltroTarea datos, @AuthenticationPrincipal Usuario usuario) {

        // primer buscamos las tareas asociadas al usuario de la sesion
        List<Tarea> tareas = tareaRepository.findByUsuario(usuario);

        // convertimos las tareas en stream para poder utilizar metodos, en este caso el filter
        Stream<Tarea> stream = tareas.stream();


        // FILTRO POR NOMBRE Y DESCRIPCION
        // si los datos ingresados por el usuario no son nulos o no esta vacio, no aplicamos el filtro y sale del if
        if(datos.busqueda() != null && !datos.busqueda().isEmpty()){
            // convertimos el texto ingresado a minuscula
            String busquedaLower = datos.busqueda().toLowerCase();

            // aplicamos la funcion lambda, donde por cada tarea con su nombre, que convertimos a minuscula,
            // la vamos a convertir a minuscula y vemos si contiene el texto que ingreso el usuario, lo mismo hacemos con la descripcion,
            // devolvera true o false si contiene alguna de las dos
            stream = stream.filter(t ->t.getNombre().toLowerCase().contains(busquedaLower) ||
                    t.getDescripcion().toLowerCase().contains(busquedaLower));
        }

        // FILTRO POR ESTADO
        // iteramos y comparamos las tareas almacenadas con el estado que ingreso el usuario
        if(datos.estado() != null){
            stream = stream.filter(t -> t.getEstado() == datos.estado());
        }

        //FILTRO POR IMPORTANCIA
        // iteramos y comparamos las tareas almacenadas por importancia con el dato importancia que ingreso el usuario
        if(datos.importancia() != null){
            stream = stream.filter(t -> t.getImportancia() == datos.importancia());
        }

        // FILTRO POR FECHAS
        // verificamos que los datos ingresados por el usuario no sean nulos,
        // aplicamos el filter e iteramos, verificando que la fecha de vencimiento no sea nula y ademas,
        // la fecha de vencimiento no sea anterior a la fecha "desde" y que no sea posterior a la fecha "hasta"
        if(datos.fechaDesde() != null && datos.fechaHasta() != null){
            stream = stream.filter(t->
                    t.getFechaVencimiento() != null &&
                    !t.getFechaVencimiento().isBefore(datos.fechaDesde()) &&
                            !t.getFechaVencimiento().isAfter(datos.fechaHasta())
            );
        }

        // FILTRO POR TAREAS VENCIDAS
        //buscamos tareas que esten activas y vencidas, recorremos cada tarea y verificamos primero que
        // la fecha de vencimiento que recordemos, es opcional, no sea nulla, y verificamos que la fecha
        // de vencimiento sea anterior a la fecha de hoy y que no este completada o cancelada
        if(datos.soloVencidas() != null && datos.soloVencidas()){
            LocalDate hoy = LocalDate.now();
            stream = stream.filter(t ->
                    t.getFechaVencimiento() != null &&
                            t.getFechaVencimiento().isBefore(hoy) &&
                            t.getEstado() != Estado.COMPLETADA &&
                            t.getEstado() != Estado.CANCELADA
            );
        }

        // FILTRAR POR DURACION DE DIAS

        if(datos.diasDuracion() != null){
            stream = stream.filter(t ->
                    t.getDuracionDias() != null &&
                            t.getDuracionDias().equals(datos.diasDuracion())
            );
        }


        // toma el campo que ingreso el usuario, y lo pasa por el comparador, el usuario puede ordenar
        // por fecha de vencimiento y creacion, por nombre y por importancia, todos estos de manera asc y desc
        if (datos.ordenarPor() != null) {
            Comparator<Tarea> comparator = obtenerComparador(datos.ordenarPor());

            if (datos.direccion() != null && datos.direccion().equalsIgnoreCase("DESC")) {
                comparator = comparator.reversed();
            }

            stream = stream.sorted(comparator);
        }

        return stream.map(DatosRespuestaTarea::new).collect(Collectors.toList());

    }


}
