package laboratorioxyz.com.ZoneControl.modulo_publico.controller;

import laboratorioxyz.com.ZoneControl.modulo_publico.dto.CatalogResponse;
import laboratorioxyz.com.ZoneControl.modulo_publico.dto.CategoryResponse;
import laboratorioxyz.com.ZoneControl.modulo_publico.dto.ContactResponse;
import laboratorioxyz.com.ZoneControl.modulo_publico.dto.InstitutionalResponse;
import laboratorioxyz.com.ZoneControl.modulo_publico.dto.OfficeResponse;
import laboratorioxyz.com.ZoneControl.modulo_publico.dto.PublicZoneResponse;
import laboratorioxyz.com.ZoneControl.modulo_publico.service.PublicService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * Controlador del módulo público.
 * Expone endpoints GET sin autenticación para que el público general
 * pueda consultar información institucional, contacto, sedes, catálogo
 * de productos y descargar el folleto PDF de Laboratorio XYZ.
 */
@RestController
@RequestMapping("/api/public")
@RequiredArgsConstructor
public class PublicController {

    private final PublicService publicService;

    @GetMapping("/institucional")
    public ResponseEntity<InstitutionalResponse> getInstitutional() {
        return ResponseEntity.ok(publicService.getInstitutionalInfo());
    }

    @GetMapping("/contacto")
    public ResponseEntity<ContactResponse> getContact() {
        return ResponseEntity.ok(publicService.getContactInfo());
    }

    @GetMapping("/sedes")
    public ResponseEntity<List<OfficeResponse>> getOffices() {
        return ResponseEntity.ok(publicService.getOffices());
    }

    @GetMapping("/catalogo")
    public ResponseEntity<List<CatalogResponse>> getCatalog() {
        return ResponseEntity.ok(publicService.getCatalog());
    }

    @GetMapping("/catalogo/categorias")
    public ResponseEntity<List<CategoryResponse>> getCategories() {
        return ResponseEntity.ok(publicService.getCategories());
    }

    @GetMapping("/zonas")
    public ResponseEntity<List<PublicZoneResponse>> getZones() {
        return ResponseEntity.ok(publicService.getZones());
    }

    @GetMapping("/folleto")
    public ResponseEntity<Resource> getBrochure() {
        Resource resource = publicService.getBrochure();
        if (resource == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"Folleto_Laboratorio_XYZ.pdf\"")
                .body(resource);
    }

    @GetMapping("/catalogo/{id}/imagen")
    public ResponseEntity<Resource> getProductImage(@PathVariable UUID id) {
        Resource resource = publicService.getProductImage(id);
        if (resource == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok()
                .contentType(imageMediaType(resource.getFilename()))
                .cacheControl(org.springframework.http.CacheControl.maxAge(java.time.Duration.ofMinutes(5)))
                .body(resource);
    }

    @GetMapping("/sedes/{id}/imagen")
    public ResponseEntity<Resource> getOfficeImage(@PathVariable UUID id) {
        Resource resource = publicService.getOfficeImage(id);
        if (resource == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok()
                .contentType(imageMediaType(resource.getFilename()))
                .cacheControl(org.springframework.http.CacheControl.maxAge(java.time.Duration.ofMinutes(5)))
                .body(resource);
    }

    private MediaType imageMediaType(String filename) {
        if (filename != null) {
            String lower = filename.toLowerCase();
            if (lower.endsWith(".png")) return MediaType.IMAGE_PNG;
            if (lower.endsWith(".webp")) return MediaType.parseMediaType("image/webp");
            if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) return MediaType.IMAGE_JPEG;
        }
        return MediaType.APPLICATION_OCTET_STREAM;
    }
}
