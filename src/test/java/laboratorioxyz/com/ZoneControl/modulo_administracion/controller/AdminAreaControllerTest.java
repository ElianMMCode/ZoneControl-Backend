package laboratorioxyz.com.ZoneControl.modulo_administracion.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import laboratorioxyz.com.ZoneControl.model.entity.ProductionArea;
import laboratorioxyz.com.ZoneControl.model.repository.ProductionAreaRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@Rollback
@WithMockUser(username = "admin@zonecontrol.com", roles = "ADMIN")
class AdminAreaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProductionAreaRepository areaRepository;

    @Test
    void listAreas_returnsAll() throws Exception {
        int baseCount = (int) areaRepository.count();

        mockMvc.perform(get("/admin/areas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(baseCount)));
    }

    @Test
    void createArea_valid_returns201() throws Exception {
        var body = Map.of("name", "Zona Estéril", "description", "Área de máxima seguridad biológica");

        mockMvc.perform(post("/admin/areas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Zona Estéril"))
                .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    void createArea_duplicateName_returns409() throws Exception {
        var body = Map.of("name", "Sala Blanca A", "description", "Duplicado");

        mockMvc.perform(post("/admin/areas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Ya existe un área con el nombre 'Sala Blanca A'"));
    }

    @Test
    void updateArea_valid_returns200() throws Exception {
        ProductionArea area = areaRepository.findByName("Sala Blanca A").orElseThrow();
        var body = Map.of("name", "Sala Blanca A Mod", "description", "Actualizada");

        mockMvc.perform(put("/admin/areas/{id}", area.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Sala Blanca A Mod"))
                .andExpect(jsonPath("$.description").value("Actualizada"));
    }

    @Test
    void updateArea_nonExistent_returns404() throws Exception {
        var body = Map.of("name", "No existe", "description", "x");

        mockMvc.perform(put("/admin/areas/{id}", UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Área no encontrada"));
    }

    @Test
    void setActive_deactivateAndReactivate_returns200() throws Exception {
        ProductionArea area = areaRepository.findByName("Sala Blanca A").orElseThrow();

        mockMvc.perform(patch("/admin/areas/{id}/status", area.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("active", false))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(false));

        mockMvc.perform(patch("/admin/areas/{id}/status", area.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("active", true))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    void setActive_nonExistent_returns404() throws Exception {
        mockMvc.perform(patch("/admin/areas/{id}/status", UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("active", false))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Área no encontrada"));
    }
}
