package laboratorioxyz.com.ZoneControl.modulo_reportes.service;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import laboratorioxyz.com.ZoneControl.model.enums.AccessResult;
import laboratorioxyz.com.ZoneControl.model.enums.PermissionStatus;
import laboratorioxyz.com.ZoneControl.modulo_autenticacion.repository.UserRepository;
import laboratorioxyz.com.ZoneControl.modulo_control_acceso.model.AccessHistory;
import laboratorioxyz.com.ZoneControl.modulo_control_acceso.repository.AccessHistoryRepository;
import laboratorioxyz.com.ZoneControl.modulo_gestion_personal.repository.AccessPermissionRepository;
import laboratorioxyz.com.ZoneControl.modulo_reportes.dto.AccessHistoryResponse;
import laboratorioxyz.com.ZoneControl.modulo_reportes.dto.ExportRequest;
import laboratorioxyz.com.ZoneControl.modulo_reportes.dto.SupervisorStatsResponse;
import laboratorioxyz.com.ZoneControl.modulo_reportes.util.PdfExporter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

@Service
@RequiredArgsConstructor
@Slf4j
public class HistoryServiceImpl implements HistoryService {

    private final AccessHistoryRepository accessHistoryRepository;
    private final AccessPermissionRepository accessPermissionRepository;
    private final UserRepository userRepository;
    private final PdfExporter pdfExporter;

    @Override
    @Transactional(readOnly = true)
    public Page<AccessHistoryResponse> search(LocalDate fechaInicio, LocalDate fechaFin,
                                               String employeeCode, String department,
                                               String productionAreaName, String resultado,
                                               Boolean conUsuario, Pageable pageable) {
        if (fechaInicio.isAfter(fechaFin)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Rango de fechas inválido");
        }

        Specification<AccessHistory> spec = (root, query, cb) -> {
            Predicate predicate = cb.conjunction();
            predicate = cb.and(predicate,
                    cb.greaterThanOrEqualTo(root.get("timestamp"), fechaInicio.atStartOfDay()));
            predicate = cb.and(predicate,
                    cb.lessThanOrEqualTo(root.get("timestamp"), fechaFin.atTime(LocalTime.MAX)));
            if (employeeCode != null && !employeeCode.isBlank()) {
                predicate = cb.and(predicate,
                        cb.equal(root.get("employee").get("employeeCode"), employeeCode));
            }
            if (department != null && !department.isBlank()) {
                predicate = cb.and(predicate,
                        cb.equal(root.get("department"), department));
            }
            if (productionAreaName != null && !productionAreaName.isBlank()) {
                predicate = cb.and(predicate,
                        cb.equal(root.get("productionAreaName"), productionAreaName));
            }
            if (resultado != null && !resultado.isBlank()) {
                predicate = cb.and(predicate,
                        cb.equal(root.get("result"), AccessResult.valueOf(resultado)));
            }
            if (conUsuario != null) {
                // LEFT JOINs explícitos: conUsuario=true exige empleado+usuario,
                // false acepta empleado sin usuario o intento no registrado.
                Join<Object, Object> employeeJoin = root.join("employee", JoinType.LEFT);
                Join<Object, Object> userJoin = employeeJoin.join("user", JoinType.LEFT);
                if (conUsuario) {
                    predicate = cb.and(predicate, cb.isNotNull(employeeJoin.get("id")));
                    predicate = cb.and(predicate, cb.isNotNull(userJoin.get("id")));
                } else {
                    predicate = cb.and(predicate, cb.or(
                            cb.isNull(employeeJoin.get("id")),
                            cb.isNull(userJoin.get("id"))));
                }
            }
            return predicate;
        };

        Page<AccessHistory> pageResult = accessHistoryRepository.findAll(spec, pageable);
        Set<UUID> employeesWithUser = resolveEmployeesWithUser(pageResult.getContent());
        return pageResult.map(h -> toResponse(h, employeesWithUser));
    }

