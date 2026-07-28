package laboratorioxyz.com.ZoneControl.modulo_gestion_personal.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class BulkUploadError {
    private int row;
    private String field;
    private String reason;
}
