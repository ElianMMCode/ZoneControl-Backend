package laboratorioxyz.com.ZoneControl.modulo_control_acceso.repository;

import laboratorioxyz.com.ZoneControl.modulo_control_acceso.model.AccessSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AccessSessionRepository extends JpaRepository<AccessSession, UUID> {

    Optional<AccessSession> findByEmployee_IdAndProductionArea_IdAndExitTimeIsNull(UUID employeeId, UUID areaId);

    List<AccessSession> findByExitTimeIsNull();
}
