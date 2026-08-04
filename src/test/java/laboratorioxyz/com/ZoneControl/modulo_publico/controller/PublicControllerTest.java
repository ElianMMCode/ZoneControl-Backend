package laboratorioxyz.com.ZoneControl.modulo_publico.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.io.File;
import java.nio.file.Files;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = "app.brochure.path=target/test-uploads/folleto")
@AutoConfigureMockMvc
class PublicControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void getInstitutional_returns200() throws Exception {
        mockMvc.perform(get("/api/public/institucional"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.info").isMap())
                .andExpect(jsonPath("$.info.companyName").exists())
                .andExpect(jsonPath("$.info.mission").exists())
                .andExpect(jsonPath("$.info.vision").exists())
                .andExpect(jsonPath("$.info.description").exists())
                .andExpect(jsonPath("$.info.productionAreas").exists());
    }

    @Test
    void getContact_returns200() throws Exception {
        mockMvc.perform(get("/api/public/contacto"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contact").isMap())
                .andExpect(jsonPath("$.contact.phone").exists())
                .andExpect(jsonPath("$.contact.email").exists())
                .andExpect(jsonPath("$.contact.socialMedia").exists());
    }

    @Test
    void getOffices_returns200() throws Exception {
        mockMvc.perform(get("/api/public/sedes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").exists())
                .andExpect(jsonPath("$[0].name").exists())
                .andExpect(jsonPath("$[0].address").exists())
                .andExpect(jsonPath("$[0].openingHours").exists())
                .andExpect(jsonPath("$[0].latitude").exists())
                .andExpect(jsonPath("$[0].longitude").exists());
    }

    @Test
    void getCatalog_returns200() throws Exception {
        mockMvc.perform(get("/api/public/catalogo"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").exists())
                .andExpect(jsonPath("$[0].name").exists())
                .andExpect(jsonPath("$[0].activeIngredient").exists())
                .andExpect(jsonPath("$[0].presentation").exists())
                .andExpect(jsonPath("$[0].productionArea").exists());
    }

    @Test
    void getBrochure_returns200() throws Exception {
        File dir = new File("target/test-uploads/folleto");
        dir.mkdirs();
        File brochure = new File(dir, "Folleto_Laboratorio_XYZ.pdf");
        try {
            Files.write(brochure.toPath(), "%PDF-1.4 test content".getBytes());
            mockMvc.perform(get("/api/public/folleto"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_PDF))
                    .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"Folleto_Laboratorio_XYZ.pdf\""));
        } finally {
            Files.deleteIfExists(brochure.toPath());
        }
    }

    @Test
    void getBrochure_notFound_returns404() throws Exception {
        new File("target/test-uploads/folleto/Folleto_Laboratorio_XYZ.pdf").delete();
        mockMvc.perform(get("/api/public/folleto"))
                .andExpect(status().isNotFound());
    }
}
