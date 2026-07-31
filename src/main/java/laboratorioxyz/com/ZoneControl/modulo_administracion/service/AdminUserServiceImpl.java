package laboratorioxyz.com.ZoneControl.modulo_administracion.service;

import jakarta.persistence.criteria.Predicate;
import laboratorioxyz.com.ZoneControl.model.enums.EmployeeStatus;
import laboratorioxyz.com.ZoneControl.model.enums.PermissionStatus;
import laboratorioxyz.com.ZoneControl.model.enums.Role;
import laboratorioxyz.com.ZoneControl.model.enums.UserStatus;
import laboratorioxyz.com.ZoneControl.modulo_administracion.dto.ResetPasswordResponse;
import laboratorioxyz.com.ZoneControl.modulo_administracion.dto.StatusUpdateRequest;
import laboratorioxyz.com.ZoneControl.modulo_administracion.dto.UpdateUserRequest;
import laboratorioxyz.com.ZoneControl.modulo_administracion.dto.UserResponse;
import laboratorioxyz.com.ZoneControl.modulo_autenticacion.dto.CreateUserRequest;
import laboratorioxyz.com.ZoneControl.modulo_autenticacion.model.User;
import laboratorioxyz.com.ZoneControl.modulo_autenticacion.repository.UserRepository;
import laboratorioxyz.com.ZoneControl.modulo_autenticacion.service.MagicLinkNotifier;
import laboratorioxyz.com.ZoneControl.modulo_autenticacion.service.SetupPasswordService;
import laboratorioxyz.com.ZoneControl.modulo_gestion_personal.model.Employee;
import laboratorioxyz.com.ZoneControl.modulo_gestion_personal.repository.AccessPermissionRepository;
import laboratorioxyz.com.ZoneControl.modulo_gestion_personal.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.security.SecureRandom;
import java.time.LocalDateTime;
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
    private final SetupPasswordService setupPasswordService;
    private final MagicLinkNotifier magicLinkNotifier;

    /**
     * Creación de usuario con magic link (HU-05).
     *
     * El ADMIN solo envía employeeCode, role y status. El firstName, lastName
     * y email se derivan del Employee vinculado (el email es el correo personal
     * del empleado, no corporativo). No se define contraseña: se genera un
     * setupToken de un solo uso, se guarda su hash SHA-256 con expiración de
     * 24h y se envía un magic link al correo del empleado para que el propio
     * usuario establezca su contraseña. De esta forma la contraseña nunca
     * viaja por email ni es conocida por el administrador.
     */
    @Override
    @Transactional
    public Map<String, UUID> create(CreateUserRequest request) {
        Employee employee = employeeRepository.findByEmployeeCode(request.getEmployeeCode())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Empleado no encontrado: " + request.getEmployeeCode()));

        if (userRepository.findByEmployee_Id(employee.getId()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "El empleado ya tiene un usuario de sistema asociado");
        }

        if (employee.getEmail() == null || employee.getEmail().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "El empleado no tiene un correo registrado. Regístrelo en Gestión Personal para poder crear el usuario");
        }

        if (userRepository.existsByEmail(employee.getEmail())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "El email ingresado ya se encuentra registrado en el sistema");
        }

        String rawToken = setupPasswordService.generateRawToken();
        User user = User.builder()
                .firstName(employee.getFirstName())
                .lastName(employee.getLastName())
                .email(employee.getEmail())
                .password(null)
                .setupToken(setupPasswordService.hashToken(rawToken))
                .setupTokenExpiry(LocalDateTime.now().plusHours(24))
                .role(request.getRole())
                .status(request.getStatus())
                .employee(employee)
                .build();

        user = userRepository.save(user);
        magicLinkNotifier.sendSetupLink(user.getEmail(),
                user.getFirstName() + " " + user.getLastName(), rawToken);
        log.info("User {} created for employee {} (magic link enviado)", user.getId(), request.getEmployeeCode());
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

    @Override
    @Transactional(readOnly = true)
    public Page<UserResponse> list(String search, Role role, UserStatus status, Pageable pageable) {
        Specification<User> spec = (root, query, cb) -> {
            Predicate predicate = cb.conjunction();
            if (search != null && !search.isBlank()) {
                String like = "%" + search.toLowerCase() + "%";
                Predicate nameMatch = cb.or(
                        cb.like(cb.lower(root.get("firstName")), like),
                        cb.like(cb.lower(root.get("lastName")), like),
                        cb.like(cb.lower(root.get("email")), like),
                        cb.like(cb.lower(root.get("employee").get("employeeCode")), like)
                );
                predicate = cb.and(predicate, nameMatch);
            }
            if (role != null) {
                predicate = cb.and(predicate, cb.equal(root.get("role"), role));
            }
            if (status != null) {
                predicate = cb.and(predicate, cb.equal(root.get("status"), status));
            }
            return predicate;
        };
        return userRepository.findAll(spec, pageable).map(this::toUserResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getById(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Usuario no encontrado"));
        return toUserResponse(user);
    }

    private UserResponse toUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .role(user.getRole())
                .status(user.getStatus())
                .requirePasswordChange(user.isRequirePasswordChange())
                .employeeCode(user.getEmployee().getEmployeeCode())
                .position(user.getEmployee().getPosition())
                .build();
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
