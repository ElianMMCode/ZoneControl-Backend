package laboratorioxyz.com.ZoneControl.modulo_publico.repository;

import laboratorioxyz.com.ZoneControl.model.entity.Office;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

/**
 * Repositorio para la entidad Office (sedes).
 * Expone el listado completo de sedes para el módulo público.
 */
public interface OfficeRepository extends JpaRepository<Office, UUID> {
}
