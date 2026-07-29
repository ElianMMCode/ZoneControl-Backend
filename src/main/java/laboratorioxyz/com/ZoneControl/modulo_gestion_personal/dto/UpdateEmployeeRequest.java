package laboratorioxyz.com.ZoneControl.modulo_gestion_personal.dto;

import laboratorioxyz.com.ZoneControl.model.enums.DocumentType;
import laboratorioxyz.com.ZoneControl.model.enums.EmployeeStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;



@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateEmployeeRequest {
    private String firstName;
    private String lastName;
    private String position;
    private DocumentType documentType;
    private String documentNumber;
    private String departmentName;
    private EmployeeStatus status;
}
