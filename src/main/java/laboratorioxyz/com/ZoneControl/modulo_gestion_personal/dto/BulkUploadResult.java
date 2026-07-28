package laboratorioxyz.com.ZoneControl.modulo_gestion_personal.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class BulkUploadResult {
    private int total;
    private int successes;
    private int errors;
    private String errorReportUrl;
}
