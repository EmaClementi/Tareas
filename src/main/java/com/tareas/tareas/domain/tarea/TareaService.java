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
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
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
            usuario.agregarTarea(nuevaTarea);
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

    private Comparator<Tarea> obtenerComparadorInteligente() {
        return (t1, t2) -> {
            LocalDate hoy = LocalDate.now();

            // 1. Tareas COMPLETADAS y CANCELADAS al final
            boolean t1Terminada = t1.getEstado() == Estado.COMPLETADA || t1.getEstado() == Estado.CANCELADA;
            boolean t2Terminada = t2.getEstado() == Estado.COMPLETADA || t2.getEstado() == Estado.CANCELADA;

            if (t1Terminada && !t2Terminada) return 1;  // t1 va después
            if (!t1Terminada && t2Terminada) return -1; // t1 va antes

            // Si ambas están terminadas, ordenar por fecha de finalización
            if (t1Terminada && t2Terminada) {
                return t2.getFechaCreacion().compareTo(t1.getFechaCreacion());
            }

            // 2. Tareas VENCIDAS primero (las más atrasadas primero)
            boolean t1Vencida = t1.estaVencida(); // Usar el método que ya tenés
            boolean t2Vencida = t2.estaVencida();

            if (t1Vencida && !t2Vencida) return -1;
            if (!t1Vencida && t2Vencida) return 1;

            if (t1Vencida && t2Vencida) {
                // Entre vencidas, las más antiguas primero
                return t1.getFechaVencimiento().compareTo(t2.getFechaVencimiento());
            }

            // 3. Tareas que vencen HOY
            boolean t1Hoy = t1.getFechaVencimiento() != null &&
                    t1.getFechaVencimiento().equals(hoy);
            boolean t2Hoy = t2.getFechaVencimiento() != null &&
                    t2.getFechaVencimiento().equals(hoy);

            if (t1Hoy && !t2Hoy) return -1;
            if (!t1Hoy && t2Hoy) return 1;

            // 4. Ordenar por IMPORTANCIA (ALTA antes que MEDIA antes que BAJA)
            int importanciaCompare = Integer.compare(
                    getImportanciaValue(t2.getImportancia()),
                    getImportanciaValue(t1.getImportancia())
            );
            if (importanciaCompare != 0) return importanciaCompare;

            // 5. Ordenar por fecha de vencimiento (próximas primero)
            if (t1.getFechaVencimiento() != null && t2.getFechaVencimiento() != null) {
                return t1.getFechaVencimiento().compareTo(t2.getFechaVencimiento());
            }

            // 6. Tareas sin fecha al final
            if (t1.getFechaVencimiento() == null && t2.getFechaVencimiento() != null) return 1;
            if (t1.getFechaVencimiento() != null && t2.getFechaVencimiento() == null) return -1;

            // 7. Si todo es igual, ordenar por fecha de creación (más recientes primero)
            return t2.getFechaCreacion().compareTo(t1.getFechaCreacion());
        };
    }

    private int getImportanciaValue(Importancia importancia) {
        return switch (importancia) {
            case ALTA -> 3;
            case MEDIA -> 2;
            case BAJA -> 1;
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

        if (datos.ordenarPor() != null && !datos.ordenarPor().isEmpty()) {
            Comparator<Tarea> comparator = obtenerComparador(datos.ordenarPor());

            if (datos.direccion() != null && datos.direccion().equalsIgnoreCase("DESC")) {
                comparator = comparator.reversed();
            }

            stream = stream.sorted(comparator);
        } else {
            stream = stream.sorted(obtenerComparadorInteligente());
        }

        return stream.map(DatosRespuestaTarea::new).collect(Collectors.toList());

    }

    public DatosEstadisticasTarea obtenerEstadisticas(@AuthenticationPrincipal Usuario usuario) {
        List<Tarea> tareas = tareaRepository.findByUsuario(usuario);

        if (tareas.isEmpty()) {
            return new DatosEstadisticasTarea(
                    0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L,
                    Map.of(), Map.of(),
                    0.0, 0.0, 0.0, 0.0, 0.0  // Todos los porcentajes en 0
            );
        }

        LocalDate hoy = LocalDate.now();
        LocalDateTime inicioHoy = hoy.atStartOfDay();
        LocalDate inicioSemana = hoy.minusDays(hoy.getDayOfWeek().getValue() - 1);

        // Total de tareas
        long total = tareas.size();

        // Por estado
        long completadas = tareas.stream()
                .filter(t -> t.getEstado() == Estado.COMPLETADA)
                .count();

        long pendientes = tareas.stream()
                .filter(t -> t.getEstado() == Estado.PENDIENTE)
                .count();

        long enProgreso = tareas.stream()
                .filter(t -> t.getEstado() == Estado.EN_PROGRESO)
                .count();

        long canceladas = tareas.stream()
                .filter(t -> t.getEstado() == Estado.CANCELADA)
                .count();

        // Vencidas (activas con fecha pasada)
        long vencidas = tareas.stream()
                .filter(t -> t.getFechaVencimiento() != null &&
                        t.getFechaVencimiento().isBefore(hoy) &&
                        t.getEstado() != Estado.COMPLETADA &&
                        t.getEstado() != Estado.CANCELADA)
                .count();

        // Completadas hoy
        long completadasHoy = tareas.stream()
                .filter(t -> t.getFechaFinalizacion() != null &&
                        t.getFechaFinalizacion().isAfter(inicioHoy))
                .count();

        // Completadas esta semana
        long completadasSemana = tareas.stream()
                .filter(t -> t.getFechaFinalizacion() != null &&
                        t.getFechaFinalizacion().toLocalDate().isAfter(inicioSemana.minusDays(1)))
                .count();

        // Mapa por estado
        Map<String, Long> porEstado = tareas.stream()
                .collect(Collectors.groupingBy(
                        t -> t.getEstado().name(),
                        Collectors.counting()
                ));

        // Mapa por importancia
        Map<String, Long> porImportancia = tareas.stream()
                .collect(Collectors.groupingBy(
                        t -> t.getImportancia().name(),
                        Collectors.counting()
                ));

        // Calcular TODOS los porcentajes
        double porcentajeCompletado = calcularPorcentaje(completadas, total);
        double porcentajePendiente = calcularPorcentaje(pendientes, total);
        double porcentajeEnProgreso = calcularPorcentaje(enProgreso, total);
        double porcentajeCancelado = calcularPorcentaje(canceladas, total);
        double porcentajeVencido = calcularPorcentaje(vencidas, total);

        return new DatosEstadisticasTarea(
                total,
                completadas,
                pendientes,
                enProgreso,
                canceladas,
                vencidas,
                completadasHoy,
                completadasSemana,
                porEstado,
                porImportancia,
                porcentajeCompletado,
                porcentajePendiente,
                porcentajeEnProgreso,
                porcentajeCancelado,
                porcentajeVencido
        );
    }

    // Método auxiliar para calcular porcentajes
    private double calcularPorcentaje(long parte, long total) {
        if (total == 0) return 0.0;
        double porcentaje = ((double) parte / total) * 100;
        return Math.round(porcentaje * 100.0) / 100.0; // Redondear a 2 decimales
    }


}
