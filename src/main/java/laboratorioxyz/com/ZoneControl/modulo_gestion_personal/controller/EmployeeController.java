package laboratorioxyz.com.ZoneControl.modulo_gestion_personal.controller;

import jakarta.validation.Valid;
import laboratorioxyz.com.ZoneControl.modulo_gestion_personal.dto.RegisterEmployeeRequest;
import laboratorioxyz.com.ZoneControl.modulo_gestion_personal.dto.RegisterEmployeeResponse;
import laboratorioxyz.com.ZoneControl.modulo_gestion_personal.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/personal")
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService employeeService;

    @PostMapping
    public ResponseEntity<RegisterEmployeeResponse> register(
            @Valid @RequestBody RegisterEmployeeRequest request) {
        RegisterEmployeeResponse response = employeeService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
