package laboratorioxyz.com.ZoneControl.modulo_publico.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

/**
 * Respuesta individual en GET /public/sedes.
 * Representa una sede física con su ubicación y horario.
 * latitud/longitud permiten integrar mapas en el frontend.
 * El id se incluye para que el panel admin pueda referenciar la sede
 * al editar/eliminar (los GET públicos son la única fuente de
 * lectura disponible, ya que no hay un GET admin con id).
 */
@Data
@AllArgsConstructor
@Builder
public class OfficeResponse {
    private UUID id;
    private String name;
    private String address;
    private String openingHours;
    private Double latitude;
    private Double longitude;
    private String imageUrl;
}
