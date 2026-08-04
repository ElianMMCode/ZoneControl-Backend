package laboratorioxyz.com.ZoneControl.modulo_gestion_personal.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreatePermissionRequest {
    private String employeeCode;
    private String productionAreaName;
    private LocalDate startDate;
    private LocalDate expirationDate;
    private LocalTime startTime;
    private LocalTime endTime;
    /** Turnos por día opcionales (3.2 §9). Si no vienen, se crea LUN-DOM con startTime/endTime. */
    private List<PermissionScheduleRequest> schedules;
}
