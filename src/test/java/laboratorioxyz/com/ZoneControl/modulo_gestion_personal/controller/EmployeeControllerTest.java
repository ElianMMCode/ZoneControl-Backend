package laboratorioxyz.com.ZoneControl.modulo_gestion_personal.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import laboratorioxyz.com.ZoneControl.model.enums.DocumentType;
import laboratorioxyz.com.ZoneControl.modulo_gestion_personal.dto.RegisterEmployeeRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@Rollback
@WithMockUser(roles = "ADMIN")
class EmployeeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
    }

    @Test
    void registerEmployee_validData_returns201() throws Exception {
        var request = RegisterEmployeeRequest.builder()
                .documentType(DocumentType.CC)
                .documentNumber("1234567890")
                .firstName("Carlos")
                .lastName("Mendoza")
                .position("Técnico")
                .departmentName("Control de Calidad")
                .build();

        mockMvc.perform(post("/personal")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.employeeCode").isString())
                .andExpect(jsonPath("$.firstName").value("Carlos"))
                .andExpect(jsonPath("$.lastName").value("Mendoza"));
    }

    @Test
    void registerEmployee_duplicateDocument_returns409() throws Exception {
        var dupDoc = "9876543210";
        var request = RegisterEmployeeRequest.builder()
                .documentType(DocumentType.CC)
                .documentNumber(dupDoc)
                .firstName("Ana")
                .lastName("López")
                .position("Analista")
                .departmentName("Control de Calidad")
                .build();

        mockMvc.perform(post("/personal")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/personal")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value(
                        "Ya existe un empleado registrado con el documento CC número " + dupDoc));
    }

    @Test
    void registerEmployee_missingFields_returns400() throws Exception {
        var request = RegisterEmployeeRequest.builder()
                .documentType(DocumentType.CC)
                .documentNumber("")
                .firstName("")
                .lastName("")
                .position("")
                .departmentName(null)
                .build();

        mockMvc.perform(post("/personal")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
