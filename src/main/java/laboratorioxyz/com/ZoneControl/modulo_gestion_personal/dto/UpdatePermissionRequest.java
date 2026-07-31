package laboratorioxyz.com.ZoneControl.modulo_gestion_personal.dto;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * DTO para PATCH /permisos/{id}.
 * Permite al gestor editar los horarios y fechas de vigencia de un
 * permiso existente. El empleado y el área no son editables porque
 * cada permiso es único por (empleado, área).
 */
public record UpdatePermissionRequest(
        LocalDate startDate,
        LocalDate expirationDate,
        LocalTime startTime,
        LocalTime endTime
) {}
