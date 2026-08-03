package laboratorioxyz.com.ZoneControl.modulo_administracion.controller;

import laboratorioxyz.com.ZoneControl.model.entity.Department;
import laboratorioxyz.com.ZoneControl.model.entity.ProductionArea;
import laboratorioxyz.com.ZoneControl.model.enums.DocumentType;
import laboratorioxyz.com.ZoneControl.model.enums.EmployeeStatus;
import laboratorioxyz.com.ZoneControl.model.enums.PermissionStatus;
import laboratorioxyz.com.ZoneControl.model.enums.Role;
import laboratorioxyz.com.ZoneControl.model.enums.UserStatus;
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
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@Rollback
@WithMockUser(username = "admin@zonecontrol.com", roles = "ADMIN")
class AdminStatsControllerTest {

    @Autowired
    private MockMvc mockMvc;

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

    @Test
    void getStats_returnsCounts() throws Exception {
        long baseUsers = userRepository.count();
        long baseActiveUsers = userRepository.countByStatus(UserStatus.ACTIVO);
        long baseInactiveUsers = userRepository.countByStatus(UserStatus.INACTIVO);
        long basePendingSetup = userRepository.countBySetupTokenIsNotNull();
        long baseEmployees = employeeRepository.count();
        long baseActiveEmployees = employeeRepository.countByStatus(EmployeeStatus.ACTIVO);
        long basePermissions = accessPermissionRepository.count();
        long baseActivePermissions = accessPermissionRepository.countByStatus(PermissionStatus.ACTIVO);
        long baseSuspendedPermissions = accessPermissionRepository.countByStatus(PermissionStatus.SUSPENDIDO);

        Department dept = departmentRepository.findByName("Control de Calidad").orElseThrow();
        ProductionArea area = productionAreaRepository.findByName("Sala Blanca A").orElseThrow();

        Employee activeEmployee = employeeRepository.save(Employee.builder()
                .employeeCode("EMP-STA-01")
                .documentType(DocumentType.CC)
                .documentNumber("910000001")
                .firstName("Stats")
                .lastName("Uno")
                .position("Analista")
                .department(dept)
                .status(EmployeeStatus.ACTIVO)
                .email("stats.uno@test.com")
                .build());
        Employee inactiveEmployee = employeeRepository.save(Employee.builder()
                .employeeCode("EMP-STA-02")
                .documentType(DocumentType.CC)
                .documentNumber("910000002")
                .firstName("Stats")
                .lastName("Dos")
                .position("Auxiliar")
                .department(dept)
                .status(EmployeeStatus.INACTIVO)
                .email("stats.dos@test.com")
                .build());

        userRepository.save(User.builder()
                .firstName("Stats")
                .lastName("Uno")
                .email("stats.uno@test.com")
                .password(passwordEncoder.encode("Pass123!"))
                .role(Role.GESTOR_PERSONAL)
                .status(UserStatus.ACTIVO)
                .setupToken("setup-pending-hash")
                .setupTokenExpiry(LocalDateTime.now().plusHours(1))
                .employee(activeEmployee)
                .build());
        userRepository.save(User.builder()
                .firstName("Stats")
                .lastName("Dos")
                .email("stats.dos@test.com")
                .password(passwordEncoder.encode("Pass123!"))
                .role(Role.GESTOR_PERSONAL)
                .status(UserStatus.INACTIVO)
                .employee(inactiveEmployee)
                .build());

        accessPermissionRepository.save(AccessPermission.builder()
                .employee(activeEmployee)
                .productionArea(area)
                .status(PermissionStatus.ACTIVO)
                .startDate(LocalDate.now())
                .expirationDate(LocalDate.now().plusMonths(1))
                .startTime(LocalTime.of(8, 0))
                .endTime(LocalTime.of(17, 0))
                .build());
        accessPermissionRepository.save(AccessPermission.builder()
                .employee(activeEmployee)
                .productionArea(area)
                .status(PermissionStatus.SUSPENDIDO)
                .startDate(LocalDate.now())
                .expirationDate(LocalDate.now().plusMonths(1))
                .startTime(LocalTime.of(8, 0))
                .endTime(LocalTime.of(17, 0))
                .build());

        mockMvc.perform(get("/api/admin/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalUsuarios").value(baseUsers + 2))
                .andExpect(jsonPath("$.usuariosActivos").value(baseActiveUsers + 1))
                .andExpect(jsonPath("$.usuariosInactivos").value(baseInactiveUsers + 1))
                .andExpect(jsonPath("$.usuariosSinConfiguracion").value(basePendingSetup + 1))
                .andExpect(jsonPath("$.totalEmpleados").value(baseEmployees + 2))
                .andExpect(jsonPath("$.empleadosActivos").value(baseActiveEmployees + 1))
                .andExpect(jsonPath("$.totalPermisos").value(basePermissions + 2))
                .andExpect(jsonPath("$.permisosActivos").value(baseActivePermissions + 1))
                .andExpect(jsonPath("$.permisosSuspendidos").value(baseSuspendedPermissions + 1));
    }
}
