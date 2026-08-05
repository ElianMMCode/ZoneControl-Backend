package laboratorioxyz.com.ZoneControl.modulo_control_acceso.dto;

import laboratorioxyz.com.ZoneControl.model.enums.AccessResult;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class ValidateAccessResponse {
    private AccessResult result;
    private String message;
    /** Datos del empleado que intentó el ingreso (null si no se pudo resolver). */
    private String employeeCode;
    private String employeeName;
    private String position;
    private String department;
}
