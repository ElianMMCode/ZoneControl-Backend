package laboratorioxyz.com.ZoneControl.modulo_gestion_personal.repository;

import laboratorioxyz.com.ZoneControl.modulo_gestion_personal.model.PermissionSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface PermissionScheduleRepository extends JpaRepository<PermissionSchedule, UUID> {

    List<PermissionSchedule> findByPermission_Id(UUID permissionId);

    boolean existsByPermission_Id(UUID permissionId);

    @Modifying
    @Query("DELETE FROM PermissionSchedule ps WHERE ps.permission.id = :permissionId")
    void deleteByPermission_Id(@Param("permissionId") UUID permissionId);
}
