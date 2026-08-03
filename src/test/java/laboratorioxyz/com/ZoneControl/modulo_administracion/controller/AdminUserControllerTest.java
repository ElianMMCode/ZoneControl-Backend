package laboratorioxyz.com.ZoneControl.modulo_administracion.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import laboratorioxyz.com.ZoneControl.model.entity.Department;
import laboratorioxyz.com.ZoneControl.model.enums.Role;
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
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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
                .email("personal.admin@test.com")
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
        mockMvc.perform(patch("/api/admin/users/{id}/status", testUser.getId())
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

        mockMvc.perform(patch("/api/admin/users/{id}/status", testUser.getId())
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

        mockMvc.perform(patch("/api/admin/users/{id}/status", admin.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("status", "INACTIVO"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("No puedes desactivar tu propia cuenta"));
    }

    @Test
    void updateStatus_nonExistentUser_returns404() throws Exception {
        mockMvc.perform(patch("/api/admin/users/{id}/status", UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("status", "ACTIVO"))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Usuario no encontrado"));
    }

    @Test
    void updateStatus_invalidStatus_returns400() throws Exception {
        mockMvc.perform(patch("/api/admin/users/{id}/status", testUser.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("status", ""))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateUser_validData_returns200() throws Exception {
        mockMvc.perform(put("/api/admin/users/{id}", testUser.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "firstName", "Updated",
                                "lastName", "Name",
                                "email", "updated@test.com",
                                "role", "ADMIN"
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Updated"))
                .andExpect(jsonPath("$.lastName").value("Name"))
                .andExpect(jsonPath("$.email").value("updated@test.com"))
                .andExpect(jsonPath("$.role").value("ADMIN"));
    }

    @Test
    void updateUser_duplicateEmail_returns409() throws Exception {
        User admin = userRepository.findByEmail("admin@zonecontrol.com").orElseThrow();

        mockMvc.perform(put("/api/admin/users/{id}", testUser.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("email", admin.getEmail()))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("El email ya está registrado"));
    }

    @Test
    void updateUser_nonExistentUser_returns404() throws Exception {
        mockMvc.perform(put("/api/admin/users/{id}", UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("firstName", "Any"))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Usuario no encontrado"));
    }

    @Test
    void resetPassword_validUser_returns200() throws Exception {
        mockMvc.perform(post("/api/admin/users/{id}/reset-password", testUser.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message")
                        .value("Enlace de configuración enviado al correo del usuario"));

        User afterReset = userRepository.findById(testUser.getId()).orElseThrow();
        assertThat(afterReset.getPassword()).isNull();
        assertThat(afterReset.getSetupToken()).isNotBlank();
        assertThat(afterReset.getSetupTokenExpiry()).isAfter(LocalDateTime.now());
    }

    @Test
    void resetPassword_nonExistentUser_returns404() throws Exception {
        mockMvc.perform(post("/api/admin/users/{id}/reset-password", UUID.randomUUID()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Usuario no encontrado"));
    }

    @Test
    void resetPassword_employeeWithoutEmail_returns400() throws Exception {
        Department dept = departmentRepository.findByName("Control de Calidad").orElseThrow();
        Employee noEmailEmployee = employeeRepository.save(Employee.builder()
                .employeeCode("EMP-ADM-03")
                .documentType(DocumentType.CC)
                .documentNumber("900000003")
                .firstName("Sin")
                .lastName("Correo")
                .position("Técnico")
                .department(dept)
                .status(EmployeeStatus.ACTIVO)
                .build());
        User noEmailUser = userRepository.save(User.builder()
                .firstName("Sin")
                .lastName("Correo")
                .email("sin.correo@test.com")
                .password(passwordEncoder.encode("Pass123!"))
                .role(Role.GESTOR_PERSONAL)
                .status(UserStatus.ACTIVO)
                .employee(noEmailEmployee)
                .build());

        mockMvc.perform(post("/api/admin/users/{id}/reset-password", noEmailUser.getId()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error")
                        .value("El empleado no tiene un correo registrado. Regístrelo en Gestión Personal para restablecer la contraseña"));
    }

    @Test
    void createUser_validEmployee_returns201() throws Exception {
        Department dept = departmentRepository.findByName("Control de Calidad").orElseThrow();
        Employee fresh = employeeRepository.save(Employee.builder()
                .employeeCode("EMP-ADM-02")
                .documentType(DocumentType.CC)
                .documentNumber("900000002")
                .firstName("Nuevo")
                .lastName("Empleado")
                .email("nuevo@test.com")
                .position("Analista")
                .department(dept)
                .status(EmployeeStatus.ACTIVO)
                .build());

        mockMvc.perform(post("/api/admin/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "employeeCode", "EMP-ADM-02",
                                "role", "SUPERVISOR_AUDITOR",
                                "status", "ACTIVO"
                        ))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isString());

                assertThat(userRepository.findByEmployee_Id(fresh.getId()))
                        .hasValueSatisfying(u -> {
                            assertThat(u.getFirstName()).isEqualTo("Nuevo");
                            assertThat(u.getLastName()).isEqualTo("Empleado");
                            assertThat(u.getEmail()).isEqualTo("nuevo@test.com");
                            assertThat(u.getPassword()).isNull();
                            assertThat(u.getRole()).isEqualTo(Role.SUPERVISOR_AUDITOR);
                    assertThat(u.getStatus()).isEqualTo(UserStatus.ACTIVO);
                    assertThat(u.getSetupToken()).isNotBlank();
                    assertThat(u.getSetupTokenExpiry()).isNotNull();
                });
    }

    @Test
    void createUser_employeeWithoutEmail_returns400() throws Exception {
        Department dept = departmentRepository.findByName("Control de Calidad").orElseThrow();
        employeeRepository.save(Employee.builder()
                .employeeCode("EMP-ADM-03")
                .documentType(DocumentType.CC)
                .documentNumber("900000003")
                .firstName("Sin")
                .lastName("Correo")
                .position("Auxiliar")
                .department(dept)
                .status(EmployeeStatus.ACTIVO)
                .build());

        mockMvc.perform(post("/api/admin/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "employeeCode", "EMP-ADM-03",
                                "role", "ADMIN",
                                "status", "ACTIVO"
                        ))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error")
                        .value("El empleado no tiene un correo registrado. Regístrelo en Gestión Personal para poder crear el usuario"));
    }

    @Test
    void createUser_employeeAlreadyLinked_returns409() throws Exception {
        testEmployee.setEmail("nuevo@test.com");
        employeeRepository.save(testEmployee);

        mockMvc.perform(post("/api/admin/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "employeeCode", "EMP-ADM-01",
                                "role", "ADMIN",
                                "status", "ACTIVO"
                        ))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error")
                        .value("El empleado ya tiene un usuario de sistema asociado"));
    }

    @Test
    void listUsers_returnsPaginatedResults() throws Exception {
        mockMvc.perform(get("/api/admin/users")
                        .param("search", "EMP-ADM-01"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].employeeCode").value("EMP-ADM-01"))
                .andExpect(jsonPath("$.content[0].position").value("Técnico"));
    }

    @Test
    void getUserById_returnsDetail() throws Exception {
        mockMvc.perform(get("/api/admin/users/{id}", testUser.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testUser.getId().toString()))
                .andExpect(jsonPath("$.email").value("admin.test@test.com"))
                .andExpect(jsonPath("$.role").value("GESTOR_PERSONAL"))
                .andExpect(jsonPath("$.employeeCode").value("EMP-ADM-01"))
                .andExpect(jsonPath("$.position").value("Técnico"));
    }

    @Test
    void getUserById_nonExistentUser_returns404() throws Exception {
        mockMvc.perform(get("/api/admin/users/{id}", UUID.randomUUID()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Usuario no encontrado"));
    }
}
