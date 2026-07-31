package laboratorioxyz.com.ZoneControl.modulo_gestion_personal.service;

import jakarta.persistence.criteria.Predicate;
import laboratorioxyz.com.ZoneControl.model.entity.ProductionArea;
import laboratorioxyz.com.ZoneControl.model.enums.EmployeeStatus;
import laboratorioxyz.com.ZoneControl.model.enums.PermissionStatus;
import laboratorioxyz.com.ZoneControl.model.repository.ProductionAreaRepository;
import laboratorioxyz.com.ZoneControl.modulo_gestion_personal.dto.CreatePermissionRequest;
import laboratorioxyz.com.ZoneControl.modulo_gestion_personal.dto.PermissionResponse;
import laboratorioxyz.com.ZoneControl.modulo_gestion_personal.dto.UpdatePermissionRequest;
import laboratorioxyz.com.ZoneControl.modulo_gestion_personal.model.AccessPermission;
import laboratorioxyz.com.ZoneControl.modulo_gestion_personal.model.Employee;
import laboratorioxyz.com.ZoneControl.modulo_gestion_personal.repository.AccessPermissionRepository;
import laboratorioxyz.com.ZoneControl.modulo_gestion_personal.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
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
        Employee employee = employeeRepository.findByEmployeeCode(request.getEmployeeCode())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Empleado no encontrado: " + request.getEmployeeCode()));

        if (employee.getStatus() != EmployeeStatus.ACTIVO) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "No se puede otorgar acceso a empleado inactivo");
        }

        ProductionArea area = productionAreaRepository.findByName(request.getProductionAreaName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Área de producción no encontrada: " + request.getProductionAreaName()));

        if (accessPermissionRepository
                .existsByEmployee_IdAndProductionArea_Id(
                        employee.getId(), area.getId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "El empleado ya tiene un permiso para esta área. Edite el permiso existente");
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

    @Override
    @Transactional(readOnly = true)
    public Page<PermissionResponse> list(String search, PermissionStatus status, Pageable pageable) {
        Specification<AccessPermission> spec = (root, query, cb) -> {
            Predicate predicate = cb.conjunction();
            if (search != null && !search.isBlank()) {
                String like = "%" + search.toLowerCase() + "%";
                Predicate nameMatch = cb.or(
                        cb.like(cb.lower(root.get("employee").get("employeeCode")), like),
                        cb.like(cb.lower(root.get("employee").get("firstName")), like),
                        cb.like(cb.lower(root.get("employee").get("lastName")), like),
                        cb.like(cb.lower(root.get("productionArea").get("name")), like)
                );
                predicate = cb.and(predicate, nameMatch);
            }
            if (status != null) {
                predicate = cb.and(predicate, cb.equal(root.get("status"), status));
            }
            return predicate;
        };
        return accessPermissionRepository.findAll(spec, pageable).map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductionArea> listAreas() {
        return productionAreaRepository.findAll();
    }

    @Override
    @Transactional
    public PermissionResponse update(UUID id, UpdatePermissionRequest request) {
        AccessPermission permission = accessPermissionRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Permiso no encontrado"));
        if (permission.getStatus() != PermissionStatus.ACTIVO) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "No se puede editar un permiso suspendido");
        }
        if (request.startDate() != null) {
            permission.setStartDate(request.startDate());
        }
        if (request.expirationDate() != null) {
            permission.setExpirationDate(request.expirationDate());
        }
        if (request.startTime() != null) {
            permission.setStartTime(request.startTime());
        }
        if (request.endTime() != null) {
            permission.setEndTime(request.endTime());
        }
        permission = accessPermissionRepository.save(permission);
        log.info("Permission updated: id={}", id);
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
