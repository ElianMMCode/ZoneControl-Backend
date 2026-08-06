package laboratorioxyz.com.ZoneControl.modulo_gestion_personal.dto;

import laboratorioxyz.com.ZoneControl.model.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

/** Cargo del catálogo con el rol de sistema que define (si lo tiene). */
@Data
@AllArgsConstructor
@Builder
public class PositionResponse {
    private UUID id;
    private String name;
    private Role systemRole;
}
