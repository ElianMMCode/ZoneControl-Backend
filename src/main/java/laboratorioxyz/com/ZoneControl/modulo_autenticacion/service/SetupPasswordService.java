package laboratorioxyz.com.ZoneControl.modulo_autenticacion.service;

import laboratorioxyz.com.ZoneControl.modulo_autenticacion.dto.SetupTokenValidationResponse;
import laboratorioxyz.com.ZoneControl.modulo_autenticacion.model.User;
import laboratorioxyz.com.ZoneControl.modulo_autenticacion.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.HexFormat;

/**
 * Flujo de configuración de contraseña por magic link (HU-05).
 *
 * El ADMIN crea el usuario con un setupToken crudo aleatorio; aquí se
 * valida el token que el usuario recibe por email y se guarda su nueva
 * contraseña (encriptada con BCrypt).
 *
 * El token se hashea con SHA-256 para permitir búsqueda directa por hash
 * (findBySetupToken) sin recorrer la tabla completa. Como el token tiene
 * alta entropía (64 caracteres aleatorios), SHA-256 es suficiente y no
 * requiere un KDF lento como BCrypt.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SetupPasswordService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    private static final SecureRandom RANDOM = new SecureRandom();

    public String generateRawToken() {
        byte[] bytes = new byte[48];
        RANDOM.nextBytes(bytes);
        return HexFormat.of().formatHex(bytes);
    }

    public String hashToken(String rawToken) {
        return sha256Hex(rawToken);
    }

    private String sha256Hex(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(value.getBytes(java.nio.charset.StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 no disponible", e);
        }
    }

    @Transactional(readOnly = true)
    public SetupTokenValidationResponse validateToken(String rawToken) {
        User user = findByTokenOrThrow(rawToken);
        return new SetupTokenValidationResponse(
                true,
                user.getId(),
                user.getFirstName() + " " + user.getLastName(),
                user.getEmail()
        );
    }

    @Transactional
    public void completeSetup(String rawToken, String newPassword) {
        User user = findByTokenOrThrow(rawToken);
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setSetupToken(null);
        user.setSetupTokenExpiry(null);
        user.setRequirePasswordChange(false);
        userRepository.save(user);
        log.info("Password configured via magic link for user {}", user.getId());
    }

    private User findByTokenOrThrow(String rawToken) {
        String hash = sha256Hex(rawToken);
        User user = userRepository.findBySetupToken(hash)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "El enlace de configuración es inválido. Solicite un nuevo enlace al administrador"));

        if (user.getSetupTokenExpiry() == null
                || user.getSetupTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new ResponseStatusException(HttpStatus.GONE,
                    "El enlace de configuración ha expirado. Contacte al administrador para generar un nuevo enlace");
        }
        return user;
    }
}
