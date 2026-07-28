package laboratorioxyz.com.ZoneControl.modulo_gestion_personal.service;

import laboratorioxyz.com.ZoneControl.model.entity.ProductionArea;
import laboratorioxyz.com.ZoneControl.model.enums.EmployeeStatus;
import laboratorioxyz.com.ZoneControl.model.enums.PermissionStatus;
import laboratorioxyz.com.ZoneControl.model.repository.ProductionAreaRepository;
import laboratorioxyz.com.ZoneControl.modulo_gestion_personal.dto.CreatePermissionRequest;
import laboratorioxyz.com.ZoneControl.modulo_gestion_personal.dto.PermissionResponse;
import laboratorioxyz.com.ZoneControl.modulo_gestion_personal.model.AccessPermission;
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
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PermissionServiceImpl implements PermissionService {

    private final EmployeeRepository employeeRepository;
    private final ProductionAreaRepository productionAreaRepository;
    private final AccessPermissionRepository accessPermissionRepository;

    @Override
    @Transactional
    public PermissionResponse grant(CreatePermissionRequest request) {
        Employee employee = employeeRepository.findById(request.getEmployeeId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Empleado no encontrado"));

        if (employee.getStatus() != EmployeeStatus.ACTIVO) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "No se puede otorgar acceso a empleado inactivo");
        }

        ProductionArea area = productionAreaRepository.findById(request.getProductionAreaId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Área de producción no encontrada"));

        if (accessPermissionRepository
                .existsByEmployee_IdAndProductionArea_IdAndStartTimeAndEndTimeAndStatus(
                        employee.getId(), area.getId(),
                        request.getStartTime(), request.getEndTime(),
                        PermissionStatus.ACTIVO)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Conflicto de permisos existente");
        }

        AccessPermission permission = AccessPermission.builder()
                .employee(employee)
                .productionArea(area)
                .status(PermissionStatus.ACTIVO)
                .startDate(request.getStartDate())
                .expirationDate(request.getExpirationDate())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .build();

        permission = accessPermissionRepository.save(permission);
        log.info("Permission granted: employee={}, area={}, id={}",
                employee.getEmployeeCode(), area.getName(), permission.getId());

        return toResponse(permission);
    }

    @Override
    @Transactional
    public Map<String, String> revoke(UUID id) {
        AccessPermission permission = accessPermissionRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Permiso no encontrado"));

        accessPermissionRepository.delete(permission);
        log.info("Permission revoked: id={}, employee={}, area={}",
                id, permission.getEmployee().getEmployeeCode(),
                permission.getProductionArea().getName());

        return Map.of("message", "Permiso revocado exitosamente");
    }

    @Override
    @Transactional
    public PermissionResponse suspend(UUID id, LocalDate reactivationDate) {
        AccessPermission permission = accessPermissionRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Permiso no encontrado"));

        permission.setStatus(PermissionStatus.SUSPENDIDO);
        permission.setReactivationDate(reactivationDate);
        permission = accessPermissionRepository.save(permission);

        log.info("Permission suspended: id={}, reactivation={}", id, reactivationDate);
        return toResponse(permission);
    }

    private PermissionResponse toResponse(AccessPermission permission) {
        return PermissionResponse.builder()
                .id(permission.getId())
                .employeeCode(permission.getEmployee().getEmployeeCode())
                .employeeName(permission.getEmployee().getFirstName()
                        + " " + permission.getEmployee().getLastName())
                .areaName(permission.getProductionArea().getName())
                .status(permission.getStatus())
                .startDate(permission.getStartDate())
                .expirationDate(permission.getExpirationDate())
                .reactivationDate(permission.getReactivationDate())
                .startTime(permission.getStartTime())
                .endTime(permission.getEndTime())
                .build();
    }
}
