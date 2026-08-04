package laboratorioxyz.com.ZoneControl.modulo_control_acceso.service;

import laboratorioxyz.com.ZoneControl.model.entity.ProductionArea;
import laboratorioxyz.com.ZoneControl.model.repository.ProductionAreaRepository;
import laboratorioxyz.com.ZoneControl.modulo_control_acceso.dto.OccupancyResponse;
import laboratorioxyz.com.ZoneControl.modulo_control_acceso.model.AccessAlert;
import laboratorioxyz.com.ZoneControl.modulo_control_acceso.model.AccessSession;
import laboratorioxyz.com.ZoneControl.modulo_control_acceso.repository.AccessAlertRepository;
import laboratorioxyz.com.ZoneControl.modulo_control_acceso.repository.AccessSessionRepository;
import laboratorioxyz.com.ZoneControl.modulo_gestion_personal.model.Employee;
import laboratorioxyz.com.ZoneControl.modulo_gestion_personal.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AccessMonitoringServiceImpl implements AccessMonitoringService {

    private final AccessSessionRepository accessSessionRepository;
    private final AccessAlertRepository accessAlertRepository;
    private final EmployeeRepository employeeRepository;
    private final ProductionAreaRepository productionAreaRepository;
    private final RealtimeEventPublisher realtimeEventPublisher;

    @Override
    @Transactional(readOnly = true)
    public OccupancyResponse occupancy() {
        List<AccessSession> active = accessSessionRepository.findByExitTimeIsNull();
        Map<String, List<OccupancyResponse.Occupant>> byArea = new LinkedHashMap<>();
        for (AccessSession s : active) {
            String area = s.getProductionArea().getName();
            OccupancyResponse.Occupant occupant = new OccupancyResponse.Occupant(
                    s.getEmployee().getEmployeeCode(),
                    s.getEmployee().getFirstName() + " " + s.getEmployee().getLastName(),
                    s.getEntryTime());
            byArea.computeIfAbsent(area, k -> new ArrayList<>()).add(occupant);
        }
        List<OccupancyResponse.AreaOccupancy> areas = byArea.entrySet().stream()
                .map(e -> new OccupancyResponse.AreaOccupancy(e.getKey(), e.getValue().size(), e.getValue()))
                .toList();
        return new OccupancyResponse(areas);
    }

    @Override
    @Transactional
    public void exit(String employeeCode, String productionAreaName) {
        Employee employee = employeeRepository.findByEmployeeCode(employeeCode)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Empleado no encontrado: " + employeeCode));
        ProductionArea area = productionAreaRepository.findByName(productionAreaName)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Área de producción no encontrada: " + productionAreaName));

        AccessSession session = accessSessionRepository
                .findByEmployee_IdAndProductionArea_IdAndExitTimeIsNull(employee.getId(), area.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "No hay una sesión activa para el empleado en esta área"));
        session.setExitTime(LocalDateTime.now());
        accessSessionRepository.save(session);
        log.info("Access exit: employee={}, area={}", employeeCode, productionAreaName);
        realtimeEventPublisher.publish("occupancy.updated",
                Map.of("timestamp", LocalDateTime.now().toString()));
    }

    @Override
    @Transactional
    public boolean setEmergency(String zoneName, boolean cerrada) {
        ProductionArea area = productionAreaRepository.findByName(zoneName)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Zona no encontrada: " + zoneName));
        area.setEmergencyClosed(cerrada);
        productionAreaRepository.save(area);

        AccessAlert alert = AccessAlert.builder()
                .tipo(AccessAlert.AlertType.ZONA_EMERGENCIA)
                .severidad(AccessAlert.AlertSeverity.MEDIUM)
                .productionAreaName(zoneName)
                .message("Zona " + zoneName + (cerrada ? " CERRADA POR EMERGENCIA" : " reabierta"))
                .timestamp(LocalDateTime.now())
                .build();
        accessAlertRepository.save(alert);

        realtimeEventPublisher.publish("zone.updated", Map.of(
                "area", zoneName, "emergencyClosed", cerrada));
        realtimeEventPublisher.publish("alert.created", Map.of("alert", alert));
        return cerrada;
    }

    @Override
    @Transactional(readOnly = true)
    public List<AccessAlert> alerts(LocalDateTime desde, Boolean leido) {
        return accessAlertRepository.findAll().stream()
                .sorted((a, b) -> b.getTimestamp().compareTo(a.getTimestamp()))
                .filter(a -> desde == null || !a.getTimestamp().isBefore(desde))
                .filter(a -> leido == null || a.isLeido() == leido)
                .toList();
    }

    @Override
    @Transactional
    public void markAlertLeido(UUID id) {
        AccessAlert alert = accessAlertRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Alerta no encontrada"));
        alert.setLeido(true);
        accessAlertRepository.save(alert);
        log.info("Alert {} marked as read", id);
    }
}
