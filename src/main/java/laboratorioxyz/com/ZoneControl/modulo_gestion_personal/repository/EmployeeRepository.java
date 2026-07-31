package laboratorioxyz.com.ZoneControl.modulo_gestion_personal.repository;

import laboratorioxyz.com.ZoneControl.model.enums.DocumentType;
import laboratorioxyz.com.ZoneControl.model.enums.EmployeeStatus;
import laboratorioxyz.com.ZoneControl.modulo_gestion_personal.model.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

/**
 * Repositorio para la entidad Employee.
 * Provee métodos para validar unicidad de documento de identidad
 * (tipo + número) y para generar el código EMP-XXXXXX secuencial.
 */
public interface EmployeeRepository extends JpaRepository<Employee, UUID>, JpaSpecificationExecutor<Employee> {
    Optional<Employee> findByEmployeeCode(String employeeCode);
    boolean existsByEmployeeCode(String employeeCode);
    boolean existsByDocumentTypeAndDocumentNumber(DocumentType documentType, String documentNumber);

    /**
     * Obtiene el código de empleado más alto en la BD.
     * Se usa para generar el siguiente código EMP-XXXXXX secuencialmente.
     * Ej: si el máximo es EMP-000005, el siguiente será EMP-000006.
     * Se eligió esta estrategia sobre una tabla de secuencia separada
     * porque es más simple y el volumen de inserción no justifica
     * la sobrecarga de una secuencia dedicada.
     */
    @Query("SELECT MAX(e.employeeCode) FROM Employee e")
    String findMaxEmployeeCode();

    long countByStatus(EmployeeStatus status);
}
