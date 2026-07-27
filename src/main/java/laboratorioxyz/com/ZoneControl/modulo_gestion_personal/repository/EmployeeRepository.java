package laboratorioxyz.com.ZoneControl.modulo_gestion_personal.repository;

import laboratorioxyz.com.ZoneControl.model.enums.DocumentType;
import laboratorioxyz.com.ZoneControl.modulo_gestion_personal.model.Employee;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface EmployeeRepository extends JpaRepository<Employee, UUID> {
    Optional<Employee> findByEmployeeCode(String employeeCode);
    boolean existsByEmployeeCode(String employeeCode);
    boolean existsByDocumentTypeAndDocumentNumber(DocumentType documentType, String documentNumber);
}
