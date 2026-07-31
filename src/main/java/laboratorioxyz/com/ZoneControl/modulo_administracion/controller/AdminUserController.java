package laboratorioxyz.com.ZoneControl.modulo_administracion.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import laboratorioxyz.com.ZoneControl.model.enums.Role;
import laboratorioxyz.com.ZoneControl.model.enums.UserStatus;
import laboratorioxyz.com.ZoneControl.modulo_administracion.dto.ResetPasswordResponse;
import laboratorioxyz.com.ZoneControl.modulo_administracion.dto.StatusUpdateRequest;
import laboratorioxyz.com.ZoneControl.modulo_administracion.dto.UpdateUserRequest;
import laboratorioxyz.com.ZoneControl.modulo_administracion.dto.UserResponse;
import laboratorioxyz.com.ZoneControl.modulo_administracion.service.AdminUserService;
import laboratorioxyz.com.ZoneControl.modulo_autenticacion.dto.CreateUserRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/admin/users")
@RequiredArgsConstructor
@Tag(name = "Módulo Administración", description = "Gestión de usuarios internos (solo ADMIN)")
public class AdminUserController {

    private final AdminUserService adminUserService;

    @Operation(summary = "Listar usuarios del sistema",
            description = "Lista paginada de usuarios con búsqueda por nombre, email o código de empleado, " +
                    "y filtros por rol y estado. Usado por la tabla de Gestión de Usuarios.")
    @ApiResponse(responseCode = "200", description = "Lista de usuarios paginada")
    @GetMapping
    public ResponseEntity<Page<UserResponse>> list(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Role role,
            @RequestParam(required = false) UserStatus status,
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(adminUserService.list(search, role, status, pageable));
    }

    @Operation(summary = "Detalle de un usuario",
            description = "Retorna los datos del usuario incluyendo su empleado vinculado.")
    @ApiResponse(responseCode = "200", description = "Detalle del usuario")
    @ApiResponse(responseCode = "404", description = "Usuario no encontrado")
    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(adminUserService.getById(id));
    }

    @Operation(summary = "Crear usuario con magic link",
            description = "Crea un usuario vinculado a un empleado existente. El sistema genera un token de un solo uso " +
                    "y envía un magic link al correo del empleado para que establezca su propia contraseña en 24h. " +
                    "El administrador nunca ve ni define la contraseña.")
    @ApiResponse(responseCode = "201", description = "Usuario creado, magic link enviado por email")
    @ApiResponse(responseCode = "400", description = "Empleado no encontrado o sin correo registrado")
    @ApiResponse(responseCode = "409", description = "Email duplicado o empleado ya vinculado a otro usuario")
    @PostMapping
    public ResponseEntity<Map<String, UUID>> create(@Valid @RequestBody CreateUserRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(adminUserService.create(request));
    }

    @Operation(summary = "Cambiar estado de un usuario",
            description = "Activa o desactiva un usuario, aplicando cascada a su empleado y permisos de acceso.")
    @ApiResponse(responseCode = "200", description = "Estado actualizado")
    @ApiResponse(responseCode = "400", description = "No puede desactivar su propia cuenta")
    @ApiResponse(responseCode = "404", description = "Usuario no encontrado")
    @PatchMapping("/{id}/status")
    public ResponseEntity<Map<String, Object>> updateStatus(
            @PathVariable UUID id,
            @Valid @RequestBody StatusUpdateRequest request) {
        String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.ok(adminUserService.updateStatus(id, request, currentUserEmail));
    }

    @Operation(summary = "Editar usuario",
            description = "Modifica nombre, apellido, email, rol o empleado vinculado de un usuario existente.")
    @ApiResponse(responseCode = "200", description = "Usuario actualizado")
    @ApiResponse(responseCode = "404", description = "Usuario no encontrado")
    @ApiResponse(responseCode = "409", description = "Email duplicado")
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateUserRequest request) {
        return ResponseEntity.ok(adminUserService.update(id, request));
    }

    @Operation(summary = "Restablecer contraseña",
            description = "Invalida la contraseña actual y envía un enlace de configuración (magic link) de un solo uso " +
                    "al correo personal del empleado para que el propio usuario establezca una nueva contraseña. " +
                    "El enlace expira en 24 horas.")
    @ApiResponse(responseCode = "200", description = "Enlace de configuración enviado al correo del usuario")
    @ApiResponse(responseCode = "400", description = "El empleado no tiene un correo registrado")
    @ApiResponse(responseCode = "404", description = "Usuario no encontrado")
    @PostMapping("/{id}/reset-password")
    public ResponseEntity<ResetPasswordResponse> resetPassword(@PathVariable UUID id) {
        return ResponseEntity.ok(adminUserService.resetPassword(id));
    }
}
