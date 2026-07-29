package laboratorioxyz.com.ZoneControl.modulo_administracion.service;

import laboratorioxyz.com.ZoneControl.modulo_administracion.dto.OfficeRequest;
import laboratorioxyz.com.ZoneControl.modulo_administracion.dto.ProductRequest;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;
import java.util.UUID;

public interface AdminContentService {
    Map<String, String> updateSection(String section, Map<String, String> content);
    Map<String, String> uploadBrochure(MultipartFile file);
    Map<String, String> deleteBrochure();
    Map<String, Object> createProduct(ProductRequest request);
    Map<String, Object> updateProduct(UUID id, ProductRequest request);
    Map<String, String> deleteProduct(UUID id);
    Map<String, Object> createOffice(OfficeRequest request);
    Map<String, Object> updateOffice(UUID id, OfficeRequest request);
    Map<String, String> deleteOffice(UUID id);
}
