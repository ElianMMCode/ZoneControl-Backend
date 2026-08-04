package laboratorioxyz.com.ZoneControl.modulo_publico.repository;

import laboratorioxyz.com.ZoneControl.modulo_publico.model.ProductCatalog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repositorio para la entidad ProductCatalog.
 * Lista completa de productos farmacéuticos visible al público.
 */
public interface ProductCatalogRepository extends JpaRepository<ProductCatalog, UUID> {

    Optional<ProductCatalog> findByName(String name);
}
