package laboratorioxyz.com.ZoneControl.modulo_gestion_personal.service;

import jakarta.persistence.criteria.Predicate;
import laboratorioxyz.com.ZoneControl.model.entity.Department;
import laboratorioxyz.com.ZoneControl.model.enums.DocumentType;
import laboratorioxyz.com.ZoneControl.model.enums.EmployeeStatus;
import laboratorioxyz.com.ZoneControl.model.enums.PermissionStatus;
import laboratorioxyz.com.ZoneControl.model.repository.DepartmentRepository;
import laboratorioxyz.com.ZoneControl.modulo_autenticacion.service.UserService;
import laboratorioxyz.com.ZoneControl.modulo_gestion_personal.dto.EmployeeSearchResponse;
import laboratorioxyz.com.ZoneControl.modulo_gestion_personal.dto.RegisterEmployeeRequest;
import laboratorioxyz.com.ZoneControl.modulo_gestion_personal.dto.RegisterEmployeeResponse;
import laboratorioxyz.com.ZoneControl.modulo_gestion_personal.dto.UpdateEmployeeRequest;
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

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final AccessPermissionRepository accessPermissionRepository;
    private final UserService userService;

    @Override
    @Transactional
    public RegisterEmployeeResponse register(RegisterEmployeeRequest request) {
        if (request.getDocumentType() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "Tipo de documento no válido. Los tipos permitidos son: CC, CE, TI, PA, RC");
        }

        if (employeeRepository.existsByDocumentTypeAndDocumentNumber(
                request.getDocumentType(), request.getDocumentNumber())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                "Ya existe un empleado registrado con el documento " + request.getDocumentType()
                + " número " + request.getDocumentNumber());
        }

        Department department = departmentRepository.findById(request.getDepartmentId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Departamento no encontrado"));

        String employeeCode = generateEmployeeCode();

        Employee employee = Employee.builder()
                .employeeCode(employeeCode)
                .documentType(request.getDocumentType())
                .documentNumber(request.getDocumentNumber())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .position(request.getPosition())
                .department(department)
                .build();

        employee = employeeRepository.save(employee);

        return RegisterEmployeeResponse.builder()
                .id(employee.getId())
                .employeeCode(employee.getEmployeeCode())
                .firstName(employee.getFirstName())
                .lastName(employee.getLastName())
                .build();
    }

    private String generateEmployeeCode() {
        String maxCode = employeeRepository.findMaxEmployeeCode();
        int nextNumber = 1;
        if (maxCode != null) {
            nextNumber = Integer.parseInt(maxCode.substring(4)) + 1;
        }
        return String.format("EMP-%06d", nextNumber);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<EmployeeSearchResponse> search(String documentType, String documentNumber,
                                                String firstName, String lastName,
                                                UUID departmentId, EmployeeStatus status,
                                                Pageable pageable) {
        if (documentType == null && documentNumber == null && firstName == null
                && lastName == null && departmentId == null && status == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Debe seleccionar al menos un filtro de búsqueda");
        }

        Specification<Employee> spec = (root, query, cb) -> {
            Predicate predicate = cb.conjunction();
            if (documentType != null && !documentType.isBlank()) {
                predicate = cb.and(predicate,
                        cb.equal(root.get("documentType"), DocumentType.valueOf(documentType)));
            }
            if (documentNumber != null && !documentNumber.isBlank()) {
                predicate = cb.and(predicate,
                        cb.like(root.get("documentNumber"), "%" + documentNumber + "%"));
            }
            if (firstName != null && !firstName.isBlank()) {
                predicate = cb.and(predicate,
                        cb.like(cb.lower(root.get("firstName")), "%" + firstName.toLowerCase() + "%"));
            }
            if (lastName != null && !lastName.isBlank()) {
                predicate = cb.and(predicate,
                        cb.like(cb.lower(root.get("lastName")), "%" + lastName.toLowerCase() + "%"));
            }
            if (departmentId != null) {
                predicate = cb.and(predicate,
                        cb.equal(root.get("department").get("id"), departmentId));
            }
            if (status != null) {
                predicate = cb.and(predicate,
                        cb.equal(root.get("status"), status));
            }
            return predicate;
        };

        return employeeRepository.findAll(spec, pageable).map(this::toSearchResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public EmployeeSearchResponse findById(UUID id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Empleado no encontrado"));
        return toSearchResponse(employee);
    }

    @Override
    @Transactional
    public EmployeeSearchResponse update(UUID id, UpdateEmployeeRequest request) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Empleado no encontrado"));

        if (request.getFirstName() != null) {
            employee.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null) {
            employee.setLastName(request.getLastName());
        }
        if (request.getPosition() != null) {
            employee.setPosition(request.getPosition());
        }
        if (request.getDocumentType() != null || request.getDocumentNumber() != null) {
            DocumentType newType = request.getDocumentType() != null
                    ? request.getDocumentType() : employee.getDocumentType();
            String newNumber = request.getDocumentNumber() != null
                    ? request.getDocumentNumber() : employee.getDocumentNumber();
            if (!newType.equals(employee.getDocumentType())
                    || !newNumber.equals(employee.getDocumentNumber())) {
                if (employeeRepository.existsByDocumentTypeAndDocumentNumber(newType, newNumber)) {
                    throw new ResponseStatusException(HttpStatus.CONFLICT,
                            "Ya existe un empleado con el documento " + newType + " número " + newNumber);
                }
                employee.setDocumentType(newType);
                employee.setDocumentNumber(newNumber);
            }
        }
        if (request.getDepartmentId() != null) {
            Department department = departmentRepository.findById(request.getDepartmentId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            "Departamento no encontrado"));
            employee.setDepartment(department);
        }
        if (request.getStatus() != null) {
            EmployeeStatus previousStatus = employee.getStatus();
            employee.setStatus(request.getStatus());
            if (request.getStatus() == EmployeeStatus.INACTIVO
                    || request.getStatus() == EmployeeStatus.SUSPENDIDO) {
                cascadeDeactivate(employee.getId());
            }
        }

        employee = employeeRepository.save(employee);
        return toSearchResponse(employee);
    }

    private void cascadeDeactivate(UUID employeeId) {
        int updatedPermissions = accessPermissionRepository.updateStatusByEmployeeId(
                employeeId, PermissionStatus.SUSPENDIDO);
        if (updatedPermissions > 0) {
            log.info("Suspended {} permissions for employee {}", updatedPermissions, employeeId);
        }
        userService.deactivateByEmployeeId(employeeId);
    }

    private EmployeeSearchResponse toSearchResponse(Employee employee) {
        return EmployeeSearchResponse.builder()
                .id(employee.getId())
                .employeeCode(employee.getEmployeeCode())
                .documentType(employee.getDocumentType())
                .documentNumber(employee.getDocumentNumber())
                .firstName(employee.getFirstName())
                .lastName(employee.getLastName())
                .position(employee.getPosition())
                .departmentName(employee.getDepartment().getName())
                .status(employee.getStatus())
                .build();
    }
}
