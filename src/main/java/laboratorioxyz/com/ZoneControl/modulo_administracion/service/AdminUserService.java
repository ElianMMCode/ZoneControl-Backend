package laboratorioxyz.com.ZoneControl.modulo_administracion.service;

import laboratorioxyz.com.ZoneControl.modulo_administracion.dto.AdminStatsResponse;
import laboratorioxyz.com.ZoneControl.modulo_administracion.dto.ResetPasswordResponse;
import laboratorioxyz.com.ZoneControl.modulo_administracion.dto.StatusUpdateRequest;
import laboratorioxyz.com.ZoneControl.modulo_administracion.dto.UpdateUserRequest;
import laboratorioxyz.com.ZoneControl.modulo_administracion.dto.UserResponse;
import laboratorioxyz.com.ZoneControl.modulo_autenticacion.dto.CreateUserRequest;
import laboratorioxyz.com.ZoneControl.model.enums.Role;
import laboratorioxyz.com.ZoneControl.model.enums.UserStatus;
import laboratorioxyz.com.ZoneControl.modulo_gestion_personal.dto.EmployeeSearchResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Map;
import java.util.UUID;

public interface AdminUserService {
    Map<String, UUID> create(CreateUserRequest request);
    Map<String, Object> updateStatus(UUID id, StatusUpdateRequest request, String currentUserEmail);
    Map<String, Object> update(UUID id, UpdateUserRequest request);
    ResetPasswordResponse resetPassword(UUID id);
    Page<UserResponse> list(String search, Role role, UserStatus status, Boolean pendientesConfiguracion, Pageable pageable);
    UserResponse getById(UUID id);
    AdminStatsResponse getStats();
    Page<EmployeeSearchResponse> listActivationCandidates(Pageable pageable);
}
