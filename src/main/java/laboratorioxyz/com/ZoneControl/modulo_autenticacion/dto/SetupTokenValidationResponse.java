package laboratorioxyz.com.ZoneControl.modulo_autenticacion.dto;

import java.util.UUID;

/**
 * DTO de salida para GET /setup-password?token=...
 * Confirma que el magic link es válido (no vencido ni usado) y devuelve
 * datos mínimos del usuario para mostrar la pantalla de configuración.
 */
public record SetupTokenValidationResponse(
        boolean valid,
        UUID userId,
        String fullName,
        String email
) {}
