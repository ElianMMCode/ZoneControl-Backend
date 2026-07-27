package laboratorioxyz.com.ZoneControl.modulo_publico.repository;

import laboratorioxyz.com.ZoneControl.modulo_publico.model.ProductCatalog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ProductCatalogRepository extends JpaRepository<ProductCatalog, UUID> {
}
