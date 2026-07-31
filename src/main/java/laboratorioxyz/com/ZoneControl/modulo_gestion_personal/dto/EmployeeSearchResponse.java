package laboratorioxyz.com.ZoneControl.modulo_gestion_personal.dto;

import laboratorioxyz.com.ZoneControl.model.enums.DocumentType;
import laboratorioxyz.com.ZoneControl.model.enums.EmployeeStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

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
}
