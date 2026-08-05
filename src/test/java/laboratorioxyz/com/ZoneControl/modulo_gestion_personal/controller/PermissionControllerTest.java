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
import java.time.LocalTime;
import java.util.List;
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

        mockMvc.perform(post("/api/permisos")
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

        mockMvc.perform(post("/api/permisos")
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

        mockMvc.perform(post("/api/permisos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/permisos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("El empleado ya tiene un permiso para esta área. Edite el permiso existente"));
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

        mockMvc.perform(post("/api/permisos")
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
        String json = mockMvc.perform(post("/api/permisos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        UUID permId = UUID.fromString(objectMapper.readTree(json).get("id").asText());

        mockMvc.perform(delete("/api/permisos/{id}", permId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Permiso revocado exitosamente"));
    }

    @Test
    void revokePermission_nonExistent_returns404() throws Exception {
        mockMvc.perform(delete("/api/permisos/{id}", UUID.randomUUID()))
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
        String json = mockMvc.perform(post("/api/permisos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        UUID permId = UUID.fromString(objectMapper.readTree(json).get("id").asText());

        var suspendRequest = new Object() {
            public String reactivationDate = LocalDate.now().plusDays(7).toString();
        };

        mockMvc.perform(patch("/api/permisos/{id}/suspend", permId)
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

        mockMvc.perform(patch("/api/permisos/{id}/suspend", UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(suspendRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Permiso no encontrado"));
    }

    @Test
    void suspendPermission_missingReactivationDate_returns400() throws Exception {
        mockMvc.perform(patch("/api/permisos/{id}/suspend", UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("La fecha de reactivación es obligatoria"));
    }

    @Test
    void grantPermission_includesSchedules() throws Exception {
        var request = new Object() {
            public String employeeCode = "EMP-PRM-01";
            public String productionAreaName = "Sala Blanca A";
            public String startDate = LocalDate.now().toString();
            public String expirationDate = LocalDate.now().plusMonths(1).toString();
            public String startTime = "08:00";
            public String endTime = "17:00";
        };

        mockMvc.perform(post("/api/permisos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.schedules").isArray())
                .andExpect(jsonPath("$.schedules.length()").value(7))
                .andExpect(jsonPath("$.schedules[0].dayOfWeek").isString());
    }

    private void grantPermission() throws Exception {
        var request = new Object() {
            public String employeeCode = "EMP-PRM-01";
            public String productionAreaName = "Sala Blanca A";
            public String startDate = LocalDate.now().toString();
            public String expirationDate = LocalDate.now().plusMonths(1).toString();
            public String startTime = "08:00";
            public String endTime = "17:00";
        };
        mockMvc.perform(post("/api/permisos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    void listPermissions_returnsPaginatedResults() throws Exception {
        grantPermission();

        mockMvc.perform(get("/api/permisos")
                        .param("search", "EMP-PRM-01"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].employeeCode").value("EMP-PRM-01"))
                .andExpect(jsonPath("$.content[0].areaName").value("Sala Blanca A"));
    }

    @Test
    void listPermissions_filtersByStatus() throws Exception {
        grantPermission();
        UUID permId = UUID.fromString(objectMapper.readTree(
                mockMvc.perform(get("/api/permisos").param("search", "EMP-PRM-01"))
                        .andReturn().getResponse().getContentAsString())
                .get("content").get(0).get("id").asText());
        var suspendBody = new Object() {
            public String reactivationDate = LocalDate.now().plusDays(7).toString();
        };
        mockMvc.perform(patch("/api/permisos/{id}/suspend", permId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(suspendBody)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/permisos")
                        .param("status", "SUSPENDIDO")
                        .param("search", "EMP-PRM-01"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].status").value("SUSPENDIDO"));
    }

    @Test
    void listPermissions_searchByEmployeeName() throws Exception {
        grantPermission();

        mockMvc.perform(get("/api/permisos")
                        .param("search", "Test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(org.hamcrest.Matchers.greaterThan(0)));
    }

    @Test
    void listPermissions_noMatch_returnsEmptyPage() throws Exception {
        mockMvc.perform(get("/api/permisos")
                        .param("search", "NOEXISTE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isEmpty())
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    @Test
    void listAreas_returnsAllAreas() throws Exception {
        mockMvc.perform(get("/api/permisos/areas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").exists())
                .andExpect(jsonPath("$.length()").value(org.hamcrest.Matchers.greaterThanOrEqualTo(5)));
    }

    @Test
    void listAreaEmployees_returnsAssignedEmployees() throws Exception {
        grantPermission();

        mockMvc.perform(get("/api/permisos/areas/{name}/empleados", "Sala Blanca A"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.employeeCode=='EMP-PRM-01')]").exists())
                .andExpect(jsonPath("$[?(@.employeeCode=='EMP-PRM-01')].employeeName").value(
                        org.hamcrest.Matchers.hasItem("Test User")))
                .andExpect(jsonPath("$[?(@.employeeCode=='EMP-PRM-01')].position").value(
                        org.hamcrest.Matchers.hasItem("Técnico")))
                .andExpect(jsonPath("$[?(@.employeeCode=='EMP-PRM-01')].department").value(
                        org.hamcrest.Matchers.hasItem("Control de Calidad")))
                .andExpect(jsonPath("$[?(@.employeeCode=='EMP-PRM-01')].employeeStatus").value(
                        org.hamcrest.Matchers.hasItem("ACTIVO")));
    }

    @Test
    void listAreaEmployees_unknownArea_returns404() throws Exception {
        mockMvc.perform(get("/api/permisos/areas/{name}/empleados", "Area Inexistente"))
                .andExpect(status().isNotFound());
    }

    @Test
    void listAreaAuthorizations_returnsFullInfo() throws Exception {
        grantPermission();

        mockMvc.perform(get("/api/permisos/areas/{name}/autorizaciones", "Sala Blanca A"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.employeeCode=='EMP-PRM-01')].id").isNotEmpty())
                .andExpect(jsonPath("$[?(@.employeeCode=='EMP-PRM-01')].permissionStatus").value(
                        org.hamcrest.Matchers.hasItem("ACTIVO")))
                .andExpect(jsonPath("$[?(@.employeeCode=='EMP-PRM-01')].startDate").isNotEmpty())
                .andExpect(jsonPath("$[?(@.employeeCode=='EMP-PRM-01')].expirationDate").isNotEmpty())
                .andExpect(jsonPath("$[?(@.employeeCode=='EMP-PRM-01')].startTime").isNotEmpty())
                .andExpect(jsonPath("$[?(@.employeeCode=='EMP-PRM-01')].endTime").isNotEmpty())
                .andExpect(jsonPath("$[?(@.employeeCode=='EMP-PRM-01')].schedules.length()").value(
                        org.hamcrest.Matchers.hasItem(7)));
    }

    @Test
    void listAreaAuthorizations_unknownArea_returns404() throws Exception {
        mockMvc.perform(get("/api/permisos/areas/{name}/autorizaciones", "Area Inexistente"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "SUPERVISOR_AUDITOR")
    void listAreaAuthorizations_supervisorCanRead_returns200() throws Exception {
        mockMvc.perform(get("/api/permisos/areas/{name}/autorizaciones", "Sala Blanca A"))
                .andExpect(status().isOk());
    }

    @Test
    void grantPermission_sameEmployeeAreaDifferentTimes_returns409() throws Exception {
        var request1 = new Object() {
            public String employeeCode = "EMP-PRM-01";
            public String productionAreaName = "Sala Blanca A";
            public String startDate = LocalDate.now().toString();
            public String expirationDate = LocalDate.now().plusMonths(1).toString();
            public String startTime = "08:00";
            public String endTime = "12:00";
        };
        mockMvc.perform(post("/api/permisos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request1)))
                .andExpect(status().isCreated());

        var request2 = new Object() {
            public String employeeCode = "EMP-PRM-01";
            public String productionAreaName = "Sala Blanca A";
            public String startDate = LocalDate.now().toString();
            public String expirationDate = LocalDate.now().plusMonths(1).toString();
            public String startTime = "13:00";
            public String endTime = "17:00";
        };
        mockMvc.perform(post("/api/permisos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request2)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("El empleado ya tiene un permiso para esta área. Edite el permiso existente"));
    }

    @Test
    void grantPermission_sameEmployeeDifferentArea_returns201() throws Exception {
        var request1 = new Object() {
            public String employeeCode = "EMP-PRM-01";
            public String productionAreaName = "Sala Blanca A";
            public String startDate = LocalDate.now().toString();
            public String expirationDate = LocalDate.now().plusMonths(1).toString();
            public String startTime = "08:00";
            public String endTime = "17:00";
        };
        mockMvc.perform(post("/api/permisos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request1)))
                .andExpect(status().isCreated());

        var request2 = new Object() {
            public String employeeCode = "EMP-PRM-01";
            public String productionAreaName = "Sala Blanca B";
            public String startDate = LocalDate.now().toString();
            public String expirationDate = LocalDate.now().plusMonths(1).toString();
            public String startTime = "08:00";
            public String endTime = "17:00";
        };
        mockMvc.perform(post("/api/permisos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request2)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.areaName").value("Sala Blanca B"));
    }

    @Test
    void editPermission_validData_returns200() throws Exception {
        grantPermission();
        UUID permId = UUID.fromString(objectMapper.readTree(
                mockMvc.perform(get("/api/permisos").param("search", "EMP-PRM-01"))
                        .andReturn().getResponse().getContentAsString())
                .get("content").get(0).get("id").asText());

        var editBody = new Object() {
            public String startTime = "06:00";
            public String endTime = "14:00";
            public String startDate = LocalDate.now().plusDays(1).toString();
            public String expirationDate = LocalDate.now().plusMonths(2).toString();
        };
        mockMvc.perform(patch("/api/permisos/{id}", permId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(editBody)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.startTime").value("06:00:00"))
                .andExpect(jsonPath("$.endTime").value("14:00:00"));
    }

    @Test
    void editPermission_nonExistent_returns404() throws Exception {
        var editBody = new Object() {
            public String startTime = "06:00";
            public String endTime = "14:00";
        };
        mockMvc.perform(patch("/api/permisos/{id}", UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(editBody)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Permiso no encontrado"));
    }

    @Test
    void editPermission_suspended_returns400() throws Exception {
        grantPermission();
        UUID permId = UUID.fromString(objectMapper.readTree(
                mockMvc.perform(get("/api/permisos").param("search", "EMP-PRM-01"))
                        .andReturn().getResponse().getContentAsString())
                .get("content").get(0).get("id").asText());
        var suspendBody = new Object() {
            public String reactivationDate = LocalDate.now().plusDays(7).toString();
        };
        mockMvc.perform(patch("/api/permisos/{id}/suspend", permId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(suspendBody)))
                .andExpect(status().isOk());

        var editBody = new Object() {
            public String startTime = "06:00";
            public String endTime = "14:00";
        };
        mockMvc.perform(patch("/api/permisos/{id}", permId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(editBody)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("No se puede editar un permiso suspendido"));
    }

    @Test
    void reactivatePermission_suspended_returns200() throws Exception {
        grantPermission();
        UUID permId = UUID.fromString(objectMapper.readTree(
                mockMvc.perform(get("/api/permisos").param("search", "EMP-PRM-01"))
                        .andReturn().getResponse().getContentAsString())
                .get("content").get(0).get("id").asText());

        var suspendBody = new Object() {
            public String reactivationDate = LocalDate.now().plusDays(7).toString();
        };
        mockMvc.perform(patch("/api/permisos/{id}/suspend", permId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(suspendBody)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUSPENDIDO"));

        mockMvc.perform(patch("/api/permisos/{id}/reactivate", permId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACTIVO"))
                .andExpect(jsonPath("$.reactivationDate").value(org.hamcrest.Matchers.nullValue()));
    }

    @Test
    void reactivatePermission_nonExistent_returns404() throws Exception {
        mockMvc.perform(patch("/api/permisos/{id}/reactivate", UUID.randomUUID()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Permiso no encontrado"));
    }

    @Test
    void createArea_valid_returns201() throws Exception {
        var body = new Object() {
            public String name = "Area Test " + UUID.randomUUID().toString().substring(0, 8);
            public String description = "Area de prueba";
        };
        mockMvc.perform(post("/api/permisos/areas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value(body.name))
                .andExpect(jsonPath("$.description").value("Area de prueba"));
    }

    @Test
    void createArea_duplicateName_returns409() throws Exception {
        var body = new Object() {
            public String name = "Sala Blanca A";
            public String description = "duplicado";
        };
        mockMvc.perform(post("/api/permisos/areas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value(
                        "Ya existe un área con el nombre: Sala Blanca A"));
    }

    @Test
    void createArea_blankName_returns400() throws Exception {
        var body = new Object() {
            public String name = "";
            public String description = "x";
        };
        mockMvc.perform(post("/api/permisos/areas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateArea_valid_returns200() throws Exception {
        var created = productionAreaRepository.findByName("Sala Blanca A").orElseThrow();
        var body = new Object() {
            public String name = "Sala Blanca A";
            public String description = "Descripción actualizada";
        };
        mockMvc.perform(put("/api/permisos/areas/{id}", created.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description").value("Descripción actualizada"));
    }

    @Test
    void updateArea_nonExistent_returns404() throws Exception {
        var body = new Object() {
            public String name = "x";
        };
        mockMvc.perform(put("/api/permisos/areas/{id}", UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteArea_noPermissions_returns200() throws Exception {
        var created = productionAreaRepository.save(ProductionArea.builder()
                .name("Area Sin Permisos " + UUID.randomUUID().toString().substring(0, 6))
                .description("temporal")
                .build());
        mockMvc.perform(delete("/api/permisos/areas/{id}", created.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Área eliminada exitosamente"));
    }

    @Test
    void deleteArea_withActivePermissions_returns409() throws Exception {
        grantPermission();
        var area = productionAreaRepository.findByName("Sala Blanca A").orElseThrow();
        mockMvc.perform(delete("/api/permisos/areas/{id}", area.getId()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error")
                        .value(org.hamcrest.Matchers.containsString("permiso(s) activo(s)")));
    }
}
