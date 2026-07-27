package laboratorioxyz.com.ZoneControl.model.repository;

import laboratorioxyz.com.ZoneControl.model.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repositorio para la entidad Department.
 * Provee métodos de búsqueda por nombre, útil para validar unicidad
 * y para asignar empleados a departamentos desde el seed o la UI.
 */
public interface DepartmentRepository extends JpaRepository<Department, UUID> {
    Optional<Department> findByName(String name);
    boolean existsByName(String name);
}
