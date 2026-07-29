package laboratorioxyz.com.ZoneControl.modulo_administracion.service;

import laboratorioxyz.com.ZoneControl.model.entity.Office;
import laboratorioxyz.com.ZoneControl.model.enums.ContentSection;
import laboratorioxyz.com.ZoneControl.modulo_administracion.dto.OfficeRequest;
import laboratorioxyz.com.ZoneControl.modulo_administracion.dto.ProductRequest;
import laboratorioxyz.com.ZoneControl.modulo_publico.model.ProductCatalog;
import laboratorioxyz.com.ZoneControl.modulo_publico.model.PublicContent;
import laboratorioxyz.com.ZoneControl.modulo_publico.repository.OfficeRepository;
import laboratorioxyz.com.ZoneControl.modulo_publico.repository.ProductCatalogRepository;
import laboratorioxyz.com.ZoneControl.modulo_publico.repository.PublicContentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminContentServiceImpl implements AdminContentService {

    private final PublicContentRepository publicContentRepository;
    private final ProductCatalogRepository productCatalogRepository;
    private final OfficeRepository officeRepository;
    private final CacheManager cacheManager;

    @Value("${app.brochure.path:uploads/folleto}")
    private String brochurePath;

    @Override
    @Transactional
    public Map<String, String> updateSection(String section, Map<String, String> content) {
        ContentSection contentSection;
        try {
            contentSection = ContentSection.valueOf(section.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Sección no válida: " + section + ". Permitidas: INSTITUTIONAL, CONTACT, LOCATIONS");
        }

        publicContentRepository.deleteAll(publicContentRepository.findBySection(contentSection));

        List<PublicContent> newContents = content.entrySet().stream()
                .map(entry -> PublicContent.builder()
                        .section(contentSection)
                        .key(entry.getKey())
                        .value(entry.getValue())
                        .build())
                .toList();
        publicContentRepository.saveAll(newContents);

        evictCache(contentSection);
        log.info("Public content section {} updated", contentSection);
        return Map.of("message", "Contenido actualizado correctamente");
    }

    @Override
    public Map<String, String> uploadBrochure(MultipartFile file) {
        String filename = file.getOriginalFilename();
        if (filename == null || !filename.toLowerCase().endsWith(".pdf")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Formato no permitido. Solo se aceptan archivos PDF");
        }

        File dir = new File(brochurePath);
        if (!dir.exists() && !dir.mkdirs()) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "No se pudo crear el directorio de almacenamiento");
        }

        File target = new File(dir, "Folleto_Laboratorio_XYZ.pdf");
        try {
            Files.copy(file.getInputStream(), target.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            log.error("Error uploading brochure", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Error al guardar el archivo");
        }

        log.info("Brochure uploaded ({} bytes)", file.getSize());
        return Map.of("message", "Folleto cargado exitosamente");
    }

    @Override
    public Map<String, String> deleteBrochure() {
        File file = new File(brochurePath, "Folleto_Laboratorio_XYZ.pdf");
        if (file.exists() && !file.delete()) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Error al eliminar el folleto");
        }
        log.info("Brochure deleted");
        return Map.of("message", "Folleto eliminado correctamente");
    }

    @Override
    @Transactional
    public Map<String, Object> createProduct(ProductRequest request) {
        ProductCatalog product = productCatalogRepository.save(ProductCatalog.builder()
                .name(request.name())
                .description(request.description())
                .activeIngredient(request.activeIngredient())
                .presentation(request.presentation())
                .productionArea(request.productionArea())
                .build());
        evictCache("catalog");
        return Map.of("id", product.getId(), "name", product.getName());
    }

    @Override
    @Transactional
    public Map<String, Object> updateProduct(UUID id, ProductRequest request) {
        ProductCatalog product = productCatalogRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Producto no encontrado"));
        product.setName(request.name());
        product.setDescription(request.description());
        product.setActiveIngredient(request.activeIngredient());
        product.setPresentation(request.presentation());
        product.setProductionArea(request.productionArea());
        productCatalogRepository.save(product);
        evictCache("catalog");
        return Map.of("id", product.getId(), "name", product.getName());
    }

    @Override
    @Transactional
    public Map<String, String> deleteProduct(UUID id) {
        if (!productCatalogRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no encontrado");
        }
        productCatalogRepository.deleteById(id);
        evictCache("catalog");
        return Map.of("message", "Producto eliminado correctamente");
    }

    @Override
    @Transactional
    public Map<String, Object> createOffice(OfficeRequest request) {
        Office office = officeRepository.save(Office.builder()
                .name(request.name())
                .address(request.address())
                .openingHours(request.openingHours())
                .latitude(request.latitude())
                .longitude(request.longitude())
                .build());
        evictCache("offices");
        return Map.of("id", office.getId(), "name", office.getName());
    }

    @Override
    @Transactional
    public Map<String, Object> updateOffice(UUID id, OfficeRequest request) {
        Office office = officeRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Sede no encontrada"));
        office.setName(request.name());
        office.setAddress(request.address());
        office.setOpeningHours(request.openingHours());
        office.setLatitude(request.latitude());
        office.setLongitude(request.longitude());
        officeRepository.save(office);
        evictCache("offices");
        return Map.of("id", office.getId(), "name", office.getName());
    }

    @Override
    @Transactional
    public Map<String, String> deleteOffice(UUID id) {
        if (!officeRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Sede no encontrada");
        }
        officeRepository.deleteById(id);
        evictCache("offices");
        return Map.of("message", "Sede eliminada correctamente");
    }

    private void evictCache(String cacheName) {
        var cache = cacheManager.getCache(cacheName);
        if (cache != null) {
            cache.clear();
        }
    }

    private void evictCache(ContentSection section) {
        evictCache(switch (section) {
            case INSTITUTIONAL -> "institutional";
            case CONTACT -> "contact";
            case LOCATIONS -> "offices";
        });
    }
}
