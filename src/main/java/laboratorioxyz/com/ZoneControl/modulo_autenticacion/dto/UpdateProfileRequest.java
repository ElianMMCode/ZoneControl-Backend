package laboratorioxyz.com.ZoneControl.modulo_autenticacion.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO de entrada para PUT /api/auth/profile.
 *
 * Permite al usuario autenticado actualizar sus propios datos de cuenta
 * (nombre, apellido y correo). El rol no se puede auto-editar.
 */
public record UpdateProfileRequest(
        @NotBlank @Size(min = 2, max = 35) String firstName,
        @NotBlank @Size(min = 2, max = 35) String lastName,
        @NotBlank @Email @Size(max = 100) String email
) {}
