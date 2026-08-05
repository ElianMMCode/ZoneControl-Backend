package laboratorioxyz.com.ZoneControl.modulo_reportes.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Vista previa del archivo periódico (HU-17, flujo "Enviar a Socio Internacional").
 * Contiene la misma agregación por departamento que el archivo, SIN datos personales.
 */
public record PeriodicReportPreviewResponse(
        int mes,
        int anio,
        String formato,
        List<String> departmentNames,
        LocalDateTime generatedAt,
        List<Row> rows) {

    public record Row(String department, String periodo, int total,
                      int autorizados, int denegados, int noRegistrados, int suspendidos) {}
}
