package laboratorioxyz.com.ZoneControl.modulo_reportes.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Vista previa del archivo periódico (HU-17, flujo "Enviar a Socio Internacional").
 * Contiene la misma agregación que el archivo, SIN datos personales: resumen por
 * departamento × área y distribución por día.
 */
public record PeriodicReportPreviewResponse(
        int mes,
        int anio,
        String formato,
        List<String> departmentNames,
        LocalDateTime generatedAt,
        List<AreaRow> areaRows,
        List<DayRow> dayRows) {

    public record AreaRow(String department, String area, int total,
                          int autorizados, int denegados, int noRegistrados, int suspendidos,
                          int pctAutorizados) {}

    public record DayRow(String dia, int total,
                         int autorizados, int denegados, int noRegistrados, int suspendidos) {}
}
