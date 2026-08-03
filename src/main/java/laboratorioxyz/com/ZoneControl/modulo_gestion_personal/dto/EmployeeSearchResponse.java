package laboratorioxyz.com.ZoneControl.modulo_gestion_personal.dto;

import laboratorioxyz.com.ZoneControl.model.enums.ContractType;
import laboratorioxyz.com.ZoneControl.model.enums.DocumentType;
import laboratorioxyz.com.ZoneControl.model.enums.EmployeeStatus;
import laboratorioxyz.com.ZoneControl.model.enums.Role;
import laboratorioxyz.com.ZoneControl.model.enums.WorkShift;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
@AllArgsConstructor
@Builder
public class EmployeeSearchResponse {
    private UUID id;
    private String employeeCode;
    private DocumentType documentType;
    private String documentNumber;
    private String firstName;
    private String lastName;
    private String position;
    private String email;
    private String departmentName;
    private EmployeeStatus status;
    private Role systemRole;
    private ContractType contractType;
    private String baseOfficeName;
    private WorkShift workShift;
    private LocalDate hireDate;
    private LocalDate contractEndDate;
    private String photoUrl;
}
