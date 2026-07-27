package laboratorioxyz.com.ZoneControl.modulo_control_acceso.repository;

import laboratorioxyz.com.ZoneControl.modulo_control_acceso.model.AccessHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AccessHistoryRepository extends JpaRepository<AccessHistory, UUID> {
}
