package laboratorioxyz.com.ZoneControl.modulo_gestion_personal.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import laboratorioxyz.com.ZoneControl.model.entity.Department;
import laboratorioxyz.com.ZoneControl.model.entity.ProductionArea;
import laboratorioxyz.com.ZoneControl.model.enums.DocumentType;
import laboratorioxyz.com.ZoneControl.model.enums.EmployeeStatus;
import laboratorioxyz.com.ZoneControl.model.enums.PermissionStatus;
import laboratorioxyz.com.ZoneControl.model.repository.DepartmentRepository;
import laboratorioxyz.com.ZoneControl.model.repository.ProductionAreaRepository;
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
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@Rollback
@WithMockUser(roles = "ADMIN")
class PermissionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private ProductionAreaRepository productionAreaRepository;

    private Employee activeEmployee;

    @BeforeEach
    void setUp() {
        Department dept = departmentRepository.findByName("Control de Calidad").orElseThrow();
        activeEmployee = employeeRepository.save(Employee.builder()
                .employeeCode("EMP-PRM-01")
                .documentType(DocumentType.CC)
                .documentNumber("1111111111")
                .firstName("Test")
                .lastName("User")
                .position("Técnico")
                .department(dept)
                .status(EmployeeStatus.ACTIVO)
                .build());
    }

    @Test
    void grantPermission_validRequest_returns201() throws Exception {
        var request = new Object() {
            public String employeeCode = "EMP-PRM-01";
            public String productionAreaName = "Sala Blanca A";
            public String startDate = LocalDate.now().toString();
            public String expirationDate = LocalDate.now().plusMonths(1).toString();
            public String startTime = "08:00";
            public String endTime = "17:00";
        };

        mockMvc.perform(post("/permisos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.employeeCode").value("EMP-PRM-01"))
                .andExpect(jsonPath("$.status").value("ACTIVO"));
    }

    @Test
    void grantPermission_inactiveEmployee_returns400() throws Exception {
        activeEmployee.setStatus(EmployeeStatus.INACTIVO);
        employeeRepository.save(activeEmployee);

        var request = new Object() {
            public String employeeCode = "EMP-PRM-01";
            public String productionAreaName = "Sala Blanca A";
            public String startDate = LocalDate.now().toString();
            public String expirationDate = LocalDate.now().plusMonths(1).toString();
            public String startTime = "08:00";
            public String endTime = "17:00";
        };

        mockMvc.perform(post("/permisos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("No se puede otorgar acceso a empleado inactivo"));
    }

    @Test
    void grantPermission_conflict_returns409() throws Exception {
        var request = new Object() {
            public String employeeCode = "EMP-PRM-01";
            public String productionAreaName = "Sala Blanca A";
            public String startDate = LocalDate.now().toString();
            public String expirationDate = LocalDate.now().plusMonths(1).toString();
            public String startTime = "08:00";
            public String endTime = "17:00";
        };

        mockMvc.perform(post("/permisos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/permisos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Conflicto de permisos existente"));
    }

    @Test
    void grantPermission_nonExistentEmployee_returns404() throws Exception {
        var request = new Object() {
            public String employeeCode = "EMP-999999";
            public String productionAreaName = "Sala Blanca A";
            public String startDate = LocalDate.now().toString();
            public String expirationDate = LocalDate.now().plusMonths(1).toString();
            public String startTime = "08:00";
            public String endTime = "17:00";
        };

        mockMvc.perform(post("/permisos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Empleado no encontrado: EMP-999999"));
    }

    @Test
    void revokePermission_valid_returns200() throws Exception {
        var request = new Object() {
            public String employeeCode = "EMP-PRM-01";
            public String productionAreaName = "Sala Blanca A";
            public String startDate = LocalDate.now().toString();
            public String expirationDate = LocalDate.now().plusMonths(1).toString();
            public String startTime = "08:00";
            public String endTime = "17:00";
        };
        String json = mockMvc.perform(post("/permisos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        UUID permId = UUID.fromString(objectMapper.readTree(json).get("id").asText());

        mockMvc.perform(delete("/permisos/{id}", permId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Permiso revocado exitosamente"));
    }

    @Test
    void revokePermission_nonExistent_returns404() throws Exception {
        mockMvc.perform(delete("/permisos/{id}", UUID.randomUUID()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Permiso no encontrado"));
    }

    @Test
    void suspendPermission_valid_returns200() throws Exception {
        var request = new Object() {
            public String employeeCode = "EMP-PRM-01";
            public String productionAreaName = "Sala Blanca A";
            public String startDate = LocalDate.now().toString();
            public String expirationDate = LocalDate.now().plusMonths(1).toString();
            public String startTime = "08:00";
            public String endTime = "17:00";
        };
        String json = mockMvc.perform(post("/permisos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        UUID permId = UUID.fromString(objectMapper.readTree(json).get("id").asText());

        var suspendRequest = new Object() {
            public String reactivationDate = LocalDate.now().plusDays(7).toString();
        };

        mockMvc.perform(patch("/permisos/{id}/suspend", permId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(suspendRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUSPENDIDO"))
                .andExpect(jsonPath("$.reactivationDate").isString());
    }

    @Test
    void suspendPermission_nonExistent_returns404() throws Exception {
        var suspendRequest = new Object() {
            public String reactivationDate = LocalDate.now().plusDays(7).toString();
        };

        mockMvc.perform(patch("/permisos/{id}/suspend", UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(suspendRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Permiso no encontrado"));
    }
}
