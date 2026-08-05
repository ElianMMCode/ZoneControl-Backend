package laboratorioxyz.com.ZoneControl.modulo_control_acceso.dto;

import laboratorioxyz.com.ZoneControl.model.enums.AccessResult;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@Builder
public class ExitResponse {
    private AccessResult result;
    private String message;
    private String employeeCode;
    private String employeeName;
    private String position;
    private String department;
    private String productionAreaName;
    private LocalDateTime timestamp;
}
