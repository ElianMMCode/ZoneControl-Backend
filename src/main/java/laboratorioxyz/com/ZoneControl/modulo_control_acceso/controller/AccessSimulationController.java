package laboratorioxyz.com.ZoneControl.modulo_control_acceso.controller;

import laboratorioxyz.com.ZoneControl.modulo_control_acceso.dto.SimulateAccessRequest;
import laboratorioxyz.com.ZoneControl.modulo_control_acceso.dto.SimulateAccessResponse;
import laboratorioxyz.com.ZoneControl.modulo_control_acceso.service.AccessSimulationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/access")
@RequiredArgsConstructor
public class AccessSimulationController {

    private final AccessSimulationService accessSimulationService;

    @PostMapping("/simulate")
    public ResponseEntity<SimulateAccessResponse> simulate(@RequestBody SimulateAccessRequest request) {
        SimulateAccessResponse response = accessSimulationService.simulate(
                request.getEmployeeCode(), request.getProductionAreaId());
        return ResponseEntity.ok(response);
    }
}
