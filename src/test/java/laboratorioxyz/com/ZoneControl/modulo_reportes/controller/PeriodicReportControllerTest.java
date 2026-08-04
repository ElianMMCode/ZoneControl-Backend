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

import java.time.LocalDateTime;
import java.util.Map;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@Rollback
@WithMockUser(roles = "SUPERVISOR_AUDITOR")
class PeriodicReportControllerTest {

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

    @BeforeEach
    void setUp() {
        Department dept = departmentRepository.findByName("Control de Calidad").orElseThrow();
        Employee emp = employeeRepository.save(Employee.builder()
                .employeeCode("EMP-PER-01")
                .documentType(DocumentType.CC)
                .documentNumber("8888888888")
                .firstName("Periodico")
                .lastName("Test")
                .position("Analista")
                .department(dept)
                .status(EmployeeStatus.ACTIVO)
                .build());
        accessHistoryRepository.save(AccessHistory.builder()
                .employee(emp)
                .department(dept.getName())
                .productionAreaName("Sala Blanca A")
                .timestamp(LocalDateTime.now())
                .result(AccessResult.AUTHORIZED)
                .build());
    }

    @Test
    void periodicReport_validCsv_returns200() throws Exception {
        mockMvc.perform(post("/api/reportes/archivo-periodico")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "mes", LocalDateTime.now().getMonthValue(),
                                "anio", LocalDateTime.now().getYear(),
                                "formato", "CSV"
                        ))))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("text/csv"));
    }

    @Test
    void periodicReport_validExcel_returns200() throws Exception {
        mockMvc.perform(post("/api/reportes/archivo-periodico")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "mes", LocalDateTime.now().getMonthValue(),
                                "anio", LocalDateTime.now().getYear(),
                                "formato", "EXCEL"
                        ))))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
    }

    @Test
    void periodicReport_validPdf_returns200() throws Exception {
        mockMvc.perform(post("/api/reportes/archivo-periodico")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "mes", LocalDateTime.now().getMonthValue(),
                                "anio", LocalDateTime.now().getYear(),
                                "formato", "PDF"
                        ))))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("application/pdf"));
    }

    @Test
    void periodicReport_noData_returns400() throws Exception {
        mockMvc.perform(post("/api/reportes/archivo-periodico")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "mes", 1,
                                "anio", 2020,
                                "formato", "CSV"
                        ))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(
                        "No se encontraron registros de acceso para el período seleccionado"));
    }

    @Test
    void periodicReport_aggregatesByDepartment_withoutPersonalData() throws Exception {
        // "Esterilización" no tiene historial sembrado → conteos deterministas.
        Department esterilizacion = departmentRepository.findByName("Esterilización").orElseThrow();
        Employee emp2 = employeeRepository.save(Employee.builder()
                .employeeCode("EMP-PER-02")
                .documentType(DocumentType.CC)
                .documentNumber("8888888889")
                .firstName("Segundo")
                .lastName("Periodico")
                .position("Operario")
                .department(esterilizacion)
                .status(EmployeeStatus.ACTIVO)
                .build());
        accessHistoryRepository.save(AccessHistory.builder()
                .employee(emp2).department(esterilizacion.getName()).productionAreaName("Sala Blanca A")
                .timestamp(LocalDateTime.now()).result(AccessResult.AUTHORIZED).build());
        accessHistoryRepository.save(AccessHistory.builder()
                .employee(emp2).department(esterilizacion.getName()).productionAreaName("Sala Blanca B")
                .timestamp(LocalDateTime.now()).result(AccessResult.DENIED).build());

        int mes = LocalDateTime.now().getMonthValue();
        int anio = LocalDateTime.now().getYear();
        String periodo = String.format("%d-%02d", anio, mes);

        mockMvc.perform(post("/api/reportes/archivo-periodico")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "mes", mes, "anio", anio, "formato", "CSV"
                        ))))
                .andExpect(status().isOk())
                // Agregación por departamento, sin datos personales
                .andExpect(content().string(containsString("Esterilización;" + periodo + ";2;1;1;0;0")))
                .andExpect(content().string(not(containsString("EMP-PER-01"))))
                .andExpect(content().string(not(containsString("Periodico Test"))));
    }

    @Test
    void periodicReport_filtersByDepartmentNames() throws Exception {
        Department produccion = departmentRepository.findByName("Producción Sólidos").orElseThrow();
        Employee emp2 = employeeRepository.save(Employee.builder()
                .employeeCode("EMP-PER-03")
                .documentType(DocumentType.CC)
                .documentNumber("8888888890")
                .firstName("Tercero")
                .lastName("Periodico")
                .position("Operario")
                .department(produccion)
                .status(EmployeeStatus.ACTIVO)
                .build());
        accessHistoryRepository.save(AccessHistory.builder()
                .employee(emp2).department(produccion.getName()).productionAreaName("Sala Blanca B")
                .timestamp(LocalDateTime.now()).result(AccessResult.AUTHORIZED).build());

        int mes = LocalDateTime.now().getMonthValue();
        int anio = LocalDateTime.now().getYear();

        mockMvc.perform(post("/api/reportes/archivo-periodico")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "mes", mes, "anio", anio, "formato", "CSV",
                                "departmentNames", java.util.List.of("Control de Calidad")
                        ))))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Control de Calidad;")))
                .andExpect(content().string(not(containsString("Producción Sólidos;"))));
    }
}
