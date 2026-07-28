package laboratorioxyz.com.ZoneControl.modulo_gestion_personal.repository;

import laboratorioxyz.com.ZoneControl.model.enums.PermissionStatus;
import laboratorioxyz.com.ZoneControl.modulo_gestion_personal.model.AccessPermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface AccessPermissionRepository extends JpaRepository<AccessPermission, UUID> {
    @Modifying
    @Query("UPDATE AccessPermission ap SET ap.status = :status WHERE ap.employee.id = :employeeId")
    int updateStatusByEmployeeId(@Param("employeeId") UUID employeeId, @Param("status") PermissionStatus status);
}
