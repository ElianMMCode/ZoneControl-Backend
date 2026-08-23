package laboratorioxyz.com.ZoneControl.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import laboratorioxyz.com.ZoneControl.modulo_gestion_personal.model.Employee;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
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

    /**
     * Ruta relativa del archivo de imagen bajo uploads/ (p. ej. offices/OF-{id}.png).
     * Se expone al público mediante GET /api/public/sedes/{id}/imagen.
     * Null si no tiene imagen.
     */
    @Column
    private String imageUrl;

    @OneToMany(mappedBy = "baseOffice")
    @JsonIgnore
    @Builder.Default
    private List<Employee> employees = new ArrayList<>();
}
