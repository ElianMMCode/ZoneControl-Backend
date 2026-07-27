package laboratorioxyz.com.ZoneControl.model.repository;

import laboratorioxyz.com.ZoneControl.model.entity.ProductionArea;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repositorio para la entidad ProductionArea.
 * Provee métodos de búsqueda por nombre para validar unicidad
 * y para asignar áreas a permisos de acceso.
 */
public interface ProductionAreaRepository extends JpaRepository<ProductionArea, UUID> {
    Optional<ProductionArea> findByName(String name);
    boolean existsByName(String name);
}
