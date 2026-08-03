package laboratorioxyz.com.ZoneControl.modulo_gestion_personal.service;

import jakarta.persistence.criteria.Predicate;
import laboratorioxyz.com.ZoneControl.model.entity.Department;
import laboratorioxyz.com.ZoneControl.model.enums.DocumentType;
import laboratorioxyz.com.ZoneControl.model.enums.EmployeeStatus;
import laboratorioxyz.com.ZoneControl.model.enums.PermissionStatus;
import laboratorioxyz.com.ZoneControl.model.repository.DepartmentRepository;
import laboratorioxyz.com.ZoneControl.modulo_autenticacion.service.UserService;
import laboratorioxyz.com.ZoneControl.modulo_gestion_personal.dto.*;
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
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

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

        Department department = departmentRepository.findByName(request.getDepartmentName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Departamento no encontrado: " + request.getDepartmentName()));

        String employeeCode = generateEmployeeCode();

        Employee employee = Employee.builder()
                .employeeCode(employeeCode)
                .documentType(request.getDocumentType())
                .documentNumber(request.getDocumentNumber())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .position(request.getPosition())
                .email(request.getEmail())
                .department(department)
                .systemRole(request.getSystemRole())
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
                                                String departmentName, EmployeeStatus status,
                                                Pageable pageable) {
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
            if (departmentName != null && !departmentName.isBlank()) {
                predicate = cb.and(predicate,
                        cb.like(cb.lower(root.get("department").get("name")),
                                "%" + departmentName.toLowerCase() + "%"));
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
        if (request.getEmail() != null) {
            employee.setEmail(request.getEmail());
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
        if (request.getDepartmentName() != null) {
            Department department = departmentRepository.findByName(request.getDepartmentName())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            "Departamento no encontrado: " + request.getDepartmentName()));
            employee.setDepartment(department);
        }
        if (request.getStatus() != null) {
            EmployeeStatus previousStatus = employee.getStatus();
            employee.setStatus(request.getStatus());
            if (request.getStatus() == EmployeeStatus.INACTIVO
                    || request.getStatus() == EmployeeStatus.SUSPENDIDO) {
                cascadeDeactivate(employee.getId());
            } else if (request.getStatus() == EmployeeStatus.ACTIVO
                    && previousStatus != EmployeeStatus.ACTIVO) {
                cascadeReactivate(employee.getId());
            }
        }
        if (request.getSystemRole() != null) {
            employee.setSystemRole(request.getSystemRole());
        }

        employee = employeeRepository.save(employee);
        return toSearchResponse(employee);
    }

    private void cascadeDeactivate(UUID employeeId) {
        var permissions = accessPermissionRepository.findByEmployee_Id(employeeId);
        permissions.forEach(p -> p.setStatus(PermissionStatus.SUSPENDIDO));
        if (!permissions.isEmpty()) {
            accessPermissionRepository.saveAll(permissions);
            log.info("Suspended {} permissions for employee {}", permissions.size(), employeeId);
        }
        userService.deactivateByEmployeeId(employeeId);
    }

    private void cascadeReactivate(UUID employeeId) {
        var permissions = accessPermissionRepository.findByEmployee_Id(employeeId);
        permissions.forEach(p -> p.setStatus(PermissionStatus.ACTIVO));
        if (!permissions.isEmpty()) {
            accessPermissionRepository.saveAll(permissions);
            log.info("Reactivated {} permissions for employee {}", permissions.size(), employeeId);
        }
        userService.reactivateByEmployeeId(employeeId);
    }

    @Override
    public byte[] generateTemplate() {
        String headers = "tipo_documento;documento_identidad;nombres;apellidos;cargo;departamento;estado";
        String example = "CC;1234567890;Juan;Pérez;Analista;Control de Calidad;ACTIVO";
        return (headers + "\n" + example).getBytes(StandardCharsets.UTF_8);
    }

    @Override
    @Transactional
    public BulkUploadResult processBulkUpload(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Debe adjuntar un archivo para procesar");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || (!originalFilename.endsWith(".csv") && !originalFilename.endsWith(".txt"))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Extensión de archivo no permitida. Solo se aceptan archivos .csv y .txt");
        }

        List<String[]> rows;
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            rows = reader.lines()
                    .map(line -> line.split(";", -1))
                    .toList();
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Error al leer el archivo: " + e.getMessage());
        }

        if (rows.size() < 2) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "El archivo no contiene datos (solo encabezados o está vacío)");
        }

        if (rows.size() > 1001) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "El archivo excede el límite permitido de 10MB o 1000 registros. "
                    + "Por favor, divida el archivo en partes más pequeñas");
        }

        String[] expectedHeaders = {"tipo_documento", "documento_identidad", "nombres", "apellidos", "cargo", "departamento", "estado"};
        String[] headers = rows.getFirst();
        for (int i = 0; i < expectedHeaders.length; i++) {
            if (!headers[i].trim().equalsIgnoreCase(expectedHeaders[i])) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Los encabezados del archivo son incorrectos. "
                        + "Descargue la plantilla para ver el formato admitido");
            }
        }

        List<BulkUploadError> errorList = new ArrayList<>();
        List<Employee> validEmployees = new ArrayList<>();
        Set<String> docsInFile = new HashSet<>();
        Map<String, Department> departmentCache = new HashMap<>();

        for (int i = 1; i < rows.size(); i++) {
            String[] row = rows.get(i);
            int rowNumber = i + 1;
            boolean hasError = false;

            if (row.length < 7) {
                errorList.add(BulkUploadError.builder()
                        .row(rowNumber).field("general")
                        .reason("Fila incompleta: se esperaban 7 columnas pero se recibieron " + row.length)
                        .build());
                continue;
            }

            String tipoDoc = row[0].trim();
            String numDoc = row[1].trim();
            String nombres = row[2].trim();
            String apellidos = row[3].trim();
            String cargo = row[4].trim();
            String deptName = row[5].trim();
            String estadoStr = row[6].trim();

            DocumentType docType;
            try {
                docType = DocumentType.valueOf(tipoDoc);
            } catch (IllegalArgumentException e) {
                errorList.add(BulkUploadError.builder()
                        .row(rowNumber).field("tipo_documento")
                        .reason("Tipo de documento no válido: " + tipoDoc + ". Permitidos: CC, CE, TI, PA, RC")
                        .build());
                hasError = true;
            }

            if (numDoc.isEmpty()) {
                errorList.add(BulkUploadError.builder()
                        .row(rowNumber).field("documento_identidad")
                        .reason("El número de documento no puede estar vacío")
                        .build());
                hasError = true;
            }

            if (nombres.length() < 2) {
                errorList.add(BulkUploadError.builder()
                        .row(rowNumber).field("nombres")
                        .reason("Los nombres deben tener al menos 2 caracteres")
                        .build());
                hasError = true;
            }

            if (apellidos.length() < 2) {
                errorList.add(BulkUploadError.builder()
                        .row(rowNumber).field("apellidos")
                        .reason("Los apellidos deben tener al menos 2 caracteres")
                        .build());
                hasError = true;
            }

            if (cargo.isEmpty()) {
                errorList.add(BulkUploadError.builder()
                        .row(rowNumber).field("cargo")
                        .reason("El cargo no puede estar vacío")
                        .build());
                hasError = true;
            }

            Department department = null;
            if (!deptName.isEmpty()) {
                department = departmentCache.computeIfAbsent(deptName,
                        name -> departmentRepository.findByName(name).orElse(null));
                if (department == null) {
                    errorList.add(BulkUploadError.builder()
                            .row(rowNumber).field("departamento")
                            .reason("Departamento no encontrado: " + deptName)
                            .build());
                    hasError = true;
                }
            } else {
                errorList.add(BulkUploadError.builder()
                        .row(rowNumber).field("departamento")
                        .reason("El departamento no puede estar vacío")
                        .build());
                hasError = true;
            }

            EmployeeStatus status;
            try {
                status = EmployeeStatus.valueOf(estadoStr);
            } catch (IllegalArgumentException e) {
                errorList.add(BulkUploadError.builder()
                        .row(rowNumber).field("estado")
                        .reason("Estado no válido: " + estadoStr + ". Permitidos: ACTIVO, INACTIVO, SUSPENDIDO")
                        .build());
                hasError = true;
                status = null;
            }

            if (hasError) continue;

            String docKey = tipoDoc + ":" + numDoc;
            if (employeeRepository.existsByDocumentTypeAndDocumentNumber(
                    DocumentType.valueOf(tipoDoc), numDoc)) {
                errorList.add(BulkUploadError.builder()
                        .row(rowNumber).field("documento_identidad")
                        .reason("Ya existe un empleado con el documento " + tipoDoc + " número " + numDoc)
                        .build());
                continue;
            }
            if (docsInFile.contains(docKey)) {
                errorList.add(BulkUploadError.builder()
                        .row(rowNumber).field("documento_identidad")
                        .reason("Documento duplicado dentro del mismo archivo: " + tipoDoc + " " + numDoc)
                        .build());
                continue;
            }
            docsInFile.add(docKey);

            validEmployees.add(Employee.builder()
                    .employeeCode(generateEmployeeCode())
                    .documentType(DocumentType.valueOf(tipoDoc))
                    .documentNumber(numDoc)
                    .firstName(nombres)
                    .lastName(apellidos)
                    .position(cargo)
                    .department(department)
                    .status(status)
                    .build());
        }

        if (!validEmployees.isEmpty()) {
            employeeRepository.saveAll(validEmployees);
            log.info("Bulk insert: {} employees saved", validEmployees.size());
        }

        return BulkUploadResult.builder()
                .total(rows.size() - 1)
                .successes(validEmployees.size())
                .errors(errorList.size())
                .errorReportUrl(buildErrorReport(errorList))
                .build();
    }

    private String buildErrorReport(List<BulkUploadError> errors) {
        if (errors.isEmpty()) return null;
        StringBuilder sb = new StringBuilder();
        sb.append("fila;campo;motivo\n");
        for (BulkUploadError err : errors) {
            sb.append(err.getRow()).append(";")
              .append(err.getField()).append(";")
              .append(err.getReason()).append("\n");
        }
        return sb.toString();
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
                .email(employee.getEmail())
                .departmentName(employee.getDepartment().getName())
                .status(employee.getStatus())
                .systemRole(employee.getSystemRole())
                .build();
    }
}
