package laboratorioxyz.com.ZoneControl.modulo_administracion.service;

import jakarta.persistence.criteria.Predicate;
import laboratorioxyz.com.ZoneControl.model.enums.EmployeeStatus;
import laboratorioxyz.com.ZoneControl.model.enums.PermissionStatus;
import laboratorioxyz.com.ZoneControl.model.enums.Role;
import laboratorioxyz.com.ZoneControl.model.enums.UserStatus;
import laboratorioxyz.com.ZoneControl.modulo_administracion.dto.AdminStatsResponse;
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
import laboratorioxyz.com.ZoneControl.modulo_gestion_personal.dto.EmployeeSearchResponse;
import laboratorioxyz.com.ZoneControl.modulo_gestion_personal.repository.AccessPermissionRepository;
import laboratorioxyz.com.ZoneControl.modulo_gestion_personal.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

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
    public Map<String, Object> create(CreateUserRequest request) {
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
        return Map.of(
                "id", user.getId(),
                "setupUrl", magicLinkNotifier.buildUrl(rawToken)
        );
    }

    @Override
    @Transactional
    public Map<String, Object> updateStatus(UUID id, StatusUpdateRequest request, String currentUserEmail) {
        User user = findUserOrThrow(id);
        User currentUser = findCurrentAdmin(currentUserEmail);
        UserStatus newStatus = UserStatus.valueOf(request.status().toUpperCase());
        applyStatusChange(user, newStatus, currentUser);
        log.info("User {} status changed to {} by admin {}", id, newStatus, currentUserEmail);

        return Map.of(
                "id", user.getId(),
                "status", user.getStatus().name(),
                "employeeStatus", user.getEmployee().getStatus().name()
        );
    }

    @Override
    @Transactional
    public Map<String, Object> update(UUID id, UpdateUserRequest request, String currentUserEmail) {
        User user = findUserOrThrow(id);
        User currentUser = findCurrentAdmin(currentUserEmail);

        if (!request.email().equals(user.getEmail())) {
            if (userRepository.existsByEmail(request.email())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "El email ya está registrado");
            }
            user.setEmail(request.email());
        }

        applyStatusChange(user, request.status(), currentUser);
        userRepository.save(user);
        log.info("User {} updated", id);

        return Map.of(
                "id", user.getId(),
                "firstName", user.getFirstName(),
                "lastName", user.getLastName(),
                "email", user.getEmail(),
                "role", user.getRole().name(),
                "status", user.getStatus().name()
        );
    }

    private User findUserOrThrow(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Usuario no encontrado"));
    }

    private User findCurrentAdmin(String currentUserEmail) {
        return userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                        "No se pudo verificar el administrador actual"));
    }

    /**
     * Cambia el estado del usuario y aplica la cascada al empleado y a sus
     * permisos de acceso (INACTIVO → empleado INACTIVO + permisos SUSPENDIDO;
     * ACTIVO → restaura). Rechaza desactivar la propia cuenta.
     */
    private void applyStatusChange(User user, UserStatus newStatus, User currentUser) {
        if (newStatus == UserStatus.INACTIVO && currentUser.getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "No puede desactivar su propia cuenta");
        }
        user.setStatus(newStatus);

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
    }

    /**
     * Restablecimiento de contraseña con magic link (HU-08).
     *
     * Por qué magic link en lugar de contraseña temporal visible al admin:
     * mantener la misma política de seguridad que la creación de usuarios
     * (HU-05) — la contraseña nunca es conocida por el administrador ni viaja
     * por email. Se invalida la contraseña actual, se genera un nuevo
     * setupToken de un solo uso con expiración de 24h y se envía el enlace al
     * correo personal del empleado.
     */
    @Override
    @Transactional
    public ResetPasswordResponse resetPassword(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Usuario no encontrado"));

        Employee employee = user.getEmployee();
        if (employee.getEmail() == null || employee.getEmail().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "El empleado no tiene un correo registrado. Regístrelo en Gestión Personal para restablecer la contraseña");
        }

        String rawToken = setupPasswordService.generateRawToken();
        user.setSetupToken(setupPasswordService.hashToken(rawToken));
        user.setSetupTokenExpiry(LocalDateTime.now().plusHours(24));
        user.setPassword(null);
        user.setRequirePasswordChange(false);
        userRepository.save(user);

        magicLinkNotifier.sendSetupLink(employee.getEmail(),
                user.getFirstName() + " " + user.getLastName(), rawToken);
        log.info("Password reset for user {} (magic link enviado)", id);
        return new ResetPasswordResponse(
                "Enlace de configuración enviado al correo del usuario",
                magicLinkNotifier.buildUrl(rawToken)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserResponse> list(String search, Role role, UserStatus status,
                                    Boolean pendientesConfiguracion, Pageable pageable) {
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
            if (Boolean.TRUE.equals(pendientesConfiguracion)) {
                predicate = cb.and(predicate, cb.isNotNull(root.get("setupToken")));
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

    @Override
    @Transactional(readOnly = true)
    public AdminStatsResponse getStats() {
        return new AdminStatsResponse(
                userRepository.count(),
                userRepository.countByStatus(UserStatus.ACTIVO),
                userRepository.countByStatus(UserStatus.INACTIVO),
                userRepository.countBySetupTokenIsNotNull(),
                employeeRepository.count(),
                employeeRepository.countByStatus(EmployeeStatus.ACTIVO),
                accessPermissionRepository.count(),
                accessPermissionRepository.countByStatus(PermissionStatus.ACTIVO),
                accessPermissionRepository.countByStatus(PermissionStatus.SUSPENDIDO)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public Page<EmployeeSearchResponse> listActivationCandidates(Pageable pageable) {
        return employeeRepository.findActivationCandidates(pageable)
                .map(this::toEmployeeResponse);
    }

    private EmployeeSearchResponse toEmployeeResponse(Employee employee) {
        return EmployeeSearchResponse.builder()
                .id(employee.getId())
                .employeeCode(employee.getEmployeeCode())
                .documentType(employee.getDocumentType())
                .documentNumber(employee.getDocumentNumber())
                .firstName(employee.getFirstName())
                .lastName(employee.getLastName())
                .position(employee.getPosition())
                .email(employee.getEmail())
                .departmentName(employee.getDepartment().getName())
                .status(employee.getStatus())
                .systemRole(employee.getSystemRole())
                .build();
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
                .pendienteActivacion(user.getSetupToken() != null)
                .employeeCode(user.getEmployee().getEmployeeCode())
                .position(user.getEmployee().getPosition())
                .build();
    }
}
