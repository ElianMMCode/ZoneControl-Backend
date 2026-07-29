package laboratorioxyz.com.ZoneControl.modulo_control_acceso.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;



@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ValidateAccessRequest {
    private String employeeCode;
    private String productionAreaName;
}
