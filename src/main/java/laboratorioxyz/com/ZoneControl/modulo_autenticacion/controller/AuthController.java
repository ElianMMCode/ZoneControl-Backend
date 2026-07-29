package laboratorioxyz.com.ZoneControl.modulo_autenticacion.controller;

import jakarta.validation.Valid;
import laboratorioxyz.com.ZoneControl.modulo_autenticacion.dto.LoginRequest;
import laboratorioxyz.com.ZoneControl.modulo_autenticacion.dto.LoginResponse;
import laboratorioxyz.com.ZoneControl.modulo_autenticacion.model.User;
import laboratorioxyz.com.ZoneControl.modulo_autenticacion.repository.UserRepository;
import laboratorioxyz.com.ZoneControl.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;

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
}
