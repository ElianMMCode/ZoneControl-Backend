package laboratorioxyz.com.ZoneControl.modulo_gestion_personal.dto;

import laboratorioxyz.com.ZoneControl.model.enums.AccessResult;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Registro plano del historial de accesos de un empleado para el modal
 * de detalle. Evita serializar la entidad AccessHistory (relación LAZY
 * con Employee provocaba errores 500 al serializar fuera de transacción).
 */
public record EmployeeAccessRecordResponse(
    UUID id,
    String employeeCode,
    String employeeName,
    String department,
    String productionAreaName,
    LocalDateTime timestamp,
    AccessResult result
) {}
