package laboratorioxyz.com.ZoneControl.modulo_gestion_personal.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import laboratorioxyz.com.ZoneControl.model.enums.ContractType;
import laboratorioxyz.com.ZoneControl.model.enums.DocumentType;
import laboratorioxyz.com.ZoneControl.model.enums.EmployeeStatus;
import laboratorioxyz.com.ZoneControl.model.enums.WorkShift;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;



@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateEmployeeRequest {

    private String firstName;

    private String lastName;

    private UUID cargoId;

    @Email
    @Size(max = 100)
    private String email;

    private DocumentType documentType;

    @Size(max = 20)
    private String documentNumber;

    private String departmentName;

    private EmployeeStatus status;

    private ContractType contractType;

    @Size(max = 80)
    private String baseOfficeName;

    private WorkShift workShift;

    private LocalDate hireDate;

    private LocalDate contractEndDate;
}
