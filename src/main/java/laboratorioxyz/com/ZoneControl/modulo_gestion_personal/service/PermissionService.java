package laboratorioxyz.com.ZoneControl.modulo_gestion_personal.service;

import laboratorioxyz.com.ZoneControl.model.entity.ProductionArea;
import laboratorioxyz.com.ZoneControl.model.enums.PermissionStatus;
import laboratorioxyz.com.ZoneControl.modulo_gestion_personal.dto.CreatePermissionRequest;
import laboratorioxyz.com.ZoneControl.modulo_gestion_personal.dto.PermissionResponse;
import laboratorioxyz.com.ZoneControl.modulo_gestion_personal.dto.UpdatePermissionRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface PermissionService {
    PermissionResponse grant(CreatePermissionRequest request);
    Map<String, String> revoke(UUID id);
    PermissionResponse suspend(UUID id, LocalDate reactivationDate);
    PermissionResponse reactivate(UUID id);
    Page<PermissionResponse> list(String search, PermissionStatus status, Pageable pageable);
    List<ProductionArea> listAreas();
    ProductionArea createArea(String name, String description);
    ProductionArea updateArea(UUID id, String name, String description);
    void deleteArea(UUID id);
    PermissionResponse update(UUID id, UpdatePermissionRequest request);
}
