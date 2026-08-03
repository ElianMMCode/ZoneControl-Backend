package laboratorioxyz.com.ZoneControl.modulo_gestion_personal.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import laboratorioxyz.com.ZoneControl.model.enums.DocumentType;
import laboratorioxyz.com.ZoneControl.model.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;



/**
 * DTO de entrada para POST /personal.
 * Las validaciones con Jakarta Validation (@NotNull, @Size)
 * producen HTTP 400 automáticamente cuando los campos no cumplen
 * las restricciones, sin necesidad de validación manual en el servicio.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegisterEmployeeRequest {

    @NotNull
    private DocumentType documentType;

    @NotNull
    @Size(max = 20)
    private String documentNumber;

    @NotNull
    @Size(min = 2, max = 35)
    private String firstName;

    @NotNull
    @Size(min = 2, max = 35)
    private String lastName;

    @NotNull
    @Size(max = 30)
    private String position;

    @NotNull
    @Size(max = 80)
    private String departmentName;

    /**
     * Correo personal opcional del empleado. Si se registra, el ADMIN
     * podrá usar este correo para crear el usuario del sistema con
     * magic link (HU-05). No es corporativo.
     */
    @Email
    @Size(max = 100)
    private String email;

    /**
     * Rol de sistema opcional. Si se asigna, el empleado aparecerá
     * como candidato en el panel "Empleados pendientes de activación"
     * del Admin. Nulo = empleado de solo acceso físico.
     */
    private Role systemRole;
}
