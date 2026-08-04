package laboratorioxyz.com.ZoneControl.modulo_administracion.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO de entrada para PUT /admin/users/{id}.
 *
 * El ADMIN solo puede editar el correo del usuario. El nombre y apellido
 * reflejan al Employee vinculado (se gestionan en Gestión Personal), el rol
 * se asigna en la creación y el estado se maneja con PATCH /{id}/status.
 */
public record UpdateUserRequest(
        @NotBlank @Email @Size(max = 100) String email
) {}
