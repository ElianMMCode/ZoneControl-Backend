package laboratorioxyz.com.ZoneControl.modulo_publico.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class CatalogResponse {
    private String name;
    private String description;
    private String activeIngredient;
    private String presentation;
    private String productionArea;
}
