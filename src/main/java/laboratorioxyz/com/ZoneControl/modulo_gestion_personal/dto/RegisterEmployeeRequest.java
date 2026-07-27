package laboratorioxyz.com.ZoneControl.modulo_gestion_personal.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import laboratorioxyz.com.ZoneControl.model.enums.DocumentType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

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
    private UUID departmentId;
}
