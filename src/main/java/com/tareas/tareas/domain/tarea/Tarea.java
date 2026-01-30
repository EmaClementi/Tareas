package com.tareas.tareas.domain.tarea;

import com.tareas.tareas.Validacion;
import com.tareas.tareas.domain.usuario.Usuario;
import com.tareas.tareas.domain.usuario.UsuarioRepository;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

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

    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_inicio")
    private LocalDate fechaInicio; // 🆕 Nueva fecha de inicio (opcional)

    @Column(name = "fecha_vencimiento")
    private LocalDate fechaVencimiento; // 🆕 Fecha de vencimiento (opcional, editable)

    @Column(name = "fecha_finalizacion")
    private LocalDateTime fechaFinalizacion; // Cuando se completa la tarea

    private Integer duracionDias; // Ahora es opcional

    @Enumerated(EnumType.STRING)
    private Estado estado;

    @Enumerated(EnumType.STRING)
    private Importancia importancia;

    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @PrePersist
    public void onCreate() {
        this.fechaCreacion = LocalDateTime.now();
    }

    public Tarea(DatosCrearTarea datos, Usuario usuario) {
        this.nombre = datos.nombre();
        this.descripcion = datos.descripcion();
        this.duracionDias = datos.duracionDias();
        this.estado = Estado.PENDIENTE;
        this.importancia = datos.importancia();
        this.usuario = usuario;
        this.fechaInicio = datos.fechaInicio();
        this.fechaVencimiento = datos.fechaVencimiento();

        // calcular fechaVencimiento si no se proporciona
        if (this.fechaVencimiento == null && this.duracionDias != null && this.duracionDias > 0) {
            this.fechaVencimiento = LocalDate.now().plusDays(this.duracionDias);
        }

        // Calcular duracionDias si se proporciona fechaVencimiento pero no duracionDias
        if (this.duracionDias == null && this.fechaVencimiento != null) {
            this.duracionDias = (int) ChronoUnit.DAYS.between(LocalDate.now(), this.fechaVencimiento);
        }
    }

    public void actualizarTarea(DatosActualizarTarea datos, Usuario usuario) {
        this.nombre = datos.nombre();
        this.descripcion = datos.descripcion();
        this.estado = datos.estado();
        this.importancia = datos.importancia();
        this.usuario = usuario;
        this.fechaInicio = datos.fechaInicio();
        this.fechaVencimiento = datos.fechaVencimiento();

        // Actualizar fechaFinalizacion cuando se marca como COMPLETADA
        if (datos.estado() == Estado.COMPLETADA && this.fechaFinalizacion == null) {
            this.fechaFinalizacion = LocalDateTime.now();
        }

        if (datos.fechaVencimiento() != null) {
            this.fechaVencimiento = datos.fechaVencimiento();
            // Recalcular duracionDias basado en la nueva fecha de vencimiento
            if (this.fechaCreacion != null) {
                this.duracionDias = (int) ChronoUnit.DAYS.between(
                        this.fechaCreacion.toLocalDate(),
                        this.fechaVencimiento
                );
            }
        } else if (datos.duracionDias() != null) {
            if (datos.duracionDias() <= 0) {
                throw new Validacion("La duración debe ser mayor a 0");
            }
            this.duracionDias = datos.duracionDias();
            this.fechaVencimiento = this.fechaCreacion.toLocalDate().plusDays(datos.duracionDias());
        }
    }

    // Método para saber si la tarea está vencida
    public boolean estaVencida() {
        if (this.estado == Estado.COMPLETADA || this.estado == Estado.CANCELADA) {
            return false;
        }
        return this.fechaVencimiento != null && LocalDate.now().isAfter(this.fechaVencimiento);
    }

    // Método para calcular días restantes
    public Long diasRestantes() {
        if (this.fechaVencimiento == null) {
            return null;
        }
        return ChronoUnit.DAYS.between(LocalDate.now(), this.fechaVencimiento);
    }
}
