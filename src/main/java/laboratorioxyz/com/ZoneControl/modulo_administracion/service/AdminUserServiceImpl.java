package laboratorioxyz.com.ZoneControl.modulo_administracion.service;

import laboratorioxyz.com.ZoneControl.model.enums.EmployeeStatus;
import laboratorioxyz.com.ZoneControl.model.enums.PermissionStatus;
import laboratorioxyz.com.ZoneControl.model.enums.UserStatus;
import laboratorioxyz.com.ZoneControl.modulo_administracion.dto.ResetPasswordResponse;
import laboratorioxyz.com.ZoneControl.modulo_administracion.dto.StatusUpdateRequest;
import laboratorioxyz.com.ZoneControl.modulo_administracion.dto.UpdateUserRequest;
import laboratorioxyz.com.ZoneControl.modulo_autenticacion.dto.CreateUserRequest;
import laboratorioxyz.com.ZoneControl.modulo_autenticacion.model.User;
import laboratorioxyz.com.ZoneControl.modulo_autenticacion.repository.UserRepository;
import laboratorioxyz.com.ZoneControl.modulo_gestion_personal.model.Employee;
import laboratorioxyz.com.ZoneControl.modulo_gestion_personal.repository.AccessPermissionRepository;
import laboratorioxyz.com.ZoneControl.modulo_gestion_personal.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.security.SecureRandom;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminUserServiceImpl implements AdminUserService {

    private final UserRepository userRepository;
    private final EmployeeRepository employeeRepository;
    private final AccessPermissionRepository accessPermissionRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public Map<String, UUID> create(CreateUserRequest request) {
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
        log.info("User {} created for employee {}", user.getId(), request.getEmployeeCode());
        return Map.of("id", user.getId());
    }

    @Override
    @Transactional
    public Map<String, Object> updateStatus(UUID id, StatusUpdateRequest request, String currentUserEmail) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Usuario no encontrado"));

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

        return Map.of(
                "id", user.getId(),
                "status", user.getStatus().name(),
                "employeeStatus", employee.getStatus().name()
        );
    }

    @Override
    @Transactional
    public Map<String, Object> update(UUID id, UpdateUserRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Usuario no encontrado"));

        if (request.email() != null && !request.email().equals(user.getEmail())) {
            if (userRepository.existsByEmail(request.email())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "El email ya está registrado");
            }
            user.setEmail(request.email());
        }
        if (request.firstName() != null) {
            user.setFirstName(request.firstName());
        }
        if (request.lastName() != null) {
            user.setLastName(request.lastName());
        }
        if (request.role() != null) {
            user.setRole(request.role());
        }
        if (request.employeeCode() != null) {
            Employee employee = employeeRepository.findByEmployeeCode(request.employeeCode())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            "Empleado no encontrado: " + request.employeeCode()));
            if (userRepository.findByEmployee_Id(employee.getId())
                    .filter(existing -> !existing.getId().equals(user.getId())).isPresent()) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "El empleado ya tiene un usuario de sistema asociado");
            }
            user.setEmployee(employee);
        }

        userRepository.save(user);
        log.info("User {} updated", id);

        return Map.of(
                "id", user.getId(),
                "firstName", user.getFirstName(),
                "lastName", user.getLastName(),
                "email", user.getEmail(),
                "role", user.getRole().name()
        );
    }

    @Override
    @Transactional
    public ResetPasswordResponse resetPassword(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Usuario no encontrado"));

        String tempPassword = generateTemporaryPassword();
        user.setPassword(passwordEncoder.encode(tempPassword));
        user.setRequirePasswordChange(true);
        userRepository.save(user);

        log.info("Password reset for user {}", id);
        return new ResetPasswordResponse(tempPassword);
    }

    private static final String UPPER = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String LOWER = "abcdefghijklmnopqrstuvwxyz";
    private static final String DIGITS = "0123456789";
    private static final String SPECIAL = "@$!%*?&";
    private static final String ALL = UPPER + LOWER + DIGITS + SPECIAL;
    private static final SecureRandom RANDOM = new SecureRandom();

    private String generateTemporaryPassword() {
        StringBuilder password = new StringBuilder();
        password.append(UPPER.charAt(RANDOM.nextInt(UPPER.length())));
        password.append(LOWER.charAt(RANDOM.nextInt(LOWER.length())));
        password.append(DIGITS.charAt(RANDOM.nextInt(DIGITS.length())));
        password.append(SPECIAL.charAt(RANDOM.nextInt(SPECIAL.length())));
        for (int i = 4; i < 12; i++) {
            password.append(ALL.charAt(RANDOM.nextInt(ALL.length())));
        }
        char[] chars = password.toString().toCharArray();
        for (int i = chars.length - 1; i > 0; i--) {
            int j = RANDOM.nextInt(i + 1);
            char tmp = chars[i];
            chars[i] = chars[j];
            chars[j] = tmp;
        }
        return new String(chars);
    }
}