    @Override
    public byte[] export(ExportRequest request) {
        LocalDate fechaInicio = request.getFechaInicio();
        LocalDate fechaFin = request.getFechaFin();

        if (fechaInicio.isAfter(fechaFin)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Rango de fechas inválido");
        }

        Specification<AccessHistory> spec = (root, query, cb) -> {
            Predicate predicate = cb.conjunction();
            predicate = cb.and(predicate,
                    cb.greaterThanOrEqualTo(root.get("timestamp"), fechaInicio.atStartOfDay()));
            predicate = cb.and(predicate,
                    cb.lessThanOrEqualTo(root.get("timestamp"), fechaFin.atTime(LocalTime.MAX)));
            if (request.getEmployeeCode() != null && !request.getEmployeeCode().isBlank()) {
                predicate = cb.and(predicate,
                        cb.equal(root.get("employee").get("employeeCode"), request.getEmployeeCode()));
            }
            if (request.getDepartamentoName() != null && !request.getDepartamentoName().isBlank()) {
                predicate = cb.and(predicate,
                        cb.equal(root.get("department"), request.getDepartamentoName()));
            }
            if (request.getProductionAreaName() != null && !request.getProductionAreaName().isBlank()) {
                predicate = cb.and(predicate,
                        cb.equal(root.get("productionAreaName"), request.getProductionAreaName()));
            }
            if (request.getResultado() != null && !request.getResultado().isBlank()) {
                predicate = cb.and(predicate,
                        cb.equal(root.get("result"), AccessResult.valueOf(request.getResultado())));
            }
            if (request.getConUsuario() != null) {
                Join<Object, Object> employeeJoin = root.join("employee", JoinType.LEFT);
                Join<Object, Object> userJoin = employeeJoin.join("user", JoinType.LEFT);
                if (request.getConUsuario()) {
                    predicate = cb.and(predicate, cb.isNotNull(employeeJoin.get("id")));
                    predicate = cb.and(predicate, cb.isNotNull(userJoin.get("id")));
                } else {
                    predicate = cb.and(predicate, cb.or(
                            cb.isNull(employeeJoin.get("id")),
                            cb.isNull(userJoin.get("id"))));
                }
            }
            return predicate;
        };

        List<AccessHistory> records = accessHistoryRepository.findAll(spec);

        if (records.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "No hay datos para exportar");
        }

