package laboratorioxyz.com.ZoneControl.modulo_publico.service;

import laboratorioxyz.com.ZoneControl.modulo_publico.dto.CatalogResponse;
import laboratorioxyz.com.ZoneControl.modulo_publico.dto.ContactResponse;
import laboratorioxyz.com.ZoneControl.modulo_publico.dto.InstitutionalResponse;
import laboratorioxyz.com.ZoneControl.modulo_publico.dto.OfficeResponse;
import laboratorioxyz.com.ZoneControl.modulo_publico.dto.PublicZoneResponse;
import org.springframework.core.io.Resource;

import java.util.List;
import java.util.UUID;

/**
 * Servicio del módulo público.
 * Define los contratos para consultar información institucional,
 * datos de contacto, sedes, catálogo de productos y folleto PDF.
 * Todos los métodos GET son cacheados para optimizar la carga
 * de contenido estático (ver @Cacheable en la implementación).
 */
public interface PublicService {
    InstitutionalResponse getInstitutionalInfo();
    ContactResponse getContactInfo();
    List<OfficeResponse> getOffices();
    List<CatalogResponse> getCatalog();
    Resource getBrochure();
    /**
     * Imagen del producto. Retorna null si el producto no existe o no tiene imagen.
     */
    Resource getProductImage(UUID id);
    /**
     * Imagen de la sede. Retorna null si la sede no existe o no tiene imagen.
     */
    Resource getOfficeImage(UUID id);
    /**
     * Zonas activas para el modo autoservicio público (/validar).
     */
    List<PublicZoneResponse> getZones();
}
