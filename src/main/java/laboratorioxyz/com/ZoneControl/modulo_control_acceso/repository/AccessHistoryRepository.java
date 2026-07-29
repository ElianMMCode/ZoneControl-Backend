package laboratorioxyz.com.ZoneControl.modulo_control_acceso.repository;

import laboratorioxyz.com.ZoneControl.modulo_control_acceso.model.AccessHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface AccessHistoryRepository extends JpaRepository<AccessHistory, UUID>,
        JpaSpecificationExecutor<AccessHistory> {

    @Query("SELECT h FROM AccessHistory h WHERE "
            + "EXTRACT(MONTH FROM h.timestamp) = :mes AND "
            + "EXTRACT(YEAR FROM h.timestamp) = :anio "
            + "ORDER BY h.timestamp DESC")
    List<AccessHistory> findByPeriod(@Param("mes") int mes, @Param("anio") int anio);
}
