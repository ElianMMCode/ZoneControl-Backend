package laboratorioxyz.com.ZoneControl.modulo_gestion_personal.repository;

import laboratorioxyz.com.ZoneControl.modulo_gestion_personal.model.AccessPermission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AccessPermissionRepository extends JpaRepository<AccessPermission, UUID> {
}
