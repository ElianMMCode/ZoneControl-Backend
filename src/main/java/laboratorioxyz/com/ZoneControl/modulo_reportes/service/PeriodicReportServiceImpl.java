package laboratorioxyz.com.ZoneControl.modulo_reportes.service;

import laboratorioxyz.com.ZoneControl.model.enums.AccessResult;
import laboratorioxyz.com.ZoneControl.modulo_control_acceso.model.AccessHistory;
import laboratorioxyz.com.ZoneControl.modulo_control_acceso.repository.AccessHistoryRepository;
import laboratorioxyz.com.ZoneControl.modulo_reportes.dto.PeriodicReportPreviewResponse;
import laboratorioxyz.com.ZoneControl.modulo_reportes.dto.PeriodicReportRequest;
import laboratorioxyz.com.ZoneControl.modulo_reportes.util.PdfExporter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Archivo periódico para el socio internacional (HU-16). Emite tres secciones
 * en inglés: resumen por departamento × área (con % de autorización),
 * distribución por día y log detallado de accesos (solo nombre, cargo,
 * código de empleado, departamento, área y resultado — sin documentos ni
 * datos sensibles). Refleja solo ingresos/intentos (las salidas EXIT quedan
 * fuera como registro de auditoría).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PeriodicReportServiceImpl implements PeriodicReportService {

    private static final String[] AREA_HEADERS = {
            "Department", "Area", "Total", "Authorized", "Denied", "Unregistered", "Suspended", "% Authorized"
    };
    private static final String[] DAY_HEADERS = {
            "Day", "Total", "Authorized", "Denied", "Unregistered", "Suspended"
    };
    private static final String[] LOG_HEADERS = {
            "Date/Time", "Employee", "Position", "Employee ID", "Department", "Area", "Result"
    };

    private final AccessHistoryRepository accessHistoryRepository;
    private final PdfExporter pdfExporter;

    @Override
    public byte[] generate(PeriodicReportRequest request) {
        List<AccessHistory> records = records(request);
        List<AreaAgg> areaRows = aggregateByArea(records);
        List<DayAgg> dayRows = aggregateByDay(records);
        List<LogRow> logRows = toLogRows(records);

        log.info("Periodic report generated: mes={}, anio={}, areas={}, dias={}, logs={}",
                request.getMes(), request.getAnio(), areaRows.size(), dayRows.size(), logRows.size());

        return switch (request.getFormato().toUpperCase()) {
            case "CSV" -> generateCsv(areaRows, dayRows, logRows, request);
            case "EXCEL" -> generateExcel(areaRows, dayRows, logRows, request);
            case "PDF" -> generatePdf(areaRows, dayRows, logRows, request);
            default -> throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Formato no soportado: " + request.getFormato());
        };
    }

    @Override
    public PeriodicReportPreviewResponse preview(PeriodicReportRequest request) {
        List<AccessHistory> records = records(request);
        List<AreaAgg> areaRows = aggregateByArea(records);
        List<DayAgg> dayRows = aggregateByDay(records);
        log.info("Periodic report preview generated: mes={}, anio={}, areas={}, dias={}",
                request.getMes(), request.getAnio(), areaRows.size(), dayRows.size());
        return new PeriodicReportPreviewResponse(
                request.getMes(), request.getAnio(), request.getFormato(),
                request.getDepartmentNames(), java.time.LocalDateTime.now(),
                areaRows.stream().map(r -> new PeriodicReportPreviewResponse.AreaRow(
                        r.department(), r.area(), r.total(),
                        r.autorizados(), r.denegados(), r.noRegistrados(), r.suspendidos(),
                        pct(r.autorizados(), r.total()))).toList(),
                dayRows.stream().map(r -> new PeriodicReportPreviewResponse.DayRow(
                        r.dia(), r.total(),
                        r.autorizados(), r.denegados(), r.noRegistrados(), r.suspendidos())).toList());
    }

    private List<AccessHistory> records(PeriodicReportRequest request) {
        List<AccessHistory> records = accessHistoryRepository.findByPeriod(request.getMes(), request.getAnio());

        if (request.getDepartmentNames() != null && !request.getDepartmentNames().isEmpty()) {
            records = records.stream()
                    .filter(h -> h.getDepartment() != null
                            && request.getDepartmentNames().contains(h.getDepartment()))
                    .toList();
        }

        // El archivo periódico del socio refleja solo ingresos/intentos; las
        // salidas (EXIT) quedan como registro de auditoría fuera de la agregación.
        records = records.stream()
                .filter(h -> h.getResult() != AccessResult.EXIT)
                .toList();

        if (records.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "No se encontraron registros de acceso para el período seleccionado");
        }
        return records;
    }

    private List<AreaAgg> aggregateByArea(List<AccessHistory> records) {
        Map<String, int[]> acc = new LinkedHashMap<>();
        for (AccessHistory h : records) {
            String dept = h.getDepartment() != null ? h.getDepartment() : "Unassigned";
            String area = h.getProductionAreaName() != null ? h.getProductionAreaName() : "No area";
            String key = dept + "\u0000" + area;
            int[] c = acc.computeIfAbsent(key, k -> new int[5]);
            count(c, h.getResult());
        }
        List<AreaAgg> rows = new ArrayList<>();
        for (Map.Entry<String, int[]> e : acc.entrySet()) {
            String[] parts = e.getKey().split("\u0000");
            int[] c = e.getValue();
            rows.add(new AreaAgg(parts[0], parts[1], c[0], c[1], c[2], c[3], c[4]));
        }
        return rows;
    }

    /**
     * Filas del log detallado: solo nombre, cargo, código de empleado,
     * departamento, área y resultado. Sin tipo/número de documento ni
     * ningún otro dato personal. Ordenadas cronológicamente.
     */
    private List<LogRow> toLogRows(List<AccessHistory> records) {
        return records.stream()
                .sorted(java.util.Comparator.comparing(AccessHistory::getTimestamp))
                .map(h -> new LogRow(
                        h.getTimestamp().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")),
                        h.getEmployee() != null
                                ? h.getEmployee().getFirstName() + " " + h.getEmployee().getLastName()
                                : "-",
                        h.getEmployee() != null && h.getEmployee().getPosition() != null
                                ? h.getEmployee().getPosition() : "-",
                        h.getEmployee() != null ? h.getEmployee().getEmployeeCode() : "-",
                        h.getDepartment() != null ? h.getDepartment() : "-",
                        h.getProductionAreaName() != null ? h.getProductionAreaName() : "-",
                        h.getResult().name()))
                .toList();
    }

    private List<DayAgg> aggregateByDay(List<AccessHistory> records) {
        Map<Integer, int[]> acc = new LinkedHashMap<>();
        for (AccessHistory h : records) {
            int day = h.getTimestamp().getDayOfMonth();
            int[] c = acc.computeIfAbsent(day, k -> new int[5]);
            count(c, h.getResult());
        }
        List<DayAgg> rows = new ArrayList<>();
        for (Map.Entry<Integer, int[]> e : acc.entrySet()) {
            int[] c = e.getValue();
            rows.add(new DayAgg(String.format("%02d", e.getKey()), c[0], c[1], c[2], c[3], c[4]));
        }
        return rows;
    }

    private void count(int[] c, AccessResult result) {
        c[0]++;
        if (result == AccessResult.AUTHORIZED) c[1]++;
        else if (result == AccessResult.DENIED) c[2]++;
        else if (result == AccessResult.UNREGISTERED) c[3]++;
        else if (result == AccessResult.SUSPENDED) c[4]++;
    }

    private int pct(int part, int total) {
        if (total <= 0) return 0;
        return Math.round((part * 100f) / total);
    }

    private byte[] generateCsv(List<AreaAgg> areaRows, List<DayAgg> dayRows,
                               List<LogRow> logRows, PeriodicReportRequest request) {
        String period = String.format("%d-%02d", request.getAnio(), request.getMes());
        StringBuilder sb = new StringBuilder();
        sb.append("PERIODIC FILE FOR PARTNER — ").append(period).append("\n\n");
        sb.append("SECTION 1: SUMMARY BY DEPARTMENT x AREA\n");
        sb.append(String.join(";", AREA_HEADERS)).append("\n");
        int[] at = new int[5];
        for (AreaAgg r : areaRows) {
            sb.append(r.department()).append(";").append(r.area()).append(";")
              .append(r.total()).append(";").append(r.autorizados()).append(";")
              .append(r.denegados()).append(";").append(r.noRegistrados()).append(";")
              .append(r.suspendidos()).append(";").append(pct(r.autorizados(), r.total())).append("\n");
            for (int i = 0; i < 5; i++) at[i] += toArr(r)[i];
        }
        sb.append("TOTAL;;").append(at[0]).append(";").append(at[1]).append(";").append(at[2]).append(";")
          .append(at[3]).append(";").append(at[4]).append(";")
          .append(pct(at[1], at[0])).append("\n\n");

        sb.append("SECTION 2: DAILY DISTRIBUTION\n");
        sb.append(String.join(";", DAY_HEADERS)).append("\n");
        int[] dt = new int[5];
        for (DayAgg r : dayRows) {
            sb.append(r.dia()).append(";").append(r.total()).append(";").append(r.autorizados()).append(";")
              .append(r.denegados()).append(";").append(r.noRegistrados()).append(";").append(r.suspendidos()).append("\n");
            for (int i = 0; i < 5; i++) dt[i] += toArr(r)[i];
        }
        sb.append("TOTAL;").append(dt[0]).append(";").append(dt[1]).append(";").append(dt[2]).append(";")
          .append(dt[3]).append(";").append(dt[4]).append("\n\n");

        sb.append("SECTION 3: ACCESS LOG (INGRESSES ONLY)\n");
        sb.append(String.join(";", LOG_HEADERS)).append("\n");
        for (LogRow r : logRows) {
            sb.append(r.dateTime()).append(";").append(r.employee()).append(";")
              .append(r.position()).append(";").append(r.employeeId()).append(";")
              .append(r.department()).append(";").append(r.area()).append(";").append(r.result()).append("\n");
        }
        return sb.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8);
    }

    private byte[] generateExcel(List<AreaAgg> areaRows, List<DayAgg> dayRows,
                                 List<LogRow> logRows, PeriodicReportRequest request) {
        try (Workbook wb = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet areaSheet = wb.createSheet("By Area");
            Row areaHeader = areaSheet.createRow(0);
            for (int i = 0; i < AREA_HEADERS.length; i++) {
                areaHeader.createCell(i).setCellValue(AREA_HEADERS[i]);
            }
            int rowNum = 1;
            int[] at = new int[5];
            for (AreaAgg r : areaRows) {
                Row row = areaSheet.createRow(rowNum++);
                row.createCell(0).setCellValue(r.department());
                row.createCell(1).setCellValue(r.area());
                row.createCell(2).setCellValue(r.total());
                row.createCell(3).setCellValue(r.autorizados());
                row.createCell(4).setCellValue(r.denegados());
                row.createCell(5).setCellValue(r.noRegistrados());
                row.createCell(6).setCellValue(r.suspendidos());
                row.createCell(7).setCellValue(pct(r.autorizados(), r.total()));
                for (int i = 0; i < 5; i++) at[i] += toArr(r)[i];
            }
            Row atRow = areaSheet.createRow(rowNum);
            atRow.createCell(0).setCellValue("TOTAL");
            atRow.createCell(1).setCellValue("");
            atRow.createCell(2).setCellValue(at[0]);
            atRow.createCell(3).setCellValue(at[1]);
            atRow.createCell(4).setCellValue(at[2]);
            atRow.createCell(5).setCellValue(at[3]);
            atRow.createCell(6).setCellValue(at[4]);
            atRow.createCell(7).setCellValue(pct(at[1], at[0]));

            Sheet daySheet = wb.createSheet("By Day");
            Row dayHeader = daySheet.createRow(0);
            for (int i = 0; i < DAY_HEADERS.length; i++) {
                dayHeader.createCell(i).setCellValue(DAY_HEADERS[i]);
            }
            rowNum = 1;
            int[] dt = new int[5];
            for (DayAgg r : dayRows) {
                Row row = daySheet.createRow(rowNum++);
                row.createCell(0).setCellValue(r.dia());
                row.createCell(1).setCellValue(r.total());
                row.createCell(2).setCellValue(r.autorizados());
                row.createCell(3).setCellValue(r.denegados());
                row.createCell(4).setCellValue(r.noRegistrados());
                row.createCell(5).setCellValue(r.suspendidos());
                for (int i = 0; i < 5; i++) dt[i] += toArr(r)[i];
            }
            Row dtRow = daySheet.createRow(rowNum);
            dtRow.createCell(0).setCellValue("TOTAL");
            dtRow.createCell(1).setCellValue(dt[0]);
            dtRow.createCell(2).setCellValue(dt[1]);
            dtRow.createCell(3).setCellValue(dt[2]);
            dtRow.createCell(4).setCellValue(dt[3]);
            dtRow.createCell(5).setCellValue(dt[4]);

            Sheet logSheet = wb.createSheet("Access Log");
            Row logHeader = logSheet.createRow(0);
            for (int i = 0; i < LOG_HEADERS.length; i++) {
                logHeader.createCell(i).setCellValue(LOG_HEADERS[i]);
            }
            rowNum = 1;
            for (LogRow r : logRows) {
                Row row = logSheet.createRow(rowNum++);
                row.createCell(0).setCellValue(r.dateTime());
                row.createCell(1).setCellValue(r.employee());
                row.createCell(2).setCellValue(r.position());
                row.createCell(3).setCellValue(r.employeeId());
                row.createCell(4).setCellValue(r.department());
                row.createCell(5).setCellValue(r.area());
                row.createCell(6).setCellValue(r.result());
            }

            wb.write(out);
            return out.toByteArray();
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Error al generar el archivo Excel");
        }
    }

    private byte[] generatePdf(List<AreaAgg> areaRows, List<DayAgg> dayRows,
                               List<LogRow> logRows, PeriodicReportRequest request) {
        String period = String.format("%d-%02d", request.getAnio(), request.getMes());
        List<PdfExporter.PdfTable> sections = new ArrayList<>();

        List<String[]> areaData = areaRows.stream().map(r -> new String[]{
                r.department(), r.area(), String.valueOf(r.total()),
                String.valueOf(r.autorizados()), String.valueOf(r.denegados()),
                String.valueOf(r.noRegistrados()), String.valueOf(r.suspendidos()),
                String.valueOf(pct(r.autorizados(), r.total()))
        }).toList();
        sections.add(new PdfExporter.PdfTable("Summary by Department x Area", AREA_HEADERS, areaData));

        List<String[]> dayData = dayRows.stream().map(r -> new String[]{
                r.dia(), String.valueOf(r.total()), String.valueOf(r.autorizados()),
                String.valueOf(r.denegados()), String.valueOf(r.noRegistrados()),
                String.valueOf(r.suspendidos())
        }).toList();
        sections.add(new PdfExporter.PdfTable("Daily Distribution", DAY_HEADERS, dayData));

        List<String[]> logData = logRows.stream().map(r -> new String[]{
                r.dateTime(), r.employee(), r.position(), r.employeeId(),
                r.department(), r.area(), r.result()
        }).toList();
        sections.add(new PdfExporter.PdfTable("Access Log (Ingresses Only)", LOG_HEADERS, logData));

        return pdfExporter.exportSections("Periodic File for Partner",
                "Period: " + period, sections);
    }

    private int[] toArr(AreaAgg r) {
        return new int[]{r.total(), r.autorizados(), r.denegados(), r.noRegistrados(), r.suspendidos()};
    }

    private int[] toArr(DayAgg r) {
        return new int[]{r.total(), r.autorizados(), r.denegados(), r.noRegistrados(), r.suspendidos()};
    }

    private record AreaAgg(String department, String area, int total,
                           int autorizados, int denegados, int noRegistrados, int suspendidos) {}

    private record DayAgg(String dia, int total,
                          int autorizados, int denegados, int noRegistrados, int suspendidos) {}

    private record LogRow(String dateTime, String employee, String position, String employeeId,
                          String department, String area, String result) {}
}
