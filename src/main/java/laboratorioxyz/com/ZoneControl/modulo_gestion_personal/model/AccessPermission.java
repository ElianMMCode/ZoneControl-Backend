package laboratorioxyz.com.ZoneControl.modulo_gestion_personal.model;

import jakarta.persistence.*;
import laboratorioxyz.com.ZoneControl.model.entity.ProductionArea;
import laboratorioxyz.com.ZoneControl.model.enums.PermissionStatus;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

/**
 * Permisos de acceso otorgados a empleados para ingresar
 * a áreas de producción restringidas en horarios específicos.
 */
@Entity
@Table(name = "access_permissions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccessPermission {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "production_area_id", nullable = false)
    private ProductionArea productionArea;

    @Enumerated(EnumType.STRING)
    @Column(length = 10, nullable = false)
    @Builder.Default
    private PermissionStatus status = PermissionStatus.ACTIVO;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate expirationDate;

    private LocalDate reactivationDate;

    @Column(nullable = false)
    private LocalTime startTime;

    @Column(nullable = false)
    private LocalTime endTime;
}
