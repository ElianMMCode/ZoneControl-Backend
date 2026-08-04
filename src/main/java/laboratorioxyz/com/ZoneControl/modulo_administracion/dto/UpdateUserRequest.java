package laboratorioxyz.com.ZoneControl.modulo_administracion.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import laboratorioxyz.com.ZoneControl.model.enums.UserStatus;

/**
 * DTO de entrada para PUT /admin/users/{id}.
 *
 * El ADMIN puede editar el correo y el estado del usuario. El nombre y
 * apellido reflejan al Employee vinculado (se gestionan en Gestión de
 * Personal) y el rol se asigna en la creación (HU-05).
 */
public record UpdateUserRequest(
        @NotBlank @Email @Size(max = 100) String email,
        @NotNull UserStatus status
) {}
