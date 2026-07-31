package laboratorioxyz.com.ZoneControl.modulo_gestion_personal.service;

import jakarta.persistence.criteria.Predicate;
import laboratorioxyz.com.ZoneControl.model.entity.ProductionArea;
import laboratorioxyz.com.ZoneControl.model.enums.EmployeeStatus;
import laboratorioxyz.com.ZoneControl.model.enums.PermissionStatus;
import laboratorioxyz.com.ZoneControl.model.repository.ProductionAreaRepository;
import laboratorioxyz.com.ZoneControl.modulo_gestion_personal.dto.AreaAuthorizationResponse;
import laboratorioxyz.com.ZoneControl.modulo_gestion_personal.dto.AreaEmployeeResponse;
import laboratorioxyz.com.ZoneControl.modulo_gestion_personal.dto.CreatePermissionRequest;
import laboratorioxyz.com.ZoneControl.modulo_gestion_personal.dto.PermissionResponse;
import laboratorioxyz.com.ZoneControl.modulo_gestion_personal.dto.PermissionScheduleRequest;
import laboratorioxyz.com.ZoneControl.modulo_gestion_personal.dto.UpdatePermissionRequest;
import laboratorioxyz.com.ZoneControl.modulo_gestion_personal.model.AccessPermission;
import laboratorioxyz.com.ZoneControl.modulo_gestion_personal.model.Employee;
import laboratorioxyz.com.ZoneControl.modulo_gestion_personal.model.PermissionSchedule;
import laboratorioxyz.com.ZoneControl.modulo_gestion_personal.repository.AccessPermissionRepository;
import laboratorioxyz.com.ZoneControl.modulo_gestion_personal.repository.EmployeeRepository;
import laboratorioxyz.com.ZoneControl.modulo_gestion_personal.repository.PermissionScheduleRepository;
import laboratorioxyz.com.ZoneControl.model.enums.WeekDay;
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
import java.time.LocalTime;
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
    private final PermissionScheduleRepository permissionScheduleRepository;

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

        if (!area.isActive()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Área de producción no encontrada o desactivada");
        }

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
        applySchedules(permission, request.getSchedules(),
                request.getStartTime(), request.getEndTime());
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
    public List<AreaEmployeeResponse> listAreaEmployees(String areaName) {
        ProductionArea area = requireArea(areaName);
        return accessPermissionRepository.findByProductionArea_Name(area.getName()).stream()
                .map(p -> {
                    Employee e = p.getEmployee();
                    return AreaEmployeeResponse.builder()
                            .employeeCode(e.getEmployeeCode())
                            .employeeName(e.getFirstName() + " " + e.getLastName())
                            .position(e.getPosition())
                            .department(e.getDepartment() != null ? e.getDepartment().getName() : null)
                            .employeeStatus(e.getStatus())
                            .build();
                })
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AreaAuthorizationResponse> listAreaAuthorizations(String areaName) {
        ProductionArea area = requireArea(areaName);
        return accessPermissionRepository.findByProductionArea_Name(area.getName()).stream()
                .map(this::toAreaAuthorizationResponse)
                .toList();
    }

    private ProductionArea requireArea(String areaName) {
        if (areaName == null || areaName.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "El nombre del área es obligatorio");
        }
        return productionAreaRepository.findByName(areaName)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Área de producción no encontrada: " + areaName));
    }

    private AreaAuthorizationResponse toAreaAuthorizationResponse(AccessPermission p) {
        Employee e = p.getEmployee();
        List<AreaAuthorizationResponse.ScheduleDto> schedules =
                permissionScheduleRepository.findByPermission_Id(p.getId()).stream()
                        .map(s -> AreaAuthorizationResponse.ScheduleDto.builder()
                                .dayOfWeek(s.getDayOfWeek().name())
                                .startTime(s.getStartTime())
                                .endTime(s.getEndTime())
                                .build())
                        .toList();
        return AreaAuthorizationResponse.builder()
                .id(p.getId())
                .employeeCode(e.getEmployeeCode())
                .employeeName(e.getFirstName() + " " + e.getLastName())
                .position(e.getPosition())
                .department(e.getDepartment() != null ? e.getDepartment().getName() : null)
                .permissionStatus(p.getStatus())
                .startDate(p.getStartDate())
                .expirationDate(p.getExpirationDate())
                .reactivationDate(p.getReactivationDate())
                .startTime(p.getStartTime())
                .endTime(p.getEndTime())
                .schedules(schedules)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductionArea> listAreas() {
        return productionAreaRepository.findAllByActive(true);
    }

    @Override
    @Transactional
    public ProductionArea createArea(String name, String description) {
        if (name == null || name.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "El nombre del área es obligatorio");
        }
        if (name.length() > 30) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "El nombre del área no puede superar los 30 caracteres");
        }
        if (productionAreaRepository.existsByName(name)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Ya existe un área con el nombre: " + name);
        }
        ProductionArea area = ProductionArea.builder()
                .name(name)
                .description(description)
                .build();
        area = productionAreaRepository.save(area);
        log.info("Production area created: id={}, name={}", area.getId(), area.getName());
        return area;
    }

    @Override
    @Transactional
    public ProductionArea updateArea(UUID id, String name, String description) {
        ProductionArea area = productionAreaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Área no encontrada"));
        if (name == null || name.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "El nombre del área es obligatorio");
        }
        if (name.length() > 30) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "El nombre del área no puede superar los 30 caracteres");
        }
        if (!area.getName().equals(name) && productionAreaRepository.existsByName(name)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Ya existe un área con el nombre: " + name);
        }
        area.setName(name);
        if (description != null) {
            area.setDescription(description);
        }
        area = productionAreaRepository.save(area);
        log.info("Production area updated: id={}", area.getId());
        return area;
    }

    @Override
    @Transactional
    public void deleteArea(UUID id) {
        ProductionArea area = productionAreaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Área no encontrada"));
        long active = accessPermissionRepository
                .countByProductionArea_IdAndStatus(id, PermissionStatus.ACTIVO);
        if (active > 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "No se puede eliminar el área porque tiene "
                            + active + " permiso(s) activo(s) asociado(s)");
        }
        productionAreaRepository.delete(area);
        log.info("Production area deleted: id={}", id);
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
        // Si se envían schedules, reemplazan los existentes; si no, se conservan.
        if (request.schedules() != null) {
            permissionScheduleRepository.deleteByPermission_Id(permission.getId());
            applySchedules(permission, request.schedules(),
                    permission.getStartTime(), permission.getEndTime());
        }
        permission = accessPermissionRepository.save(permission);
        log.info("Permission updated: id={}", id);
        return toResponse(permission);
    }

    @Override
    @Transactional
    public PermissionResponse reactivate(UUID id) {
        AccessPermission permission = accessPermissionRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Permiso no encontrado"));
        if (permission.getStatus() != PermissionStatus.SUSPENDIDO) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "El permiso ya está activo");
        }
        permission.setStatus(PermissionStatus.ACTIVO);
        permission.setReactivationDate(null);
        permission = accessPermissionRepository.save(permission);
        log.info("Permission reactivated: id={}", id);
        return toResponse(permission);
    }

    /**
     * Crea los turnos por día de un permiso (3.2 §9). Si no se envían schedules
     * se genera el schedule LUN-DOM con los horarios base (migración implícita
     * para los permisos existentes). Los horarios con startTime > endTime
     * representan un turno nocturno que cruza la medianoche.
     */
    private void applySchedules(AccessPermission permission, List<PermissionScheduleRequest> schedules,
                                LocalTime defaultStart, LocalTime defaultEnd) {
        if (schedules == null || schedules.isEmpty()) {
            for (WeekDay day : WeekDay.values()) {
                permissionScheduleRepository.save(PermissionSchedule.builder()
                        .permission(permission)
                        .dayOfWeek(day)
                        .startTime(defaultStart)
                        .endTime(defaultEnd)
                        .build());
            }
            return;
        }
        for (PermissionScheduleRequest req : schedules) {
            permissionScheduleRepository.save(PermissionSchedule.builder()
                    .permission(permission)
                    .dayOfWeek(WeekDay.valueOf(req.dayOfWeek()))
                    .startTime(req.startTime())
                    .endTime(req.endTime())
                    .build());
        }
    }

    private PermissionResponse toResponse(AccessPermission permission) {
        List<PermissionResponse.PermissionScheduleItem> schedules =
                permissionScheduleRepository.findByPermission_Id(permission.getId()).stream()
                        .map(s -> new PermissionResponse.PermissionScheduleItem(
                                s.getDayOfWeek().name(), s.getStartTime(), s.getEndTime()))
                        .toList();
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
                .schedules(schedules)
                .build();
    }
}
