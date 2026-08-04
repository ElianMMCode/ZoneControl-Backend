package laboratorioxyz.com.ZoneControl.modulo_control_acceso.controller;

import laboratorioxyz.com.ZoneControl.model.repository.ProductionAreaRepository;
import laboratorioxyz.com.ZoneControl.modulo_control_acceso.dto.EmergencyRequest;
import laboratorioxyz.com.ZoneControl.modulo_control_acceso.dto.ExitRequest;
import laboratorioxyz.com.ZoneControl.modulo_control_acceso.dto.OccupancyResponse;
import laboratorioxyz.com.ZoneControl.modulo_control_acceso.dto.ValidateAccessRequest;
import laboratorioxyz.com.ZoneControl.modulo_control_acceso.dto.ValidateAccessResponse;
import laboratorioxyz.com.ZoneControl.modulo_control_acceso.model.AccessAlert;
import laboratorioxyz.com.ZoneControl.modulo_control_acceso.service.AccessMonitoringService;
import laboratorioxyz.com.ZoneControl.modulo_control_acceso.service.AccessValidationService;
import laboratorioxyz.com.ZoneControl.modulo_control_acceso.service.RealtimeEventPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/access")
@RequiredArgsConstructor
public class AccessController {

    private final AccessValidationService accessValidationService;
    private final AccessMonitoringService accessMonitoringService;
    private final RealtimeEventPublisher realtimeEventPublisher;
    private final ProductionAreaRepository productionAreaRepository;

    @PostMapping("/validate")
    public ResponseEntity<ValidateAccessResponse> validate(@RequestBody ValidateAccessRequest request) {
        ValidateAccessResponse response = accessValidationService.validate(
                request.getEmployeeCode(), request.getProductionAreaName());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/exit")
    public ResponseEntity<Map<String, String>> exit(@RequestBody ExitRequest request) {
        accessMonitoringService.exit(request.employeeCode(), request.productionAreaName());
        return ResponseEntity.ok(Map.of("message", "Salida registrada"));
    }

    @GetMapping("/occupancy")
    public ResponseEntity<OccupancyResponse> occupancy() {
        return ResponseEntity.ok(accessMonitoringService.occupancy());
    }

    @PostMapping("/zones/{name}/emergency")
    public ResponseEntity<Map<String, String>> setEmergency(
            @PathVariable String name, @RequestBody EmergencyRequest request) {
        accessMonitoringService.setEmergency(name, request.cerrada());
        return ResponseEntity.ok(Map.of("message",
                request.cerrada() ? "Zona cerrada por emergencia" : "Zona reabierta"));
    }

    @GetMapping("/alerts")
    public ResponseEntity<List<AccessAlert>> alerts(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime desde,
            @RequestParam(required = false) Boolean leido) {
        return ResponseEntity.ok(accessMonitoringService.alerts(desde, leido));
    }

    @GetMapping("/stream")
    public SseEmitter stream() {
        SseEmitter emitter = realtimeEventPublisher.subscribe();
        List<Map<String, Object>> zones = productionAreaRepository.findAll().stream()
                .map(z -> Map.<String, Object>of(
                        "name", z.getName(), "emergencyClosed", z.isEmergencyClosed()))
                .toList();
        Map<String, Object> snapshot = Map.of(
                "type", "snapshot",
                "zones", zones,
                "occupancy", accessMonitoringService.occupancy().areas());
        realtimeEventPublisher.sendSnapshot(emitter, snapshot);
        return emitter;
    }
}
