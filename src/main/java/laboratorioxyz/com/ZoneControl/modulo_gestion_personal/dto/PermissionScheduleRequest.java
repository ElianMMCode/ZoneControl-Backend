package laboratorioxyz.com.ZoneControl.modulo_gestion_personal.dto;

import java.time.LocalTime;

/**
 * Turno/horario por día (3.2 §9, HU-26). Opcional en Create/UpdatePermission.
 */
public record PermissionScheduleRequest(
        String dayOfWeek,
        LocalTime startTime,
        LocalTime endTime
) {}
