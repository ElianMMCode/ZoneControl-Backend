package laboratorioxyz.com.ZoneControl.modulo_publico.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
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
                .andExpect(jsonPath("$[0].name").exists())
                .andExpect(jsonPath("$[0].address").exists())
                .andExpect(jsonPath("$[0].latitude").exists())
                .andExpect(jsonPath("$[0].longitude").exists());
    }

    @Test
    void getCatalog_returns200() throws Exception {
        mockMvc.perform(get("/api/public/catalogo"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].name").exists())
                .andExpect(jsonPath("$[0].activeIngredient").exists())
                .andExpect(jsonPath("$[0].presentation").exists())
                .andExpect(jsonPath("$[0].productionArea").exists());
    }

    @Test
    void getBrochure_notFound_returns404() throws Exception {
        mockMvc.perform(get("/api/public/folleto"))
                .andExpect(status().isNotFound());
    }
}
