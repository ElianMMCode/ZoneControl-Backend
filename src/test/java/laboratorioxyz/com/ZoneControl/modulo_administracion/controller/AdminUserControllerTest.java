package laboratorioxyz.com.ZoneControl.modulo_administracion.controller;

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
import org.junit.jupiter.api.BeforeEach;
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
@WithMockUser(username = "admin@zonecontrol.com", roles = "ADMIN")
class AdminUserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private ProductionAreaRepository productionAreaRepository;

    @Autowired
    private AccessPermissionRepository accessPermissionRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    private User testUser;
    private Employee testEmployee;
    private UUID permissionId;

    @BeforeEach
    void setUp() {
        Department dept = departmentRepository.findByName("Control de Calidad").orElseThrow();
        testEmployee = employeeRepository.save(Employee.builder()
                .employeeCode("EMP-ADM-01")
                .documentType(DocumentType.CC)
                .documentNumber("900000001")
                .firstName("Admin")
                .lastName("Test")
                .position("Técnico")
                .department(dept)
                .status(EmployeeStatus.ACTIVO)
                .build());

        testUser = userRepository.save(User.builder()
                .firstName("Admin")
                .lastName("Test")
                .email("admin.test@test.com")
                .password(passwordEncoder.encode("Pass123!"))
                .role(Role.GESTOR_PERSONAL)
                .status(UserStatus.ACTIVO)
                .employee(testEmployee)
                .build());

        ProductionArea area = productionAreaRepository.findByName("Sala Blanca A").orElseThrow();
        permissionId = accessPermissionRepository.save(AccessPermission.builder()
                .employee(testEmployee)
                .productionArea(area)
                .status(PermissionStatus.ACTIVO)
                .startDate(LocalDate.now())
                .expirationDate(LocalDate.now().plusMonths(1))
                .startTime(LocalTime.of(8, 0))
                .endTime(LocalTime.of(17, 0))
                .build()).getId();
    }

    @Test
    void deactivateUser_cascadesToEmployeeAndPermissions() throws Exception {
        mockMvc.perform(patch("/admin/users/{id}/status", testUser.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("status", "INACTIVO"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("INACTIVO"))
                .andExpect(jsonPath("$.employeeStatus").value("INACTIVO"));

        assertThat(employeeRepository.findById(testEmployee.getId()))
                .hasValueSatisfying(e -> assertThat(e.getStatus()).isEqualTo(EmployeeStatus.INACTIVO));
        assertThat(accessPermissionRepository.findById(permissionId))
                .hasValueSatisfying(p -> assertThat(p.getStatus()).isEqualTo(PermissionStatus.SUSPENDIDO));
    }

    @Test
    void reactivateUser_restoresEmployeeAndPermissions() throws Exception {
        testUser.setStatus(UserStatus.INACTIVO);
        testEmployee.setStatus(EmployeeStatus.INACTIVO);
        employeeRepository.save(testEmployee);
        accessPermissionRepository.updateStatusByEmployeeId(testEmployee.getId(), PermissionStatus.SUSPENDIDO);
        userRepository.save(testUser);

        mockMvc.perform(patch("/admin/users/{id}/status", testUser.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("status", "ACTIVO"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACTIVO"))
                .andExpect(jsonPath("$.employeeStatus").value("ACTIVO"));

        assertThat(employeeRepository.findById(testEmployee.getId()))
                .hasValueSatisfying(e -> assertThat(e.getStatus()).isEqualTo(EmployeeStatus.ACTIVO));
        assertThat(accessPermissionRepository.findById(permissionId))
                .hasValueSatisfying(p -> assertThat(p.getStatus()).isEqualTo(PermissionStatus.ACTIVO));
    }

    @Test
    void deactivateSelf_returns400() throws Exception {
        User admin = userRepository.findByEmail("admin@zonecontrol.com").orElseThrow();

        mockMvc.perform(patch("/admin/users/{id}/status", admin.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("status", "INACTIVO"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("No puedes desactivar tu propia cuenta"));
    }

    @Test
    void updateStatus_nonExistentUser_returns404() throws Exception {
        mockMvc.perform(patch("/admin/users/{id}/status", UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("status", "ACTIVO"))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Usuario no encontrado"));
    }

    @Test
    void updateStatus_invalidStatus_returns400() throws Exception {
        mockMvc.perform(patch("/admin/users/{id}/status", testUser.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("status", ""))))
                .andExpect(status().isBadRequest());
    }
}
