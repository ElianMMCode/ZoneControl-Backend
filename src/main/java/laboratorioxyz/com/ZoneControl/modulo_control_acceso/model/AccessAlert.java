package laboratorioxyz.com.ZoneControl.modulo_control_acceso.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Alerta de anomalía de acceso (§9.3 item 2.4). Se detecta on-write durante
 * la validación: ≥3 denegaciones en 15 min, cierre/reapertura de zona por
 * emergencia.
 */
@Entity
@Table(name = "access_alerts")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccessAlert {

    public enum AlertType { DENEGACIONES_REPETIDAS, ZONA_EMERGENCIA }

    public enum AlertSeverity { LOW, MEDIUM, HIGH }

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(length = 30, nullable = false)
    private AlertType tipo;

    @Enumerated(EnumType.STRING)
    @Column(length = 10, nullable = false)
    private AlertSeverity severidad;

    @Column(length = 20)
    private String employeeCode;

    /**
     * Snapshot de si el empleado involucrado tenía cuenta de sistema (User)
     * al momento de generarse la alerta. ZONA_EMERGENCIA no involucra
     * empleado y queda en false.
     */
    @Column(name = "has_user", nullable = false)
    @org.hibernate.annotations.ColumnDefault("false")
    @Builder.Default
    private boolean hasUser = false;

    @Column(length = 30)
    private String productionAreaName;

    @Column(length = 300, nullable = false)
    private String message;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @Column(nullable = false)
    @Builder.Default
    private boolean leido = false;
}
