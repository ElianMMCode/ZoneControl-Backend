package laboratorioxyz.com.ZoneControl.modulo_reportes.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import laboratorioxyz.com.ZoneControl.modulo_reportes.dto.AccessHistoryResponse;
import laboratorioxyz.com.ZoneControl.modulo_reportes.dto.ExportRequest;
import laboratorioxyz.com.ZoneControl.modulo_reportes.dto.SupervisorStatsResponse;
import laboratorioxyz.com.ZoneControl.modulo_reportes.service.HistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;


@RestController
@RequestMapping("/api/historial")
@RequiredArgsConstructor
@Tag(name = "Módulo Reportes", description = "Historial de accesos y reportes (SUPERVISOR_AUDITOR)")
public class HistoryController {

    private final HistoryService historyService;

    @Operation(summary = "Consultar historial de accesos",
            description = "Lista paginada del historial de accesos por rango de fechas con filtros opcionales de empleado y resultado.")
    @ApiResponse(responseCode = "200", description = "Historial paginado")
    @ApiResponse(responseCode = "400", description = "Rango de fechas inválido")
    @GetMapping
    public ResponseEntity<Page<AccessHistoryResponse>> search(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin,
            @RequestParam(required = false) String employeeCode,
            @RequestParam(required = false) String department,
            @RequestParam(required = false) String productionAreaName,
            @RequestParam(required = false) String resultado,
            @RequestParam(required = false) Boolean conUsuario,
            @PageableDefault(size = 20, sort = "timestamp", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<AccessHistoryResponse> result = historyService.search(
                fechaInicio, fechaFin, employeeCode, department, productionAreaName, resultado,
                conUsuario, pageable);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/export")
    public ResponseEntity<byte[]> export(@RequestBody ExportRequest request) {
        byte[] data = historyService.export(request);
        String ext = switch (request.getFormato().toUpperCase()) {
            case "CSV" -> "csv";
            case "EXCEL" -> "xlsx";
            case "PDF" -> "pdf";
            default -> "csv";
        };
        String contentType = switch (request.getFormato().toUpperCase()) {
            case "CSV" -> "text/csv;charset=UTF-8";
            case "EXCEL" -> "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
            case "PDF" -> "application/pdf";
            default -> "application/octet-stream";
        };
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=historial_accesos." + ext)
                .body(data);
    }

    @Operation(summary = "Indicadores del dashboard del supervisor",
            description = "Contadores agregados para las tarjetas KPI del dashboard del supervisor/auditor: " +
                    "accesos del día por resultado, permisos activos/suspendidos y empleados con acceso vigente.")
    @ApiResponse(responseCode = "200", description = "Indicadores agregados")
    @GetMapping("/stats")
    public ResponseEntity<SupervisorStatsResponse> getStats() {
        return ResponseEntity.ok(historyService.getStats());
    }
}
