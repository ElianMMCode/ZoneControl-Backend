package laboratorioxyz.com.ZoneControl.modulo_publico.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

/**
 * Respuesta individual en GET /public/catalogo.
 * Representa un producto farmacéutico con sus datos principales.
 * productionArea se almacena como String desnormalizado porque
 * en el catálogo público solo se muestra el nombre del área,
 * sin necesidad de relación con la entidad ProductionArea.
 */
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
