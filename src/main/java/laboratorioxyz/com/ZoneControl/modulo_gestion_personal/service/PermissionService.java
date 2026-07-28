package laboratorioxyz.com.ZoneControl.modulo_gestion_personal.service;

import laboratorioxyz.com.ZoneControl.modulo_gestion_personal.dto.CreatePermissionRequest;
import laboratorioxyz.com.ZoneControl.modulo_gestion_personal.dto.PermissionResponse;

import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

public interface PermissionService {
    PermissionResponse grant(CreatePermissionRequest request);
    Map<String, String> revoke(UUID id);
    PermissionResponse suspend(UUID id, LocalDate reactivationDate);
}
