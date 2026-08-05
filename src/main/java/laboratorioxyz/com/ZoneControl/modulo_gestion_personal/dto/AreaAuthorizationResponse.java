package laboratorioxyz.com.ZoneControl.modulo_gestion_personal.dto;

import laboratorioxyz.com.ZoneControl.model.enums.PermissionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

/**
 * Autorización de acceso a un área (vista "Autorizaciones" del panel de
 * zonas). Incluye la información completa del permiso: empleado, vigencia,
 * horario base y turnos por día (LUN..DOM).
 */
@Data
@AllArgsConstructor
@Builder
public class AreaAuthorizationResponse {
    private UUID id;
    private String employeeCode;
    private String employeeName;
    private String position;
    private String department;
    private PermissionStatus permissionStatus;
    private LocalDate startDate;
    private LocalDate expirationDate;
    private LocalDate reactivationDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private List<ScheduleDto> schedules;

    @Data
    @AllArgsConstructor
    @Builder
    public static class ScheduleDto {
        private String dayOfWeek;
        private LocalTime startTime;
        private LocalTime endTime;
    }
}
