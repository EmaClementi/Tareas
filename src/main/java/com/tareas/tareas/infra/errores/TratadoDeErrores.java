package com.tareas.tareas.infra.errores;

import com.tareas.tareas.Validacion;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.List;

@RestControllerAdvice
public class TratadoDeErrores {
    // MANEJA ERRORES DONDE LA ENTIDAD NO EXISTE
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity tratarError404(){

        ErrorResponse response = new ErrorResponse(
                HttpStatus.NOT_FOUND.value(),
                "Entidad no encontrada",
                null,
                LocalDateTime.now()
                );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    // ERROR EN LOS DATOS RECIBIDOS DESDE EL FRONT QUE MANEJA EL VALID
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity tratarError400(MethodArgumentNotValidException e){
        var errores = e.getFieldErrors().stream().map(DatosErrorValidacion::new).toList();

        ErrorResponse response = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Error de validacion",
                errores,
                LocalDateTime.now()
                );
        return ResponseEntity.badRequest().body(response);
    }

    // 400 EXCEPCION PERSONALIZADA
    @ExceptionHandler(Validacion.class)
    public ResponseEntity tratarErrorDeValidacion(Validacion e){

        ErrorResponse response = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Error de validacion",
                List.of(e.getMessage()),
                LocalDateTime.now()
        );
        return ResponseEntity.badRequest().body(response);
    }
    // 500 ERRORES GENERALES

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> manejarErrorGeneral(Exception e) {
        ErrorResponse response = new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Error interno en el servidor",
                List.of(e.getMessage()),
                LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
    // ERROR 400 CUANDO EL CUERPO DE LA SOLICITUD LLEGA VACIO
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> manejarJsonInvalido(HttpMessageNotReadableException e) {
        ErrorResponse response = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Cuerpo de la solicitud inválido o vacío",
                List.of(e.getMessage()),
                LocalDateTime.now()
        );
        return ResponseEntity.badRequest().body(response);
    }

    private record DatosErrorValidacion(String campo, String error){
        public DatosErrorValidacion(FieldError error) {
            this(error.getField(), error.getDefaultMessage());
        }
    }
}
