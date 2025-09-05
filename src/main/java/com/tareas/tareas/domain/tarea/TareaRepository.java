package com.tareas.tareas.domain.tarea;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TareaRepository extends JpaRepository<Tarea, Long> {

    Optional<Tarea> findByNombre(String nombre);
}
