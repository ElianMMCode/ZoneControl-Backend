package laboratorioxyz.com.ZoneControl.modulo_autenticacion.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import laboratorioxyz.com.ZoneControl.model.enums.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de entrada para POST /admin/users.
 *
 * Con el flujo de magic link (HU-05), el ADMIN solo envía el código del
 * empleado y el estado inicial. El firstName, lastName, email y el ROL se
 * derivan del Employee vinculado (el rol nace del cargo del empleado), y la
 * contraseña se establece por el propio usuario a través del enlace de
 * configuración enviado por email.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateUserRequest {

    @NotNull
    @Size(max = 12)
    private String employeeCode;

    @NotNull
    private UserStatus status;
}
