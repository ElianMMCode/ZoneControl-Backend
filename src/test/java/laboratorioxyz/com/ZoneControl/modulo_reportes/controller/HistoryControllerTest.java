package laboratorioxyz.com.ZoneControl.modulo_reportes.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import laboratorioxyz.com.ZoneControl.model.entity.Department;
import laboratorioxyz.com.ZoneControl.model.enums.AccessResult;
import laboratorioxyz.com.ZoneControl.model.enums.DocumentType;
import laboratorioxyz.com.ZoneControl.model.enums.EmployeeStatus;
import laboratorioxyz.com.ZoneControl.model.repository.DepartmentRepository;
import laboratorioxyz.com.ZoneControl.modulo_control_acceso.model.AccessHistory;
import laboratorioxyz.com.ZoneControl.modulo_control_acceso.repository.AccessHistoryRepository;
import laboratorioxyz.com.ZoneControl.modulo_gestion_personal.model.Employee;
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
import java.time.LocalDateTime;
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
        mockMvc.perform(get("/historial")
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
        mockMvc.perform(get("/historial")
                        .param("fechaInicio", "2026-08-01")
                        .param("fechaFin", "2026-07-01"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Rango de fechas inválido"));
    }

    @Test
    void getHistory_noResults_returnsEmptyPage() throws Exception {
        mockMvc.perform(get("/historial")
                        .param("fechaInicio", "2025-01-01")
                        .param("fechaFin", "2025-01-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    @Test
    void exportHistory_validCsv_returns200() throws Exception {
        mockMvc.perform(post("/historial/export")
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
        mockMvc.perform(post("/historial/export")
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
        mockMvc.perform(post("/historial/export")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "formato", "CSV",
                                "fechaInicio", "2025-01-01",
                                "fechaFin", "2025-01-31"
                        ))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("No hay datos para exportar"));
    }
}
