package laboratorioxyz.com.ZoneControl.modulo_administracion.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AreaRequest(
        @NotBlank @Size(max = 30) String name,
        @Size(max = 200) String description
) {}
