package laboratorioxyz.com.ZoneControl.modulo_administracion.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ProductRequest(
    @NotBlank @Size(max = 80) String name,
    @Size(max = 1000) String description,
    @Size(max = 40) String activeIngredient,
    @Size(max = 40) String presentation,
    @Size(max = 30) String productionArea
) {}
