package laboratorioxyz.com.ZoneControl.model.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.UUID;

/**
 * Áreas restringidas de producción a las que se controla el acceso.
 * Ej: Sala Blanca A, Sala Blanca B, Laboratorio QC, etc.
 */
@Entity
@Table(name = "production_areas")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductionArea {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Size(max = 30)
    @Column(length = 30, unique = true, nullable = false)
    private String name;

    @Size(max = 200)
    @Column(length = 200)
    private String description;
}
