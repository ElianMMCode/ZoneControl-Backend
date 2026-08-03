package laboratorioxyz.com.ZoneControl.modulo_autenticacion.controller;

import jakarta.validation.Valid;
import laboratorioxyz.com.ZoneControl.modulo_autenticacion.dto.SetupPasswordRequest;
import laboratorioxyz.com.ZoneControl.modulo_autenticacion.dto.SetupTokenValidationResponse;
import laboratorioxyz.com.ZoneControl.modulo_autenticacion.service.SetupPasswordService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Controlador del flujo de configuración de contraseña por magic link (HU-05).
 * Endpoints públicos (sin autenticación): el usuario aún no tiene credenciales
 * cuando hace clic en el enlace recibido por email.
 */
@RestController
@RequestMapping("/api/setup-password")
@RequiredArgsConstructor
public class SetupPasswordController {

    private final SetupPasswordService setupPasswordService;

    @GetMapping
    public ResponseEntity<SetupTokenValidationResponse> validateToken(@RequestParam("token") String token) {
        return ResponseEntity.ok(setupPasswordService.validateToken(token));
    }

    @PostMapping
    public ResponseEntity<Map<String, String>> completeSetup(
            @Valid @RequestBody SetupPasswordRequest request) {
        setupPasswordService.completeSetup(request.token(), request.newPassword());
        return ResponseEntity.ok(Map.of("message",
                "Contraseña configurada exitosamente. Ya puede iniciar sesión."));
    }
}
