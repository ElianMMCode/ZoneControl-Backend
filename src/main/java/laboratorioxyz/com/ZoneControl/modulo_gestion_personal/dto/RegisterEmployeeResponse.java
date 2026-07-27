package laboratorioxyz.com.ZoneControl.modulo_gestion_personal.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

/**
 * DTO de salida para POST /personal.
 * Retorna el ID interno UUID del empleado y el código EMP-XXXXXX
 * generado automáticamente, que es el identificador visible
 * que se usará en la simulación de acceso (HU-18).
 */
@Data
@AllArgsConstructor
@Builder
public class RegisterEmployeeResponse {
    private UUID id;
    private String employeeCode;
    private String firstName;
    private String lastName;
}
