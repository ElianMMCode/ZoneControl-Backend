package laboratorioxyz.com.ZoneControl.modulo_publico.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.Map;

/**
 * Respuesta del endpoint GET /public/contacto.
 * Contiene un mapa clave-valor con teléfono, correo electrónico
 * y redes sociales de Laboratorio XYZ.
 */
@Data
@AllArgsConstructor
@Builder
public class ContactResponse {
    private Map<String, String> contact;
}
