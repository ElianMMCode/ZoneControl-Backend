package laboratorioxyz.com.ZoneControl.modulo_gestion_personal.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import laboratorioxyz.com.ZoneControl.model.entity.Department;
import laboratorioxyz.com.ZoneControl.model.enums.DocumentType;
import laboratorioxyz.com.ZoneControl.model.enums.EmployeeStatus;
import laboratorioxyz.com.ZoneControl.model.enums.Role;
import laboratorioxyz.com.ZoneControl.model.repository.DepartmentRepository;
import laboratorioxyz.com.ZoneControl.modulo_gestion_personal.model.Employee;
import laboratorioxyz.com.ZoneControl.modulo_gestion_personal.model.Position;
import laboratorioxyz.com.ZoneControl.modulo_gestion_personal.repository.EmployeeRepository;
import laboratorioxyz.com.ZoneControl.modulo_gestion_personal.repository.PositionRepository;
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

import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@Rollback
@WithMockUser(roles = "ADMIN")
class CargoControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private PositionRepository positionRepository;
    @Autowired private EmployeeRepository employeeRepository;
    @Autowired private DepartmentRepository departmentRepository;

    private Department dept;

    @BeforeEach
    void setUp() {
        dept = departmentRepository.findByName("Control de Calidad").orElseThrow();
    }

    @Test
    void listCargos_returnsCatalog() throws Exception {
        mockMvc.perform(get("/api/personal/cargos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].name").exists())
                .andExpect(jsonPath("$[0].systemRole").exists());
    }

    @Test
    void createCargo_valid_returns201() throws Exception {
        mockMvc.perform(post("/api/personal/cargos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "name", "Cargo Nuevo " + UUID.randomUUID().toString().substring(0, 6),
                                "systemRole", "GESTOR_PERSONAL"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").isString())
                .andExpect(jsonPath("$.systemRole").value("GESTOR_PERSONAL"));
    }

    @Test
    void createCargo_duplicateName_returns409() throws Exception {
        Position cargo = positionRepository.save(Position.builder()
                .name("Cargo Duplicado " + UUID.randomUUID().toString().substring(0, 6)).build());

        mockMvc.perform(post("/api/personal/cargos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "name", cargo.getName()))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value(
                        "Ya existe un cargo con el nombre: " + cargo.getName()));
    }

    @Test
    void createCargo_blankName_returns400() throws Exception {
        mockMvc.perform(post("/api/personal/cargos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("name", ""))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateCargo_syncsRoleOnLinkedEmployees() throws Exception {
        Position cargo = positionRepository.save(Position.builder()
                .name("Cargo Sinc " + UUID.randomUUID().toString().substring(0, 6)).build());
        Employee emp = employeeRepository.save(Employee.builder()
                .employeeCode("EMP-CRG-01")
                .documentType(DocumentType.CC)
                .documentNumber("500000001")
                .firstName("Cargo")
                .lastName("Test")
                .position(cargo.getName())
                .cargo(cargo)
                .department(dept)
                .status(EmployeeStatus.ACTIVO)
                .build());

        mockMvc.perform(put("/api/personal/cargos/{id}", cargo.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "name", cargo.getName(),
                                "systemRole", "SUPERVISOR_AUDITOR"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.systemRole").value("SUPERVISOR_AUDITOR"));

        Employee after = employeeRepository.findById(emp.getId()).orElseThrow();
        assertEquals(Role.SUPERVISOR_AUDITOR, after.getSystemRole(),
                "El rol derivado del cargo debe sincronizarse en el empleado");
    }

    @Test
    void updateCargo_nonExistent_returns404() throws Exception {
        mockMvc.perform(put("/api/personal/cargos/{id}", UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("name", "x"))))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteCargo_withoutEmployees_returns200() throws Exception {
        Position cargo = positionRepository.save(Position.builder()
                .name("Cargo Solo " + UUID.randomUUID().toString().substring(0, 6)).build());

        mockMvc.perform(delete("/api/personal/cargos/{id}", cargo.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Cargo eliminado exitosamente"));
    }

    @Test
    void deleteCargo_withLinkedEmployees_returns409() throws Exception {
        Position cargo = positionRepository.save(Position.builder()
                .name("Cargo Bloqueado " + UUID.randomUUID().toString().substring(0, 6)).build());
        employeeRepository.save(Employee.builder()
                .employeeCode("EMP-CRG-02")
                .documentType(DocumentType.CC)
                .documentNumber("500000002")
                .firstName("Cargo")
                .lastName("Bloqueo")
                .position(cargo.getName())
                .cargo(cargo)
                .department(dept)
                .status(EmployeeStatus.ACTIVO)
                .build());

        mockMvc.perform(delete("/api/personal/cargos/{id}", cargo.getId()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error")
                        .value(org.hamcrest.Matchers.containsString("empleado(s) vinculado(s)")));

        assertTrue(positionRepository.findById(cargo.getId()).isPresent());
    }
}
