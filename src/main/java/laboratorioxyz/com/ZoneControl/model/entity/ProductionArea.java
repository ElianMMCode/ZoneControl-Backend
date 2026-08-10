package laboratorioxyz.com.ZoneControl.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import laboratorioxyz.com.ZoneControl.modulo_gestion_personal.model.AccessPermission;
import laboratorioxyz.com.ZoneControl.modulo_control_acceso.model.AccessSession;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
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

    @Column(nullable = false, columnDefinition = "boolean not null default false")
    @Builder.Default
    private boolean emergencyClosed = false;

    @OneToMany(mappedBy = "productionArea")
    @JsonIgnore
    @Builder.Default
    private List<AccessPermission> permissions = new ArrayList<>();

    @OneToMany(mappedBy = "productionArea")
    @JsonIgnore
    @Builder.Default
    private List<AccessSession> sessions = new ArrayList<>();
}
