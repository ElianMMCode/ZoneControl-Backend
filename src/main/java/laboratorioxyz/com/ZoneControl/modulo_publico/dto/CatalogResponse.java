package laboratorioxyz.com.ZoneControl.modulo_publico.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

/**
 * Respuesta individual en GET /public/catalogo.
 * Representa un producto farmacéutico con sus datos principales.
 * productionArea se almacena como String desnormalizado porque
 * en el catálogo público solo se muestra el nombre del área,
 * sin necesidad de relación con la entidad ProductionArea.
 * El id se incluye para que el panel admin pueda referenciar el
 * producto al editar/eliminar.
 */
@Data
@AllArgsConstructor
@Builder
public class CatalogResponse {
    private UUID id;
    private String name;
    private String description;
    private String activeIngredient;
    private String presentation;
    private String productionArea;
    private UUID categoryId;
    private String categoryName;
    private String imageUrl;
}
