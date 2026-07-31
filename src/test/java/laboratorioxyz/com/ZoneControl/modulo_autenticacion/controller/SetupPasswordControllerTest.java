package laboratorioxyz.com.ZoneControl.modulo_autenticacion.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import laboratorioxyz.com.ZoneControl.model.entity.Department;
import laboratorioxyz.com.ZoneControl.model.enums.DocumentType;
import laboratorioxyz.com.ZoneControl.model.enums.EmployeeStatus;
import laboratorioxyz.com.ZoneControl.model.enums.Role;
import laboratorioxyz.com.ZoneControl.model.enums.UserStatus;
import laboratorioxyz.com.ZoneControl.model.repository.DepartmentRepository;
import laboratorioxyz.com.ZoneControl.modulo_autenticacion.model.User;
import laboratorioxyz.com.ZoneControl.modulo_autenticacion.repository.UserRepository;
import laboratorioxyz.com.ZoneControl.modulo_autenticacion.service.SetupPasswordService;
import laboratorioxyz.com.ZoneControl.modulo_gestion_personal.model.Employee;
import laboratorioxyz.com.ZoneControl.modulo_gestion_personal.repository.EmployeeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@Rollback
class SetupPasswordControllerTest {

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
    private SetupPasswordService setupPasswordService;

    private User userWithToken;
    private String rawToken;

    @BeforeEach
    void setUp() {
        Department dept = departmentRepository.findByName("Control de Calidad").orElseThrow();
        Employee employee = employeeRepository.save(Employee.builder()
                .employeeCode("EMP-SET-01")
                .documentType(DocumentType.CC)
                .documentNumber("900000100")
                .firstName("Set")
                .lastName("Up")
                .email("setup@test.com")
                .position("Analista")
                .department(dept)
                .status(EmployeeStatus.ACTIVO)
                .build());

        rawToken = setupPasswordService.generateRawToken();
        userWithToken = userRepository.save(User.builder()
                .firstName("Set")
                .lastName("Up")
                .email("setup@test.com")
                .role(Role.SUPERVISOR_AUDITOR)
                .status(UserStatus.ACTIVO)
                .employee(employee)
                .setupToken(setupPasswordService.hashToken(rawToken))
                .setupTokenExpiry(LocalDateTime.now().plusHours(24))
                .build());
    }

    @Test
    void validateToken_validToken_returns200() throws Exception {
        mockMvc.perform(get("/setup-password").param("token", rawToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid").value(true))
                .andExpect(jsonPath("$.userId").value(userWithToken.getId().toString()))
                .andExpect(jsonPath("$.fullName").value("Set Up"))
                .andExpect(jsonPath("$.email").value("setup@test.com"));
    }

    @Test
    void validateToken_invalidToken_returns404() throws Exception {
        mockMvc.perform(get("/setup-password").param("token", "token-invalido"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error")
                        .value("El enlace de configuración es inválido. Solicite un nuevo enlace al administrador"));
    }

    @Test
    void validateToken_expiredToken_returns410() throws Exception {
        userWithToken.setSetupTokenExpiry(LocalDateTime.now().minusMinutes(1));
        userRepository.save(userWithToken);

        mockMvc.perform(get("/setup-password").param("token", rawToken))
                .andExpect(status().isGone())
                .andExpect(jsonPath("$.error")
                        .value("El enlace de configuración ha expirado. Contacte al administrador para generar un nuevo enlace"));
    }

    @Test
    void completeSetup_validToken_setsPasswordAndClearsToken() throws Exception {
        mockMvc.perform(post("/setup-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "token", rawToken,
                                "newPassword", "NuevaPass1!"
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message")
                        .value("Contraseña configurada exitosamente. Ya puede iniciar sesión."));

        assertThat(userRepository.findById(userWithToken.getId()))
                .hasValueSatisfying(u -> {
                    assertThat(u.getPassword()).isNotBlank();
                    assertThat(u.getPassword()).doesNotContain("NuevaPass1!");
                    assertThat(u.getSetupToken()).isNull();
                    assertThat(u.getSetupTokenExpiry()).isNull();
                    assertThat(u.isRequirePasswordChange()).isFalse();
                });
    }

    @Test
    void completeSetup_weakPassword_returns400() throws Exception {
        mockMvc.perform(post("/setup-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "token", rawToken,
                                "newPassword", "corta"
                        ))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void completeSetup_invalidToken_returns404() throws Exception {
        mockMvc.perform(post("/setup-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "token", UUID.randomUUID().toString(),
                                "newPassword", "NuevaPass1!"
                        ))))
                .andExpect(status().isNotFound());
    }
}
