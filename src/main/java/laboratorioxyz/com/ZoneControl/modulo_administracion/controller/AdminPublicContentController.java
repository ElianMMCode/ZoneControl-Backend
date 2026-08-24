package laboratorioxyz.com.ZoneControl.modulo_administracion.controller;

import jakarta.validation.Valid;
import laboratorioxyz.com.ZoneControl.modulo_administracion.dto.CategoryRequest;
import laboratorioxyz.com.ZoneControl.modulo_administracion.dto.OfficeRequest;
import laboratorioxyz.com.ZoneControl.modulo_administracion.dto.ProductRequest;
import laboratorioxyz.com.ZoneControl.modulo_publico.dto.CatalogResponse;
import laboratorioxyz.com.ZoneControl.modulo_publico.dto.CategoryResponse;
import laboratorioxyz.com.ZoneControl.modulo_publico.dto.OfficeResponse;
import laboratorioxyz.com.ZoneControl.modulo_administracion.service.AdminContentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/contenido-publico")
@RequiredArgsConstructor
public class AdminPublicContentController {

    private final AdminContentService adminContentService;

    @PutMapping("/{section}")
    public ResponseEntity<Map<String, String>> updateSection(
            @PathVariable String section,
            @RequestBody Map<String, String> content) {
        return ResponseEntity.ok(adminContentService.updateSection(section, content));
    }

    @PostMapping("/folleto")
    public ResponseEntity<Map<String, String>> uploadBrochure(@RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(adminContentService.uploadBrochure(file));
    }

    @DeleteMapping("/folleto")
    public ResponseEntity<Map<String, String>> deleteBrochure() {
        return ResponseEntity.ok(adminContentService.deleteBrochure());
    }

    @PostMapping("/productos")
    public ResponseEntity<Map<String, Object>> createProduct(
            @Valid @RequestBody ProductRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(adminContentService.createProduct(request));
    }

    @PutMapping("/productos/{id}")
    public ResponseEntity<Map<String, Object>> updateProduct(
            @PathVariable UUID id,
            @Valid @RequestBody ProductRequest request) {
        return ResponseEntity.ok(adminContentService.updateProduct(id, request));
    }

    @DeleteMapping("/productos/{id}")
    public ResponseEntity<Map<String, String>> deleteProduct(@PathVariable UUID id) {
        return ResponseEntity.ok(adminContentService.deleteProduct(id));
    }

    @PostMapping(value = "/productos/{id}/imagen", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CatalogResponse> uploadProductImage(
            @PathVariable UUID id,
            @RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(adminContentService.uploadProductImage(id, file));
    }

    @DeleteMapping("/productos/{id}/imagen")
    public ResponseEntity<CatalogResponse> deleteProductImage(@PathVariable UUID id) {
        return ResponseEntity.ok(adminContentService.deleteProductImage(id));
    }

    @GetMapping("/categorias")
    public ResponseEntity<List<CategoryResponse>> listCategories() {
        return ResponseEntity.ok(adminContentService.getCategories());
    }

    @PostMapping("/categorias")
    public ResponseEntity<CategoryResponse> createCategory(
            @Valid @RequestBody CategoryRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(adminContentService.createCategory(request));
    }

    @PutMapping("/categorias/{id}")
    public ResponseEntity<CategoryResponse> updateCategory(
            @PathVariable UUID id,
            @Valid @RequestBody CategoryRequest request) {
        return ResponseEntity.ok(adminContentService.updateCategory(id, request));
    }

    @DeleteMapping("/categorias/{id}")
    public ResponseEntity<Map<String, String>> deleteCategory(@PathVariable UUID id) {
        return ResponseEntity.ok(adminContentService.deleteCategory(id));
    }

    @PostMapping("/sedes")
    public ResponseEntity<Map<String, Object>> createOffice(
            @Valid @RequestBody OfficeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(adminContentService.createOffice(request));
    }

    @PutMapping("/sedes/{id}")
    public ResponseEntity<Map<String, Object>> updateOffice(
            @PathVariable UUID id,
            @Valid @RequestBody OfficeRequest request) {
        return ResponseEntity.ok(adminContentService.updateOffice(id, request));
    }

    @DeleteMapping("/sedes/{id}")
    public ResponseEntity<Map<String, String>> deleteOffice(@PathVariable UUID id) {
        return ResponseEntity.ok(adminContentService.deleteOffice(id));
    }

    @PostMapping(value = "/sedes/{id}/imagen", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<OfficeResponse> uploadOfficeImage(
            @PathVariable UUID id,
            @RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(adminContentService.uploadOfficeImage(id, file));
    }

    @DeleteMapping("/sedes/{id}/imagen")
    public ResponseEntity<OfficeResponse> deleteOfficeImage(@PathVariable UUID id) {
        return ResponseEntity.ok(adminContentService.deleteOfficeImage(id));
    }
}