        return switch (request.getFormato().toUpperCase()) {
            case "CSV" -> generateCsv(records, fechaInicio, fechaFin);
            case "EXCEL" -> generateExcel(records, fechaInicio, fechaFin);
            case "PDF" -> generatePdf(records, fechaInicio, fechaFin);
            default -> throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Formato no soportado: " + request.getFormato());
        };
    }

    private byte[] generatePdf(List<AccessHistory> records, LocalDate desde, LocalDate hasta) {
        String[] headers = {"Fecha", "Hora", "ID Empleado", "Nombre", "Cargo", "Departamento", "Área", "Resultado"};
        List<String[]> rows = records.stream().map(h -> new String[]{
                h.getTimestamp().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                h.getTimestamp().format(DateTimeFormatter.ofPattern("HH:mm:ss")),
                h.getEmployee() != null ? h.getEmployee().getEmployeeCode() : "N/A",
                h.getEmployee() != null ? h.getEmployee().getFirstName() + " " + h.getEmployee().getLastName() : "N/A",
                h.getEmployee() != null ? h.getEmployee().getPosition() : "",
                h.getDepartment() != null ? h.getDepartment() : "",
                h.getProductionAreaName() != null ? h.getProductionAreaName() : "",
                h.getResult() != null ? h.getResult().name() : ""
        }).toList();
        long autorizados = records.stream().filter(h -> h.getResult() == AccessResult.AUTHORIZED).count();
        String subtitle = "Período: " + desde + " a " + hasta
                + " — Total=" + records.size()
                + ", Autorizados=" + autorizados
                + ", Otros=" + (records.size() - autorizados);
        return pdfExporter.exportTable("Historial de Accesos", subtitle, headers, rows);
    }

    private byte[] generateCsv(List<AccessHistory> records, LocalDate desde, LocalDate hasta) {
        StringBuilder sb = new StringBuilder();
        sb.append("Fecha;Hora;ID Empleado;Nombre;Cargo;Departamento;Área;Resultado\n");
        DateTimeFormatter dateFmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        DateTimeFormatter timeFmt = DateTimeFormatter.ofPattern("HH:mm:ss");
        for (AccessHistory h : records) {
            sb.append(h.getTimestamp().format(dateFmt)).append(";")
              .append(h.getTimestamp().format(timeFmt)).append(";")
              .append(h.getEmployee() != null ? h.getEmployee().getEmployeeCode() : "N/A").append(";")
              .append(h.getEmployee() != null ? h.getEmployee().getFirstName() + " " + h.getEmployee().getLastName() : "N/A").append(";")
              .append(h.getEmployee() != null ? h.getEmployee().getPosition() : "").append(";")
              .append(h.getDepartment() != null ? h.getDepartment() : "").append(";")
              .append(h.getProductionAreaName() != null ? h.getProductionAreaName() : "").append(";")
              .append(h.getResult()).append("\n");
        }
        long validos = records.size();
        long autorizados = records.stream().filter(h -> h.getResult() == AccessResult.AUTHORIZED).count();
        sb.append("\nResumen: Total=").append(validos)
          .append(", Autorizados=").append(autorizados)
          .append(", Otros=").append(validos - autorizados).append("\n");
        return sb.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8);
    }

    private byte[] generateExcel(List<AccessHistory> records, LocalDate desde, LocalDate hasta) {
        try (Workbook wb = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = wb.createSheet("Historial de Accesos");
            Row header = sheet.createRow(0);
            String[] cols = {"Fecha", "Hora", "ID Empleado", "Nombre", "Cargo", "Departamento", "Área", "Resultado"};
            for (int i = 0; i < cols.length; i++) {
                header.createCell(i).setCellValue(cols[i]);
            }
            DateTimeFormatter dateFmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            DateTimeFormatter timeFmt = DateTimeFormatter.ofPattern("HH:mm:ss");
            int rowNum = 1;
            for (AccessHistory h : records) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(h.getTimestamp().format(dateFmt));
                row.createCell(1).setCellValue(h.getTimestamp().format(timeFmt));
                row.createCell(2).setCellValue(h.getEmployee() != null ? h.getEmployee().getEmployeeCode() : "N/A");
                row.createCell(3).setCellValue(h.getEmployee() != null ? h.getEmployee().getFirstName() + " " + h.getEmployee().getLastName() : "N/A");
                row.createCell(4).setCellValue(h.getEmployee() != null ? h.getEmployee().getPosition() : "");
                row.createCell(5).setCellValue(h.getDepartment() != null ? h.getDepartment() : "");
                row.createCell(6).setCellValue(h.getProductionAreaName() != null ? h.getProductionAreaName() : "");
                row.createCell(7).setCellValue(h.getResult().name());
            }
            wb.write(out);
            return out.toByteArray();
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Error al generar el archivo Excel");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public SupervisorStatsResponse getStats() {
        return new SupervisorStatsResponse(
                accessHistoryRepository.countTodayByResultIsNot(AccessResult.EXIT),
                accessHistoryRepository.countTodayByResult(AccessResult.AUTHORIZED),
                accessHistoryRepository.countTodayByResult(AccessResult.DENIED),
                accessHistoryRepository.countTodayByResult(AccessResult.UNREGISTERED),
                accessHistoryRepository.countTodayByResult(AccessResult.SUSPENDED),
                accessPermissionRepository.countByStatus(PermissionStatus.ACTIVO),
                accessPermissionRepository.countByStatus(PermissionStatus.SUSPENDIDO),
accessPermissionRepository.countDistinctEmployeesWithActivePermissions()
        );
    }

    /**
     * Resuelve en una sola consulta qué empleados de la página tienen cuenta
     * de sistema, evitando N+1 al mapear hasUser por registro.
     */
    private Set<UUID> resolveEmployeesWithUser(List<AccessHistory> records) {
        List<UUID> employeeIds = records.stream()
                .map(AccessHistory::getEmployee)
                .filter(java.util.Objects::nonNull)
                .map(e -> e.getId())
                .distinct()
                .toList();
        if (employeeIds.isEmpty()) {
            return Set.of();
        }
        return new HashSet<>(userRepository.findEmployeeIdsWithUser(employeeIds));
    }

    private AccessHistoryResponse toResponse(AccessHistory h, Set<UUID> employeesWithUser) {
        boolean hasUser = h.getEmployee() != null
                && employeesWithUser.contains(h.getEmployee().getId());
        return AccessHistoryResponse.builder()
                .id(h.getId())
                .employeeId(h.getEmployee() != null ? h.getEmployee().getId() : null)
                .employeeCode(h.getEmployee() != null ? h.getEmployee().getEmployeeCode() : null)
                .employeeName(h.getEmployee() != null
                        ? h.getEmployee().getFirstName() + " " + h.getEmployee().getLastName() : null)
                .position(h.getEmployee() != null ? h.getEmployee().getPosition() : null)
                .department(h.getDepartment())
                .productionAreaName(h.getProductionAreaName())
                .timestamp(h.getTimestamp())
                .result(h.getResult())
                .hasUser(hasUser)
                .build();
    }
}
