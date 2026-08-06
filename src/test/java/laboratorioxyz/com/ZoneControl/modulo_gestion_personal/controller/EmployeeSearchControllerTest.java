package laboratorioxyz.com.ZoneControl.modulo_gestion_personal.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import laboratorioxyz.com.ZoneControl.model.entity.Department;
import laboratorioxyz.com.ZoneControl.model.enums.DocumentType;
import laboratorioxyz.com.ZoneControl.model.enums.EmployeeStatus;
import laboratorioxyz.com.ZoneControl.model.repository.DepartmentRepository;
import laboratorioxyz.com.ZoneControl.modulo_gestion_personal.dto.RegisterEmployeeRequest;
import laboratorioxyz.com.ZoneControl.modulo_gestion_personal.dto.UpdateEmployeeRequest;
import laboratorioxyz.com.ZoneControl.modulo_gestion_personal.model.Employee;
import laboratorioxyz.com.ZoneControl.modulo_gestion_personal.model.Position;
import laboratorioxyz.com.ZoneControl.modulo_gestion_personal.repository.EmployeeRepository;
import laboratorioxyz.com.ZoneControl.modulo_gestion_personal.repository.PositionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@Rollback
@WithMockUser(roles = "ADMIN")
class EmployeeSearchControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private PositionRepository positionRepository;

    private Department department;
    private Employee seedEmployee;
    private Position cargo;

    @BeforeEach
    void setUp() {
        department = departmentRepository.findByName("Control de Calidad").orElseThrow();
        cargo = positionRepository.save(Position.builder()
                .name("Cargo Search " + System.nanoTime()).build());
        seedEmployee = employeeRepository.save(Employee.builder()
                .employeeCode("EMP-TEST-01")
                .documentType(DocumentType.CC)
                .documentNumber("1111111111")
                .firstName("Carlos")
                .lastName("Mendoza")
                .position("Técnico")
                .department(department)
                .status(EmployeeStatus.ACTIVO)
                .build());
    }

    @Test
    void searchEmployees_withFilters_returnsPaginatedResults() throws Exception {
        mockMvc.perform(get("/api/personal")
                        .param("firstName", "Carlos")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].firstName").value("Carlos"))
                .andExpect(jsonPath("$.content[0].lastName").value("Mendoza"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void searchEmployees_noFilters_returnsEmptyPage() throws Exception {
        // Antes exigía al menos un filtro (400). Ahora se permite paginar sin
        // filtros para que el selector de "Crear Usuario" pueda listar todos
        // los candidatos sin necesidad de teclear criterios.
        mockMvc.perform(get("/api/personal"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void searchEmployees_noResults_returnsEmptyPage() throws Exception {
        mockMvc.perform(get("/api/personal")
                        .param("firstName", "NoExiste")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    @Test
    void searchEmployees_byDocumentNumber_returnsResults() throws Exception {
        employeeRepository.save(Employee.builder()
                .employeeCode("EMP-TEST-02")
                .documentType(DocumentType.CC)
                .documentNumber("2222222222")
                .firstName("Ana")
                .lastName("López")
                .position("Analista")
                .department(department)
                .status(EmployeeStatus.ACTIVO)
                .build());

        mockMvc.perform(get("/api/personal")
                        .param("documentNumber", "222")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].firstName").value("Ana"));
    }

    @Test
    void searchEmployees_multipleFiltersAnd_returnsIntersection() throws Exception {
        employeeRepository.save(Employee.builder()
                .employeeCode("EMP-TEST-03")
                .documentType(DocumentType.CC)
                .documentNumber("4444444444")
                .firstName("Pedro")
                .lastName("Ramírez")
                .position("Técnico")
                .department(department)
                .status(EmployeeStatus.ACTIVO)
                .build());
        employeeRepository.save(Employee.builder()
                .employeeCode("EMP-TEST-04")
                .documentType(DocumentType.CC)
                .documentNumber("5555555555")
                .firstName("Pedro")
                .lastName("Pérez")
                .position("Analista")
                .department(department)
                .status(EmployeeStatus.ACTIVO)
                .build());

        mockMvc.perform(get("/api/personal")
                        .param("firstName", "Pedro")
                        .param("lastName", "Ramírez")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].documentNumber").value("4444444444"));
    }

    @Test
    void searchEmployees_byStatus_returnsFilteredResults() throws Exception {
        mockMvc.perform(get("/api/personal")
                        .param("status", "ACTIVO")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").isNumber());
    }

    @Test
    void getEmployeeById_exists_returns200() throws Exception {
        mockMvc.perform(get("/api/personal/{id}", seedEmployee.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Carlos"))
                .andExpect(jsonPath("$.lastName").value("Mendoza"))
                .andExpect(jsonPath("$.employeeCode").value("EMP-TEST-01"));
    }

    @Test
    void getEmployeeById_notFound_returns404() throws Exception {
        mockMvc.perform(get("/api/personal/{id}", UUID.randomUUID()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Empleado no encontrado"));
    }

    @Test
    void updateEmployee_validFields_returns200() throws Exception {
        var updateReq = UpdateEmployeeRequest.builder()
                .firstName("Carlos Alberto")
                .cargoId(cargo.getId())
                .build();

        mockMvc.perform(patch("/api/personal/{id}", seedEmployee.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Carlos Alberto"))
                .andExpect(jsonPath("$.position").value(cargo.getName()))
                .andExpect(jsonPath("$.lastName").value("Mendoza"));
    }

    @Test
    void updateEmployee_statusToInactive_returns200() throws Exception {
        var updateReq = UpdateEmployeeRequest.builder()
                .status(EmployeeStatus.INACTIVO)
                .build();

        mockMvc.perform(patch("/api/personal/{id}", seedEmployee.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("INACTIVO"));
    }

    @Test
    void updateEmployee_statusToSuspended_returns200() throws Exception {
        var updateReq = UpdateEmployeeRequest.builder()
                .status(EmployeeStatus.SUSPENDIDO)
                .build();

        mockMvc.perform(patch("/api/personal/{id}", seedEmployee.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUSPENDIDO"));
    }

    @Test
    void updateEmployee_nonExistent_returns404() throws Exception {
        var updateReq = UpdateEmployeeRequest.builder()
                .firstName("Test")
                .build();

        mockMvc.perform(patch("/api/personal/{id}", UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Empleado no encontrado"));
    }

    @Test
    void updateEmployee_duplicateDocument_returns409() throws Exception {
        employeeRepository.save(Employee.builder()
                .employeeCode("EMP-TEST-05")
                .documentType(DocumentType.CC)
                .documentNumber("6666666666")
                .firstName("Otro")
                .lastName("Empleado")
                .position("Analista")
                .department(department)
                .status(EmployeeStatus.ACTIVO)
                .build());

        var updateReq = UpdateEmployeeRequest.builder()
                .documentNumber("6666666666")
                .build();

        mockMvc.perform(patch("/api/personal/{id}", seedEmployee.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value(
                        "Ya existe un empleado con el documento CC número 6666666666"));
    }
}
