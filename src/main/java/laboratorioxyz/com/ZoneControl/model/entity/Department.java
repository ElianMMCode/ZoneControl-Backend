package laboratorioxyz.com.ZoneControl.model.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.UUID;

/**
 * Departamentos de producción de la compañía farmacéutica.
 * Cada empleado pertenece a un departamento.
 */
@Entity
@Table(name = "departments")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Department {

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
