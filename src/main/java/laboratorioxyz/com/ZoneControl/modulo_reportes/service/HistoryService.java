package laboratorioxyz.com.ZoneControl.modulo_reportes.service;

import laboratorioxyz.com.ZoneControl.modulo_reportes.dto.AccessHistoryResponse;
import laboratorioxyz.com.ZoneControl.modulo_reportes.dto.ExportRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;

public interface HistoryService {
    Page<AccessHistoryResponse> search(LocalDate fechaInicio, LocalDate fechaFin,
                                        String employeeCode, String resultado,
                                        Pageable pageable);
    byte[] export(ExportRequest request);
}
