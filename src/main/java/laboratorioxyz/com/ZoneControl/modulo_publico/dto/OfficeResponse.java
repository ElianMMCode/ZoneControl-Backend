package laboratorioxyz.com.ZoneControl.modulo_publico.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

/**
 * Respuesta individual en GET /public/sedes.
 * Representa una sede física con su ubicación y horario.
 * latitud/longitud permiten integrar mapas en el frontend.
 */
@Data
@AllArgsConstructor
@Builder
public class OfficeResponse {
    private String name;
    private String address;
    private String openingHours;
    private Double latitude;
    private Double longitude;
}
