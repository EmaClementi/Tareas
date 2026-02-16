package com.tareas.tareas.domain.usuario;

import com.tareas.tareas.domain.tarea.DatosRespuestaTarea;
import com.tareas.tareas.domain.tarea.Tarea;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
@Table(name = "usuarios")
@Entity(name = "Usuario")
public class Usuario implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nombre;
    private String apellido;

    @Column(unique = true, nullable = false)
    private String email;
    @Setter
    private String clave;

    @Enumerated(EnumType.STRING)
    private Role role;

    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Tarea> tareas = new ArrayList<>();

    public Usuario(DatosCrearUsuario datos) {
        this.nombre = datos.nombre();
        this.apellido = datos.apellido();
        this.email = datos.email();
        this.clave = datos.clave();
        this.tareas = datos.tareas();
        this.role = Role.USER;
    }

    public void actualizarUsuario(DatosActualizarUsuario datos) {
        this.nombre = datos.nombre();
        this.apellido = datos.apellido();
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

    public void agregarTarea(Tarea tarea) {
        if (this.tareas == null) {
            this.tareas = new ArrayList<>();
        }
        this.tareas.add(tarea);
        tarea.setUsuario(this);
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(role.name()));
    }

    @Override
    public String getPassword() {
        return clave;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

}
