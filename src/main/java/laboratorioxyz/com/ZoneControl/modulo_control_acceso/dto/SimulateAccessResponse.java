package laboratorioxyz.com.ZoneControl.modulo_control_acceso.dto;

import laboratorioxyz.com.ZoneControl.model.enums.AccessResult;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class SimulateAccessResponse {
    private AccessResult result;
    private String message;
}
