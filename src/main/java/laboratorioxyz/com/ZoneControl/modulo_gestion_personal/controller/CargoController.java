package laboratorioxyz.com.ZoneControl.modulo_gestion_personal.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import laboratorioxyz.com.ZoneControl.modulo_gestion_personal.dto.PositionResponse;
import laboratorioxyz.com.ZoneControl.modulo_gestion_personal.service.CargoService;
import laboratorioxyz.com.ZoneControl.model.enums.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/personal/cargos")
@RequiredArgsConstructor
@Tag(name = "Módulo Gestión Personal", description = "Catálogo de cargos (el rol de usuario deriva del cargo)")
public class CargoController {

    private final CargoService cargoService;

    @Operation(summary = "Listar cargos del catálogo",
            description = "Catálogo de cargos con el rol de sistema que cada uno define (si lo tiene).")
    @ApiResponse(responseCode = "200", description = "Lista de cargos")
    @GetMapping
    public ResponseEntity<List<PositionResponse>> list() {
        return ResponseEntity.ok(cargoService.list());
    }

    @Operation(summary = "Crear cargo",
            description = "Crea un cargo con su rol de sistema (opcional). Nombre único, máximo 40 caracteres.")
    @ApiResponse(responseCode = "201", description = "Cargo creado")
    @ApiResponse(responseCode = "400", description = "Datos inválidos")
    @ApiResponse(responseCode = "409", description = "Ya existe un cargo con ese nombre")
    @PostMapping
    public ResponseEntity<PositionResponse> create(@RequestBody Map<String, String> body) {
        PositionResponse response = cargoService.create(
                body.get("name"), roleOrNull(body.get("systemRole")));
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Editar cargo",
            description = "Actualiza el nombre y/o el rol de sistema de un cargo; sincroniza el rol en los empleados vinculados.")
    @ApiResponse(responseCode = "200", description = "Cargo actualizado")
    @ApiResponse(responseCode = "404", description = "Cargo no encontrado")
    @ApiResponse(responseCode = "409", description = "Ya existe un cargo con ese nombre")
    @PutMapping("/{id}")
    public ResponseEntity<PositionResponse> update(
            @PathVariable UUID id,
            @RequestBody Map<String, String> body) {
        PositionResponse response = cargoService.update(
                id, body.get("name"), roleOrNull(body.get("systemRole")));
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Eliminar cargo",
            description = "Elimina el cargo si no tiene empleados vinculados.")
    @ApiResponse(responseCode = "200", description = "Cargo eliminado")
    @ApiResponse(responseCode = "404", description = "Cargo no encontrado")
    @ApiResponse(responseCode = "409", description = "El cargo tiene empleados vinculados")
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> delete(@PathVariable UUID id) {
        cargoService.delete(id);
        return ResponseEntity.ok(Map.of("message", "Cargo eliminado exitosamente"));
    }

    private Role roleOrNull(String value) {
        if (value == null || value.isBlank()) return null;
        return Role.valueOf(value);
    }
}
