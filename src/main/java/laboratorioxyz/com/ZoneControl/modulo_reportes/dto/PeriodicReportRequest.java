package laboratorioxyz.com.ZoneControl.modulo_reportes.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PeriodicReportRequest {
    private int mes;
    private int anio;
    private String formato;
    /** Filtro opcional por departamentos (1.2 §9). Si es null/vacío se incluyen todos. */
    private List<String> departmentNames;
}
