package laboratorioxyz.com.ZoneControl.modulo_control_acceso.service;

import laboratorioxyz.com.ZoneControl.model.entity.ProductionArea;
import laboratorioxyz.com.ZoneControl.model.enums.AccessResult;
import laboratorioxyz.com.ZoneControl.model.enums.EmployeeStatus;
import laboratorioxyz.com.ZoneControl.model.repository.ProductionAreaRepository;
import laboratorioxyz.com.ZoneControl.modulo_control_acceso.dto.ValidateAccessResponse;
import laboratorioxyz.com.ZoneControl.modulo_control_acceso.model.AccessAlert;
import laboratorioxyz.com.ZoneControl.modulo_control_acceso.model.AccessHistory;
import laboratorioxyz.com.ZoneControl.modulo_control_acceso.model.AccessSession;
import laboratorioxyz.com.ZoneControl.modulo_control_acceso.repository.AccessAlertRepository;
import laboratorioxyz.com.ZoneControl.modulo_control_acceso.repository.AccessHistoryRepository;
import laboratorioxyz.com.ZoneControl.modulo_control_acceso.repository.AccessSessionRepository;
import laboratorioxyz.com.ZoneControl.modulo_gestion_personal.model.Employee;
import laboratorioxyz.com.ZoneControl.modulo_gestion_personal.repository.AccessPermissionRepository;
import laboratorioxyz.com.ZoneControl.modulo_gestion_personal.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AccessValidationServiceImpl implements AccessValidationService {

    private final EmployeeRepository employeeRepository;
    private final ProductionAreaRepository productionAreaRepository;
    private final AccessPermissionRepository accessPermissionRepository;
    private final AccessHistoryRepository accessHistoryRepository;
    private final AccessSessionRepository accessSessionRepository;
    private final AccessAlertRepository accessAlertRepository;
    private final RealtimeEventPublisher realtimeEventPublisher;

    @Override
    @Transactional
    public ValidateAccessResponse validate(String employeeCode, String productionAreaName) {
        ProductionArea area = productionAreaRepository.findByName(productionAreaName)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Área de producción no encontrada: " + productionAreaName));

        if (!area.isActive()) {
            logAccess(null, area.getName(), AccessResult.DENIED);
            publishValidated(null, area.getName(), AccessResult.DENIED,
                    "ÁREA INACTIVA");
            return buildResponse(AccessResult.DENIED, "ÁREA INACTIVA", null);
        }

        // Kill switch de emergencia (2.2): la zona cerrada deniega el ingreso.
        if (area.isEmergencyClosed()) {
            logAccess(null, area.getName(), AccessResult.DENIED);
            publishValidated(null, area.getName(), AccessResult.DENIED,
                    "ZONA CERRADA POR EMERGENCIA");
            return buildResponse(AccessResult.DENIED, "ZONA CERRADA POR EMERGENCIA", null);
        }

        Employee employee = employeeRepository.findByEmployeeCode(employeeCode).orElse(null);

        if (employee == null) {
            logAccess(null, area.getName(), AccessResult.UNREGISTERED);
            publishValidated(null, area.getName(), AccessResult.UNREGISTERED, "NO REGISTRADO");
            return buildResponse(AccessResult.UNREGISTERED, "NO REGISTRADO", null);
        }

        if (employee.getStatus() != EmployeeStatus.ACTIVO) {
            logAccess(employee, area.getName(), AccessResult.DENIED);
            maybeAlertRepeatedDenials(employee);
            publishValidated(employee, area.getName(), AccessResult.DENIED, "INGRESO DENEGADO");
            return buildResponse(AccessResult.DENIED, "INGRESO DENEGADO", employee);
        }

        boolean hasValidPermission = accessPermissionRepository.hasValidPermission(
                employee.getId(), area.getId(), LocalDate.now(), LocalTime.now(),
                laboratorioxyz.com.ZoneControl.model.enums.WeekDay.today());

        if (!hasValidPermission) {
            logAccess(employee, area.getName(), AccessResult.SUSPENDED);
            publishValidated(employee, area.getName(), AccessResult.SUSPENDED, "ACCESO SUSPENDIDO");
            return buildResponse(AccessResult.SUSPENDED, "ACCESO SUSPENDIDO", employee);
        }

        // Acceso autorizado: cerrar sesión previa (si existe) y abrir una nueva (2.1).
        closeOpenSession(employee.getId(), area.getId());
        accessSessionRepository.save(AccessSession.builder()
                .employee(employee)
                .productionArea(area)
                .entryTime(LocalDateTime.now())
                .build());

        logAccess(employee, area.getName(), AccessResult.AUTHORIZED);
        publishValidated(employee, area.getName(), AccessResult.AUTHORIZED, "INGRESO AUTORIZADO");
        publishOccupancy();
        return buildResponse(AccessResult.AUTHORIZED, "INGRESO AUTORIZADO", employee);
    }

    private ValidateAccessResponse buildResponse(AccessResult result, String message, Employee employee) {
        return ValidateAccessResponse.builder()
                .result(result)
                .message(message)
                .employeeCode(employee != null ? employee.getEmployeeCode() : null)
                .employeeName(employee != null
                        ? employee.getFirstName() + " " + employee.getLastName() : null)
                .position(employee != null ? employee.getPosition() : null)
                .department(employee != null && employee.getDepartment() != null
                        ? employee.getDepartment().getName() : null)
                .build();
    }

    private void closeOpenSession(java.util.UUID employeeId, java.util.UUID areaId) {
        Optional<AccessSession> open = accessSessionRepository
                .findByEmployee_IdAndProductionArea_IdAndExitTimeIsNull(employeeId, areaId);
        open.ifPresent(s -> {
            s.setExitTime(LocalDateTime.now());
            accessSessionRepository.save(s);
        });
    }

    private void maybeAlertRepeatedDenials(Employee employee) {
        LocalDateTime since = LocalDateTime.now().minusMinutes(15);
        long denials = accessHistoryRepository.countByEmployeeAndResultSince(
                employee.getId(), AccessResult.DENIED, since);
        if (denials >= 3) {
            createAlert(AccessAlert.AlertType.DENEGACIONES_REPETIDAS,
                    AccessAlert.AlertSeverity.MEDIUM, employee.getEmployeeCode(), null,
                    "≥3 intentos denegados del empleado " + employee.getEmployeeCode() + " en 15 min",
                    employee.getUser() != null);
        }
    }

    private void createAlert(AccessAlert.AlertType tipo, AccessAlert.AlertSeverity severidad,
                             String employeeCode, String areaName, String message, boolean hasUser) {
        AccessAlert alert = AccessAlert.builder()
                .tipo(tipo).severidad(severidad)
                .employeeCode(employeeCode).productionAreaName(areaName)
                .message(message).timestamp(LocalDateTime.now())
                .hasUser(hasUser)
                .build();
        accessAlertRepository.save(alert);
        realtimeEventPublisher.publish("alert.created", Map.of(
                "alert", alert));
    }

    private void publishValidated(Employee employee, String areaName, AccessResult result, String message) {
        // HashMap permite valores null (employeeName es null si el empleado no se resuelve).
        java.util.Map<String, Object> payload = new java.util.LinkedHashMap<>();
        payload.put("employeeId", employee != null ? employee.getId() : null);
        payload.put("employeeCode", employee != null ? employee.getEmployeeCode() : "UNKNOWN");
        payload.put("employeeName", employee != null
                ? employee.getFirstName() + " " + employee.getLastName() : null);
        payload.put("area", areaName);
        payload.put("result", result.name());
        payload.put("message", message);
        payload.put("timestamp", LocalDateTime.now().toString());
        realtimeEventPublisher.publish("access.validated", payload);
    }

    private void publishOccupancy() {
        realtimeEventPublisher.publish("occupancy.updated", Map.of(
                "timestamp", LocalDateTime.now().toString()));
    }

    private void logAccess(Employee employee, String areaName, AccessResult result) {
        AccessHistory history = AccessHistory.builder()
                .employee(employee)
                .department(employee != null ? employee.getDepartment().getName() : null)
                .productionAreaName(areaName)
                .timestamp(LocalDateTime.now())
                .result(result)
                .build();
        accessHistoryRepository.save(history);
        log.info("Access validation: employee={}, area={}, result={}",
                employee != null ? employee.getEmployeeCode() : "UNKNOWN",
                areaName, result);
    }
}
