package laboratorioxyz.com.ZoneControl.modulo_administracion.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import laboratorioxyz.com.ZoneControl.model.entity.Department;
import laboratorioxyz.com.ZoneControl.model.enums.DocumentType;
import laboratorioxyz.com.ZoneControl.model.enums.EmployeeStatus;
import laboratorioxyz.com.ZoneControl.model.enums.Role;
import laboratorioxyz.com.ZoneControl.model.enums.UserStatus;
import laboratorioxyz.com.ZoneControl.model.repository.DepartmentRepository;
import laboratorioxyz.com.ZoneControl.modulo_autenticacion.model.User;
import laboratorioxyz.com.ZoneControl.modulo_autenticacion.repository.UserRepository;
import laboratorioxyz.com.ZoneControl.modulo_gestion_personal.model.Employee;
import laboratorioxyz.com.ZoneControl.modulo_gestion_personal.repository.EmployeeRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests for the admin dashboard support endpoints:
 * - {@code pendientesConfiguracion=true} filter on GET /api/admin/users
 * - GET /api/admin/users/candidatos (employees eligible for user activation)
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@Rollback
@WithMockUser(username = "admin@zonecontrol.com", roles = "ADMIN")
class AdminDashboardDataTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private EmployeeRepository employeeRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private DepartmentRepository departmentRepository;
    @Autowired private BCryptPasswordEncoder passwordEncoder;

    private Employee newEmployee(String code, String doc, String email, Role systemRole, boolean withUser) {
        Department dept = departmentRepository.findByName("Control de Calidad").orElseThrow();
        Employee employee = Employee.builder()
                .employeeCode(code)
                .documentType(DocumentType.CC)
                .documentNumber(doc)
                .firstName("Test")
                .lastName(code)
                .position("Coordinador de Personal")
                .department(dept)
                .status(EmployeeStatus.ACTIVO)
                .email(email)
                .systemRole(systemRole)
                .build();
        employee = employeeRepository.save(employee);
        if (withUser) {
            User user = User.builder()
                    .firstName("Test")
                    .lastName(code)
                    .email(email)
                    .password(passwordEncoder.encode("Pass123!"))
                    .role(Role.GESTOR_PERSONAL)
                    .status(UserStatus.ACTIVO)
                    .requirePasswordChange(false)
                    .employee(employee)
                    .build();
            userRepository.save(user);
        }
        return employee;
    }

    @Test
    void listUsers_pendientesConfiguracionTrue_returnsOnlyUsersWithSetupToken() throws Exception {
        Employee noToken = newEmployee("EMP-DASH-01", "800000001", "dash01@test.com", Role.GESTOR_PERSONAL, true);
        Employee withToken = newEmployee("EMP-DASH-02", "800000002", "dash02@test.com", Role.GESTOR_PERSONAL, true);
        withToken.setEmail("dash02@test.com");
        employeeRepository.save(withToken);
        User u = userRepository.findByEmployee_Id(withToken.getId()).orElseThrow();
        u.setSetupToken("deadbeef");
        u.setSetupTokenExpiry(java.time.LocalDateTime.now().plusHours(1));
        u.setPassword(null);
        userRepository.save(u);

        mockMvc.perform(get("/api/admin/users").param("pendientesConfiguracion", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[?(@.employeeCode=='EMP-DASH-02')]").exists())
                .andExpect(jsonPath("$.content[?(@.employeeCode=='EMP-DASH-01')]").doesNotExist());
    }

    @Test
    void listUsers_pendientesConfiguracionFalse_omitsFilter() throws Exception {
        Employee noToken = newEmployee("EMP-DASH-03", "800000003", "dash03@test.com", Role.GESTOR_PERSONAL, true);
        mockMvc.perform(get("/api/admin/users").param("pendientesConfiguracion", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[?(@.employeeCode=='EMP-DASH-03')]").exists());
    }

    @Test
    void candidatos_returnsOnlyEmployeesWithSystemRoleAndEmailAndNoUser() throws Exception {
        // válido: systemRole + email + sin usuario
        Employee valid = newEmployee("EMP-DASH-C1", "800000010", "dashc1@test.com", Role.GESTOR_PERSONAL, false);
        // no candidato: sin systemRole
        newEmployee("EMP-DASH-C2", "800000011", "dashc2@test.com", null, false);
        // no candidato: sin email
        newEmployee("EMP-DASH-C3", "800000012", null, Role.SUPERVISOR_AUDITOR, false);
        // no candidato: ya tiene usuario
        newEmployee("EMP-DASH-C4", "800000013", "dashc4@test.com", Role.ADMIN, true);

        mockMvc.perform(get("/api/admin/users/candidatos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[?(@.employeeCode=='EMP-DASH-C1')]").exists())
                .andExpect(jsonPath("$.content[?(@.employeeCode=='EMP-DASH-C2')]").doesNotExist())
                .andExpect(jsonPath("$.content[?(@.employeeCode=='EMP-DASH-C3')]").doesNotExist())
                .andExpect(jsonPath("$.content[?(@.employeeCode=='EMP-DASH-C4')]").doesNotExist());
    }

    @Test
    void candidatos_includesEligibleEmployeesFromDatabase() throws Exception {
        newEmployee("EMP-DASH-C5", "800000015", "dashc5@test.com", Role.GESTOR_PERSONAL, false);
        newEmployee("EMP-DASH-C6", "800000016", "dashc6@test.com", Role.SUPERVISOR_AUDITOR, false);

        mockMvc.perform(get("/api/admin/users/candidatos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[?(@.employeeCode=='EMP-DASH-C5')]").exists())
                .andExpect(jsonPath("$.content[?(@.employeeCode=='EMP-DASH-C6')]").exists());
    }
}
