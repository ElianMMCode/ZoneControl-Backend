package laboratorioxyz.com.ZoneControl.modulo_autenticacion.service;

import laboratorioxyz.com.ZoneControl.modulo_autenticacion.dto.UpdateProfileRequest;

import java.util.Map;
import java.util.UUID;

public interface UserService {
    void deactivateByEmployeeId(UUID employeeId);
    void reactivateByEmployeeId(UUID employeeId);
    Map<String, String> changePassword(UUID userId, String currentPassword, String newPassword);
    Map<String, Object> updateProfile(UUID userId, UpdateProfileRequest request);
}
