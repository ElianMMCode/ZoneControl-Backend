package laboratorioxyz.com.ZoneControl.modulo_publico.repository;

import laboratorioxyz.com.ZoneControl.model.enums.ContentSection;
import laboratorioxyz.com.ZoneControl.modulo_publico.model.PublicContent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PublicContentRepository extends JpaRepository<PublicContent, UUID> {
    List<PublicContent> findBySection(ContentSection section);
}
