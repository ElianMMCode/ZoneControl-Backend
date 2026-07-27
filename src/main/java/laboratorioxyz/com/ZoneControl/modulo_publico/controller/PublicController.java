package laboratorioxyz.com.ZoneControl.modulo_publico.controller;

import laboratorioxyz.com.ZoneControl.modulo_publico.dto.CatalogResponse;
import laboratorioxyz.com.ZoneControl.modulo_publico.dto.ContactResponse;
import laboratorioxyz.com.ZoneControl.modulo_publico.dto.InstitutionalResponse;
import laboratorioxyz.com.ZoneControl.modulo_publico.dto.OfficeResponse;
import laboratorioxyz.com.ZoneControl.modulo_publico.service.PublicService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/public")
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
}
