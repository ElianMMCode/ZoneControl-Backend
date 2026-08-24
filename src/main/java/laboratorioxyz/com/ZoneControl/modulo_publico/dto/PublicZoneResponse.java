package laboratorioxyz.com.ZoneControl.modulo_publico.dto;

/**
 * Zona de producción expuesta en el modo autoservicio público (/validar).
 * Solo datos no sensibles: nombre, descripción y estado de emergencia.
 */
public record PublicZoneResponse(
    String name,
    String description,
    boolean emergencyClosed
) {}
