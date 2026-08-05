package laboratorioxyz.com.ZoneControl.modulo_control_acceso.repository;

import laboratorioxyz.com.ZoneControl.model.enums.AccessResult;
import laboratorioxyz.com.ZoneControl.modulo_control_acceso.model.AccessHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface AccessHistoryRepository extends JpaRepository<AccessHistory, UUID>,
        JpaSpecificationExecutor<AccessHistory> {

    Page<AccessHistory> findByEmployee_IdOrderByTimestampDesc(UUID employeeId, Pageable pageable);

    @Query("SELECT h FROM AccessHistory h WHERE "
            + "EXTRACT(MONTH FROM h.timestamp) = :mes AND "
            + "EXTRACT(YEAR FROM h.timestamp) = :anio "
            + "ORDER BY h.timestamp DESC")
    List<AccessHistory> findByPeriod(@Param("mes") int mes, @Param("anio") int anio);

    @Query("SELECT COUNT(h) FROM AccessHistory h WHERE CAST(h.timestamp AS date) = CURRENT_DATE AND h.result <> :result")
    long countTodayByResultIsNot(@Param("result") AccessResult result);

    @Query("SELECT COUNT(h) FROM AccessHistory h WHERE CAST(h.timestamp AS date) = CURRENT_DATE AND h.result = :result")
    long countTodayByResult(@Param("result") AccessResult result);

    @Query("SELECT COUNT(h) FROM AccessHistory h WHERE h.employee.id = :employeeId "
            + "AND h.result = :result AND h.timestamp >= :since")
    long countByEmployeeAndResultSince(@Param("employeeId") UUID employeeId,
                                       @Param("result") AccessResult result,
                                       @Param("since") java.time.LocalDateTime since);
}
