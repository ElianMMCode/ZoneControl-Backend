package laboratorioxyz.com.ZoneControl.modulo_administracion.dto;

import laboratorioxyz.com.ZoneControl.model.enums.Role;

import java.util.List;
import java.util.Map;

/**
 * Matriz de roles y permisos (HU-27 / §9 item 1.5). Solo lectura: refleja
 * las reglas de acceso definidas en SecurityConfig; no hay enforcement en BD.
 * Cada celda indica el nivel de acceso del rol sobre el módulo:
 * NINGUNO, LECTURA (solo consulta) o ESCRITURA.
 */
public record RoleMatrixResponse(
        List<Role> roles,
        List<RoleMatrixModule> modules
) {

    public enum AccessLevel { NINGUNO, LECTURA, ESCRITURA }

    public record RoleMatrixModule(
            String module,
            String icon,
            Map<Role, AccessLevel> access
    ) {}
}
