package laboratorioxyz.com.ZoneControl.modulo_control_acceso.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import laboratorioxyz.com.ZoneControl.model.entity.Department;
import laboratorioxyz.com.ZoneControl.model.enums.DocumentType;
import laboratorioxyz.com.ZoneControl.model.enums.EmployeeStatus;
import laboratorioxyz.com.ZoneControl.model.enums.PermissionStatus;
import laboratorioxyz.com.ZoneControl.model.repository.DepartmentRepository;
import laboratorioxyz.com.ZoneControl.model.repository.ProductionAreaRepository;
import laboratorioxyz.com.ZoneControl.modulo_control_acceso.repository.AccessHistoryRepository;
import laboratorioxyz.com.ZoneControl.modulo_gestion_personal.model.AccessPermission;
import laboratorioxyz.com.ZoneControl.modulo_gestion_personal.model.Employee;
import laboratorioxyz.com.ZoneControl.modulo_gestion_personal.repository.AccessPermissionRepository;
import laboratorioxyz.com.ZoneControl.modulo_gestion_personal.repository.EmployeeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@Rollback
@WithMockUser(roles = "SUPERVISOR_AUDITOR")
class AccessControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private AccessPermissionRepository accessPermissionRepository;

    @Autowired
    private AccessHistoryRepository accessHistoryRepository;

    @Autowired
    private ProductionAreaRepository productionAreaRepository;

    private Department dept;
    private final String areaName = "Sala Blanca A";

    @BeforeEach
    void setUp() {
        dept = departmentRepository.findByName("Control de Calidad").orElseThrow();
    }

    private Employee createEmployee(String code, String docNum, EmployeeStatus status) {
        return employeeRepository.save(Employee.builder()
                .employeeCode(code)
                .documentType(DocumentType.CC)
                .documentNumber(docNum)
                .firstName("Test")
                .lastName("User")
                .position("Técnico")
                .department(dept)
                .status(status)
                .build());
    }

    private void grantPermission(Employee employee) {
        accessPermissionRepository.save(AccessPermission.builder()
                .employee(employee)
                .productionArea(productionAreaRepository.findByName("Sala Blanca A").orElseThrow())
                .status(PermissionStatus.ACTIVO)
                .startDate(LocalDate.now().minusDays(1))
                .expirationDate(LocalDate.now().plusDays(30))
                .startTime(LocalTime.of(6, 0))
                .endTime(LocalTime.of(22, 0))
                .build());
    }

    private String requestBody(String employeeCode, String productionAreaName) {
        return "{\"employeeCode\":\"" + employeeCode + "\",\"productionAreaName\":\"" + productionAreaName + "\"}";
    }

    @Test
    void validate_authorizedEmployee_returnsAuthorized() throws Exception {
        Employee emp = createEmployee("EMP-TEST-01", "1111111111", EmployeeStatus.ACTIVO);
        grantPermission(emp);

        mockMvc.perform(post("/access/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody("EMP-TEST-01", areaName)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("AUTHORIZED"))
                .andExpect(jsonPath("$.message").value("INGRESO AUTORIZADO"));
    }

    @Test
    void validate_inactiveEmployee_returnsDenied() throws Exception {
        Employee emp = createEmployee("EMP-TEST-02", "2222222222", EmployeeStatus.INACTIVO);

        mockMvc.perform(post("/access/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody("EMP-TEST-02", areaName)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("DENIED"))
                .andExpect(jsonPath("$.message").value("INGRESO DENEGADO"));
    }

    @Test
    void validate_suspendedEmployee_returnsDenied() throws Exception {
        Employee emp = createEmployee("EMP-TEST-03", "3333333333", EmployeeStatus.SUSPENDIDO);

        mockMvc.perform(post("/access/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody("EMP-TEST-03", areaName)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("DENIED"))
                .andExpect(jsonPath("$.message").value("INGRESO DENEGADO"));
    }

    @Test
    void validate_unregisteredEmployee_returnsUnregistered() throws Exception {
        mockMvc.perform(post("/access/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody("EMP-999999", areaName)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("UNREGISTERED"))
                .andExpect(jsonPath("$.message").value("NO REGISTRADO"));
    }

    @Test
    void validate_noValidPermission_returnsSuspended() throws Exception {
        Employee emp = createEmployee("EMP-TEST-04", "4444444444", EmployeeStatus.ACTIVO);

        mockMvc.perform(post("/access/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody("EMP-TEST-04", areaName)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("SUSPENDED"))
                .andExpect(jsonPath("$.message").value("ACCESO SUSPENDIDO"));
    }

    @Test
    void validate_permissionForDifferentArea_returnsSuspended() throws Exception {
        Employee emp = createEmployee("EMP-TEST-05", "5555555555", EmployeeStatus.ACTIVO);
        grantPermission(emp);

        mockMvc.perform(post("/access/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody("EMP-TEST-05", "Sala Blanca B")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("SUSPENDED"))
                .andExpect(jsonPath("$.message").value("ACCESO SUSPENDIDO"));
    }

    @Test
    void validate_logsAccessHistory() throws Exception {
        Employee emp = createEmployee("EMP-TEST-06", "6666666666", EmployeeStatus.ACTIVO);
        grantPermission(emp);

        mockMvc.perform(post("/access/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody("EMP-TEST-06", areaName)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("AUTHORIZED"));

        long count = accessHistoryRepository.count();
        assert count > 0 : "AccessHistory should have been logged";
    }
}
