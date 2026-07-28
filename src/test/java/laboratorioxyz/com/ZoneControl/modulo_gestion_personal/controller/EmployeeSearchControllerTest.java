package laboratorioxyz.com.ZoneControl.modulo_gestion_personal.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import laboratorioxyz.com.ZoneControl.model.entity.Department;
import laboratorioxyz.com.ZoneControl.model.enums.DocumentType;
import laboratorioxyz.com.ZoneControl.model.repository.DepartmentRepository;
import laboratorioxyz.com.ZoneControl.modulo_gestion_personal.dto.RegisterEmployeeRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@Rollback
class EmployeeSearchControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private DepartmentRepository departmentRepository;

    private Department department;

    @BeforeEach
    void setUp() {
        department = departmentRepository.findByName("Control de Calidad").orElseThrow();
    }

    @Test
    void searchEmployees_withFilters_returnsPaginatedResults() throws Exception {
        var empReq = RegisterEmployeeRequest.builder()
                .documentType(DocumentType.CC)
                .documentNumber("1111111111")
                .firstName("Carlos")
                .lastName("Mendoza")
                .position("Técnico")
                .departmentId(department.getId())
                .build();

        mockMvc.perform(post("/personal")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(empReq)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/personal")
                        .param("firstName", "Carlos")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].firstName").value("Carlos"))
                .andExpect(jsonPath("$.content[0].lastName").value("Mendoza"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void searchEmployees_noFilters_returns400() throws Exception {
        mockMvc.perform(get("/personal"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Debe seleccionar al menos un filtro de búsqueda"));
    }

    @Test
    void searchEmployees_noResults_returnsEmptyPage() throws Exception {
        mockMvc.perform(get("/personal")
                        .param("firstName", "NoExiste")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    @Test
    void searchEmployees_byDocumentNumber_returnsResults() throws Exception {
        var emp1 = RegisterEmployeeRequest.builder()
                .documentType(DocumentType.CC)
                .documentNumber("2222222222")
                .firstName("Ana")
                .lastName("López")
                .position("Analista")
                .departmentId(department.getId())
                .build();

        var emp2 = RegisterEmployeeRequest.builder()
                .documentType(DocumentType.CC)
                .documentNumber("3333333333")
                .firstName("Luis")
                .lastName("García")
                .position("Técnico")
                .departmentId(department.getId())
                .build();

        mockMvc.perform(post("/personal")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(emp1)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/personal")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(emp2)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/personal")
                        .param("documentNumber", "222")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].firstName").value("Ana"));
    }

    @Test
    void searchEmployees_multipleFiltersAnd_returnsIntersection() throws Exception {
        var emp1 = RegisterEmployeeRequest.builder()
                .documentType(DocumentType.CC)
                .documentNumber("4444444444")
                .firstName("Pedro")
                .lastName("Ramírez")
                .position("Técnico")
                .departmentId(department.getId())
                .build();

        var emp2 = RegisterEmployeeRequest.builder()
                .documentType(DocumentType.CC)
                .documentNumber("5555555555")
                .firstName("Pedro")
                .lastName("Pérez")
                .position("Técnico")
                .departmentId(department.getId())
                .build();

        mockMvc.perform(post("/personal")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(emp1)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/personal")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(emp2)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/personal")
                        .param("firstName", "Pedro")
                        .param("lastName", "Ramírez")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].documentNumber").value("4444444444"));
    }
}
