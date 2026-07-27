package laboratorioxyz.com.ZoneControl.modulo_gestion_personal.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
@Builder
public class RegisterEmployeeResponse {
    private UUID id;
    private String employeeCode;
    private String firstName;
    private String lastName;
}
