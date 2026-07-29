package laboratorioxyz.com.ZoneControl.modulo_administracion.dto;

import jakarta.validation.constraints.NotBlank;

public record StatusUpdateRequest(
    @NotBlank String status
) {}
