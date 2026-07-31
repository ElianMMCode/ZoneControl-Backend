package laboratorioxyz.com.ZoneControl.modulo_administracion.dto;

/**
 * Indicadores agregados para el dashboard del administrador (GET /admin/stats).
 * Alimenta las tarjetas KPI de la vista de Dashboard: usuarios, empleados y
 * permisos. usuariosSinConfiguracion cuenta los usuarios que aún tienen un
 * setupToken vigente (magic link pendiente de completar).
 */
public record AdminStatsResponse(
        long totalUsuarios,
        long usuariosActivos,
        long usuariosInactivos,
        long usuariosSinConfiguracion,
        long totalEmpleados,
        long empleadosActivos,
        long totalPermisos,
        long permisosActivos,
        long permisosSuspendidos
) {}
