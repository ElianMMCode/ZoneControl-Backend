package laboratorioxyz.com.ZoneControl.modulo_reportes.service;

import laboratorioxyz.com.ZoneControl.modulo_reportes.dto.PeriodicReportRequest;

public interface PeriodicReportService {
    byte[] generate(PeriodicReportRequest request);
}
