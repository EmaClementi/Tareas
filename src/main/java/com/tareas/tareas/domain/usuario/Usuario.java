package com.tareas.tareas.domain.usuario;

import com.tareas.tareas.domain.tarea.DatosRespuestaTarea;
import com.tareas.tareas.domain.tarea.Tarea;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
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
    private List<Tarea> tareas = new ArrayList<>();

    public Usuario(DatosCrearUsuario datos) {
        this.nombre = datos.nombre();
        this.email = datos.email();
        this.clave = datos.clave();
        this.tareas = datos.tareas();
    }

    public void actualizarUsuario(DatosActualizarUsuario datos) {
        this.nombre = datos.nombre();
        this.email = datos.email();
        this.clave = datos.clave();
    }

    public List<DatosRespuestaTarea> tareas(){
        if(tareas == null){
            return List.of();

        }else{
            return tareas.stream()
                    .map(t->new DatosRespuestaTarea(t)).toList();
        }
    }
}
