package laboratorioxyz.com.ZoneControl.modulo_gestion_personal.service;

import laboratorioxyz.com.ZoneControl.model.entity.Office;
import laboratorioxyz.com.ZoneControl.model.enums.EmployeeStatus;
import laboratorioxyz.com.ZoneControl.modulo_control_acceso.model.AccessHistory;
import laboratorioxyz.com.ZoneControl.modulo_gestion_personal.dto.BulkUploadResult;
import laboratorioxyz.com.ZoneControl.modulo_gestion_personal.dto.EmployeeSearchResponse;
import laboratorioxyz.com.ZoneControl.modulo_gestion_personal.dto.PermissionResponse;
import laboratorioxyz.com.ZoneControl.modulo_gestion_personal.dto.RegisterEmployeeRequest;
import laboratorioxyz.com.ZoneControl.modulo_gestion_personal.dto.RegisterEmployeeResponse;
import laboratorioxyz.com.ZoneControl.modulo_gestion_personal.dto.UpdateEmployeeRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

public interface EmployeeService {
    RegisterEmployeeResponse register(RegisterEmployeeRequest request);
    Page<EmployeeSearchResponse> search(String documentType, String documentNumber, String firstName,
                                        String lastName, String departmentName, EmployeeStatus status, Pageable pageable);
    EmployeeSearchResponse findById(UUID id);
    EmployeeSearchResponse update(UUID id, UpdateEmployeeRequest request);
    byte[] generateTemplate();
    BulkUploadResult processBulkUpload(MultipartFile file);
    List<String> listDepartmentNames();
    List<Office> listOffices();
    List<PermissionResponse> findPermissionsByEmployee(UUID employeeId);
    List<AccessHistory> findAccessHistoryByEmployee(UUID employeeId, int limit);
    EmployeeSearchResponse uploadPhoto(UUID id, MultipartFile file);
    byte[] loadPhoto(UUID id);
    EmployeeSearchResponse deletePhoto(UUID id);
}
