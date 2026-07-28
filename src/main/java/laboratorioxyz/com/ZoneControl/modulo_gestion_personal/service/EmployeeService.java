package laboratorioxyz.com.ZoneControl.modulo_gestion_personal.service;

import laboratorioxyz.com.ZoneControl.model.enums.EmployeeStatus;
import laboratorioxyz.com.ZoneControl.modulo_gestion_personal.dto.EmployeeSearchResponse;
import laboratorioxyz.com.ZoneControl.modulo_gestion_personal.dto.RegisterEmployeeRequest;
import laboratorioxyz.com.ZoneControl.modulo_gestion_personal.dto.RegisterEmployeeResponse;
import laboratorioxyz.com.ZoneControl.modulo_gestion_personal.dto.UpdateEmployeeRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface EmployeeService {
    RegisterEmployeeResponse register(RegisterEmployeeRequest request);
    Page<EmployeeSearchResponse> search(String documentType, String documentNumber, String firstName,
                                        String lastName, UUID departmentId, EmployeeStatus status, Pageable pageable);
    EmployeeSearchResponse findById(UUID id);
    EmployeeSearchResponse update(UUID id, UpdateEmployeeRequest request);
}
