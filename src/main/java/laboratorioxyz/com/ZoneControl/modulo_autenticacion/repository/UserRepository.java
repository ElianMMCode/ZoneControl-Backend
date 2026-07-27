package laboratorioxyz.com.ZoneControl.modulo_autenticacion.repository;

import laboratorioxyz.com.ZoneControl.modulo_autenticacion.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repositorio para la entidad User.
 * findByEmail se usa en el flujo de login (HU-03) para autenticar
 * usuarios por correo electrónico.
 */
public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
}
