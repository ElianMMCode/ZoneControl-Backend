package laboratorioxyz.com.ZoneControl.modulo_reportes.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExportRequest {
    private String formato;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private String employeeCode;
    private String departamentoName;
    private String productionAreaName;
    private String resultado;
    /** null = todos, true = empleados con usuario, false = sin usuario. */
    private Boolean conUsuario;
}
