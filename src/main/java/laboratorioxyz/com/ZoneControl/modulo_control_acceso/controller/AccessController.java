package laboratorioxyz.com.ZoneControl.modulo_control_acceso.controller;

import laboratorioxyz.com.ZoneControl.modulo_control_acceso.dto.ValidateAccessRequest;
import laboratorioxyz.com.ZoneControl.modulo_control_acceso.dto.ValidateAccessResponse;
import laboratorioxyz.com.ZoneControl.modulo_control_acceso.service.AccessValidationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/access")
@RequiredArgsConstructor
public class AccessController {

    private final AccessValidationService accessValidationService;

    @PostMapping("/validate")
    public ResponseEntity<ValidateAccessResponse> validate(@RequestBody ValidateAccessRequest request) {
        ValidateAccessResponse response = accessValidationService.validate(
                request.getEmployeeCode(), request.getProductionAreaName());
        return ResponseEntity.ok(response);
    }
}
