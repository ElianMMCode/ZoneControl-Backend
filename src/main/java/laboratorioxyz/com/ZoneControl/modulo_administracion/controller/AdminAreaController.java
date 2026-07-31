package laboratorioxyz.com.ZoneControl.modulo_administracion.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import laboratorioxyz.com.ZoneControl.modulo_administracion.dto.AreaRequest;
import laboratorioxyz.com.ZoneControl.modulo_administracion.dto.AreaResponse;
import laboratorioxyz.com.ZoneControl.modulo_administracion.service.AdminAreaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/admin/areas")
@RequiredArgsConstructor
@Tag(name = "Módulo Administración", description = "Gestión de usuarios internos y configuración (solo ADMIN)")
public class AdminAreaController {

    private final AdminAreaService adminAreaService;

    @Operation(summary = "Listar áreas de producción",
            description = "Retorna todas las áreas de producción, activas e inactivas (para gestión).")
    @ApiResponse(responseCode = "200", description = "Lista de áreas")
    @GetMapping
    public ResponseEntity<List<AreaResponse>> list() {
        return ResponseEntity.ok(adminAreaService.list());
    }

    @Operation(summary = "Crear área de producción",
            description = "Crea una nueva área restringida de producción. El nombre debe ser único.")
    @ApiResponse(responseCode = "201", description = "Área creada")
    @ApiResponse(responseCode = "409", description = "Ya existe un área con ese nombre")
    @PostMapping
    public ResponseEntity<AreaResponse> create(@Valid @RequestBody AreaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(adminAreaService.create(request));
    }

    @Operation(summary = "Editar área de producción",
            description = "Modifica el nombre y/o descripción de un área existente.")
    @ApiResponse(responseCode = "200", description = "Área actualizada")
    @ApiResponse(responseCode = "404", description = "Área no encontrada")
    @ApiResponse(responseCode = "409", description = "El nuevo nombre ya existe en otra área")
    @PutMapping("/{id}")
    public ResponseEntity<AreaResponse> update(@PathVariable UUID id, @Valid @RequestBody AreaRequest request) {
        return ResponseEntity.ok(adminAreaService.update(id, request));
    }

    @Operation(summary = "Activar / desactivar área",
            description = "Desactiva un área en lugar de eliminarla (preserva los permisos e historial asociados). " +
                    "Las áreas inactivas no aparecen en el selector de permisos ni en la información pública.")
    @ApiResponse(responseCode = "200", description = "Estado actualizado")
    @ApiResponse(responseCode = "404", description = "Área no encontrada")
    @PatchMapping("/{id}/status")
    public ResponseEntity<Map<String, Object>> setActive(@PathVariable UUID id,
                                                          @RequestBody Map<String, Boolean> body) {
        boolean active = body.getOrDefault("active", true);
        adminAreaService.setActive(id, active);
        return ResponseEntity.ok(Map.of("id", id, "active", active));
    }
}
