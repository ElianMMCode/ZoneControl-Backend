package laboratorioxyz.com.ZoneControl.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * Manejador global de excepciones para toda la aplicación.
 * Centraliza las respuestas de error HTTP para mantener un formato
 * consistente en todos los módulos.
 *
 * - ResponseStatusException → error controlado con mensaje específico
 * - MethodArgumentNotValidException → errores de validación por campo (@Valid)
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Maneja excepciones lanzadas intencionalmente desde los servicios
     * con ResponseStatusException (ej: 409 conflicto, 400 bad request).
     * Retorna { "error": "mensaje descriptivo" } con el status code correspondiente.
     */
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, String>> handleResponseStatus(ResponseStatusException ex) {
        return ResponseEntity.status(ex.getStatusCode())
                .body(Map.of("error", ex.getReason()));
    }

    /**
     * Maneja errores de validación de Jakarta Validation (@Valid).
     * Retorna { "errors": { "campo1": "mensaje1", "campo2": "mensaje2" } }
     * con HTTP 400, detallando qué campo falló y por qué.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> errors = ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(
                        f -> f.getField(),
                        f -> f.getDefaultMessage(),
                        (a, b) -> a
                ));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("errors", errors));
    }
}
