package laboratorioxyz.com.ZoneControl.modulo_control_acceso.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SimulateAccessRequest {
    private String employeeCode;
    private UUID productionAreaId;
}
