package laboratorioxyz.com.ZoneControl.modulo_publico.repository;

import laboratorioxyz.com.ZoneControl.model.entity.Office;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface OfficeRepository extends JpaRepository<Office, UUID> {
}
