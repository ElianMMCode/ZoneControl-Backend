package laboratorioxyz.com.ZoneControl.modulo_gestion_personal.model;

import jakarta.persistence.*;
import laboratorioxyz.com.ZoneControl.model.enums.WeekDay;
import lombok.*;

import java.time.LocalTime;
import java.util.UUID;

/**
 * Turno/horario por día de la semana de un permiso (3.2 §9, HU-26).
 * Un permiso puede tener varios schedules (uno por día); si no se especifica
 * ninguno se crea el schedule LUN-DOM con los horarios base del permiso.
 */
@Entity
@Table(name = "permission_schedules")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PermissionSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "permission_id", nullable = false)
    private AccessPermission permission;

    @Enumerated(EnumType.STRING)
    @Column(length = 3, nullable = false)
    private WeekDay dayOfWeek;

    @Column(nullable = false)
    private LocalTime startTime;

    @Column(nullable = false)
    private LocalTime endTime;
}
