package laboratorioxyz.com.ZoneControl.modulo_autenticacion.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import laboratorioxyz.com.ZoneControl.modulo_autenticacion.dto.ChangePasswordRequest;
import laboratorioxyz.com.ZoneControl.modulo_autenticacion.dto.LoginRequest;
import laboratorioxyz.com.ZoneControl.modulo_autenticacion.dto.LoginResponse;
import laboratorioxyz.com.ZoneControl.modulo_autenticacion.model.User;
import laboratorioxyz.com.ZoneControl.modulo_autenticacion.repository.UserRepository;
import laboratorioxyz.com.ZoneControl.modulo_autenticacion.service.UserService;
import laboratorioxyz.com.ZoneControl.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Módulo Autenticación", description = "Login JWT, cambio de contraseña y configuración inicial")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    private final UserService userService;

    @Operation(summary = "Iniciar sesión",
            description = "Autentica al usuario con email y contraseña y retorna un token JWT.")
    @ApiResponse(responseCode = "200", description = "Login exitoso, retorna token y datos del usuario")
    @ApiResponse(responseCode = "401", description = "Credenciales incorrectas")
    @ApiResponse(responseCode = "403", description = "Cuenta desactivada")
    @ApiResponse(responseCode = "404", description = "Usuario no registrado")
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Usuario no registrado"));

        if (user.getStatus() != laboratorioxyz.com.ZoneControl.model.enums.UserStatus.ACTIVO) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Cuenta desactivada, contacte al administrador");
        }

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.email(), request.password())
            );
        } catch (BadCredentialsException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                    "Credenciales incorrectas");
        }

        String token = jwtTokenProvider.generateToken(
                user.getId().toString(),
                user.getEmail(),
                user.getRole().name()
        );

        LoginResponse.Usuario usuario = new LoginResponse.Usuario(
                user.getId(),
                user.getFirstName() + " " + user.getLastName(),
                user.getEmail(),
                user.getRole().name()
        );

        return ResponseEntity.ok(new LoginResponse(token, usuario, user.isRequirePasswordChange()));
    }

    @Operation(summary = "Cambiar contraseña propia",
            description = "Permite al usuario autenticado cambiar su contraseña actual por una nueva. " +
                    "Valida la contraseña actual, que la nueva cumpla los requisitos de seguridad y " +
                    "que no sea igual a la actual.")
    @ApiResponse(responseCode = "200", description = "Contraseña actualizada correctamente")
    @ApiResponse(responseCode = "400", description = "Contraseña actual incorrecta, nueva no cumple requisitos o igual a la actual")
    @ApiResponse(responseCode = "401", description = "Token JWT inválido o expirado")
    @ApiResponse(responseCode = "404", description = "Usuario no encontrado")
    @PostMapping("/change-password")
    public ResponseEntity<Map<String, String>> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        String currentUserId = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.ok(userService.changePassword(UUID.fromString(currentUserId), request.currentPassword(), request.newPassword()));
    }
}
