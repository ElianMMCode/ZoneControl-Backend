package laboratorioxyz.com.ZoneControl.modulo_reportes.service;

import laboratorioxyz.com.ZoneControl.model.enums.AccessResult;
import laboratorioxyz.com.ZoneControl.modulo_control_acceso.model.AccessHistory;
import laboratorioxyz.com.ZoneControl.modulo_control_acceso.repository.AccessHistoryRepository;
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
 * Archivo periódico para el socio internacional (HU-17). Emite una
 * agregación por departamento SIN datos personales (columnas: Departamento,
 * Período, Total, Autorizados, Denegados, No Registrados, Suspendidos),
 * conforme al plan y a la normativa de protección de datos.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PeriodicReportServiceImpl implements PeriodicReportService {

    private static final String[] HEADERS = {
            "Departamento", "Período", "Total", "Autorizados", "Denegados", "No Registrados", "Suspendidos"
    };

    private final AccessHistoryRepository accessHistoryRepository;
    private final PdfExporter pdfExporter;

    @Override
    public byte[] generate(PeriodicReportRequest request) {
        List<DepartmentAgg> rows = buildRows(request);

        log.info("Periodic report generated: mes={}, anio={}, departamentos={}",
                request.getMes(), request.getAnio(), rows.size());

        return switch (request.getFormato().toUpperCase()) {
            case "CSV" -> generateCsv(rows, request);
            case "EXCEL" -> generateExcel(rows, request);
            case "PDF" -> generatePdf(rows, request);
            default -> throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Formato no soportado: " + request.getFormato());
        };
    }

    @Override
    public laboratorioxyz.com.ZoneControl.modulo_reportes.dto.PeriodicReportPreviewResponse preview(PeriodicReportRequest request) {
        List<DepartmentAgg> rows = buildRows(request);
        log.info("Periodic report preview generated: mes={}, anio={}, departamentos={}",
                request.getMes(), request.getAnio(), rows.size());
        return new laboratorioxyz.com.ZoneControl.modulo_reportes.dto.PeriodicReportPreviewResponse(
                request.getMes(), request.getAnio(), request.getFormato(),
                request.getDepartmentNames(), java.time.LocalDateTime.now(),
                rows.stream().map(r -> new laboratorioxyz.com.ZoneControl.modulo_reportes.dto.PeriodicReportPreviewResponse.Row(
                        r.department(), r.periodo(), r.total(),
                        r.autorizados(), r.denegados(), r.noRegistrados(), r.suspendidos())).toList());
    }

    private List<DepartmentAgg> buildRows(PeriodicReportRequest request) {
        List<AccessHistory> records = accessHistoryRepository.findByPeriod(request.getMes(), request.getAnio());

        if (request.getDepartmentNames() != null && !request.getDepartmentNames().isEmpty()) {
            records = records.stream()
                    .filter(h -> h.getDepartment() != null
                            && request.getDepartmentNames().contains(h.getDepartment()))
                    .toList();
        }

        if (records.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "No se encontraron registros de acceso para el período seleccionado");
        }

        return aggregate(records, request);
    }

    private List<DepartmentAgg> aggregate(List<AccessHistory> records, PeriodicReportRequest request) {
        Map<String, int[]> acc = new LinkedHashMap<>();
        for (AccessHistory h : records) {
            String dept = h.getDepartment() != null ? h.getDepartment() : "Sin departamento";
            int[] c = acc.computeIfAbsent(dept, k -> new int[5]);
            c[0]++;
            if (h.getResult() == AccessResult.AUTHORIZED) c[1]++;
            else if (h.getResult() == AccessResult.DENIED) c[2]++;
            else if (h.getResult() == AccessResult.UNREGISTERED) c[3]++;
            else if (h.getResult() == AccessResult.SUSPENDED) c[4]++;
        }
        String periodo = String.format("%d-%02d", request.getAnio(), request.getMes());
        List<DepartmentAgg> rows = new ArrayList<>();
        for (Map.Entry<String, int[]> e : acc.entrySet()) {
            int[] c = e.getValue();
            rows.add(new DepartmentAgg(e.getKey(), periodo, c[0], c[1], c[2], c[3], c[4]));
        }
        return rows;
    }

    private byte[] generateCsv(List<DepartmentAgg> rows, PeriodicReportRequest request) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.join(";", HEADERS)).append("\n");
        int total = 0, auth = 0, den = 0, noReg = 0, susp = 0;
        for (DepartmentAgg r : rows) {
            sb.append(r.department()).append(";").append(r.periodo()).append(";")
              .append(r.total()).append(";").append(r.autorizados()).append(";")
              .append(r.denegados()).append(";").append(r.noRegistrados()).append(";")
              .append(r.suspendidos()).append("\n");
            total += r.total(); auth += r.autorizados(); den += r.denegados();
            noReg += r.noRegistrados(); susp += r.suspendidos();
        }
        sb.append("TOTAL;").append(String.format("%d-%02d", request.getAnio(), request.getMes())).append(";")
          .append(total).append(";").append(auth).append(";").append(den).append(";")
          .append(noReg).append(";").append(susp).append("\n");
        return sb.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8);
    }

    private byte[] generateExcel(List<DepartmentAgg> rows, PeriodicReportRequest request) {
        try (Workbook wb = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = wb.createSheet("Archivo Periodico");
            Row header = sheet.createRow(0);
            for (int i = 0; i < HEADERS.length; i++) {
                header.createCell(i).setCellValue(HEADERS[i]);
            }
            int rowNum = 1;
            int total = 0, auth = 0, den = 0, noReg = 0, susp = 0;
            for (DepartmentAgg r : rows) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(r.department());
                row.createCell(1).setCellValue(r.periodo());
                row.createCell(2).setCellValue(r.total());
                row.createCell(3).setCellValue(r.autorizados());
                row.createCell(4).setCellValue(r.denegados());
                row.createCell(5).setCellValue(r.noRegistrados());
                row.createCell(6).setCellValue(r.suspendidos());
                total += r.total(); auth += r.autorizados(); den += r.denegados();
                noReg += r.noRegistrados(); susp += r.suspendidos();
            }
            Row totalRow = sheet.createRow(rowNum);
            totalRow.createCell(0).setCellValue("TOTAL");
            totalRow.createCell(1).setCellValue(String.format("%d-%02d", request.getAnio(), request.getMes()));
            totalRow.createCell(2).setCellValue(total);
            totalRow.createCell(3).setCellValue(auth);
            totalRow.createCell(4).setCellValue(den);
            totalRow.createCell(5).setCellValue(noReg);
            totalRow.createCell(6).setCellValue(susp);
            wb.write(out);
            return out.toByteArray();
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Error al generar el archivo Excel");
        }
    }

    private byte[] generatePdf(List<DepartmentAgg> rows, PeriodicReportRequest request) {
        List<String[]> data = rows.stream().map(r -> new String[]{
                r.department(), r.periodo(),
                String.valueOf(r.total()), String.valueOf(r.autorizados()),
                String.valueOf(r.denegados()), String.valueOf(r.noRegistrados()),
                String.valueOf(r.suspendidos())
        }).toList();
        return pdfExporter.exportTable("Archivo Periódico",
                "Período: " + String.format("%d-%02d", request.getAnio(), request.getMes()),
                HEADERS, data);
    }

    private record DepartmentAgg(String department, String periodo, int total,
                                 int autorizados, int denegados, int noRegistrados, int suspendidos) {}
}
