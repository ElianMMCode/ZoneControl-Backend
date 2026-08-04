package laboratorioxyz.com.ZoneControl.modulo_reportes.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import laboratorioxyz.com.ZoneControl.model.entity.Department;
import laboratorioxyz.com.ZoneControl.model.entity.ProductionArea;
import laboratorioxyz.com.ZoneControl.model.enums.AccessResult;
import laboratorioxyz.com.ZoneControl.model.enums.DocumentType;
import laboratorioxyz.com.ZoneControl.model.enums.EmployeeStatus;
import laboratorioxyz.com.ZoneControl.model.enums.PermissionStatus;
import laboratorioxyz.com.ZoneControl.model.repository.DepartmentRepository;
import laboratorioxyz.com.ZoneControl.model.repository.ProductionAreaRepository;
import laboratorioxyz.com.ZoneControl.modulo_control_acceso.model.AccessHistory;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@Rollback
@WithMockUser(roles = "SUPERVISOR_AUDITOR")
class HistoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AccessHistoryRepository accessHistoryRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private ProductionAreaRepository productionAreaRepository;

    @Autowired
    private AccessPermissionRepository accessPermissionRepository;

    private Department dept;

    @BeforeEach
    void setUp() {
        dept = departmentRepository.findByName("Control de Calidad").orElseThrow();
        Employee emp = employeeRepository.save(Employee.builder()
                .employeeCode("EMP-HIST-01")
                .documentType(DocumentType.CC)
                .documentNumber("9999999999")
                .firstName("Historial")
                .lastName("Test")
                .position("Analista")
                .department(dept)
                .status(EmployeeStatus.ACTIVO)
                .build());
        accessHistoryRepository.save(AccessHistory.builder()
                .employee(emp)
                .department(dept.getName())
                .productionAreaName("Sala Blanca A")
                .timestamp(LocalDateTime.of(2026, 7, 15, 10, 30))
                .result(AccessResult.AUTHORIZED)
                .build());
        accessHistoryRepository.save(AccessHistory.builder()
                .employee(emp)
                .department(dept.getName())
                .productionAreaName("Sala Blanca B")
                .timestamp(LocalDateTime.of(2026, 7, 16, 14, 0))
                .result(AccessResult.DENIED)
                .build());
    }

    @Test
    void getHistory_validRange_returns200() throws Exception {
        mockMvc.perform(get("/api/historial")
                        .param("fechaInicio", "2026-07-01")
                        .param("fechaFin", "2026-07-31")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").value(2));
    }

    @Test
    void getHistory_invalidRange_returns400() throws Exception {
        mockMvc.perform(get("/api/historial")
                        .param("fechaInicio", "2026-08-01")
                        .param("fechaFin", "2026-07-01"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Rango de fechas inválido"));
    }

    @Test
    void getHistory_noResults_returnsEmptyPage() throws Exception {
        mockMvc.perform(get("/api/historial")
                        .param("fechaInicio", "2025-01-01")
                        .param("fechaFin", "2025-01-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    @Test
    void getHistory_filterByDepartment_returnsOnlyMatching() throws Exception {
        Department produccion = departmentRepository.findByName("Producción Sólidos").orElseThrow();
        Employee emp = employeeRepository.save(Employee.builder()
                .employeeCode("EMP-DEPT-01")
                .documentType(DocumentType.CC)
                .documentNumber("7777777001")
                .firstName("Depto")
                .lastName("Test")
                .position("Operario")
                .department(produccion)
                .status(EmployeeStatus.ACTIVO)
                .build());
        accessHistoryRepository.save(AccessHistory.builder()
                .employee(emp)
                .department(produccion.getName())
                .productionAreaName("Sala Blanca B")
                .timestamp(LocalDateTime.of(2026, 7, 10, 9, 0))
                .result(AccessResult.AUTHORIZED)
                .build());

        mockMvc.perform(get("/api/historial")
                        .param("fechaInicio", "2026-07-01")
                        .param("fechaFin", "2026-07-31")
                        .param("department", "Producción Sólidos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].department").value("Producción Sólidos"));
    }

    @Test
    void exportHistory_filterByDepartment_appliesFilter() throws Exception {
        mockMvc.perform(post("/api/historial/export")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "formato", "CSV",
                                "fechaInicio", "2026-07-01",
                                "fechaFin", "2026-07-31",
                                "departamentoName", "Control de Calidad"
                        ))))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("text/csv"));
    }

    @Test
    void exportHistory_departmentWithoutData_returns400() throws Exception {
        mockMvc.perform(post("/api/historial/export")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "formato", "CSV",
                                "fechaInicio", "2026-07-01",
                                "fechaFin", "2026-07-31",
                                "departamentoName", "Departamento Inexistente"
                        ))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("No hay datos para exportar"));
    }

    @Test
    void exportHistory_validCsv_returns200() throws Exception {
        mockMvc.perform(post("/api/historial/export")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "formato", "CSV",
                                "fechaInicio", "2026-07-01",
                                "fechaFin", "2026-07-31"
                        ))))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("text/csv"));
    }

    @Test
    void exportHistory_validExcel_returns200() throws Exception {
        mockMvc.perform(post("/api/historial/export")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "formato", "EXCEL",
                                "fechaInicio", "2026-07-01",
                                "fechaFin", "2026-07-31"
                        ))))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
    }

    @Test
    void exportHistory_noData_returns400() throws Exception {
        mockMvc.perform(post("/api/historial/export")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "formato", "CSV",
                                "fechaInicio", "2025-01-01",
                                "fechaFin", "2025-01-31"
                        ))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("No hay datos para exportar"));
    }

    @Test
    void getStats_returnsCounts() throws Exception {
        // Capturamos el baseline (DataInitializer puede haber sembrado
        // registros de hoy) y validamos solo los deltas introducidos
        // por este test: +3 accesos (2 AUTHORIZED + 1 DENIED) y
        // +1 permiso ACTIVO y +1 SUSPENDIDO.
        long baseTotal = accessHistoryRepository.findAll().stream()
                .filter(h -> h.getTimestamp() != null
                        && h.getTimestamp().toLocalDate().isEqual(LocalDate.now()))
                .count();
        long baseAutorizados = accessHistoryRepository.findAll().stream()
                .filter(h -> h.getTimestamp() != null
                        && h.getTimestamp().toLocalDate().isEqual(LocalDate.now())
                        && h.getResult() == AccessResult.AUTHORIZED)
                .count();
        long baseDenegados = accessHistoryRepository.findAll().stream()
                .filter(h -> h.getTimestamp() != null
                        && h.getTimestamp().toLocalDate().isEqual(LocalDate.now())
                        && h.getResult() == AccessResult.DENIED)
                .count();
        long baseNoRegistrados = accessHistoryRepository.findAll().stream()
                .filter(h -> h.getTimestamp() != null
                        && h.getTimestamp().toLocalDate().isEqual(LocalDate.now())
                        && h.getResult() == AccessResult.UNREGISTERED)
                .count();
        long baseAccesosSuspendidos = accessHistoryRepository.findAll().stream()
                .filter(h -> h.getTimestamp() != null
                        && h.getTimestamp().toLocalDate().isEqual(LocalDate.now())
                        && h.getResult() == AccessResult.SUSPENDED)
                .count();
        long baseActivos = accessPermissionRepository.countByStatus(PermissionStatus.ACTIVO);
        long baseSuspendidos = accessPermissionRepository.countByStatus(PermissionStatus.SUSPENDIDO);
        long baseEmpleadosConAcceso = accessPermissionRepository.countDistinctEmployeesWithActivePermissions();

        Employee emp = employeeRepository.save(Employee.builder()
                .employeeCode("EMP-STS-01")
                .documentType(DocumentType.CC)
                .documentNumber("900000010")
                .firstName("Stats")
                .lastName("Test")
                .position("Técnico")
                .department(dept)
                .status(EmployeeStatus.ACTIVO)
                .email("stats@test.com")
                .build());

        accessHistoryRepository.save(AccessHistory.builder()
                .employee(emp).department("Control de Calidad").productionAreaName("Sala Blanca A")
                .timestamp(LocalDateTime.now()).result(AccessResult.AUTHORIZED).build());
        accessHistoryRepository.save(AccessHistory.builder()
                .employee(emp).department("Control de Calidad").productionAreaName("Sala Blanca A")
                .timestamp(LocalDateTime.now()).result(AccessResult.DENIED).build());
        accessHistoryRepository.save(AccessHistory.builder()
                .employee(emp).department("Control de Calidad").productionAreaName("Sala Blanca A")
                .timestamp(LocalDateTime.now()).result(AccessResult.AUTHORIZED).build());

        ProductionArea area = productionAreaRepository.findByName("Sala Blanca A").orElseThrow();
        accessPermissionRepository.save(AccessPermission.builder()
                .employee(emp).productionArea(area).status(PermissionStatus.ACTIVO)
                .startDate(LocalDate.now()).expirationDate(LocalDate.now().plusMonths(1))
                .startTime(LocalTime.of(8, 0)).endTime(LocalTime.of(17, 0)).build());
        accessPermissionRepository.save(AccessPermission.builder()
                .employee(emp).productionArea(area).status(PermissionStatus.SUSPENDIDO)
                .startDate(LocalDate.now()).expirationDate(LocalDate.now().plusMonths(1))
                .startTime(LocalTime.of(8, 0)).endTime(LocalTime.of(17, 0)).build());

        mockMvc.perform(get("/api/historial/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalAccesosHoy").value(baseTotal + 3))
                .andExpect(jsonPath("$.accesosAutorizadosHoy").value(baseAutorizados + 2))
                .andExpect(jsonPath("$.accesosDenegadosHoy").value(baseDenegados + 1))
                .andExpect(jsonPath("$.accesosNoRegistradosHoy").value(baseNoRegistrados))
                .andExpect(jsonPath("$.accesosSuspendidosHoy").value(baseAccesosSuspendidos))
                .andExpect(jsonPath("$.totalPermisosActivos").value(baseActivos + 1))
                .andExpect(jsonPath("$.totalPermisosSuspendidos").value(baseSuspendidos + 1))
                .andExpect(jsonPath("$.empleadosConAcceso").value(baseEmpleadosConAcceso + 1));
    }
}
