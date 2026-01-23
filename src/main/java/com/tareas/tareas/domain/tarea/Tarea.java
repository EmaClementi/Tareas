package com.tareas.tareas.domain.tarea;

import com.tareas.tareas.domain.usuario.Usuario;
import com.tareas.tareas.domain.usuario.UsuarioRepository;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
@Entity(name = "Tarea")
@Table(name = "tareas")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class Tarea {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nombre;
    private String descripcion;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaFinalizacion;

    @Enumerated(EnumType.STRING)
    private Estado estado;

    @Enumerated(EnumType.STRING)
    private Importancia importancia;

    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;


    public Tarea(DatosCrearTarea datos, Usuario usuario) {
        this.nombre = datos.nombre();
        this.descripcion = datos.descripcion();
        this.fechaCreacion = LocalDateTime.now();
        this.fechaFinalizacion = LocalDateTime.now();
        this.estado = datos.estado();
        this.importancia = datos.importancia();
        this.usuario = usuario;
    }
    public void actualizarTarea(DatosActualizarTarea datos, Usuario usuario){
        this.nombre = datos.nombre();
        this.descripcion = datos.descripcion();
        this.fechaCreacion = datos.fecha_creacion();
        this.fechaFinalizacion = datos.fecha_finalizacion();
        this.estado = datos.estado();
        this.importancia = datos.importancia();
        this.usuario = usuario;
    }
}
