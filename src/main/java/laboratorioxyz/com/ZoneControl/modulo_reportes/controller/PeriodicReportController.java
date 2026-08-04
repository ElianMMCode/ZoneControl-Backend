package laboratorioxyz.com.ZoneControl.modulo_reportes.controller;

import laboratorioxyz.com.ZoneControl.modulo_reportes.dto.PeriodicReportRequest;
import laboratorioxyz.com.ZoneControl.modulo_reportes.service.PeriodicReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reportes")
@RequiredArgsConstructor
public class PeriodicReportController {

    private final PeriodicReportService periodicReportService;

    @PostMapping("/archivo-periodico")
    public ResponseEntity<byte[]> generate(@RequestBody PeriodicReportRequest request) {
        byte[] data = periodicReportService.generate(request);
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
                        "attachment; filename=archivo_periodico_" + request.getMes() + "_" + request.getAnio() + "." + ext)
                .body(data);
    }
}
