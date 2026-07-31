package laboratorioxyz.com.ZoneControl.modulo_reportes.dto;

/**
 * Indicadores agregados para el dashboard del supervisor (GET /historial/stats).
 * Alimenta las tarjetas KPI de la vista de Dashboard del supervisor/auditor:
 * accesos del día por resultado, permisos activos/suspendidos y empleados con acceso.
 */
public record SupervisorStatsResponse(
        long totalAccesosHoy,
        long accesosAutorizadosHoy,
        long accesosDenegadosHoy,
        long accesosNoRegistradosHoy,
        long accesosSuspendidosHoy,
        long totalPermisosActivos,
        long totalPermisosSuspendidos,
        long empleadosConAcceso
) {}
