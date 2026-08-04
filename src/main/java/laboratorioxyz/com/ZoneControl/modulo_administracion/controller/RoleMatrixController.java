package laboratorioxyz.com.ZoneControl.modulo_administracion.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import laboratorioxyz.com.ZoneControl.modulo_administracion.dto.RoleMatrixResponse;
import laboratorioxyz.com.ZoneControl.modulo_administracion.service.RoleMatrixService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/role-matrix")
@RequiredArgsConstructor
@Tag(name = "Módulo Administración", description = "Matriz de roles y permisos (solo ADMIN)")
public class RoleMatrixController {

    private final RoleMatrixService roleMatrixService;

    @Operation(summary = "Matriz de roles y permisos",
            description = "Matriz módulo × rol → booleano reconstruida desde SecurityConfig. Solo consulta (HU-27).")
    @ApiResponse(responseCode = "200", description = "Matriz de roles")
    @GetMapping
    public ResponseEntity<RoleMatrixResponse> getMatrix() {
        return ResponseEntity.ok(roleMatrixService.getMatrix());
    }
}
