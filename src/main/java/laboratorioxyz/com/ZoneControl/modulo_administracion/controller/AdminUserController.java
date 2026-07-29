package laboratorioxyz.com.ZoneControl.modulo_administracion.controller;

import jakarta.validation.Valid;
import laboratorioxyz.com.ZoneControl.model.enums.EmployeeStatus;
import laboratorioxyz.com.ZoneControl.model.enums.PermissionStatus;
import laboratorioxyz.com.ZoneControl.model.enums.UserStatus;
import laboratorioxyz.com.ZoneControl.modulo_administracion.dto.StatusUpdateRequest;
import laboratorioxyz.com.ZoneControl.modulo_autenticacion.dto.CreateUserRequest;
import laboratorioxyz.com.ZoneControl.modulo_autenticacion.model.User;
import laboratorioxyz.com.ZoneControl.modulo_autenticacion.repository.UserRepository;
import laboratorioxyz.com.ZoneControl.modulo_gestion_personal.model.AccessPermission;
import laboratorioxyz.com.ZoneControl.modulo_gestion_personal.model.Employee;
import laboratorioxyz.com.ZoneControl.modulo_gestion_personal.repository.AccessPermissionRepository;
import laboratorioxyz.com.ZoneControl.modulo_gestion_personal.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/admin/users")
@RequiredArgsConstructor
@Slf4j
public class AdminUserController {

    private final UserRepository userRepository;
    private final EmployeeRepository employeeRepository;
    private final AccessPermissionRepository accessPermissionRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    @PostMapping
    public ResponseEntity<Map<String, UUID>> create(@Valid @RequestBody CreateUserRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El email ya está registrado");
        }

        Employee employee = employeeRepository.findByEmployeeCode(request.getEmployeeCode())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Empleado no encontrado: " + request.getEmployeeCode()));

        if (userRepository.findByEmployee_Id(employee.getId()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "El empleado ya tiene un usuario de sistema asociado");
        }

        User user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .status(UserStatus.ACTIVO)
                .requirePasswordChange(true)
                .employee(employee)
                .build();

        user = userRepository.save(user);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("id", user.getId()));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<Map<String, Object>> updateStatus(
            @PathVariable UUID id,
            @Valid @RequestBody StatusUpdateRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Usuario no encontrado"));

        String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                        "No se pudo verificar el administrador actual"));

        UserStatus newStatus = UserStatus.valueOf(request.status().toUpperCase());

        if (newStatus == UserStatus.INACTIVO && currentUser.getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "No puedes desactivar tu propia cuenta");
        }

        user.setStatus(newStatus);
        userRepository.save(user);

        Employee employee = user.getEmployee();
        employee.setStatus(newStatus == UserStatus.INACTIVO
                ? EmployeeStatus.INACTIVO : EmployeeStatus.ACTIVO);
        employeeRepository.save(employee);

        var permissions = accessPermissionRepository.findByEmployee_Id(employee.getId());
        PermissionStatus permStatus = newStatus == UserStatus.INACTIVO
                ? PermissionStatus.SUSPENDIDO : PermissionStatus.ACTIVO;
        permissions.forEach(p -> p.setStatus(permStatus));
        if (!permissions.isEmpty()) {
            accessPermissionRepository.saveAll(permissions);
        }

        log.info("User {} status changed to {} by admin {}", id, newStatus, currentUserEmail);

        return ResponseEntity.ok(Map.of(
                "id", user.getId(),
                "status", user.getStatus().name(),
                "employeeStatus", employee.getStatus().name()
        ));
    }
}
