package laboratorioxyz.com.ZoneControl.modulo_autenticacion.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * DTO de entrada para POST /setup-password.
 * Recibe el token del magic link (sin hashear, llega por la URL del email)
 * y la nueva contraseña elegida por el usuario.
 *
 * La contraseña debe cumplir: mínimo 8 caracteres, al menos una mayúscula,
 * una minúscula, un dígito y un carácter especial (@$!%*?&).
 */
public record SetupPasswordRequest(
        @NotBlank String token,
        @NotBlank
        @Size(min = 8, max = 60)
        @Pattern(regexp = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@$!%*?&]).{8,60}$",
                message = "La contraseña debe tener mínimo 8 caracteres, al menos una letra mayúscula, una letra minúscula, un número y un carácter especial (@$!%*?&)")
        String newPassword
) {}
