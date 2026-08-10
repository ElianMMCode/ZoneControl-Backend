package laboratorioxyz.com.ZoneControl.modulo_control_acceso.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import laboratorioxyz.com.ZoneControl.model.entity.Department;
import laboratorioxyz.com.ZoneControl.model.enums.AccessResult;
import laboratorioxyz.com.ZoneControl.model.enums.DocumentType;
import laboratorioxyz.com.ZoneControl.model.enums.EmployeeStatus;
import laboratorioxyz.com.ZoneControl.model.enums.PermissionStatus;
import laboratorioxyz.com.ZoneControl.model.repository.DepartmentRepository;
import laboratorioxyz.com.ZoneControl.model.repository.ProductionAreaRepository;
import laboratorioxyz.com.ZoneControl.modulo_control_acceso.model.AccessAlert;
import laboratorioxyz.com.ZoneControl.modulo_control_acceso.model.AccessHistory;
import laboratorioxyz.com.ZoneControl.modulo_control_acceso.model.AccessSession;
import laboratorioxyz.com.ZoneControl.modulo_control_acceso.repository.AccessAlertRepository;
import laboratorioxyz.com.ZoneControl.modulo_control_acceso.repository.AccessHistoryRepository;
import laboratorioxyz.com.ZoneControl.modulo_control_acceso.repository.AccessSessionRepository;
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
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Fase C §9.3 (2.1 sesiones, 2.2 emergencia, 2.4 alertas) para ADMIN/SUPERVISOR.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@Rollback
@WithMockUser(roles = "SUPERVISOR_AUDITOR")
class AccessMonitoringControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private EmployeeRepository employeeRepository;
    @Autowired private DepartmentRepository departmentRepository;
    @Autowired private AccessPermissionRepository accessPermissionRepository;
    @Autowired private AccessSessionRepository accessSessionRepository;
    @Autowired private AccessAlertRepository accessAlertRepository;
    @Autowired private AccessHistoryRepository accessHistoryRepository;
    @Autowired private ProductionAreaRepository productionAreaRepository;
    @Autowired private JdbcTemplate jdbcTemplate;

    private Department dept;
    private final String areaName = "Sala Blanca A";

    @BeforeEach
    void setUp() {
        dept = departmentRepository.findByName("Control de Calidad").orElseThrow();
    }

    private Employee createEmployee(String code, String docNum, EmployeeStatus status) {
        return employeeRepository.save(Employee.builder()
                .employeeCode(code).documentType(DocumentType.CC).documentNumber(docNum)
                .firstName("Monitor").lastName("Test").position("Técnico")
                .department(dept).status(status).build());
    }

    private void grantPermission(Employee employee) {
        accessPermissionRepository.save(AccessPermission.builder()
                .employee(employee)
                .productionArea(productionAreaRepository.findByName("Sala Blanca A").orElseThrow())
                .status(PermissionStatus.ACTIVO)
                .startDate(LocalDate.now().minusDays(1))
                .expirationDate(LocalDate.now().plusDays(30))
                .startTime(LocalTime.of(0, 0))
                .endTime(LocalTime.of(23, 59))
                .build());
    }

    private void validate(String employeeCode) throws Exception {
        mockMvc.perform(post("/api/access/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"employeeCode\":\"" + employeeCode + "\",\"productionAreaName\":\"" + areaName + "\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void authorizedEntry_createsSession_andExitCloses() throws Exception {
        Employee emp = createEmployee("EMP-MON-01", "120000001", EmployeeStatus.ACTIVO);
        grantPermission(emp);
        validate("EMP-MON-01");

        List<AccessSession> active = accessSessionRepository.findByExitTimeIsNull().stream()
                .filter(s -> s.getEmployee().getEmployeeCode().equals("EMP-MON-01"))
                .toList();
        assertEquals(1, active.size());
        assertEquals("EMP-MON-01", active.get(0).getEmployee().getEmployeeCode());

        mockMvc.perform(post("/api/access/exit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "employeeCode", "EMP-MON-01", "productionAreaName", areaName))))
                .andExpect(status().isOk());

        List<AccessSession> stillActive = accessSessionRepository.findByExitTimeIsNull().stream()
                .filter(s -> s.getEmployee().getEmployeeCode().equals("EMP-MON-01"))
                .toList();
        assertTrue(stillActive.isEmpty());
    }

    @Test
    void exit_withoutActiveSession_returns400() throws Exception {
        createEmployee("EMP-MON-99", "120000099", EmployeeStatus.ACTIVO);
        mockMvc.perform(post("/api/access/exit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "employeeCode", "EMP-MON-99", "productionAreaName", areaName))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void exit_registersExitHistory() throws Exception {
        Employee emp = createEmployee("EMP-MON-06", "120000006", EmployeeStatus.ACTIVO);
        grantPermission(emp);
        validate("EMP-MON-06");

        mockMvc.perform(post("/api/access/exit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "employeeCode", "EMP-MON-06", "productionAreaName", areaName))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("EXIT"))
                .andExpect(jsonPath("$.message").value("Salida registrada"))
                .andExpect(jsonPath("$.employeeCode").value("EMP-MON-06"))
                .andExpect(jsonPath("$.department").value(dept.getName()))
                .andExpect(jsonPath("$.productionAreaName").value(areaName));

        List<AccessHistory> exits = accessHistoryRepository.findAll().stream()
                .filter(h -> h.getResult() == AccessResult.EXIT)
                .filter(h -> h.getEmployee() != null && emp.getId().equals(h.getEmployee().getId()))
                .toList();
        assertFalse(exits.isEmpty(), "La salida debe quedar registrada en access_history");
        AccessHistory exit = exits.get(exits.size() - 1);
        assertEquals(dept.getName(), exit.getDepartment());
        assertEquals(areaName, exit.getProductionAreaName());
        assertEquals("EMP-MON-06", exit.getEmployee().getEmployeeCode());
    }

    @Test
    void deleteNocturnalAlerts_removesLegacyRows() {
        jdbcTemplate.execute("ALTER TABLE access_alerts DROP CONSTRAINT IF EXISTS access_alerts_tipo_check");

        jdbcTemplate.update("INSERT INTO access_alerts (id, tipo, severidad, message, timestamp, leido) "
                        + "VALUES (?, 'ACCESO_NOCTURNO', 'LOW', 'legacy', ?, false)",
                UUID.randomUUID(), LocalDateTime.now());

        long before = accessAlertRepository.count();
        accessAlertRepository.deleteNocturnalAlerts();

        assertEquals(before - 1, accessAlertRepository.count());
        Long remaining = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM access_alerts WHERE tipo = 'ACCESO_NOCTURNO'", Long.class);
        assertEquals(0L, remaining);
    }

    @Test
    void doubleEntry_closesPreviousSession() throws Exception {
        Employee emp = createEmployee("EMP-MON-02", "120000002", EmployeeStatus.ACTIVO);
        grantPermission(emp);
        validate("EMP-MON-02");
        validate("EMP-MON-02");

        List<AccessSession> all = accessSessionRepository.findAll().stream()
                .filter(s -> s.getEmployee().getEmployeeCode().equals("EMP-MON-02"))
                .sorted((a, b) -> a.getEntryTime().compareTo(b.getEntryTime()))
                .toList();
        assertEquals(2, all.size());
        assertNotNull(all.get(0).getExitTime(), "La primera sesión debe quedar cerrada");
        assertNull(all.get(1).getExitTime(), "La segunda sesión debe estar activa");
    }

    @Test
    void occupancy_returnsAreaWithPeople() throws Exception {
        Employee emp = createEmployee("EMP-MON-03", "120000003", EmployeeStatus.ACTIVO);
        grantPermission(emp);
        validate("EMP-MON-03");

        // El seed puede cargar aforos en las salas; validamos que el empleado
        // del test aparezca dentro de la ocupación de su área.
        mockMvc.perform(get("/api/access/occupancy"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.areas[?(@.area=='" + areaName + "')].people[?(@.employeeCode=='EMP-MON-03')]")
                        .exists());
    }

    @Test
    void emergencyClosed_deniesAccess_andReopenAllows() throws Exception {
        Employee emp = createEmployee("EMP-MON-04", "120000004", EmployeeStatus.ACTIVO);
        grantPermission(emp);

        mockMvc.perform(post("/api/access/zones/{name}/emergency", "Sala Blanca A")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"cerrada\":true}"))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/access/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"employeeCode\":\"EMP-MON-04\",\"productionAreaName\":\"" + areaName + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("DENIED"))
                .andExpect(jsonPath("$.message").value("ZONA CERRADA POR EMERGENCIA"));

        mockMvc.perform(post("/api/access/zones/{name}/emergency", "Sala Blanca A")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"cerrada\":false}"))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/access/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"employeeCode\":\"EMP-MON-04\",\"productionAreaName\":\"" + areaName + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("AUTHORIZED"));
    }

    @Test
    void repeatedDenials_generateAlert() throws Exception {
        Employee emp = createEmployee("EMP-MON-05", "120000005", EmployeeStatus.INACTIVO);

        // 3 denegaciones en el mismo minuto → alerta DENEGACIONES_REPETIDAS.
        for (int i = 0; i < 3; i++) {
            validate("EMP-MON-05");
        }

        mockMvc.perform(get("/api/access/alerts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.tipo=='DENEGACIONES_REPETIDAS')]").exists());
    }

    @Test
    void stream_startsAsync() throws Exception {
        mockMvc.perform(get("/api/access/stream"))
                .andExpect(request().asyncStarted());
    }

    @Test
    void markAlertLeido_marksAlertAsRead() throws Exception {
        AccessAlert alert = accessAlertRepository.save(AccessAlert.builder()
                .tipo(AccessAlert.AlertType.DENEGACIONES_REPETIDAS)
                .severidad(AccessAlert.AlertSeverity.MEDIUM)
                .message("≥3 intentos denegados")
                .timestamp(java.time.LocalDateTime.now())
                .build());

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .patch("/api/access/alerts/{id}/leido", alert.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Alerta marcada como leída"));

        assertTrue(accessAlertRepository.findById(alert.getId()).orElseThrow().isLeido());
    }
}
