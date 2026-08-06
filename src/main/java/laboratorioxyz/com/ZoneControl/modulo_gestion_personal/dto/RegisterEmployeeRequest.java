package laboratorioxyz.com.ZoneControl.modulo_gestion_personal.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import laboratorioxyz.com.ZoneControl.model.enums.ContractType;
import laboratorioxyz.com.ZoneControl.model.enums.DocumentType;
import laboratorioxyz.com.ZoneControl.model.enums.WorkShift;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;



/**
 * DTO de entrada para POST /personal.
 * Las validaciones con Jakarta Validation (@NotNull, @Size)
 * producen HTTP 400 automáticamente cuando los campos no cumplen
 * las restricciones, sin necesidad de validación manual en el servicio.
 *
 * Los datos de "empleado real" (tipo de contrato, sede, turno,
 * fechas de vigencia) son opcionales en el registro; pueden
 * completarse o corregirse después en PATCH /api/personal/{id}.
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
    private UUID cargoId;

    @NotNull
    @Size(max = 80)
    private String departmentName;

    /**
     * Correo opcional del empleado. Si se registra, el ADMIN
     * podrá usar este correo para crear el usuario del sistema con
     * magic link (HU-05). No es corporativo.
     */
    @Email
    @Size(max = 100)
    private String email;

    private ContractType contractType;

    @Size(max = 80)
    private String baseOfficeName;

    private WorkShift workShift;

    private LocalDate hireDate;

    private LocalDate contractEndDate;
}
