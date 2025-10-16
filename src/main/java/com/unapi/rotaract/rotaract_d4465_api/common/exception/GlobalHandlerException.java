package com.unapi.rotaract.rotaract_d4465_api.common.exception;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import io.swagger.v3.oas.annotations.Hidden;

import com.unapi.rotaract.rotaract_d4465_api.common.dtos.ExceptionResponseDto;
import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice
@Hidden
public class GlobalHandlerException {
    /**
     * Manejador global de excepciones para la aplicación.
     *
     * Esta clase intercepta excepciones lanzadas por los controladores REST y
     * construye respuestas estándar de error serializables por JSON, utilizando
     * el DTO {@link com.unapi.rotaract.rotaract_d4465_api.common.dtos.ExceptionResponseDto}.
     *
     * Las respuestas contienen la fecha del error, el código HTTP, un mensaje
     * legible y una lista de errores detallados.
     */

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ExceptionResponseDto> handleException(Exception ex, HttpServletRequest request) {
        ExceptionResponseDto body = new ExceptionResponseDto(
            java.time.LocalDate.now(),
            500,
            java.util.List.of(ex.getMessage()),
            "Error interno del servidor"
        );
        return ResponseEntity.status(500).body(body);
    }


    /**
     * Manejador para errores de validación de argumentos de métodos.
     *
     * Captura {@link MethodArgumentNotValidException} generadas cuando los
     * parámetros anotados con validaciones (por ejemplo, @Valid) no cumplen
     * las restricciones. Construye una lista de errores indicando el campo y
     * el mensaje de validación asociado, y devuelve un código HTTP 400 (Bad Request).
     *
     * @param ex excepción lanzada por Spring con detalles de los errores de validación
     * @param request objeto de petición (no utilizado directamente) que representa la solicitud HTTP
     * @return ResponseEntity con {@link com.unapi.rotaract.rotaract_d4465_api.common.dtos.ExceptionResponseDto}
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ExceptionResponseDto> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex, HttpServletRequest request) {
        List<String> errors = ex.getFieldErrors()
                                    .stream()
                                    .map(error -> error.getField() + " : " + error.getDefaultMessage())
                                    .toList();

        return ResponseEntity.status(400).body(
            new ExceptionResponseDto(
                java.time.LocalDate.now(),
                400,
                errors,
                "Solicitud incorrecta"
            )
        );
    }

}
