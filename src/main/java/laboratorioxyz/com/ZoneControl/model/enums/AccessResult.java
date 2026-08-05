package laboratorioxyz.com.ZoneControl.model.enums;

/**
 * Resultado de un intento de acceso simulado a área restringida.
 */
public enum AccessResult {
    AUTHORIZED,     // Ingreso permitido
    DENIED,         // Ingreso denegado (empleado inactivo o permiso vencido)
    UNREGISTERED,   // No registrado (EMP no existe)
    SUSPENDED,      // Acceso suspendido (permiso suspendido)
    EXIT            // Salida registrada (cierre de sesión)
}
