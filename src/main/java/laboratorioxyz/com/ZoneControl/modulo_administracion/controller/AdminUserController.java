package laboratorioxyz.com.ZoneControl.modulo_administracion.controller;

import jakarta.validation.Valid;
import laboratorioxyz.com.ZoneControl.modulo_administracion.dto.ResetPasswordResponse;
import laboratorioxyz.com.ZoneControl.modulo_administracion.dto.StatusUpdateRequest;
import laboratorioxyz.com.ZoneControl.modulo_administracion.dto.UpdateUserRequest;
import laboratorioxyz.com.ZoneControl.modulo_administracion.service.AdminUserService;
import laboratorioxyz.com.ZoneControl.modulo_autenticacion.dto.CreateUserRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final AdminUserService adminUserService;

    @PostMapping
    public ResponseEntity<Map<String, UUID>> create(@Valid @RequestBody CreateUserRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(adminUserService.create(request));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<Map<String, Object>> updateStatus(
            @PathVariable UUID id,
            @Valid @RequestBody StatusUpdateRequest request) {
        String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.ok(adminUserService.updateStatus(id, request, currentUserEmail));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateUserRequest request) {
        return ResponseEntity.ok(adminUserService.update(id, request));
    }

    @PostMapping("/{id}/reset-password")
    public ResponseEntity<ResetPasswordResponse> resetPassword(@PathVariable UUID id) {
        return ResponseEntity.ok(adminUserService.resetPassword(id));
    }
}
