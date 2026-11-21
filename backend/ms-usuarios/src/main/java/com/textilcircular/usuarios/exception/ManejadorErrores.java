package com.textilcircular.usuarios.exception;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ManejadorErrores {
    public record ErrorResponse(String codigo, String mensaje) {}

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ErrorResponse> manejarApi(ApiException error) {
        return ResponseEntity.status(error.getEstado())
                .body(new ErrorResponse(error.getCodigo(), error.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> manejarDatos(IllegalArgumentException error) {
        return ResponseEntity.badRequest().body(new ErrorResponse("DATOS_INVALIDOS", error.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> manejarValidacion(MethodArgumentNotValidException error) {
        String mensaje = error.getBindingResult().getAllErrors().get(0).getDefaultMessage();
        return ResponseEntity.badRequest().body(new ErrorResponse("DATOS_INVALIDOS", mensaje));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> manejarJson() {
        return ResponseEntity.badRequest().body(new ErrorResponse("DATOS_INVALIDOS", "Revisa los datos enviados"));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> manejarDuplicado() {
        // La restricción de la base también cubre registros simultáneos.
        return ResponseEntity.status(409).body(
                new ErrorResponse("DATOS_DUPLICADOS", "El correo o el RUT ya está registrado"));
    }
}
