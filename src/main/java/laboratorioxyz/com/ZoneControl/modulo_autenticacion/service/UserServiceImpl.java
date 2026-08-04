package laboratorioxyz.com.ZoneControl.modulo_autenticacion.service;

import laboratorioxyz.com.ZoneControl.model.enums.UserStatus;
import laboratorioxyz.com.ZoneControl.modulo_autenticacion.dto.UpdateProfileRequest;
import laboratorioxyz.com.ZoneControl.modulo_autenticacion.model.User;
import laboratorioxyz.com.ZoneControl.modulo_autenticacion.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void deactivateByEmployeeId(UUID employeeId) {
        userRepository.findByEmployee_Id(employeeId).ifPresent(user -> {
            user.setStatus(UserStatus.INACTIVO);
            userRepository.save(user);
            log.info("User {} deactivated due to employee {} status change", user.getId(), employeeId);
        });
    }

    @Override
    @Transactional
    public void reactivateByEmployeeId(UUID employeeId) {
        userRepository.findByEmployee_Id(employeeId).ifPresent(user -> {
            user.setStatus(UserStatus.ACTIVO);
            userRepository.save(user);
            log.info("User {} reactivated due to employee {} status change", user.getId(), employeeId);
        });
    }

    @Override
    @Transactional
    public Map<String, String> changePassword(UUID userId, String currentPassword, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Usuario no encontrado"));
        if (user.getPassword() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Debe configurar su contraseña mediante el enlace enviado a su correo");
        }
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "La contraseña actual es incorrecta");
        }
        if (currentPassword.equals(newPassword)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "La nueva contraseña no puede ser igual a la actual");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setRequirePasswordChange(false);
        userRepository.save(user);
        log.info("Password changed for user {}", userId);
        return Map.of("message", "Contraseña actualizada correctamente");
    }

    @Override
    @Transactional
    public Map<String, Object> updateProfile(UUID userId, UpdateProfileRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Usuario no encontrado"));

        if (!request.email().equals(user.getEmail()) && userRepository.existsByEmail(request.email())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "El email ya está registrado");
        }

        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        user.setEmail(request.email());
        userRepository.save(user);
        log.info("Profile updated for user {}", userId);

        return Map.of(
                "id", user.getId(),
                "firstName", user.getFirstName(),
                "lastName", user.getLastName(),
                "email", user.getEmail(),
                "role", user.getRole().name()
        );
    }
}
