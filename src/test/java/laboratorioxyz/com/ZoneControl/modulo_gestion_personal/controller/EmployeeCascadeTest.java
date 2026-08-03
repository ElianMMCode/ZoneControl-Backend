package laboratorioxyz.com.ZoneControl.modulo_gestion_personal.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import laboratorioxyz.com.ZoneControl.model.entity.Department;
import laboratorioxyz.com.ZoneControl.model.entity.ProductionArea;
import laboratorioxyz.com.ZoneControl.model.enums.*;
import laboratorioxyz.com.ZoneControl.model.repository.DepartmentRepository;
import laboratorioxyz.com.ZoneControl.model.repository.ProductionAreaRepository;
import laboratorioxyz.com.ZoneControl.modulo_autenticacion.model.User;
import laboratorioxyz.com.ZoneControl.modulo_autenticacion.repository.UserRepository;
import laboratorioxyz.com.ZoneControl.modulo_gestion_personal.model.AccessPermission;
import laboratorioxyz.com.ZoneControl.modulo_gestion_personal.model.Employee;
import laboratorioxyz.com.ZoneControl.modulo_gestion_personal.repository.AccessPermissionRepository;
import laboratorioxyz.com.ZoneControl.modulo_gestion_personal.repository.EmployeeRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@Rollback
@WithMockUser(roles = "ADMIN")
class EmployeeCascadeTest {

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

    @Autowired
    private AccessPermissionRepository accessPermissionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    private Employee createEmployee(String code, String docNum, String email) {
        Department dept = departmentRepository.findByName("Control de Calidad").orElseThrow();
        Employee emp = employeeRepository.save(Employee.builder()
                .employeeCode(code)
                .documentType(DocumentType.CC)
                .documentNumber(docNum)
                .firstName("Cascade")
                .lastName("Test")
                .position("Técnico")
                .department(dept)
                .status(EmployeeStatus.ACTIVO)
                .build());

        userRepository.save(User.builder()
                .firstName("Cascade")
                .lastName("Test")
                .email(email)
                .password(passwordEncoder.encode("Pass123!"))
                .role(Role.GESTOR_PERSONAL)
                .status(UserStatus.ACTIVO)
                .employee(emp)
                .build());

        return emp;
    }

    private UUID createPermission(Employee emp) {
        ProductionArea area = productionAreaRepository.findByName("Sala Blanca A").orElseThrow();
        return accessPermissionRepository.save(AccessPermission.builder()
                .employee(emp)
                .productionArea(area)
                .status(PermissionStatus.ACTIVO)
                .startDate(LocalDate.now())
                .expirationDate(LocalDate.now().plusMonths(1))
                .startTime(LocalTime.of(8, 0))
                .endTime(LocalTime.of(17, 0))
                .build()).getId();
    }

    @Test
    void deactivateEmployee_cascadesToUserAndPermissions() throws Exception {
        Employee emp = createEmployee("EMP-CSC-01", "800000001", "csc.down@test.com");
        createPermission(emp);

        mockMvc.perform(patch("/api/personal/{id}", emp.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("status", "INACTIVO"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("INACTIVO"));

        assertThat(userRepository.findByEmployee_Id(emp.getId()))
                .hasValueSatisfying(u -> assertThat(u.getStatus()).isEqualTo(UserStatus.INACTIVO));
        assertThat(accessPermissionRepository.findAll()).anyMatch(
                p -> p.getEmployee().getId().equals(emp.getId())
                        && p.getStatus() == PermissionStatus.SUSPENDIDO);
    }

    @Test
    void reactivateEmployee_restoresUserAndPermissions() throws Exception {
        Employee emp = createEmployee("EMP-CSC-02", "800000002", "csc.up@test.com");
        UUID permId = createPermission(emp);

        mockMvc.perform(patch("/api/personal/{id}", emp.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("status", "INACTIVO"))))
                .andExpect(status().isOk());

        assertThat(accessPermissionRepository.findById(permId))
                .hasValueSatisfying(p -> assertThat(p.getStatus()).isEqualTo(PermissionStatus.SUSPENDIDO));

        mockMvc.perform(patch("/api/personal/{id}", emp.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("status", "ACTIVO"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACTIVO"));

        assertThat(userRepository.findByEmployee_Id(emp.getId()))
                .hasValueSatisfying(u -> assertThat(u.getStatus()).isEqualTo(UserStatus.ACTIVO));
        assertThat(accessPermissionRepository.findById(permId))
                .hasValueSatisfying(p -> assertThat(p.getStatus()).isEqualTo(PermissionStatus.ACTIVO));
    }
}
