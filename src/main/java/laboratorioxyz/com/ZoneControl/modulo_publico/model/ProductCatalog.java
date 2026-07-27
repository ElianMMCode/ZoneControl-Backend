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
}
