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

/**
 * Controlador de gestión de personal.
 * POST /personal para registrar un nuevo empleado con generación
 * automática de código EMP-XXXXXX.
 *
 * Respuestas:
 * - 201 Created + { id, employeeCode, firstName, lastName }
 * - 400 Bad Request si faltan campos obligatorios o tipo documento inválido
 * - 409 Conflict si ya existe la combinación tipo+número de documento
 */
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
