package laboratorioxyz.com.ZoneControl.modulo_administracion.dto;

import laboratorioxyz.com.ZoneControl.model.enums.Role;
import laboratorioxyz.com.ZoneControl.model.enums.UserStatus;
import lombok.Builder;

import java.util.UUID;

/**
 * DTO de salida para listar/detallar usuarios del sistema (GET /admin/users).
 * Incluye datos del empleado vinculado (código, cargo y correo personal)
 * que el ADMIN usa en la tabla de Gestión de Usuarios.
 */
@Builder
public record UserResponse(
        UUID id,
        String firstName,
        String lastName,
        String email,
        Role role,
        UserStatus status,
        boolean requirePasswordChange,
        String employeeCode,
        String position
) {}
