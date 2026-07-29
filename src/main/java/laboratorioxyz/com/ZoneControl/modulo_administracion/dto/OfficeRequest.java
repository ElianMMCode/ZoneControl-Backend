package laboratorioxyz.com.ZoneControl.modulo_administracion.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record OfficeRequest(
    @NotBlank @Size(max = 30) String name,
    @NotBlank @Size(max = 50) String address,
    @Size(max = 30) String openingHours,
    Double latitude,
    Double longitude
) {}
