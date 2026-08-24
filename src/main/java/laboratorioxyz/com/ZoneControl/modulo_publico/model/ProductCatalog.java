package laboratorioxyz.com.ZoneControl.modulo_publico.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.UUID;

/**
 * Catálogo de productos farmacéuticos de la compañía.
 * Se expone en el módulo público sin autenticación.
 */
@Entity
@Table(name = "product_catalog")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductCatalog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Size(max = 80)
    @Column(length = 80, nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Size(max = 40)
    @Column(length = 40)
    private String activeIngredient;

    @Size(max = 40)
    @Column(length = 40)
    private String presentation;

    @Size(max = 30)
    @Column(length = 30)
    private String productionArea;

    /**
     * Ruta relativa del archivo de imagen bajo uploads/ (p. ej. products/PR-{id}.png).
     * Se expone al público mediante GET /api/public/catalogo/{id}/imagen.
     * Null si no tiene imagen.
     */
    @Column
    private String imageUrl;

    /**
     * Categoría comercial del producto para agrupar secciones en el landing.
     * Null si aún no fue clasificado.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private ProductCategory category;
}
