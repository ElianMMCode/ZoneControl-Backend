package laboratorioxyz.com.ZoneControl.modulo_gestion_personal.controller;

import laboratorioxyz.com.ZoneControl.modulo_gestion_personal.dto.CreatePermissionRequest;
import laboratorioxyz.com.ZoneControl.modulo_gestion_personal.dto.PermissionResponse;
import laboratorioxyz.com.ZoneControl.modulo_gestion_personal.service.PermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/permisos")
@RequiredArgsConstructor
public class PermissionController {

    private final PermissionService permissionService;

    @PostMapping
    public ResponseEntity<PermissionResponse> grant(@RequestBody CreatePermissionRequest request) {
        PermissionResponse response = permissionService.grant(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> revoke(@PathVariable UUID id) {
        Map<String, String> response = permissionService.revoke(id);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/suspend")
    public ResponseEntity<PermissionResponse> suspend(
            @PathVariable UUID id,
            @RequestBody Map<String, String> body) {
        LocalDate reactivationDate = LocalDate.parse(body.get("reactivationDate"));
        PermissionResponse response = permissionService.suspend(id, reactivationDate);
        return ResponseEntity.ok(response);
    }
}
