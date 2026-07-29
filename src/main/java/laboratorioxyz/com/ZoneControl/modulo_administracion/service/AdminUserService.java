package laboratorioxyz.com.ZoneControl.modulo_administracion.service;

import laboratorioxyz.com.ZoneControl.modulo_administracion.dto.ResetPasswordResponse;
import laboratorioxyz.com.ZoneControl.modulo_administracion.dto.StatusUpdateRequest;
import laboratorioxyz.com.ZoneControl.modulo_administracion.dto.UpdateUserRequest;
import laboratorioxyz.com.ZoneControl.modulo_autenticacion.dto.CreateUserRequest;

import java.util.Map;
import java.util.UUID;

public interface AdminUserService {
    Map<String, UUID> create(CreateUserRequest request);
    Map<String, Object> updateStatus(UUID id, StatusUpdateRequest request, String currentUserEmail);
    Map<String, Object> update(UUID id, UpdateUserRequest request);
    ResetPasswordResponse resetPassword(UUID id);
}
