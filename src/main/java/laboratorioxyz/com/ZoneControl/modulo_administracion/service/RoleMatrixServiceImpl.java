package laboratorioxyz.com.ZoneControl.modulo_administracion.service;

import laboratorioxyz.com.ZoneControl.model.enums.Role;
import laboratorioxyz.com.ZoneControl.modulo_administracion.dto.RoleMatrixResponse;
import laboratorioxyz.com.ZoneControl.modulo_administracion.dto.RoleMatrixResponse.RoleMatrixModule;
import org.springframework.stereotype.Service;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Matriz de roles reconstruida a partir de las reglas reales de
 * {@code SecurityConfig} (los matchers de cada módulo determinan qué rol
 * tiene acceso). Debe mantenerse alineada con SecurityConfig; se expone vía
 * GET /api/admin/role-matrix para que la UI la muestre "verdad por
 * construcción".
 */
@Service
public class RoleMatrixServiceImpl implements RoleMatrixService {

    private static final List<Role> ROLES = List.of(
            Role.ADMIN, Role.GESTOR_PERSONAL, Role.SUPERVISOR_AUDITOR
    );

    private static Map<Role, Boolean> access(Role... allowed) {
        Map<Role, Boolean> map = new EnumMap<>(Role.class);
        for (Role r : ROLES) map.put(r, false);
        for (Role r : allowed) map.put(r, true);
        return map;
    }

    private static final List<RoleMatrixModule> MODULES = List.of(
            new RoleMatrixModule("Usuarios del sistema", "group", access(Role.ADMIN)),
            new RoleMatrixModule("Contenido público", "public", access(Role.ADMIN)),
            new RoleMatrixModule("Áreas de producción", "domain", access(Role.ADMIN, Role.GESTOR_PERSONAL)),
            new RoleMatrixModule("Gestión de personal", "badge", access(Role.ADMIN, Role.GESTOR_PERSONAL)),
            new RoleMatrixModule("Permisos de acceso", "vpn_key", access(Role.ADMIN, Role.GESTOR_PERSONAL)),
            new RoleMatrixModule("Control de acceso físico", "meeting_room", access(Role.ADMIN, Role.SUPERVISOR_AUDITOR)),
            new RoleMatrixModule("Reportes / Auditoría", "summarize", access(Role.ADMIN, Role.SUPERVISOR_AUDITOR)),
            new RoleMatrixModule("Ajustes / Perfil", "settings", access(ROLES.toArray(new Role[0])))
    );

    @Override
    public RoleMatrixResponse getMatrix() {
        return new RoleMatrixResponse(ROLES, MODULES);
    }
}
