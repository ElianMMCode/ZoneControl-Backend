package laboratorioxyz.com.ZoneControl.modulo_autenticacion.repository;

import laboratorioxyz.com.ZoneControl.modulo_autenticacion.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;
import java.util.UUID;

/**
 * Repositorio para la entidad User.
 * findByEmail se usa en el flujo de login (HU-03) para autenticar
 * usuarios por correo electrónico.
 * findBySetupToken se usa en el flujo de magic link (HU-05): el token
 * crudo se hashea con SHA-256 en el servicio antes de consultar.
 * JpaSpecificationExecutor habilita el listado paginado con filtros
 * dinámicos de la pantalla de Gestión de Usuarios (HU-05).
 */
public interface UserRepository extends JpaRepository<User, UUID>, JpaSpecificationExecutor<User> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    Optional<User> findByEmployee_Id(UUID employeeId);
    Optional<User> findBySetupToken(String setupToken);
}
