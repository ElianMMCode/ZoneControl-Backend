package laboratorioxyz.com.ZoneControl.modulo_control_acceso.service;

import laboratorioxyz.com.ZoneControl.modulo_control_acceso.dto.OccupancyResponse;
import laboratorioxyz.com.ZoneControl.modulo_control_acceso.model.AccessAlert;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Monitoreo en tiempo real del control de acceso (2.1–2.4 §9):
 * ocupación, salida, cierre de emergencia y alertas. Roles: ADMIN/SUPERVISOR.
 */
public interface AccessMonitoringService {
    OccupancyResponse occupancy();

    void exit(String employeeCode, String productionAreaName);

    boolean setEmergency(String zoneName, boolean cerrada);

    List<AccessAlert> alerts(LocalDateTime desde, Boolean leido);

    void markAlertLeido(UUID id);
}
