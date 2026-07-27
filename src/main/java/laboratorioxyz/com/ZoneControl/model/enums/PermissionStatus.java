package laboratorioxyz.com.ZoneControl.model.enums;

/**
 * Estado de un permiso de acceso a área de producción.
 * ACTIVO: el empleado puede acceder en horario permitido.
 * SUSPENDIDO: acceso temporalmente revocado, puede tener fecha de reactivación.
 */
public enum PermissionStatus {
    ACTIVO,
    SUSPENDIDO
}
