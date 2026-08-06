package laboratorioxyz.com.ZoneControl.modulo_administracion.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * HU-27 / §9 item 1.5: GET /api/admin/role-matrix (solo ADMIN) devuelve la
 * matriz módulo × rol → nivel de acceso (NINGUNO/LECTURA/ESCRITURA)
 * reconstruida desde SecurityConfig.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@Rollback
class RoleMatrixControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser(roles = "ADMIN")
    void getRoleMatrix_asAdmin_returnsMatrix() throws Exception {
        mockMvc.perform(get("/api/admin/role-matrix"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.roles.length()").value(3))
                .andExpect(jsonPath("$.roles[0]").value("ADMIN"))
                .andExpect(jsonPath("$.modules").isArray())
                .andExpect(jsonPath("$.modules[0].module").exists())
                .andExpect(jsonPath("$.modules[0].access.ADMIN").value("ESCRITURA"))
                .andExpect(jsonPath("$.modules[0].access.GESTOR_PERSONAL").value("NINGUNO"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getRoleMatrix_includesCargosModuleWithLevels() throws Exception {
        mockMvc.perform(get("/api/admin/role-matrix"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.modules[?(@.module=='Cargos')].access.ADMIN").value("ESCRITURA"))
                .andExpect(jsonPath("$.modules[?(@.module=='Cargos')].access.GESTOR_PERSONAL").value("LECTURA"))
                .andExpect(jsonPath("$.modules[?(@.module=='Cargos')].access.SUPERVISOR_AUDITOR").value("LECTURA"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getRoleMatrix_supervisorHasReadOnAreasAndPersonal() throws Exception {
        mockMvc.perform(get("/api/admin/role-matrix"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.modules[?(@.module=='Áreas de producción')].access.SUPERVISOR_AUDITOR").value("LECTURA"))
                .andExpect(jsonPath("$.modules[?(@.module=='Gestión de personal')].access.SUPERVISOR_AUDITOR").value("LECTURA"))
                .andExpect(jsonPath("$.modules[?(@.module=='Permisos de acceso')].access.SUPERVISOR_AUDITOR").value("LECTURA"))
                .andExpect(jsonPath("$.modules[?(@.module=='Control de acceso físico')].access.SUPERVISOR_AUDITOR").value("ESCRITURA"));
    }

    @Test
    @WithMockUser(roles = "GESTOR_PERSONAL")
    void getRoleMatrix_asGestor_returns403() throws Exception {
        mockMvc.perform(get("/api/admin/role-matrix"))
                .andExpect(status().isForbidden());
    }
}
