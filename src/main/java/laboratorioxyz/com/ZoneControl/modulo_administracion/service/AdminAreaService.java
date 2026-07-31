package laboratorioxyz.com.ZoneControl.modulo_administracion.service;

import laboratorioxyz.com.ZoneControl.modulo_administracion.dto.AreaRequest;
import laboratorioxyz.com.ZoneControl.modulo_administracion.dto.AreaResponse;

import java.util.List;
import java.util.UUID;

public interface AdminAreaService {
    List<AreaResponse> list();
    AreaResponse create(AreaRequest request);
    AreaResponse update(UUID id, AreaRequest request);
    void setActive(UUID id, boolean active);
}
