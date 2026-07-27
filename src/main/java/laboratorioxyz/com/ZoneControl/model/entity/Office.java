package laboratorioxyz.com.ZoneControl.model.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.UUID;

/**
 * Sedes físicas de la compañía farmacéutica.
 * Información de ubicación y horarios para el módulo público.
 */
@Entity
@Table(name = "offices")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Office {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Size(max = 30)
    @Column(length = 30, nullable = false)
    private String name;

    @Size(max = 50)
    @Column(length = 50, nullable = false)
    private String address;

    @Size(max = 30)
    @Column(length = 30)
    private String openingHours;

    private Double latitude;

    private Double longitude;
}
