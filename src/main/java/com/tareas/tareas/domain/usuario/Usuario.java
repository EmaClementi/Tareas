package com.tareas.tareas.domain.usuario;

import com.tareas.tareas.domain.tarea.Tarea;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;


@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
@Table(name = "usuarios")
@Entity(name = "Usuario")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nombre;
    private String email;
    private String clave;

    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL)
    private List<Tarea> tareas;

    public Usuario(DatosCrearUsuario datos) {
        this.nombre = datos.nombre();
        this.email = datos.email();
        this.clave = datos.clave();
        this.tareas = datos.tareas();
    }
}
