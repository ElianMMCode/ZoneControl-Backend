package laboratorioxyz.com.ZoneControl.modulo_autenticacion.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import laboratorioxyz.com.ZoneControl.model.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;



@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateUserRequest {

    @NotNull
    @Size(min = 2, max = 35)
    private String firstName;

    @NotNull
    @Size(min = 2, max = 35)
    private String lastName;

    @NotNull
    @Email
    @Size(max = 100)
    private String email;

    @NotNull
    @Size(min = 8)
    private String password;

    @NotNull
    private Role role;

    @NotNull
    @Size(max = 12)
    private String employeeCode;
}
