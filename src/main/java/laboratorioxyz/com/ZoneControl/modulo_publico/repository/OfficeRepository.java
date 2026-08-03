package laboratorioxyz.com.ZoneControl.modulo_publico.repository;

import laboratorioxyz.com.ZoneControl.model.entity.Office;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repositorio para la entidad Office (sedes).
 * Expone el listado completo de sedes para el módulo público
 * y la búsqueda por nombre (necesaria para asignar la sede base
 * de un empleado desde el módulo de gestión de personal).
 */
public interface OfficeRepository extends JpaRepository<Office, UUID> {

    Optional<Office> findByName(String name);

    List<Office> findAllByOrderByNameAsc();
}
