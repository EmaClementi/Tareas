package com.tareas.tareas.domain.tarea;

import com.tareas.tareas.domain.usuario.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TareaRepository extends JpaRepository<Tarea, Long> {

    Optional<Tarea> findByNombre(String nombre);

    @Override
    Optional<Tarea> findById(Long id);

    List<Tarea> findByNombreContainsIgnoreCase(String nombre);

    boolean existsByUsuarioIdAndNombre(Long usuarioId, String nombre);

    List<Tarea> findByUsuario(Usuario usuario);
}
