package laboratorioxyz.com.ZoneControl.modulo_autenticacion.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * DTO de entrada para POST /auth/change-password.
 * Permite al usuario autenticado cambiar su propia contraseña validando
 * la actual. La nueva contraseña debe cumplir los mismos requisitos que
 * en el flujo de configuración inicial (SetupPasswordRequest).
 */
public record ChangePasswordRequest(
        @NotBlank String currentPassword,
        @NotBlank
        @Size(min = 8, max = 60)
        @Pattern(regexp = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@$!%*?&]).{8,60}$",
                message = "La contraseña debe tener mínimo 8 caracteres, al menos una letra mayúscula, una letra minúscula, un número y un carácter especial (@$!%*?&)")
        String newPassword
) {}
