package laboratorioxyz.com.ZoneControl.modulo_gestion_personal.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import laboratorioxyz.com.ZoneControl.model.entity.Department;
import laboratorioxyz.com.ZoneControl.model.enums.DocumentType;
import laboratorioxyz.com.ZoneControl.model.enums.EmployeeStatus;
import laboratorioxyz.com.ZoneControl.model.enums.PermissionStatus;
import laboratorioxyz.com.ZoneControl.model.enums.WeekDay;
import laboratorioxyz.com.ZoneControl.model.repository.DepartmentRepository;
import laboratorioxyz.com.ZoneControl.model.repository.ProductionAreaRepository;
import laboratorioxyz.com.ZoneControl.modulo_gestion_personal.model.AccessPermission;
import laboratorioxyz.com.ZoneControl.modulo_gestion_personal.model.Employee;
import laboratorioxyz.com.ZoneControl.modulo_gestion_personal.model.PermissionSchedule;
import laboratorioxyz.com.ZoneControl.modulo_gestion_personal.repository.AccessPermissionRepository;
import laboratorioxyz.com.ZoneControl.modulo_gestion_personal.repository.EmployeeRepository;
import laboratorioxyz.com.ZoneControl.modulo_gestion_personal.repository.PermissionScheduleRepository;
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
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Turnos y horarios por día (3.2 §9, HU-26): la validación exige un schedule
 * cuyo día coincida con hoy y cuya ventana contenga la hora actual. Los
 * permisos sin schedules mantienen el comportamiento base (migración LUN-DOM).
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@Rollback
class PermissionScheduleControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private EmployeeRepository employeeRepository;
    @Autowired private DepartmentRepository departmentRepository;
    @Autowired private AccessPermissionRepository accessPermissionRepository;
    @Autowired private PermissionScheduleRepository permissionScheduleRepository;
    @Autowired private ProductionAreaRepository productionAreaRepository;

    private Department dept;
    private final String areaName = "Sala Blanca A";
    private final String areaIdName = "Sala Blanca A";

    @BeforeEach
    void setUp() {
        dept = departmentRepository.findByName("Control de Calidad").orElseThrow();
    }

    private Employee createEmployee(String code, String docNum) {
        return employeeRepository.save(Employee.builder()
                .employeeCode(code).documentType(DocumentType.CC).documentNumber(docNum)
                .firstName("Turno").lastName("Test").position("Técnico")
                .department(dept).status(EmployeeStatus.ACTIVO).build());
    }

    private AccessPermission savePermission(Employee employee) {
        return accessPermissionRepository.save(AccessPermission.builder()
                .employee(employee)
                .productionArea(productionAreaRepository.findByName(areaIdName).orElseThrow())
                .status(PermissionStatus.ACTIVO)
                .startDate(LocalDate.now().minusDays(1))
                .expirationDate(LocalDate.now().plusDays(30))
                .startTime(LocalTime.of(0, 0))
                .endTime(LocalTime.of(23, 59))
                .build());
    }

    private void scheduleForDays(AccessPermission permission, WeekDay... days) {
        for (WeekDay d : days) {
            permissionScheduleRepository.save(PermissionSchedule.builder()
                    .permission(permission).dayOfWeek(d)
                    .startTime(LocalTime.of(0, 0)).endTime(LocalTime.of(23, 59)).build());
        }
    }

    private void validate(String employeeCode) throws Exception {
        mockMvc.perform(post("/api/access/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"employeeCode\":\"" + employeeCode + "\",\"productionAreaName\":\"" + areaName + "\"}"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "SUPERVISOR_AUDITOR")
    void scheduleForToday_authorized() throws Exception {
        Employee emp = createEmployee("EMP-SCH-01", "130000001");
        AccessPermission p = savePermission(emp);
        scheduleForDays(p, WeekDay.today());

        mockMvc.perform(post("/api/access/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"employeeCode\":\"EMP-SCH-01\",\"productionAreaName\":\"" + areaName + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("AUTHORIZED"));
    }

    @Test
    @WithMockUser(roles = "SUPERVISOR_AUDITOR")
    void scheduleNotForToday_suspended() throws Exception {
        Employee emp = createEmployee("EMP-SCH-02", "130000002");
        AccessPermission p = savePermission(emp);
        // Todos los días EXCEPTO hoy → la validación debe fallar.
        WeekDay[] others = java.util.Arrays.stream(WeekDay.values())
                .filter(d -> d != WeekDay.today())
                .toArray(WeekDay[]::new);
        scheduleForDays(p, others);

        mockMvc.perform(post("/api/access/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"employeeCode\":\"EMP-SCH-02\",\"productionAreaName\":\"" + areaName + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("SUSPENDED"));
    }

    @Test
    @WithMockUser(roles = "SUPERVISOR_AUDITOR")
    void scheduleTimeOutsideWindow_suspended() throws Exception {
        Employee emp = createEmployee("EMP-SCH-03", "130000003");
        AccessPermission p = savePermission(emp);
        // Ventana futura (now+1h..now+2h) que nunca contiene la hora actual.
        permissionScheduleRepository.save(PermissionSchedule.builder()
                .permission(p).dayOfWeek(WeekDay.today())
                .startTime(LocalTime.now().plusHours(1))
                .endTime(LocalTime.now().plusHours(2)).build());

        mockMvc.perform(post("/api/access/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"employeeCode\":\"EMP-SCH-03\",\"productionAreaName\":\"" + areaName + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("SUSPENDED"));
    }

    @Test
    @WithMockUser(roles = "SUPERVISOR_AUDITOR")
    void existingPermissionWithoutSchedules_usesBaseTime() throws Exception {
        Employee emp = createEmployee("EMP-SCH-04", "130000004");
        savePermission(emp); // sin schedules → fallback al horario base 00:00-23:59

        mockMvc.perform(post("/api/access/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"employeeCode\":\"EMP-SCH-04\",\"productionAreaName\":\"" + areaName + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("AUTHORIZED"));
    }

    @Test
    @WithMockUser(roles = "GESTOR_PERSONAL")
    void createPermission_withSchedules_returns201() throws Exception {
        Employee emp = createEmployee("EMP-SCH-05", "130000005");

        mockMvc.perform(post("/api/permisos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "employeeCode", "EMP-SCH-05",
                                "productionAreaName", areaName,
                                "startDate", LocalDate.now().toString(),
                                "expirationDate", LocalDate.now().plusDays(30).toString(),
                                "startTime", "08:00",
                                "endTime", "17:00",
                                "schedules", List.of(Map.of(
                                        "dayOfWeek", "LUN", "startTime", "08:00", "endTime", "17:00"))
                        ))))
                .andExpect(status().isCreated());

        AccessPermission p = accessPermissionRepository.findByEmployee_Id(emp.getId()).get(0);
        List<PermissionSchedule> schedules = permissionScheduleRepository.findByPermission_Id(p.getId());
        assertEquals(1, schedules.size());
        assertEquals(WeekDay.LUN, schedules.get(0).getDayOfWeek());
    }

    @Test
    @WithMockUser(roles = "GESTOR_PERSONAL")
    void createPermission_defaultsToLunDomSchedules() throws Exception {
        Employee emp = createEmployee("EMP-SCH-06", "130000006");

        mockMvc.perform(post("/api/permisos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "employeeCode", "EMP-SCH-06",
                                "productionAreaName", areaName,
                                "startDate", LocalDate.now().toString(),
                                "expirationDate", LocalDate.now().plusDays(30).toString(),
                                "startTime", "08:00",
                                "endTime", "17:00"
                        ))))
                .andExpect(status().isCreated());

        AccessPermission p = accessPermissionRepository.findByEmployee_Id(emp.getId()).get(0);
        assertEquals(7, permissionScheduleRepository.findByPermission_Id(p.getId()).size());
    }
}
