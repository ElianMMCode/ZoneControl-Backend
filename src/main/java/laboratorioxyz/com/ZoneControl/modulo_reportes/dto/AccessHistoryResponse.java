package laboratorioxyz.com.ZoneControl.modulo_reportes.dto;

import laboratorioxyz.com.ZoneControl.model.enums.AccessResult;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@Builder
public class AccessHistoryResponse {
    private UUID id;
    private UUID employeeId;
    private String employeeCode;
    private String employeeName;
    private String position;
    private String department;
    private String productionAreaName;
    private LocalDateTime timestamp;
    private AccessResult result;
    /** true si el empleado involucrado tiene cuenta de sistema (User). */
    private boolean hasUser;
}
