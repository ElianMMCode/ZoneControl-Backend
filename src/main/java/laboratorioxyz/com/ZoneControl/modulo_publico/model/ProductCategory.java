package laboratorioxyz.com.ZoneControl.modulo_publico.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.UUID;

/**
 * Categoría del catálogo de productos (p. ej. Analgésicos, Cardiovasculares).
 * Tabla propia gestionada por el administrador; los productos la referencian.
 */
@Entity
@Table(name = "product_category")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Size(max = 80)
    @Column(length = 80, nullable = false, unique = true)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;
}
