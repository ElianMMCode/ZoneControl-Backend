package laboratorioxyz.com.ZoneControl.modulo_administracion.service;

import laboratorioxyz.com.ZoneControl.model.entity.Office;
import laboratorioxyz.com.ZoneControl.model.enums.ContentSection;
import laboratorioxyz.com.ZoneControl.modulo_administracion.dto.OfficeRequest;
import laboratorioxyz.com.ZoneControl.modulo_administracion.dto.ProductRequest;
import laboratorioxyz.com.ZoneControl.modulo_publico.dto.CatalogResponse;
import laboratorioxyz.com.ZoneControl.modulo_publico.dto.OfficeResponse;
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
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminContentServiceImpl implements AdminContentService {

    private static final long MAX_BROCHURE_BYTES = 10L * 1024 * 1024;
    private static final Path PRODUCT_IMAGE_DIR = Paths.get("uploads", "products");
    private static final Path OFFICE_IMAGE_DIR = Paths.get("uploads", "offices");
    private static final Set<String> ALLOWED_IMAGE_EXT = Set.of("jpg", "jpeg", "png", "webp");
    private static final long MAX_IMAGE_BYTES = 2L * 1024 * 1024;

    @jakarta.annotation.PostConstruct
    void initImageDirs() {
        try {
            Files.createDirectories(PRODUCT_IMAGE_DIR);
            Files.createDirectories(OFFICE_IMAGE_DIR);
        } catch (IOException e) {
            log.warn("No se pudieron crear los directorios de imágenes: {}", e.getMessage());
        }
    }

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
                    "Sección no válida: " + section + ". Permitidas: INSTITUTIONAL, CONTACT");
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
        if (file.getSize() > MAX_BROCHURE_BYTES) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "El archivo excede el tamaño máximo permitido de 10MB");
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
        ProductCatalog product = productCatalogRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no encontrado"));
        deleteFile(product.getImageUrl());
        productCatalogRepository.deleteById(id);
        evictCache("catalog");
        return Map.of("message", "Producto eliminado correctamente");
    }

    @Override
    @Transactional
    public CatalogResponse uploadProductImage(UUID id, MultipartFile file) {
        ProductCatalog product = requireProduct(id);
        String filename = saveImage(file, PRODUCT_IMAGE_DIR, "PR-" + id);
        deleteFile(product.getImageUrl());
        product.setImageUrl(filename);
        productCatalogRepository.save(product);
        evictCache("catalog");
        return toCatalogResponse(product);
    }

    @Override
    @Transactional
    public CatalogResponse deleteProductImage(UUID id) {
        ProductCatalog product = requireProduct(id);
        deleteFile(product.getImageUrl());
        product.setImageUrl(null);
        productCatalogRepository.save(product);
        evictCache("catalog");
        return toCatalogResponse(product);
    }

    private ProductCatalog requireProduct(UUID id) {
        return productCatalogRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Producto no encontrado"));
    }

    private CatalogResponse toCatalogResponse(ProductCatalog p) {
        return CatalogResponse.builder()
                .id(p.getId())
                .name(p.getName())
                .description(p.getDescription())
                .activeIngredient(p.getActiveIngredient())
                .presentation(p.getPresentation())
                .productionArea(p.getProductionArea())
                .imageUrl(p.getImageUrl() != null
                        ? "/api/public/catalogo/" + p.getId() + "/imagen" : null)
                .build();
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
        Office office = officeRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Sede no encontrada"));
        deleteFile(office.getImageUrl());
        officeRepository.deleteById(id);
        evictCache("offices");
        return Map.of("message", "Sede eliminada correctamente");
    }

    @Override
    @Transactional
    public OfficeResponse uploadOfficeImage(UUID id, MultipartFile file) {
        Office office = officeRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Sede no encontrada"));
        String filename = saveImage(file, OFFICE_IMAGE_DIR, "OF-" + id);
        deleteFile(office.getImageUrl());
        office.setImageUrl(filename);
        officeRepository.save(office);
        evictCache("offices");
        return toOfficeResponse(office);
    }

    @Override
    @Transactional
    public OfficeResponse deleteOfficeImage(UUID id) {
        Office office = officeRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Sede no encontrada"));
        deleteFile(office.getImageUrl());
        office.setImageUrl(null);
        officeRepository.save(office);
        evictCache("offices");
        return toOfficeResponse(office);
    }

    private OfficeResponse toOfficeResponse(Office o) {
        return OfficeResponse.builder()
                .id(o.getId())
                .name(o.getName())
                .address(o.getAddress())
                .openingHours(o.getOpeningHours())
                .latitude(o.getLatitude())
                .longitude(o.getLongitude())
                .imageUrl(o.getImageUrl() != null
                        ? "/api/public/sedes/" + o.getId() + "/imagen" : null)
                .build();
    }

    /**
     * Guarda la imagen validando extensión y tamaño. El nombre se deriva del
     * prefijo dado (PR-{id} / OF-{id}) conservando la extensión original.
     * Retorna la ruta pública relativa para servir el archivo.
     */
    private String saveImage(MultipartFile file, Path dir, String baseName) {
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Debe adjuntar una imagen");
        }
        if (file.getSize() > MAX_IMAGE_BYTES) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "La imagen excede el tamaño máximo permitido de 2MB");
        }
        String original = file.getOriginalFilename();
        String ext = original != null && original.contains(".")
                ? original.substring(original.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT)
                : "";
        if (!ALLOWED_IMAGE_EXT.contains(ext)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Extensión no permitida. Solo se aceptan: jpg, jpeg, png, webp");
        }
        try {
            Files.createDirectories(dir);
            Path target = dir.resolve(baseName + "." + ext);
            Files.write(target, file.getBytes());
            return dir.getFileName().resolve(baseName + "." + ext).toString();
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "No se pudo guardar la imagen: " + e.getMessage());
        }
    }

    /**
     * Elimina el archivo en disco correspondiente a una ruta relativa
     * bajo uploads/ (p. ej. products/PR-{id}.png).
     */
    private void deleteFile(String relativePath) {
        if (relativePath == null || relativePath.isBlank()) return;
        try {
            Files.deleteIfExists(Paths.get("uploads").resolve(relativePath));
        } catch (IOException e) {
            log.warn("No se pudo eliminar la imagen {}: {}", relativePath, e.getMessage());
        }
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
        });
    }
}
