package laboratorioxyz.com.ZoneControl.security;

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
import org.springframework.test.annotation.Rollback;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * HU-07 gap (1.6 §9): al desactivar un usuario, sus tokens JWT dejan de ser
 * válidos inmediatamente (401).
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@Rollback
class JwtInvalidationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Test
    void deactivatedUser_tokenIsRejected_returns401() throws Exception {
        Department dept = departmentRepository.findByName("Control de Calidad").orElseThrow();
        Employee emp = employeeRepository.save(Employee.builder()
                .employeeCode("EMP-JWT-01")
                .documentType(DocumentType.CC)
                .documentNumber("710000001")
                .firstName("Jwt")
                .lastName("Test")
                .position("Analista")
                .department(dept)
                .status(EmployeeStatus.ACTIVO)
                .email("jwt.test@zonecontrol.com")
                .build());
        User user = userRepository.save(User.builder()
                .firstName("Jwt")
                .lastName("Test")
                .email("jwt.test@zonecontrol.com")
                .role(Role.SUPERVISOR_AUDITOR)
                .status(UserStatus.ACTIVO)
                .requirePasswordChange(false)
                .employee(emp)
                .build());

        String token = jwtTokenProvider.generateToken(
                user.getId().toString(), user.getEmail(), user.getRole().name());

        // Usuario activo → el token funciona.
        mockMvc.perform(get("/api/historial/stats").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        // Se desactiva el usuario.
        user.setStatus(UserStatus.INACTIVO);
        userRepository.save(user);

        // El mismo token ya no es válido → 401.
        mockMvc.perform(get("/api/historial/stats").header("Authorization", "Bearer " + token))
                .andExpect(status().isUnauthorized());
    }
}
