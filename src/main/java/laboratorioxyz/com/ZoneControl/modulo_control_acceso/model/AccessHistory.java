package laboratorioxyz.com.ZoneControl.modulo_control_acceso.model;

import jakarta.persistence.*;
import laboratorioxyz.com.ZoneControl.model.enums.AccessResult;
import laboratorioxyz.com.ZoneControl.modulo_gestion_personal.model.Employee;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Historial de cada intento de acceso simulado a áreas restringidas.
 * Cada registro guarda el resultado independientemente de si fue exitoso o no.
 */
@Entity
@Table(name = "access_history")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccessHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id")
    private Employee employee;

    @Column(length = 80)
    private String department;

    @Column(length = 30)
    private String productionAreaName;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @Enumerated(EnumType.STRING)
    @Column(length = 15, nullable = false)
    private AccessResult result;
}
