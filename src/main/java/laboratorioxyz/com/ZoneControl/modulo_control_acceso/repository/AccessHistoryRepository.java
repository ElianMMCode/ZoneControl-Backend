package laboratorioxyz.com.ZoneControl.modulo_control_acceso.repository;

import laboratorioxyz.com.ZoneControl.modulo_control_acceso.model.AccessHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

/**
 * Repositorio para la entidad AccessHistory (historial de accesos).
 * Se expandirá con consultas para filtros por fecha, empleado,
 * departamento y resultado (HU-15).
 */
public interface AccessHistoryRepository extends JpaRepository<AccessHistory, UUID> {
}
