package laboratorioxyz.com.ZoneControl.modulo_gestion_personal.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import laboratorioxyz.com.ZoneControl.model.entity.Department;
import laboratorioxyz.com.ZoneControl.model.enums.DocumentType;
import laboratorioxyz.com.ZoneControl.model.enums.EmployeeStatus;
import laboratorioxyz.com.ZoneControl.model.repository.DepartmentRepository;
import laboratorioxyz.com.ZoneControl.modulo_gestion_personal.model.Employee;
import laboratorioxyz.com.ZoneControl.modulo_gestion_personal.repository.EmployeeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@Rollback
@WithMockUser(roles = "ADMIN")
class BulkUploadControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    private Department department;

    @BeforeEach
    void setUp() {
        department = departmentRepository.findByName("Control de Calidad").orElseThrow();
    }

    @Test
    void downloadTemplate_returnsCsv() throws Exception {
        mockMvc.perform(get("/personal/bulk/plantilla"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("text/csv"))
                .andExpect(content().string(
                        org.hamcrest.Matchers.startsWith("tipo_documento;documento_identidad;nombres;apellidos;cargo;departamento;estado")));
    }

    @Test
    void uploadBulk_validCsv_returns200() throws Exception {
        String csv = "tipo_documento;documento_identidad;nombres;apellidos;cargo;departamento;estado\n"
                + "CC;1234567890;Juan;Pérez;Analista;Control de Calidad;ACTIVO\n"
                + "CE;9876543210;María;Gómez;Técnico;Control de Calidad;ACTIVO";

        MockMultipartFile file = new MockMultipartFile(
                "file", "empleados.csv", "text/csv", csv.getBytes(StandardCharsets.UTF_8));

        mockMvc.perform(multipart("/personal/bulk").file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(2))
                .andExpect(jsonPath("$.successes").value(2))
                .andExpect(jsonPath("$.errors").value(0));
    }

    @Test
    void uploadBulk_invalidExtension_returns400() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "empleados.pdf", "application/pdf", "fake content".getBytes());

        mockMvc.perform(multipart("/personal/bulk").file(file))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(
                        "Extensión de archivo no permitida. Solo se aceptan archivos .csv y .txt"));
    }

    @Test
    void uploadBulk_wrongHeaders_returns400() throws Exception {
        String csv = "nombre;edad;cargo\n"
                + "Juan;30;Analista";

        MockMultipartFile file = new MockMultipartFile(
                "file", "mal.csv", "text/csv", csv.getBytes(StandardCharsets.UTF_8));

        mockMvc.perform(multipart("/personal/bulk").file(file))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(
                        org.hamcrest.Matchers.containsString("encabezados")));
    }

    @Test
    void uploadBulk_mixedRows_returnsSummaryWithErrors() throws Exception {
        String csv = "tipo_documento;documento_identidad;nombres;apellidos;cargo;departamento;estado\n"
                + "CC;1111111111;Ana;López;Analista;Control de Calidad;ACTIVO\n"
                + "XX;2222222222;Pedro;Ramírez;Técnico;Control de Calidad;ACTIVO\n"
                + "CC;3333333333;Luis;García;Analista;Control de Calidad;ACTIVO";

        MockMultipartFile file = new MockMultipartFile(
                "file", "data.csv", "text/csv", csv.getBytes(StandardCharsets.UTF_8));

        mockMvc.perform(multipart("/personal/bulk").file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(3))
                .andExpect(jsonPath("$.successes").value(2))
                .andExpect(jsonPath("$.errors").value(1))
                .andExpect(jsonPath("$.errorReportUrl").isString());
    }

    @Test
    void uploadBulk_duplicateDocument_returnsPartialSuccess() throws Exception {
        String nextCode = employeeRepository.findMaxEmployeeCode();
        employeeRepository.save(Employee.builder()
                .employeeCode(nextCode != null ? "EMP-000099" : "EMP-000001")
                .documentType(DocumentType.CC)
                .documentNumber("9999999999")
                .firstName("Existente")
                .lastName("Ya")
                .position("Test")
                .department(department)
                .status(EmployeeStatus.ACTIVO)
                .build());

        String csv = "tipo_documento;documento_identidad;nombres;apellidos;cargo;departamento;estado\n"
                + "CC;9999999999;Duplicado;Error;Test;Control de Calidad;ACTIVO\n"
                + "CC;8888888888;Nuevo;Ok;Analista;Control de Calidad;ACTIVO";

        MockMultipartFile file = new MockMultipartFile(
                "file", "data.csv", "text/csv", csv.getBytes(StandardCharsets.UTF_8));

        mockMvc.perform(multipart("/personal/bulk").file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(2))
                .andExpect(jsonPath("$.successes").value(1))
                .andExpect(jsonPath("$.errors").value(1));
    }

    @Test
    void uploadBulk_noFile_returns400() throws Exception {
        MockMultipartFile empty = new MockMultipartFile("file", new byte[0]);
        mockMvc.perform(multipart("/personal/bulk").file(empty))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Debe adjuntar un archivo para procesar"));
    }
}
