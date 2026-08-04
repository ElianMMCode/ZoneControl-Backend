package laboratorioxyz.com.ZoneControl.modulo_control_acceso.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Ocupación en tiempo real por área (§9.3 item 2.1).
 */
public record OccupancyResponse(List<AreaOccupancy> areas) {

    public record AreaOccupancy(String area, int aforo, List<Occupant> people) {}

    public record Occupant(String employeeCode, String nombre, LocalDateTime entryTime) {}
}
