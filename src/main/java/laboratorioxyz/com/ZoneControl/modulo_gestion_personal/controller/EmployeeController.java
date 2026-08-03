package laboratorioxyz.com.ZoneControl.modulo_gestion_personal.controller;

import jakarta.validation.Valid;
import laboratorioxyz.com.ZoneControl.model.entity.Office;
import laboratorioxyz.com.ZoneControl.model.enums.EmployeeStatus;
import laboratorioxyz.com.ZoneControl.modulo_control_acceso.model.AccessHistory;
import laboratorioxyz.com.ZoneControl.modulo_gestion_personal.dto.*;
import laboratorioxyz.com.ZoneControl.modulo_gestion_personal.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;


/**
 * Controlador de gestión de personal.
 * POST /personal para registrar un nuevo empleado con generación
 * automática de código EMP-XXXXXX.
 *
 * Respuestas:
 * - 201 Created + { id, employeeCode, firstName, lastName }
 * - 400 Bad Request si faltan campos obligatorios o tipo documento inválido
 * - 409 Conflict si ya existe la combinación tipo+número de documento
 */
@RestController
@RequestMapping("/api/personal")
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService employeeService;

    @PostMapping
    public ResponseEntity<RegisterEmployeeResponse> register(
            @Valid @RequestBody RegisterEmployeeRequest request) {
        RegisterEmployeeResponse response = employeeService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/departamentos")
    public ResponseEntity<List<String>> listDepartments() {
        return ResponseEntity.ok(employeeService.listDepartmentNames());
    }

    @GetMapping("/sedes")
    public ResponseEntity<List<Office>> listOffices() {
        return ResponseEntity.ok(employeeService.listOffices());
    }

    @GetMapping
    public ResponseEntity<Page<EmployeeSearchResponse>> search(
            @RequestParam(required = false) String documentType,
            @RequestParam(required = false) String documentNumber,
            @RequestParam(required = false) String firstName,
            @RequestParam(required = false) String lastName,
            @RequestParam(required = false) String departmentName,
            @RequestParam(required = false) EmployeeStatus status,
            @PageableDefault(size = 10) Pageable pageable) {
        Page<EmployeeSearchResponse> result = employeeService.search(
                documentType, documentNumber, firstName, lastName, departmentName, status, pageable);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/bulk/plantilla")
    public ResponseEntity<byte[]> downloadTemplate() {
        byte[] template = employeeService.generateTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/csv"));
        headers.set(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=plantilla_carga_masiva_personal.csv");
        return ResponseEntity.ok().headers(headers).body(template);
    }

    @PostMapping("/bulk")
    public ResponseEntity<BulkUploadResult> uploadBulk(
            @RequestParam("file") MultipartFile file) {
        BulkUploadResult result = employeeService.processBulkUpload(file);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EmployeeSearchResponse> getById(@PathVariable UUID id) {
        EmployeeSearchResponse response = employeeService.findById(id);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<EmployeeSearchResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateEmployeeRequest request) {
        EmployeeSearchResponse response = employeeService.update(id, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/permisos")
    public ResponseEntity<List<PermissionResponse>> getPermissions(@PathVariable UUID id) {
        return ResponseEntity.ok(employeeService.findPermissionsByEmployee(id));
    }

    @GetMapping("/{id}/accesos")
    public ResponseEntity<List<AccessHistory>> getAccessHistory(
            @PathVariable UUID id,
            @RequestParam(required = false, defaultValue = "20") int limit) {
        return ResponseEntity.ok(employeeService.findAccessHistoryByEmployee(id, limit));
    }

    @PostMapping(value = "/{id}/photo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<EmployeeSearchResponse> uploadPhoto(
            @PathVariable UUID id,
            @RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(employeeService.uploadPhoto(id, file));
    }

    @GetMapping("/{id}/photo")
    public ResponseEntity<byte[]> getPhoto(@PathVariable UUID id) {
        byte[] data = employeeService.loadPhoto(id);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.IMAGE_PNG);
        return ResponseEntity.ok().headers(headers).body(data);
    }

    @DeleteMapping("/{id}/photo")
    public ResponseEntity<EmployeeSearchResponse> deletePhoto(@PathVariable UUID id) {
        return ResponseEntity.ok(employeeService.deletePhoto(id));
    }
}
