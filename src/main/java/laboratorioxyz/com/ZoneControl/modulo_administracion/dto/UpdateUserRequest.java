package laboratorioxyz.com.ZoneControl.modulo_administracion.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import laboratorioxyz.com.ZoneControl.model.enums.Role;

public record UpdateUserRequest(
    @Size(min = 2, max = 35) String firstName,
    @Size(min = 2, max = 35) String lastName,
    @Email @Size(max = 100) String email,
    Role role,
    @Size(max = 12) String employeeCode
) {}
