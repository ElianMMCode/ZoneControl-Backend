package laboratorioxyz.com.ZoneControl.modulo_administracion.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import laboratorioxyz.com.ZoneControl.modulo_administracion.dto.AdminStatsResponse;
import laboratorioxyz.com.ZoneControl.modulo_administracion.service.AdminUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/stats")
@RequiredArgsConstructor
@Tag(name = "Módulo Administración", description = "Gestión de usuarios internos (solo ADMIN)")
public class AdminStatsController {

    private final AdminUserService adminUserService;

    @Operation(summary = "Indicadores del dashboard",
            description = "Contadores agregados para las tarjetas KPI del dashboard del administrador: " +
                    "usuarios por estado y pendientes de configuración de contraseña, empleados por estado " +
                    "y permisos por estado.")
    @ApiResponse(responseCode = "200", description = "Indicadores agregados")
    @GetMapping
    public ResponseEntity<AdminStatsResponse> getStats() {
        return ResponseEntity.ok(adminUserService.getStats());
    }
}
