package laboratorioxyz.com.ZoneControl.modulo_publico.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.Map;

/**
 * Respuesta del endpoint GET /public/institucional.
 * Contiene un mapa clave-valor con la misión, visión, descripción
 * de la empresa y las áreas de producción disponibles.
 */
@Data
@AllArgsConstructor
@Builder
public class InstitutionalResponse {
    private Map<String, String> info;
}
