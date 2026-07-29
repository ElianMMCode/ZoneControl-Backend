package laboratorioxyz.com.ZoneControl.modulo_reportes.controller;

import laboratorioxyz.com.ZoneControl.modulo_reportes.dto.AccessHistoryResponse;
import laboratorioxyz.com.ZoneControl.modulo_reportes.dto.ExportRequest;
import laboratorioxyz.com.ZoneControl.modulo_reportes.service.HistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/historial")
@RequiredArgsConstructor
public class HistoryController {

    private final HistoryService historyService;

    @GetMapping
    public ResponseEntity<Page<AccessHistoryResponse>> search(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin,
            @RequestParam(required = false) UUID personalId,
            @RequestParam(required = false) String resultado,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<AccessHistoryResponse> result = historyService.search(
                fechaInicio, fechaFin, personalId, resultado, pageable);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/export")
    public ResponseEntity<byte[]> export(@RequestBody ExportRequest request) {
        byte[] data = historyService.export(request);
        String ext = switch (request.getFormato().toUpperCase()) {
            case "CSV" -> "csv";
            case "EXCEL" -> "xlsx";
            default -> "csv";
        };
        String contentType = switch (request.getFormato().toUpperCase()) {
            case "CSV" -> "text/csv";
            case "EXCEL" -> "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
            default -> "application/octet-stream";
        };
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=historial_accesos." + ext)
                .body(data);
    }
}
