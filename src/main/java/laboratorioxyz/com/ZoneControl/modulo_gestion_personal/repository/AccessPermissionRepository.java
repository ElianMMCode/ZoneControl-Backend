package laboratorioxyz.com.ZoneControl.modulo_gestion_personal.repository;

import laboratorioxyz.com.ZoneControl.model.enums.PermissionStatus;
import laboratorioxyz.com.ZoneControl.modulo_gestion_personal.model.AccessPermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

public interface AccessPermissionRepository extends JpaRepository<AccessPermission, UUID> {

    List<AccessPermission> findByEmployee_Id(UUID employeeId);

    @Modifying
    @Query("UPDATE AccessPermission ap SET ap.status = :status WHERE ap.employee.id = :employeeId")
    int updateStatusByEmployeeId(@Param("employeeId") UUID employeeId, @Param("status") PermissionStatus status);

    boolean existsByEmployee_IdAndProductionArea_IdAndStartTimeAndEndTimeAndStatus(
            UUID employeeId, UUID productionAreaId, LocalTime startTime, LocalTime endTime, PermissionStatus status);

    @Query("SELECT COUNT(ap) > 0 FROM AccessPermission ap "
            + "WHERE ap.employee.id = :employeeId AND ap.productionArea.id = :areaId "
            + "AND ap.status = 'ACTIVO' "
            + "AND ap.startDate <= :today AND ap.expirationDate >= :today "
            + "AND ap.startTime <= :now AND ap.endTime >= :now")
    boolean hasValidPermission(@Param("employeeId") UUID employeeId,
                                @Param("areaId") UUID areaId,
                                @Param("today") LocalDate today,
                                @Param("now") LocalTime now);
}
