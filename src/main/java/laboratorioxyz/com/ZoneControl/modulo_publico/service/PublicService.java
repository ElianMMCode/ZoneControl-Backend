package laboratorioxyz.com.ZoneControl.modulo_publico.service;

import laboratorioxyz.com.ZoneControl.modulo_publico.dto.CatalogResponse;
import laboratorioxyz.com.ZoneControl.modulo_publico.dto.ContactResponse;
import laboratorioxyz.com.ZoneControl.modulo_publico.dto.InstitutionalResponse;
import laboratorioxyz.com.ZoneControl.modulo_publico.dto.OfficeResponse;
import org.springframework.core.io.Resource;

import java.util.List;

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
}
