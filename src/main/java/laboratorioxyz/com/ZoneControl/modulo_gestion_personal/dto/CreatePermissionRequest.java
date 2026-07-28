package laboratorioxyz.com.ZoneControl.modulo_gestion_personal.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreatePermissionRequest {
    private UUID employeeId;
    private UUID productionAreaId;
    private LocalDate startDate;
    private LocalDate expirationDate;
    private LocalTime startTime;
    private LocalTime endTime;
}
