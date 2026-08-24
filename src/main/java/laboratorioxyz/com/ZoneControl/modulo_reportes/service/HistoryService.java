package laboratorioxyz.com.ZoneControl.modulo_reportes.service;

import laboratorioxyz.com.ZoneControl.modulo_reportes.dto.AccessHistoryResponse;
import laboratorioxyz.com.ZoneControl.modulo_reportes.dto.ExportRequest;
import laboratorioxyz.com.ZoneControl.modulo_reportes.dto.SupervisorStatsResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;

public interface HistoryService {
    Page<AccessHistoryResponse> search(LocalDate fechaInicio, LocalDate fechaFin,
                                        String employeeCode, String department,
                                        String productionAreaName, String resultado,
                                        Boolean conUsuario, Pageable pageable);
    byte[] export(ExportRequest request);
    SupervisorStatsResponse getStats();
}
