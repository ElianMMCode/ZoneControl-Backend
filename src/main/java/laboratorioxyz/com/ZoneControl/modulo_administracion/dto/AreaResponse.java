package laboratorioxyz.com.ZoneControl.modulo_administracion.dto;

import java.util.UUID;

public record AreaResponse(
        UUID id,
        String name,
        String description,
        boolean active
) {}
