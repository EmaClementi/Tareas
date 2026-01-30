package com.tareas.tareas.domain.tarea;

import com.tareas.tareas.domain.usuario.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface TareaRepository extends JpaRepository<Tarea, Long> {

    Optional<Tarea> findByNombre(String nombre);

    @Override
    Optional<Tarea> findById(Long id);

    List<Tarea> findByNombreContainsIgnoreCase(String nombre);

    boolean existsByUsuarioIdAndNombre(Long usuarioId, String nombre);

    List<Tarea> findByUsuario(Usuario usuario);

    Optional<Tarea> findByIdAndUsuario(Long idTarea, Usuario usuario);

    List<Tarea> findByNombreContainingIgnoreCaseAndUsuario(String nombre, Usuario usuario);

    List<Tarea> findByUsuarioAndEstado(Usuario usuario, Estado estado);

    List<Tarea> findByUsuarioAndImportancia(Usuario usuario, Importancia importancia);

    List<Tarea> findByUsuarioAndFechaVencimientoBetween(Usuario usuario, LocalDate inicio, LocalDate fin);

    @Query("SELECT t FROM Tarea t WHERE t.usuario = :usuario AND t.fechaVencimiento < :fecha AND t.estado != 'COMPLETADA' AND t.estado != 'CANCELADA'")
    List<Tarea> findTareasVencidas(@Param("usuario") Usuario usuario, @Param("fecha") LocalDate fecha);

    List<Tarea> findByUsuarioOrderByFechaVencimientoAsc(Usuario usuario);

    List<Tarea> findByUsuarioOrderByFechaVencimientoDesc(Usuario usuario);

    List<Tarea> findByUsuarioOrderByImportanciaDesc(Usuario usuario);

    List<Tarea> findByUsuarioOrderByFechaCreacionDesc(Usuario usuario);

}
