package laboratorioxyz.com.ZoneControl.modulo_gestion_personal.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import laboratorioxyz.com.ZoneControl.model.enums.Role;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Cargo del catálogo de la compañía. Cada cargo puede definir un rol de
 * sistema (nullable): el rol de un usuario se deriva del cargo del empleado
 * al que pertenece, de modo que el rol no se elige manualmente.
 */
@Entity
@Table(name = "positions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Position {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Size(max = 40)
    @Column(length = 40, nullable = false, unique = true)
    private String name;

    /**
     * Rol de sistema que se deriva de este cargo. Null = el empleado con este
     * cargo no es candidato a usuario del sistema (solo acceso físico).
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "system_role", length = 20, nullable = true)
    private Role systemRole;

    @OneToMany(mappedBy = "cargo")
    @JsonIgnore
    @Builder.Default
    private List<Employee> employees = new ArrayList<>();
}
