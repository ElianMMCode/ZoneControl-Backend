package laboratorioxyz.com.ZoneControl.model.enums;

/**
 * Secciones del módulo público editables por el administrador (HU-19).
 * Las sedes no se gestionan aquí: tienen CRUD propio
 * (/api/admin/contenido-publico/sedes) sobre la tabla offices.
 */
public enum ContentSection {
    INSTITUTIONAL,  // Misión, visión, descripción
    CONTACT         // Teléfonos, email, redes sociales
}
