package laboratorioxyz.com.ZoneControl.modulo_gestion_personal.service;

import laboratorioxyz.com.ZoneControl.modulo_gestion_personal.dto.PositionResponse;
import laboratorioxyz.com.ZoneControl.model.enums.Role;

import java.util.List;
import java.util.UUID;

public interface CargoService {

    List<PositionResponse> list();

    PositionResponse create(String name, Role systemRole);

    PositionResponse update(UUID id, String name, Role systemRole);

    void delete(UUID id);
}
