package laboratorioxyz.com.ZoneControl.modulo_control_acceso.model;

import jakarta.persistence.*;
import laboratorioxyz.com.ZoneControl.model.entity.ProductionArea;
import laboratorioxyz.com.ZoneControl.modulo_gestion_personal.model.Employee;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Sesión de ocupación de una zona ("quién está dentro", §9.3 item 2.1).
 * Se crea al validar un acceso autorizado y se cierra al registrar la salida
 * (POST /api/access/exit). Solo ADMIN/SUPERVISOR (rol SEGURIDAD eliminado).
 */
@Entity
@Table(name = "access_sessions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccessSession {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "production_area_id", nullable = false)
    private ProductionArea productionArea;

    @Column(nullable = false)
    private LocalDateTime entryTime;

    @Column(nullable = true)
    private LocalDateTime exitTime;
}
