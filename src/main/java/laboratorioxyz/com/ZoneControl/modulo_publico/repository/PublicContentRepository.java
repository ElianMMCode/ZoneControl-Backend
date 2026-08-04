package laboratorioxyz.com.ZoneControl.modulo_publico.repository;

import laboratorioxyz.com.ZoneControl.model.enums.ContentSection;
import laboratorioxyz.com.ZoneControl.modulo_publico.model.PublicContent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

/**
 * Repositorio para la entidad PublicContent (contenido clave-valor).
 * findBySection permite obtener todo el contenido de una sección
 * (INSTITUTIONAL, CONTACT) para construir las respuestas.
 */
public interface PublicContentRepository extends JpaRepository<PublicContent, UUID> {
    List<PublicContent> findBySection(ContentSection section);
}
