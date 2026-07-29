package laboratorioxyz.com.ZoneControl.modulo_reportes.service;

import laboratorioxyz.com.ZoneControl.model.enums.AccessResult;
import laboratorioxyz.com.ZoneControl.modulo_control_acceso.model.AccessHistory;
import laboratorioxyz.com.ZoneControl.modulo_control_acceso.repository.AccessHistoryRepository;
import laboratorioxyz.com.ZoneControl.modulo_reportes.dto.PeriodicReportRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PeriodicReportServiceImpl implements PeriodicReportService {

    private final AccessHistoryRepository accessHistoryRepository;

    @Override
    public byte[] generate(PeriodicReportRequest request) {
        List<AccessHistory> records = accessHistoryRepository.findByPeriod(request.getMes(), request.getAnio());

        if (records.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "No se encontraron registros de acceso para el período seleccionado");
        }

        log.info("Periodic report generated: mes={}, anio={}, registros={}",
                request.getMes(), request.getAnio(), records.size());

        return switch (request.getFormato().toUpperCase()) {
            case "CSV" -> generateCsv(records, request);
            case "EXCEL" -> generateExcel(records, request);
            default -> throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Formato no soportado: " + request.getFormato());
        };
    }

    private byte[] generateCsv(List<AccessHistory> records, PeriodicReportRequest request) {
        StringBuilder sb = new StringBuilder();
        sb.append("Fecha/Hora;ID Empleado;Nombre;Cargo;Departamento;Resultado\n");
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        for (AccessHistory h : records) {
            sb.append(h.getTimestamp().format(dtf)).append(";")
              .append(h.getEmployee() != null ? h.getEmployee().getEmployeeCode() : "N/A").append(";")
              .append(h.getEmployee() != null ? h.getEmployee().getFirstName() + " " + h.getEmployee().getLastName() : "N/A").append(";")
              .append(h.getEmployee() != null ? h.getEmployee().getPosition() : "").append(";")
              .append(h.getDepartment() != null ? h.getDepartment() : "").append(";")
              .append(h.getResult()).append("\n");
        }
        return sb.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8);
    }

    private byte[] generateExcel(List<AccessHistory> records, PeriodicReportRequest request) {
        try (Workbook wb = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = wb.createSheet("Archivo Periodico");
            String[] cols = {"Fecha/Hora", "ID Empleado", "Nombre", "Cargo", "Departamento", "Resultado"};
            Row header = sheet.createRow(0);
            for (int i = 0; i < cols.length; i++) {
                header.createCell(i).setCellValue(cols[i]);
            }
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
            int rowNum = 1;
            for (AccessHistory h : records) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(h.getTimestamp().format(dtf));
                row.createCell(1).setCellValue(h.getEmployee() != null ? h.getEmployee().getEmployeeCode() : "N/A");
                row.createCell(2).setCellValue(h.getEmployee() != null ? h.getEmployee().getFirstName() + " " + h.getEmployee().getLastName() : "N/A");
                row.createCell(3).setCellValue(h.getEmployee() != null ? h.getEmployee().getPosition() : "");
                row.createCell(4).setCellValue(h.getDepartment() != null ? h.getDepartment() : "");
                row.createCell(5).setCellValue(h.getResult().name());
            }
            wb.write(out);
            return out.toByteArray();
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Error al generar el archivo Excel");
        }
    }
}
