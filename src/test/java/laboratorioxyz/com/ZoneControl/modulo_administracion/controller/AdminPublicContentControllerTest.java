package laboratorioxyz.com.ZoneControl.modulo_administracion.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import laboratorioxyz.com.ZoneControl.modulo_publico.model.ProductCatalog;
import laboratorioxyz.com.ZoneControl.modulo_publico.repository.ProductCatalogRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@Rollback
@WithMockUser(roles = "ADMIN")
class AdminPublicContentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProductCatalogRepository productCatalogRepository;

    @Test
    void updateSection_valid_returns200() throws Exception {
        mockMvc.perform(put("/admin/contenido-publico/INSTITUTIONAL")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "mission", "Test mission",
                                "vision", "Test vision"
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Contenido actualizado correctamente"));
    }

    @Test
    void updateSection_invalidSection_returns400() throws Exception {
        mockMvc.perform(put("/admin/contenido-publico/INVALID")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(
                        "Sección no válida: INVALID. Permitidas: INSTITUTIONAL, CONTACT, LOCATIONS"));
    }

    @Test
    void uploadBrochure_validPdf_returns200() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "test.pdf", "application/pdf", "%PDF-1.4 test content".getBytes());

        mockMvc.perform(multipart("/admin/contenido-publico/folleto").file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Folleto cargado exitosamente"));
    }

    @Test
    void uploadBrochure_invalidExtension_returns400() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "test.txt", "text/plain", "not a pdf".getBytes());

        mockMvc.perform(multipart("/admin/contenido-publico/folleto").file(file))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(
                        "Formato no permitido. Solo se aceptan archivos PDF"));
    }

    @Test
    void deleteBrochure_returns200() throws Exception {
        mockMvc.perform(delete("/admin/contenido-publico/folleto"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Folleto eliminado correctamente"));
    }

    @Test
    void createProduct_valid_returns201() throws Exception {
        mockMvc.perform(post("/admin/contenido-publico/productos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "name", "Test Product",
                                "description", "Test description",
                                "activeIngredient", "TestIngredient",
                                "presentation", "10mg",
                                "productionArea", "Sala Blanca A"
                        ))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Test Product"));
    }

    @Test
    void createProduct_missingName_returns400() throws Exception {
        mockMvc.perform(post("/admin/contenido-publico/productos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "description", "Test"
                        ))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateProduct_valid_returns200() throws Exception {
        ProductCatalog product = productCatalogRepository.save(ProductCatalog.builder()
                .name("Original")
                .description("Original desc")
                .build());

        mockMvc.perform(put("/admin/contenido-publico/productos/{id}", product.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "name", "Updated",
                                "description", "Updated desc"
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated"));
    }

    @Test
    void updateProduct_nonExistent_returns404() throws Exception {
        mockMvc.perform(put("/admin/contenido-publico/productos/{id}", UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "name", "Any",
                                "description", "Any"
                        ))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Producto no encontrado"));
    }

    @Test
    void deleteProduct_valid_returns200() throws Exception {
        ProductCatalog product = productCatalogRepository.save(ProductCatalog.builder()
                .name("To Delete")
                .build());

        mockMvc.perform(delete("/admin/contenido-publico/productos/{id}", product.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Producto eliminado correctamente"));
    }

    @Test
    void deleteProduct_nonExistent_returns404() throws Exception {
        mockMvc.perform(delete("/admin/contenido-publico/productos/{id}", UUID.randomUUID()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Producto no encontrado"));
    }

    @Test
    void createOffice_valid_returns201() throws Exception {
        mockMvc.perform(post("/admin/contenido-publico/sedes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "name", "Sede Principal",
                                "address", "Calle 123",
                                "openingHours", "8:00-17:00",
                                "latitude", 4.7,
                                "longitude", -74.1
                        ))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Sede Principal"));
    }

    @Test
    void updateOffice_nonExistent_returns404() throws Exception {
        mockMvc.perform(put("/admin/contenido-publico/sedes/{id}", UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "name", "Any",
                                "address", "Any"
                        ))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Sede no encontrada"));
    }

    @Test
    void deleteOffice_nonExistent_returns404() throws Exception {
        mockMvc.perform(delete("/admin/contenido-publico/sedes/{id}", UUID.randomUUID()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Sede no encontrada"));
    }
}
