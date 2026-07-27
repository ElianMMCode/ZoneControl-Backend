package laboratorioxyz.com.ZoneControl.modulo_gestion_personal.repository;

import laboratorioxyz.com.ZoneControl.modulo_gestion_personal.model.AccessPermission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

/**
 * Repositorio para la entidad AccessPermission (permisos de acceso a áreas).
 * Se expandirá con consultas para validar conflictos de horario
 * y filtrar permisos vigentes por empleado (HU-11, HU-18).
 */
public interface AccessPermissionRepository extends JpaRepository<AccessPermission, UUID> {
}
