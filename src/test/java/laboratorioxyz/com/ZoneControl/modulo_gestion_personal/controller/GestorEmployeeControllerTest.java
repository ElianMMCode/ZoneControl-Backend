package laboratorioxyz.com.ZoneControl.modulo_gestion_personal.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import laboratorioxyz.com.ZoneControl.model.entity.Department;
import laboratorioxyz.com.ZoneControl.model.enums.ContractType;
import laboratorioxyz.com.ZoneControl.model.enums.DocumentType;
import laboratorioxyz.com.ZoneControl.model.enums.EmployeeStatus;
import laboratorioxyz.com.ZoneControl.model.enums.PermissionStatus;
import laboratorioxyz.com.ZoneControl.model.enums.WorkShift;
import laboratorioxyz.com.ZoneControl.model.repository.DepartmentRepository;
import laboratorioxyz.com.ZoneControl.model.repository.ProductionAreaRepository;
import laboratorioxyz.com.ZoneControl.modulo_control_acceso.model.AccessHistory;
import laboratorioxyz.com.ZoneControl.modulo_control_acceso.repository.AccessHistoryRepository;
import laboratorioxyz.com.ZoneControl.model.enums.AccessResult;
import laboratorioxyz.com.ZoneControl.modulo_gestion_personal.dto.RegisterEmployeeRequest;
import laboratorioxyz.com.ZoneControl.modulo_gestion_personal.dto.UpdateEmployeeRequest;
import laboratorioxyz.com.ZoneControl.modulo_gestion_personal.model.AccessPermission;
import laboratorioxyz.com.ZoneControl.modulo_gestion_personal.model.Employee;
import laboratorioxyz.com.ZoneControl.modulo_gestion_personal.model.Position;
import laboratorioxyz.com.ZoneControl.modulo_gestion_personal.repository.AccessPermissionRepository;
import laboratorioxyz.com.ZoneControl.modulo_gestion_personal.repository.EmployeeRepository;
import laboratorioxyz.com.ZoneControl.modulo_gestion_personal.repository.PositionRepository;
import laboratorioxyz.com.ZoneControl.modulo_publico.repository.OfficeRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests para los nuevos endpoints del módulo de Gestión de Personal
 * (HU-25 ampliación: modelo real + foto + historial de accesos).
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@Rollback
@WithMockUser(roles = "ADMIN")
class GestorEmployeeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private ProductionAreaRepository productionAreaRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private OfficeRepository officeRepository;

    @Autowired
    private AccessPermissionRepository accessPermissionRepository;

    @Autowired
    private AccessHistoryRepository accessHistoryRepository;

    @Autowired
    private PositionRepository positionRepository;

    @Test
    void listDepartments_returnsNames() throws Exception {
        mockMvc.perform(get("/api/personal/departamentos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[?(@ == 'Control de Calidad')]").exists());
    }

    @Test
    void listOffices_returnsOffices() throws Exception {
        mockMvc.perform(get("/api/personal/sedes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].name").exists());
    }

    @Test
    void registerEmployee_withRichProfile_returns201() throws Exception {
        Department dept = departmentRepository.findByName("Control de Calidad").orElseThrow();
        Position cargo = positionRepository.save(Position.builder()
                .name("Coordinador " + System.nanoTime()).build());
        var request = RegisterEmployeeRequest.builder()
                .documentType(DocumentType.CC)
                .documentNumber("300000001")
                .firstName("Hugo")
                .lastName("Lozano")
                .cargoId(cargo.getId())
                .departmentName(dept.getName())
                .email("hugo.lozano@laboratorioxzy.com.co")
                .contractType(ContractType.TIEMPO_COMPLETO)
                .workShift(WorkShift.DIURNO)
                .hireDate(LocalDate.of(2024, 1, 15))
                .build();

        mockMvc.perform(post("/api/personal")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.employeeCode").isString());

        Employee saved = employeeRepository.findByDocumentTypeAndDocumentNumber(
                DocumentType.CC, "300000001").orElseThrow();
        org.junit.jupiter.api.Assertions.assertEquals(
                ContractType.TIEMPO_COMPLETO, saved.getContractType());
        org.junit.jupiter.api.Assertions.assertEquals(
                WorkShift.DIURNO, saved.getWorkShift());
        org.junit.jupiter.api.Assertions.assertEquals(
                LocalDate.of(2024, 1, 15), saved.getHireDate());
    }

    @Test
    void updateEmployee_withBaseOffice_returns200() throws Exception {
        Department dept = departmentRepository.findByName("Control de Calidad").orElseThrow();
        var office = officeRepository.findByName("Sede Principal Bogotá").orElseThrow();
        Employee emp = employeeRepository.save(Employee.builder()
                .employeeCode(uniqueCode("UPD"))
                .documentType(DocumentType.CC)
                .documentNumber("300000002")
                .firstName("Ana")
                .lastName("Uribe")
                .position("Auxiliar")
                .department(dept)
                .status(EmployeeStatus.ACTIVO)
                .build());

        var body = UpdateEmployeeRequest.builder()
                .baseOfficeName(office.getName())
                .contractType(ContractType.CONTRATISTA)
                .workShift(WorkShift.MIXTO)
                .hireDate(LocalDate.of(2025, 5, 1))
                .contractEndDate(LocalDate.of(2026, 5, 1))
                .build();

        mockMvc.perform(patch("/api/personal/{id}", emp.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.baseOfficeName").value(office.getName()))
                .andExpect(jsonPath("$.contractType").value("CONTRATISTA"))
                .andExpect(jsonPath("$.workShift").value("MIXTO"));
    }

    @Test
    void updateEmployee_invalidOffice_returns400() throws Exception {
        Department dept = departmentRepository.findByName("Control de Calidad").orElseThrow();
        Employee emp = employeeRepository.save(Employee.builder()
                .employeeCode(uniqueCode("NO"))
                .documentType(DocumentType.CC)
                .documentNumber("300000003")
                .firstName("Pedro")
                .lastName("Null")
                .position("Aux")
                .department(dept)
                .status(EmployeeStatus.ACTIVO)
                .build());

        var body = UpdateEmployeeRequest.builder()
                .baseOfficeName("Sede Inexistente")
                .build();

        mockMvc.perform(patch("/api/personal/{id}", emp.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(
                        org.hamcrest.Matchers.containsString("Sede no encontrada")));
    }

    @Test
    void getEmployeePermissions_returnsEmpty() throws Exception {
        Department dept = departmentRepository.findByName("Control de Calidad").orElseThrow();
        Employee emp = employeeRepository.save(Employee.builder()
                .employeeCode(uniqueCode("PERM"))
                .documentType(DocumentType.CC)
                .documentNumber("300000004")
                .firstName("Sin")
                .lastName("Permisos")
                .position("Aux")
                .department(dept)
                .status(EmployeeStatus.ACTIVO)
                .build());

        mockMvc.perform(get("/api/personal/{id}/permisos", emp.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void getEmployeePermissions_returnsList() throws Exception {
        Department dept = departmentRepository.findByName("Control de Calidad").orElseThrow();
        var area = productionAreaRepository.findByName("Sala Blanca A").orElseThrow();
        Employee emp = employeeRepository.save(Employee.builder()
                .employeeCode(uniqueCode("WITH"))
                .documentType(DocumentType.CC)
                .documentNumber("300000005")
                .firstName("Con")
                .lastName("Permisos")
                .position("Op")
                .department(dept)
                .status(EmployeeStatus.ACTIVO)
                .build());
        accessPermissionRepository.save(AccessPermission.builder()
                .employee(emp)
                .productionArea(area)
                .status(PermissionStatus.ACTIVO)
                .startDate(LocalDate.now())
                .expirationDate(LocalDate.now().plusMonths(6))
                .startTime(LocalTime.of(8, 0))
                .endTime(LocalTime.of(17, 0))
                .build());

        mockMvc.perform(get("/api/personal/{id}/permisos", emp.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].areaName").value("Sala Blanca A"))
                .andExpect(jsonPath("$[0].status").value("ACTIVO"));
    }

    @Test
    void getEmployeePermissions_notFound_returns404() throws Exception {
        mockMvc.perform(get("/api/personal/{id}/permisos", UUID.randomUUID()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Empleado no encontrado"));
    }

    @Test
    void getEmployeeAccessHistory_returnsList() throws Exception {
        Department dept = departmentRepository.findByName("Control de Calidad").orElseThrow();
        Employee emp = employeeRepository.save(Employee.builder()
                .employeeCode(uniqueCode("HIST"))
                .documentType(DocumentType.CC)
                .documentNumber("300000006")
                .firstName("Con")
                .lastName("Historial")
                .position("Op")
                .department(dept)
                .status(EmployeeStatus.ACTIVO)
                .build());
        accessHistoryRepository.save(AccessHistory.builder()
                .employee(emp)
                .department("Control de Calidad")
                .productionAreaName("Sala Blanca A")
                .timestamp(LocalDateTime.now().minusHours(2))
                .result(AccessResult.AUTHORIZED)
                .build());
        accessHistoryRepository.save(AccessHistory.builder()
                .employee(emp)
                .department("Control de Calidad")
                .productionAreaName("Sala Blanca A")
                .timestamp(LocalDateTime.now().minusHours(1))
                .result(AccessResult.DENIED)
                .build());

        mockMvc.perform(get("/api/personal/{id}/accesos", emp.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].result").value("DENIED"))
                .andExpect(jsonPath("$[1].result").value("AUTHORIZED"));
    }

    @Test
    void getEmployeeAccessHistory_notFound_returns404() throws Exception {
        mockMvc.perform(get("/api/personal/{id}/accesos", UUID.randomUUID()))
                .andExpect(status().isNotFound());
    }

    @Test
    void getEmployeeAccessHistory_respectsLimit() throws Exception {
        Department dept = departmentRepository.findByName("Control de Calidad").orElseThrow();
        Employee emp = employeeRepository.save(Employee.builder()
                .employeeCode(uniqueCode("LIM"))
                .documentType(DocumentType.CC)
                .documentNumber("300000007")
                .firstName("Limit")
                .lastName("Test")
                .position("Op")
                .department(dept)
                .status(EmployeeStatus.ACTIVO)
                .build());
        for (int i = 0; i < 5; i++) {
            accessHistoryRepository.save(AccessHistory.builder()
                    .employee(emp)
                    .department("Control de Calidad")
                    .productionAreaName("Sala Blanca A")
                    .timestamp(LocalDateTime.now().minusMinutes(i * 10L))
                    .result(AccessResult.AUTHORIZED)
                    .build());
        }

        mockMvc.perform(get("/api/personal/{id}/accesos", emp.getId())
                        .param("limit", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void uploadPhoto_validPng_returns200() throws Exception {
        Department dept = departmentRepository.findByName("Control de Calidad").orElseThrow();
        Employee emp = employeeRepository.save(Employee.builder()
                .employeeCode(uniqueCode("PHO"))
                .documentType(DocumentType.CC)
                .documentNumber("300000010")
                .firstName("Con")
                .lastName("Foto")
                .position("Op")
                .department(dept)
                .status(EmployeeStatus.ACTIVO)
                .build());

        // 1x1 PNG transparente mínimo (67 bytes)
        byte[] png = new byte[]{
                (byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A,
                0x00, 0x00, 0x00, 0x0D, 0x49, 0x48, 0x44, 0x52,
                0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00, 0x01,
                0x08, 0x06, 0x00, 0x00, 0x00, 0x1F, 0x15, (byte) 0xC4,
                (byte) 0x89, 0x00, 0x00, 0x00, 0x0D, 0x49, 0x44, 0x41,
                0x54, 0x78, (byte) 0x9C, 0x62, 0x00, 0x01, 0x00, 0x00,
                0x05, 0x00, 0x01, 0x0D, 0x0A, 0x2D, (byte) 0xB4, 0x00,
                0x00, 0x00, 0x00, 0x49, 0x45, 0x4E, 0x44, (byte) 0xAE,
                0x42, 0x60, (byte) 0x82
        };
        MockMultipartFile file = new MockMultipartFile(
                "file", "avatar.png", "image/png", png);

        mockMvc.perform(multipart("/api/personal/{id}/photo", emp.getId())
                        .file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.photoUrl").value(
                        org.hamcrest.Matchers.containsString(emp.getEmployeeCode() + ".png")));
    }

    @Test
    void uploadPhoto_invalidExtension_returns400() throws Exception {
        Department dept = departmentRepository.findByName("Control de Calidad").orElseThrow();
        Employee emp = employeeRepository.save(Employee.builder()
                .employeeCode(uniqueCode("BAD"))
                .documentType(DocumentType.CC)
                .documentNumber("300000011")
                .firstName("Bad")
                .lastName("Ext")
                .position("Op")
                .department(dept)
                .status(EmployeeStatus.ACTIVO)
                .build());
        MockMultipartFile file = new MockMultipartFile(
                "file", "foto.gif", "image/gif", "fake".getBytes());

        mockMvc.perform(multipart("/api/personal/{id}/photo", emp.getId())
                        .file(file))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error")
                        .value(org.hamcrest.Matchers.containsString("Extensión")));
    }

    @Test
    void uploadPhoto_nonExistentEmployee_returns404() throws Exception {
        byte[] png = new byte[]{(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A};
        MockMultipartFile file = new MockMultipartFile(
                "file", "avatar.png", "image/png", png);
        mockMvc.perform(multipart("/api/personal/{id}/photo", UUID.randomUUID())
                        .file(file))
                .andExpect(status().isNotFound());
    }

    @Test
    void getPhoto_withoutUpload_returnsDefaultPng() throws Exception {
        Department dept = departmentRepository.findByName("Control de Calidad").orElseThrow();
        Employee emp = employeeRepository.save(Employee.builder()
                .employeeCode(uniqueCode("DEF"))
                .documentType(DocumentType.CC)
                .documentNumber("300000012")
                .firstName("Sin")
                .lastName("Foto")
                .position("Op")
                .department(dept)
                .status(EmployeeStatus.ACTIVO)
                .build());

        mockMvc.perform(get("/api/personal/{id}/photo", emp.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType("image/png"))
                .andExpect(result -> {
                    byte[] body = result.getResponse().getContentAsByteArray();
                    org.junit.jupiter.api.Assertions.assertTrue(
                            body.length > 0, "La imagen por defecto no debe estar vacía");
                });
    }

    @Test
    void getPhoto_nonExistentEmployee_returns404() throws Exception {
        mockMvc.perform(get("/api/personal/{id}/photo", UUID.randomUUID()))
                .andExpect(status().isNotFound());
    }

    @Test
    void deletePhoto_removesAndReturnsNullUrl() throws Exception {
        Department dept = departmentRepository.findByName("Control de Calidad").orElseThrow();
        Employee emp = employeeRepository.save(Employee.builder()
                .employeeCode(uniqueCode("DEL"))
                .documentType(DocumentType.CC)
                .documentNumber("300000013")
                .firstName("Del")
                .lastName("Foto")
                .position("Op")
                .department(dept)
                .status(EmployeeStatus.ACTIVO)
                .build());
        emp.setPhotoUrl("/uploads/photos/nonexistente.png");
        employeeRepository.save(emp);

        mockMvc.perform(delete("/api/personal/{id}/photo", emp.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.photoUrl")
                        .value(org.hamcrest.Matchers.nullValue()));
    }

    /**
     * Genera un código único de 12 caracteres (EMP-XXXXX) usando el
     * sufijo y el contador estático. Útil para tests porque el campo
     * {@code employeeCode} está limitado a 12 caracteres.
     */
    private static int counter = 0;
    private static String uniqueCode(String suffix) {
        counter++;
        return String.format("EMP-%s%04d", suffix, counter);
    }
}
