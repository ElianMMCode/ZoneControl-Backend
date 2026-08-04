package laboratorioxyz.com.ZoneControl.modulo_gestion_personal.service;

import jakarta.persistence.criteria.Predicate;
import laboratorioxyz.com.ZoneControl.model.entity.Department;
import laboratorioxyz.com.ZoneControl.model.entity.Office;
import laboratorioxyz.com.ZoneControl.model.enums.DocumentType;
import laboratorioxyz.com.ZoneControl.model.enums.EmployeeStatus;
import laboratorioxyz.com.ZoneControl.model.enums.PermissionStatus;
import laboratorioxyz.com.ZoneControl.model.repository.DepartmentRepository;
import laboratorioxyz.com.ZoneControl.modulo_autenticacion.service.UserService;
import laboratorioxyz.com.ZoneControl.modulo_control_acceso.model.AccessHistory;
import laboratorioxyz.com.ZoneControl.modulo_control_acceso.repository.AccessHistoryRepository;
import laboratorioxyz.com.ZoneControl.modulo_gestion_personal.dto.*;
import laboratorioxyz.com.ZoneControl.modulo_gestion_personal.model.AccessPermission;
import laboratorioxyz.com.ZoneControl.modulo_gestion_personal.model.Employee;
import laboratorioxyz.com.ZoneControl.modulo_gestion_personal.repository.AccessPermissionRepository;
import laboratorioxyz.com.ZoneControl.modulo_gestion_personal.repository.EmployeeRepository;
import laboratorioxyz.com.ZoneControl.modulo_gestion_personal.repository.PermissionScheduleRepository;
import laboratorioxyz.com.ZoneControl.modulo_publico.repository.OfficeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import jakarta.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final OfficeRepository officeRepository;
    private final AccessPermissionRepository accessPermissionRepository;
    private final AccessHistoryRepository accessHistoryRepository;
    private final PermissionScheduleRepository permissionScheduleRepository;
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

        Department department = resolveDepartment(request.getDepartmentName());
        Office baseOffice = resolveBaseOffice(request.getBaseOfficeName());

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
                .contractType(request.getContractType())
                .baseOffice(baseOffice)
                .workShift(request.getWorkShift())
                .hireDate(request.getHireDate())
                .contractEndDate(request.getContractEndDate())
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
            employee.setDepartment(resolveDepartment(request.getDepartmentName()));
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
        if (request.getContractType() != null) {
            employee.setContractType(request.getContractType());
        }
        if (request.getBaseOfficeName() != null) {
            employee.setBaseOffice(resolveBaseOffice(request.getBaseOfficeName()));
        }
        if (request.getWorkShift() != null) {
            employee.setWorkShift(request.getWorkShift());
        }
        if (request.getHireDate() != null) {
            employee.setHireDate(request.getHireDate());
        }
        if (request.getContractEndDate() != null) {
            employee.setContractEndDate(request.getContractEndDate());
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
        String headers = "tipo_documento;documento_identidad;nombres;apellidos;cargo;"
                + "departamento;estado;fecha_ingreso";
        String example = "CC;1234567890;Juan;Pérez;Analista;Control de Calidad;ACTIVO;2026-01-15";
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

        if (file.getSize() > MAX_BULK_BYTES) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "El archivo excede el límite permitido de 10MB o 1000 registros. "
                    + "Por favor, divida el archivo en partes más pequeñas");
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

        String[] expectedHeaders = {"tipo_documento", "documento_identidad", "nombres",
                "apellidos", "cargo", "departamento", "estado", "fecha_ingreso"};
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

            if (row.length < 8) {
                errorList.add(BulkUploadError.builder()
                        .row(rowNumber).field("general")
                        .reason("Fila incompleta: se esperaban 8 columnas pero se recibieron " + row.length)
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
            String fechaIngreso = row[7].trim();

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

            java.time.LocalDate hireDate = null;
            if (!fechaIngreso.isEmpty()) {
                try {
                    hireDate = java.time.LocalDate.parse(fechaIngreso);
                } catch (java.time.format.DateTimeParseException e) {
                    errorList.add(BulkUploadError.builder()
                            .row(rowNumber).field("fecha_ingreso")
                            .reason("Formato de fecha de ingreso inválido. Use YYYY-MM-DD")
                            .build());
                    hasError = true;
                }
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
                    .hireDate(hireDate)
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

    @Override
    @Transactional(readOnly = true)
    public List<String> listDepartmentNames() {
        return departmentRepository.findAll().stream()
                .map(Department::getName)
                .sorted()
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Office> listOffices() {
        return officeRepository.findAllByOrderByNameAsc();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PermissionResponse> findPermissionsByEmployee(UUID employeeId) {
        if (!employeeRepository.existsById(employeeId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Empleado no encontrado");
        }
        return accessPermissionRepository.findByEmployee_Id(employeeId).stream()
                .map(this::toPermissionResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AccessHistory> findAccessHistoryByEmployee(UUID employeeId, int limit) {
        if (!employeeRepository.existsById(employeeId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Empleado no encontrado");
        }
        int safeLimit = Math.max(1, Math.min(limit, 200));
        Page<AccessHistory> page = accessHistoryRepository
                .findByEmployee_IdOrderByTimestampDesc(employeeId, PageRequest.of(0, safeLimit));
        return page.getContent();
    }

    private Department resolveDepartment(String name) {
        if (name == null || name.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "El departamento es obligatorio");
        }
        return departmentRepository.findByName(name)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Departamento no encontrado: " + name));
    }

    private Office resolveBaseOffice(String name) {
        if (name == null || name.isBlank()) {
            return null;
        }
        return officeRepository.findByName(name)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Sede no encontrada: " + name));
    }

    private PermissionResponse toPermissionResponse(AccessPermission permission) {
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
                .contractType(employee.getContractType())
                .baseOfficeName(employee.getBaseOffice() != null
                        ? employee.getBaseOffice().getName() : null)
                .workShift(employee.getWorkShift())
                .hireDate(employee.getHireDate())
                .contractEndDate(employee.getContractEndDate())
                .photoUrl(employee.getPhotoUrl())
                .build();
    }

    private static final Path PHOTO_DIR = Paths.get("uploads", "photos");
    private static final Set<String> ALLOWED_PHOTO_EXT = Set.of("jpg", "jpeg", "png", "webp");
    private static final long MAX_PHOTO_BYTES = 2L * 1024 * 1024; // 2 MB
    private static final long MAX_BULK_BYTES = 10L * 1024 * 1024; // 10 MB

    @PostConstruct
    void initPhotoDir() {
        try {
            Files.createDirectories(PHOTO_DIR);
        } catch (IOException e) {
            log.warn("No se pudo crear el directorio de fotos: {}", e.getMessage());
        }
    }

    @Override
    @Transactional
    public EmployeeSearchResponse uploadPhoto(UUID id, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Debe adjuntar una imagen para subir");
        }
        if (file.getSize() > MAX_PHOTO_BYTES) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "La imagen excede el tamaño máximo permitido de 2MB");
        }
        String original = file.getOriginalFilename();
        String ext = original != null && original.contains(".")
                ? original.substring(original.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT)
                : "";
        if (!ALLOWED_PHOTO_EXT.contains(ext)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Extensión no permitida. Solo se aceptan: jpg, jpeg, png, webp");
        }
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Empleado no encontrado"));
        deletePhotoFile(employee.getPhotoUrl());
        String filename = employee.getEmployeeCode() + "." + ext;
        Path target = PHOTO_DIR.resolve(filename);
        try {
            Files.write(target, file.getBytes());
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "No se pudo guardar la imagen: " + e.getMessage());
        }
        String relativePath = "/uploads/photos/" + filename;
        employee.setPhotoUrl(relativePath);
        employeeRepository.save(employee);
        log.info("Photo uploaded for employee {}: {}", employee.getEmployeeCode(), relativePath);
        return toSearchResponse(employee);
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] loadPhoto(UUID id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Empleado no encontrado"));
        if (employee.getPhotoUrl() != null) {
            Path path = photoFilePath(employee.getPhotoUrl());
            if (Files.exists(path)) {
                try {
                    return Files.readAllBytes(path);
                } catch (IOException e) {
                    log.warn("No se pudo leer la foto {}: {}", path, e.getMessage());
                }
            } else {
                log.warn("Foto registrada pero archivo ausente en disco: {}", path);
            }
        }
        return defaultEmployeePhoto();
    }

    private byte[] defaultEmployeePhoto() {
        try (var in = getClass().getResourceAsStream("/employee-default.png")) {
            if (in == null) {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                        "Imagen por defecto no encontrada en el classpath");
            }
            return in.readAllBytes();
        } catch (IOException e) {
            log.error("No se pudo cargar la imagen de empleado por defecto", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "No se pudo cargar la imagen por defecto");
        }
    }

    @Override
    @Transactional
    public EmployeeSearchResponse deletePhoto(UUID id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Empleado no encontrado"));
        deletePhotoFile(employee.getPhotoUrl());
        employee.setPhotoUrl(null);
        employeeRepository.save(employee);
        return toSearchResponse(employee);
    }

    private void deletePhotoFile(String photoUrl) {
        if (photoUrl == null) return;
        try {
            Files.deleteIfExists(photoFilePath(photoUrl));
        } catch (IOException e) {
            log.warn("No se pudo eliminar la foto anterior {}: {}", photoUrl, e.getMessage());
        }
    }

    private Path photoFilePath(String photoUrl) {
        String filename = photoUrl.substring(photoUrl.lastIndexOf('/') + 1);
        return PHOTO_DIR.resolve(filename);
    }
}
