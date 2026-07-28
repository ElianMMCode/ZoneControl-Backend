package laboratorioxyz.com.ZoneControl.modulo_autenticacion.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import laboratorioxyz.com.ZoneControl.model.enums.Role;
import laboratorioxyz.com.ZoneControl.model.enums.UserStatus;
import laboratorioxyz.com.ZoneControl.modulo_gestion_personal.model.Employee;
import lombok.*;

import java.util.UUID;

/**
 * Usuarios internos del sistema con acceso a los módulos administrativos.
 * Cada usuario tiene un rol que define sus permisos.
 */
@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Size(max = 35)
    @Column(length = 35, nullable = false)
    private String firstName;

    @Size(max = 35)
    @Column(length = 35, nullable = false)
    private String lastName;

    @Email
    @Size(max = 100)
    @Column(length = 100, unique = true, nullable = false)
    private String email;

    @Size(max = 60)
    @Column(length = 60, nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private Role role;

    @Enumerated(EnumType.STRING)
    @Column(length = 10, nullable = false)
    @Builder.Default
    private UserStatus status = UserStatus.ACTIVO;

    @Column(nullable = false)
    @Builder.Default
    private boolean requirePasswordChange = false;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false, unique = true)
    private Employee employee;
}
