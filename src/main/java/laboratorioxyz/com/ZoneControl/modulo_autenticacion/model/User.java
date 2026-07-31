package laboratorioxyz.com.ZoneControl.modulo_autenticacion.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import laboratorioxyz.com.ZoneControl.model.enums.Role;
import laboratorioxyz.com.ZoneControl.model.enums.UserStatus;
import laboratorioxyz.com.ZoneControl.modulo_gestion_personal.model.Employee;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Usuarios internos del sistema con acceso a los módulos administrativos.
 * Cada usuario tiene un rol que define sus permisos.
 * 
 * La contraseña se establece mediante un magic link enviado por email,
 * por lo que password es nullable hasta que el usuario complete el
 * proceso de configuración.
 *
 * setupToken almacena el hash SHA-256 (hex, 64 chars) del token crudo de un
 * solo uso. Se eligió SHA-256 sobre BCrypt porque el token es una cadena
 * aleatoria de 64 caracteres (alta entropía, no requiere un KDF lento) y
 * permite búsqueda directa en BD por hash sin recorrer todos los usuarios.
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
    @Column(length = 60, nullable = true)
    private String password;

    @Column(unique = true, length = 64, nullable = true)
    private String setupToken;

    @Column(nullable = true)
    private LocalDateTime setupTokenExpiry;

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
