package laboratorioxyz.com.ZoneControl.modulo_autenticacion.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Notificador de magic links (HU-05).
 *
 * Cuando el ADMIN crea un usuario, este servicio construye el enlace de
 * configuración con el token de un solo uso y lo "envía" al correo del
 * empleado.
 *
 * Por qué se elige magic link sobre enviar la contraseña en texto plano:
 * la contraseña nunca viaja por canales no seguros ni es conocida por el
 * administrador; el usuario elige la suya propia al hacer clic en el enlace.
 * Esto cumple la recomendación NIST SP 800-63B de no enviar secrets por email.
 *
 * NOTA: Por ahora solo se registra el enlace en el log (debug del flujo).
 * Cuando se configure un servidor SMTP real, se debe reemplazar esta
 * implementación por un envío con JavaMailSender sin cambiar la firma.
 */
@Service
@Slf4j
public class MagicLinkNotifier {

    @Value("${app.app-url:http://localhost:5173}")
    private String appUrl;

    public void sendSetupLink(String email, String fullName, String rawToken) {
        String url = buildUrl(rawToken);
        log.info("[MAGIC LINK] Para: {} ({}). Enlace de configuración (expira en 24h): {}", email, fullName, url);
        // TODO: reemplazar por JavaMailSender cuando exista SMTP configurado.
        // No enviar la contraseña en texto plano; solo el enlace con el token.
    }

    /**
     * Construye la URL completa del enlace de configuración. Se expone para que
     * los servicios la devuelvan en la respuesta (demo sin SMTP): el admin puede
     * abrir la vista de configuración en una nueva ventana.
     */
    public String buildUrl(String rawToken) {
        return appUrl + "/configurar-contrasena?token=" + rawToken;
    }
}
