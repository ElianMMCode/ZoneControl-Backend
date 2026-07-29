package laboratorioxyz.com.ZoneControl.modulo_reportes.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PeriodicReportRequest {
    private int mes;
    private int anio;
    private String formato;
}
