package laboratorioxyz.com.ZoneControl.modulo_gestion_personal.dto;

import laboratorioxyz.com.ZoneControl.model.enums.EmployeeStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

/**
 * Empleado asignado a un área de producción (vista "Empleados asignados"
 * del panel de zonas). Datos mínimos: código, nombre, cargo, departamento
 * y estado del empleado.
 */
@Data
@AllArgsConstructor
@Builder
public class AreaEmployeeResponse {
    private String employeeCode;
    private String employeeName;
    private String position;
    private String department;
    private EmployeeStatus employeeStatus;
}
