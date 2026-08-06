package laboratorioxyz.com.ZoneControl.modulo_gestion_personal.repository;

import laboratorioxyz.com.ZoneControl.modulo_gestion_personal.model.Position;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PositionRepository extends JpaRepository<Position, UUID> {

    Optional<Position> findByName(String name);

    boolean existsByName(String name);
}
