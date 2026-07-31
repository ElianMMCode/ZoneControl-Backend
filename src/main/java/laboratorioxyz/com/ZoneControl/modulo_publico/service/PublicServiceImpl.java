package laboratorioxyz.com.ZoneControl.modulo_publico.service;

import laboratorioxyz.com.ZoneControl.model.entity.Office;
import laboratorioxyz.com.ZoneControl.model.enums.ContentSection;
import laboratorioxyz.com.ZoneControl.model.repository.ProductionAreaRepository;
import laboratorioxyz.com.ZoneControl.modulo_publico.dto.CatalogResponse;
import laboratorioxyz.com.ZoneControl.modulo_publico.dto.ContactResponse;
import laboratorioxyz.com.ZoneControl.modulo_publico.dto.InstitutionalResponse;
import laboratorioxyz.com.ZoneControl.modulo_publico.dto.OfficeResponse;
import laboratorioxyz.com.ZoneControl.modulo_publico.model.ProductCatalog;
import laboratorioxyz.com.ZoneControl.modulo_publico.model.PublicContent;
import laboratorioxyz.com.ZoneControl.modulo_publico.repository.OfficeRepository;
import laboratorioxyz.com.ZoneControl.modulo_publico.repository.ProductCatalogRepository;
import laboratorioxyz.com.ZoneControl.modulo_publico.repository.PublicContentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Implementación del servicio público.
 * Los datos se sirven desde la tabla public_contents (clave-valor por sección),
 * offices y product_catalog. Se usa caché por nombre separado para evitar
 * que un tipo de respuesta invalide o sobrescriba a otro
 * (cada @Cacheable tiene su propio espacio de nombres).
 *
 * La información institucional incluye companyName y productionAreas como
 * valores por defecto si no están configurados explícitamente en la BD.
 * productionAreas se obtiene desde la tabla production_areas para mantener
 * sincronizadas las áreas activas sin duplicación de datos.
 */
@Service
@RequiredArgsConstructor
public class PublicServiceImpl implements PublicService {

    private final PublicContentRepository publicContentRepository;
    private final OfficeRepository officeRepository;
    private final ProductCatalogRepository productCatalogRepository;
    private final ProductionAreaRepository productionAreaRepository;

    @Value("${app.brochure.path:uploads/folleto}")
    private String brochurePath;

    @Override
    @Cacheable("institutional")
    public InstitutionalResponse getInstitutionalInfo() {
        List<PublicContent> contents = publicContentRepository.findBySection(ContentSection.INSTITUTIONAL);
        Map<String, String> info = contents.stream()
                .collect(Collectors.toMap(PublicContent::getKey, PublicContent::getValue));
        if (!info.containsKey("companyName")) {
            info.put("companyName", "Laboratorio XYZ");
        }
        if (!info.containsKey("productionAreas")) {
            List<String> areaNames = productionAreaRepository.findAllByActive(true)
                    .stream().map(area -> area.getName())
                    .collect(Collectors.toList());
            info.put("productionAreas", String.join(", ", areaNames));
        }
        return InstitutionalResponse.builder().info(info).build();
    }

    @Override
    @Cacheable("contact")
    public ContactResponse getContactInfo() {
        List<PublicContent> contents = publicContentRepository.findBySection(ContentSection.CONTACT);
        Map<String, String> contact = contents.stream()
                .collect(Collectors.toMap(PublicContent::getKey, PublicContent::getValue));
        return ContactResponse.builder().contact(contact).build();
    }

    @Override
    @Cacheable("offices")
    public List<OfficeResponse> getOffices() {
        List<Office> offices = officeRepository.findAll();
        return offices.stream()
                .map(o -> OfficeResponse.builder()
                        .id(o.getId())
                        .name(o.getName())
                        .address(o.getAddress())
                        .openingHours(o.getOpeningHours())
                        .latitude(o.getLatitude())
                        .longitude(o.getLongitude())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    @Cacheable("catalog")
    public List<CatalogResponse> getCatalog() {
        List<ProductCatalog> products = productCatalogRepository.findAll();
        return products.stream()
                .map(p -> CatalogResponse.builder()
                        .id(p.getId())
                        .name(p.getName())
                        .description(p.getDescription())
                        .activeIngredient(p.getActiveIngredient())
                        .presentation(p.getPresentation())
                        .productionArea(p.getProductionArea())
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * Sirve el archivo PDF del folleto si existe en el directorio configurado.
     * Retorna null si no hay folleto cargado, y el controller responde 404.
     * El folleto lo gestiona el administrador vía HU-19.
     */
    @Override
    public Resource getBrochure() {
        File file = new File(brochurePath, "Folleto_Laboratorio_XYZ.pdf");
        if (!file.exists()) {
            return null;
        }
        return new FileSystemResource(file);
    }
}
