package laboratorioxyz.com.ZoneControl.modulo_administracion.service;

import laboratorioxyz.com.ZoneControl.model.enums.Role;
import laboratorioxyz.com.ZoneControl.modulo_administracion.dto.RoleMatrixResponse;
import laboratorioxyz.com.ZoneControl.modulo_administracion.dto.RoleMatrixResponse.AccessLevel;
import laboratorioxyz.com.ZoneControl.modulo_administracion.dto.RoleMatrixResponse.RoleMatrixModule;
import org.springframework.stereotype.Service;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Matriz de roles reconstruida a partir de las reglas reales de
 * {@code SecurityConfig} (los matchers de cada módulo determinan qué rol
 * tiene acceso y a qué nivel: lectura o escritura). Debe mantenerse alineada
 * con SecurityConfig; se expone vía GET /api/admin/role-matrix para que la UI
 * la muestre "verdad por construcción".
 *
 * <p>Niveles de acceso por celda: NINGUNO, LECTURA (solo consulta) o
 * ESCRITURA. Por ejemplo, el SUPERVISOR_AUDITOR puede LECTURA en áreas,
 * cargos y catálogos de personal (los usa en el panel de zonas y reportes),
 * y ESCRITURA en control de acceso físico y reportes.</p>
 */
@Service
public class RoleMatrixServiceImpl implements RoleMatrixService {

    private static final List<Role> ROLES = List.of(
            Role.ADMIN, Role.GESTOR_PERSONAL, Role.SUPERVISOR_AUDITOR
    );

    private static Map<Role, AccessLevel> levels(Object... specs) {
        Map<Role, AccessLevel> map = new EnumMap<>(Role.class);
        for (Role r : ROLES) map.put(r, AccessLevel.NINGUNO);
        for (int i = 0; i < specs.length; i += 2) {
            map.put((Role) specs[i], (AccessLevel) specs[i + 1]);
        }
        return map;
    }

    private static AccessLevel lec() { return AccessLevel.LECTURA; }
    private static AccessLevel esc() { return AccessLevel.ESCRITURA; }

    private static final List<RoleMatrixModule> MODULES = List.of(
            new RoleMatrixModule("Usuarios del sistema", "group",
                    levels(Role.ADMIN, esc())),
            new RoleMatrixModule("Contenido público", "public",
                    levels(Role.ADMIN, esc())),
            new RoleMatrixModule("Áreas de producción", "domain",
                    levels(Role.ADMIN, esc(), Role.GESTOR_PERSONAL, esc(), Role.SUPERVISOR_AUDITOR, lec())),
            new RoleMatrixModule("Cargos", "badge",
                    levels(Role.ADMIN, esc(), Role.GESTOR_PERSONAL, lec(), Role.SUPERVISOR_AUDITOR, lec())),
            new RoleMatrixModule("Gestión de personal", "groups",
                    levels(Role.ADMIN, esc(), Role.GESTOR_PERSONAL, esc(), Role.SUPERVISOR_AUDITOR, lec())),
            new RoleMatrixModule("Permisos de acceso", "vpn_key",
                    levels(Role.ADMIN, esc(), Role.GESTOR_PERSONAL, esc(), Role.SUPERVISOR_AUDITOR, lec())),
            new RoleMatrixModule("Control de acceso físico", "meeting_room",
                    levels(Role.ADMIN, esc(), Role.SUPERVISOR_AUDITOR, esc())),
            new RoleMatrixModule("Reportes / Auditoría", "summarize",
                    levels(Role.ADMIN, esc(), Role.SUPERVISOR_AUDITOR, esc())),
            new RoleMatrixModule("Ajustes / Perfil", "settings",
                    levels(Role.ADMIN, esc(), Role.GESTOR_PERSONAL, esc(), Role.SUPERVISOR_AUDITOR, esc()))
    );

    @Override
    public RoleMatrixResponse getMatrix() {
        return new RoleMatrixResponse(ROLES, MODULES);
    }
}
