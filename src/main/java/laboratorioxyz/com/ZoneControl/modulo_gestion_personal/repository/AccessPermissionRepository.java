package laboratorioxyz.com.ZoneControl.modulo_gestion_personal.repository;

import laboratorioxyz.com.ZoneControl.model.enums.PermissionStatus;
import laboratorioxyz.com.ZoneControl.modulo_gestion_personal.model.AccessPermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

/**
 * Repositorio para la entidad AccessPermission.
 * JpaSpecificationExecutor habilita el listado paginado con filtros
 * dinámicos de la vista de Gestión de Permisos.
 * existsByEmployee_IdAndProductionArea_Id implementa la regla de unicidad
 * "un permiso por (empleado, área)" — cualquier permiso existente para
 * la misma combinación bloquea la creación de uno nuevo.
 */
public interface AccessPermissionRepository extends JpaRepository<AccessPermission, UUID>, JpaSpecificationExecutor<AccessPermission> {

    List<AccessPermission> findByEmployee_Id(UUID employeeId);

    @Modifying
    @Query("UPDATE AccessPermission ap SET ap.status = :status WHERE ap.employee.id = :employeeId")
    int updateStatusByEmployeeId(@Param("employeeId") UUID employeeId, @Param("status") PermissionStatus status);

    boolean existsByEmployee_IdAndProductionArea_Id(UUID employeeId, UUID productionAreaId);

    @Query("SELECT COUNT(ap) > 0 FROM AccessPermission ap "
            + "WHERE ap.employee.id = :employeeId AND ap.productionArea.id = :areaId "
            + "AND ap.status = 'ACTIVO' "
            + "AND ap.startDate <= :today AND ap.expirationDate >= :today "
            + "AND ap.startTime <= :now AND ap.endTime >= :now")
    boolean hasValidPermission(@Param("employeeId") UUID employeeId,
                                @Param("areaId") UUID areaId,
                                @Param("today") LocalDate today,
                                @Param("now") LocalTime now);

    long countByStatus(PermissionStatus status);

    long countByProductionArea_IdAndStatus(UUID areaId, PermissionStatus status);

    @Query("SELECT COUNT(DISTINCT ap.employee.id) FROM AccessPermission ap WHERE ap.status = 'ACTIVO'")
    long countDistinctEmployeesWithActivePermissions();
}
