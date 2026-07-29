package laboratorioxyz.com.ZoneControl.modulo_control_acceso.service;

import laboratorioxyz.com.ZoneControl.model.entity.ProductionArea;
import laboratorioxyz.com.ZoneControl.model.enums.AccessResult;
import laboratorioxyz.com.ZoneControl.model.enums.EmployeeStatus;
import laboratorioxyz.com.ZoneControl.model.repository.ProductionAreaRepository;
import laboratorioxyz.com.ZoneControl.modulo_control_acceso.dto.SimulateAccessResponse;
import laboratorioxyz.com.ZoneControl.modulo_control_acceso.model.AccessHistory;
import laboratorioxyz.com.ZoneControl.modulo_control_acceso.repository.AccessHistoryRepository;
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
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AccessSimulationServiceImpl implements AccessSimulationService {

    private final EmployeeRepository employeeRepository;
    private final ProductionAreaRepository productionAreaRepository;
    private final AccessPermissionRepository accessPermissionRepository;
    private final AccessHistoryRepository accessHistoryRepository;

    @Override
    @Transactional
    public SimulateAccessResponse simulate(String employeeCode, UUID productionAreaId) {
        ProductionArea area = productionAreaRepository.findById(productionAreaId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Área de producción no encontrada"));

        Employee employee = employeeRepository.findByEmployeeCode(employeeCode).orElse(null);

        if (employee == null) {
            logAccess(null, area.getName(), AccessResult.UNREGISTERED);
            return new SimulateAccessResponse(AccessResult.UNREGISTERED, "NO REGISTRADO");
        }

        if (employee.getStatus() != EmployeeStatus.ACTIVO) {
            logAccess(employee, area.getName(), AccessResult.DENIED);
            return new SimulateAccessResponse(AccessResult.DENIED, "INGRESO DENEGADO");
        }

        boolean hasValidPermission = accessPermissionRepository.hasValidPermission(
                employee.getId(), productionAreaId, LocalDate.now(), LocalTime.now());

        if (!hasValidPermission) {
            logAccess(employee, area.getName(), AccessResult.SUSPENDED);
            return new SimulateAccessResponse(AccessResult.SUSPENDED, "ACCESO SUSPENDIDO");
        }

        logAccess(employee, area.getName(), AccessResult.AUTHORIZED);
        return new SimulateAccessResponse(AccessResult.AUTHORIZED, "INGRESO AUTORIZADO");
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
        log.info("Access simulation: employee={}, area={}, result={}", 
                employee != null ? employee.getEmployeeCode() : "UNKNOWN",
                areaName, result);
    }
}
