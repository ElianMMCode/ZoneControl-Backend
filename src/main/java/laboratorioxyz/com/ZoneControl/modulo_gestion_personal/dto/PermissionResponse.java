package laboratorioxyz.com.ZoneControl.modulo_gestion_personal.dto;

import laboratorioxyz.com.ZoneControl.model.enums.PermissionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@Builder
public class PermissionResponse {
    private UUID id;
    private String employeeCode;
    private String employeeName;
    private String areaName;
    private PermissionStatus status;
    private LocalDate startDate;
    private LocalDate expirationDate;
    private LocalDate reactivationDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private List<PermissionScheduleItem> schedules;

    /**
     * Turno por día de la semana de un permiso (HU-26).
     */
    public record PermissionScheduleItem(String dayOfWeek, LocalTime startTime, LocalTime endTime) {}
}
