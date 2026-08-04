package laboratorioxyz.com.ZoneControl.modulo_control_acceso.repository;

import laboratorioxyz.com.ZoneControl.modulo_control_acceso.model.AccessAlert;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AccessAlertRepository extends JpaRepository<AccessAlert, UUID> {
}
