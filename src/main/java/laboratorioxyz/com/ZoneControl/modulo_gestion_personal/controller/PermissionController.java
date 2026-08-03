package laboratorioxyz.com.ZoneControl.modulo_gestion_personal.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import laboratorioxyz.com.ZoneControl.model.entity.ProductionArea;
import laboratorioxyz.com.ZoneControl.model.enums.PermissionStatus;
import laboratorioxyz.com.ZoneControl.modulo_gestion_personal.dto.CreatePermissionRequest;
import laboratorioxyz.com.ZoneControl.modulo_gestion_personal.dto.PermissionResponse;
import laboratorioxyz.com.ZoneControl.modulo_gestion_personal.dto.UpdatePermissionRequest;
import laboratorioxyz.com.ZoneControl.modulo_gestion_personal.service.PermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/permisos")
@RequiredArgsConstructor
@Tag(name = "Módulo Gestión Personal", description = "Gestión de permisos de acceso a áreas (GESTOR_PERSONAL)")
public class PermissionController {

    private final PermissionService permissionService;

    @Operation(summary = "Otorgar permiso de acceso",
            description = "Crea un permiso de acceso para un empleado activo sobre un área restringida. " +
                    "Regla de unicidad: un permiso por (empleado, área). Si ya existe uno se retorna 409 " +
                    "indicando que edite el existente.")
    @ApiResponse(responseCode = "201", description = "Permiso otorgado")
    @ApiResponse(responseCode = "400", description = "Empleado inactivo o área no encontrada")
    @ApiResponse(responseCode = "404", description = "Empleado no encontrado")
    @ApiResponse(responseCode = "409", description = "El empleado ya tiene un permiso para esta área")
    @PostMapping
    public ResponseEntity<PermissionResponse> grant(@RequestBody CreatePermissionRequest request) {
        PermissionResponse response = permissionService.grant(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Revocar permiso",
            description = "Elimina definitivamente un permiso de acceso.")
    @ApiResponse(responseCode = "200", description = "Permiso revocado")
    @ApiResponse(responseCode = "404", description = "Permiso no encontrado")
    public ResponseEntity<Map<String, String>> revoke(@PathVariable UUID id) {
        Map<String, String> response = permissionService.revoke(id);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/suspend")
    @Operation(summary = "Suspender permiso",
            description = "Suspende temporalmente un permiso hasta una fecha de reactivación.")
    @ApiResponse(responseCode = "200", description = "Permiso suspendido")
    @ApiResponse(responseCode = "404", description = "Permiso no encontrado")
    public ResponseEntity<PermissionResponse> suspend(
            @PathVariable UUID id,
            @RequestBody Map<String, String> body) {
        LocalDate reactivationDate = LocalDate.parse(body.get("reactivationDate"));
        PermissionResponse response = permissionService.suspend(id, reactivationDate);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/reactivate")
    @Operation(summary = "Reactivar permiso",
            description = "Reactiva un permiso previamente suspendido y limpia la fecha de reactivación.")
    @ApiResponse(responseCode = "200", description = "Permiso reactivado")
    @ApiResponse(responseCode = "404", description = "Permiso no encontrado")
    public ResponseEntity<PermissionResponse> reactivate(@PathVariable UUID id) {
        return ResponseEntity.ok(permissionService.reactivate(id));
    }

    @Operation(summary = "Listar permisos",
            description = "Lista paginada de permisos con búsqueda por código de empleado, nombre o área y filtro por estado.")
    @ApiResponse(responseCode = "200", description = "Lista de permisos paginada")
    @GetMapping
    public ResponseEntity<Page<PermissionResponse>> list(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) PermissionStatus status,
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(permissionService.list(search, status, pageable));
    }

    @Operation(summary = "Listar áreas de producción",
            description = "Catálogo de áreas restringidas disponibles para asignar permisos.")
    @ApiResponse(responseCode = "200", description = "Lista de áreas")
    @GetMapping("/areas")
    public ResponseEntity<List<ProductionArea>> listAreas() {
        return ResponseEntity.ok(permissionService.listAreas());
    }

    @PostMapping("/areas")
    @Operation(summary = "Crear área de producción",
            description = "Crea un área nueva. Nombre único, máximo 30 caracteres.")
    @ApiResponse(responseCode = "201", description = "Área creada")
    @ApiResponse(responseCode = "400", description = "Datos inválidos")
    @ApiResponse(responseCode = "409", description = "Ya existe un área con ese nombre")
    public ResponseEntity<ProductionArea> createArea(@RequestBody Map<String, String> body) {
        ProductionArea area = permissionService.createArea(
                body.get("name"), body.get("description"));
        return ResponseEntity.status(HttpStatus.CREATED).body(area);
    }

    @PutMapping("/areas/{id}")
    @Operation(summary = "Editar área de producción")
    @ApiResponse(responseCode = "200", description = "Área actualizada")
    @ApiResponse(responseCode = "404", description = "Área no encontrada")
    public ResponseEntity<ProductionArea> updateArea(
            @PathVariable UUID id,
            @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(permissionService.updateArea(
                id, body.get("name"), body.get("description")));
    }

    @DeleteMapping("/areas/{id}")
    @Operation(summary = "Eliminar área de producción",
            description = "Elimina el área si no tiene permisos activos asociados.")
    @ApiResponse(responseCode = "200", description = "Área eliminada")
    @ApiResponse(responseCode = "404", description = "Área no encontrada")
    @ApiResponse(responseCode = "409", description = "El área tiene permisos activos asociados")
    public ResponseEntity<Map<String, String>> deleteArea(@PathVariable UUID id) {
        permissionService.deleteArea(id);
        return ResponseEntity.ok(Map.of("message", "Área eliminada exitosamente"));
    }

    @Operation(summary = "Editar permiso",
            description = "Modifica horarios y fechas de vigencia de un permiso ACTIVO existente. " +
                    "No permite cambiar el empleado ni el área.")
    @ApiResponse(responseCode = "200", description = "Permiso actualizado")
    @ApiResponse(responseCode = "400", description = "No se puede editar un permiso suspendido")
    @ApiResponse(responseCode = "404", description = "Permiso no encontrado")
    @PatchMapping("/{id}")
    public ResponseEntity<PermissionResponse> update(
            @PathVariable UUID id,
            @RequestBody UpdatePermissionRequest request) {
        return ResponseEntity.ok(permissionService.update(id, request));
    }
}
