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
 * Archivo periódico para el socio internacional (HU-17). Emite una agregación
 * SIN datos personales en dos secciones: resumen por departamento × área
 * (con % de autorización) y distribución por día. Refleja solo ingresos/intentos
 * (las salidas EXIT quedan fuera como registro de auditoría).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PeriodicReportServiceImpl implements PeriodicReportService {

    private static final String[] AREA_HEADERS = {
            "Departamento", "Área", "Total", "Autorizados", "Denegados", "No Registrados", "Suspendidos", "% Autorizados"
    };
    private static final String[] DAY_HEADERS = {
            "Día", "Total", "Autorizados", "Denegados", "No Registrados", "Suspendidos"
    };

    private final AccessHistoryRepository accessHistoryRepository;
    private final PdfExporter pdfExporter;

    @Override
    public byte[] generate(PeriodicReportRequest request) {
        List<AccessHistory> records = records(request);
        List<AreaAgg> areaRows = aggregateByArea(records);
        List<DayAgg> dayRows = aggregateByDay(records);

        log.info("Periodic report generated: mes={}, anio={}, areas={}, dias={}",
                request.getMes(), request.getAnio(), areaRows.size(), dayRows.size());

        return switch (request.getFormato().toUpperCase()) {
            case "CSV" -> generateCsv(areaRows, dayRows, request);
            case "EXCEL" -> generateExcel(areaRows, dayRows, request);
            case "PDF" -> generatePdf(areaRows, dayRows, request);
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
            String dept = h.getDepartment() != null ? h.getDepartment() : "Sin departamento";
            String area = h.getProductionAreaName() != null ? h.getProductionAreaName() : "Sin área";
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

    private byte[] generateCsv(List<AreaAgg> areaRows, List<DayAgg> dayRows, PeriodicReportRequest request) {
        String periodo = String.format("%d-%02d", request.getAnio(), request.getMes());
        StringBuilder sb = new StringBuilder();
        sb.append("ARCHIVO PERIÓDICO PARA EL SOCIO — ").append(periodo).append("\n\n");
        sb.append("SECCIÓN 1: RESUMEN POR DEPARTAMENTO × ÁREA\n");
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

        sb.append("SECCIÓN 2: DISTRIBUCIÓN POR DÍA\n");
        sb.append(String.join(";", DAY_HEADERS)).append("\n");
        int[] dt = new int[5];
        for (DayAgg r : dayRows) {
            sb.append(r.dia()).append(";").append(r.total()).append(";").append(r.autorizados()).append(";")
              .append(r.denegados()).append(";").append(r.noRegistrados()).append(";").append(r.suspendidos()).append("\n");
            for (int i = 0; i < 5; i++) dt[i] += toArr(r)[i];
        }
        sb.append("TOTAL;").append(dt[0]).append(";").append(dt[1]).append(";").append(dt[2]).append(";")
          .append(dt[3]).append(";").append(dt[4]).append("\n");
        return sb.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8);
    }

    private byte[] generateExcel(List<AreaAgg> areaRows, List<DayAgg> dayRows, PeriodicReportRequest request) {
        try (Workbook wb = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            String periodo = String.format("%d-%02d", request.getAnio(), request.getMes());

            Sheet areaSheet = wb.createSheet("Por Área");
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

            Sheet daySheet = wb.createSheet("Por Día");
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

            wb.write(out);
            return out.toByteArray();
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Error al generar el archivo Excel");
        }
    }

    private byte[] generatePdf(List<AreaAgg> areaRows, List<DayAgg> dayRows, PeriodicReportRequest request) {
        String periodo = String.format("%d-%02d", request.getAnio(), request.getMes());
        List<PdfExporter.PdfTable> sections = new ArrayList<>();

        List<String[]> areaData = areaRows.stream().map(r -> new String[]{
                r.department(), r.area(), String.valueOf(r.total()),
                String.valueOf(r.autorizados()), String.valueOf(r.denegados()),
                String.valueOf(r.noRegistrados()), String.valueOf(r.suspendidos()),
                String.valueOf(pct(r.autorizados(), r.total()))
        }).toList();
        sections.add(new PdfExporter.PdfTable("Resumen por departamento × área", AREA_HEADERS, areaData));

        List<String[]> dayData = dayRows.stream().map(r -> new String[]{
                r.dia(), String.valueOf(r.total()), String.valueOf(r.autorizados()),
                String.valueOf(r.denegados()), String.valueOf(r.noRegistrados()),
                String.valueOf(r.suspendidos())
        }).toList();
        sections.add(new PdfExporter.PdfTable("Distribución por día", DAY_HEADERS, dayData));

        return pdfExporter.exportSections("Archivo Periódico para el Socio",
                "Período: " + periodo, sections);
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
}
