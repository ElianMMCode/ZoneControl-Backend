package laboratorioxyz.com.ZoneControl.modulo_publico.service;

import laboratorioxyz.com.ZoneControl.modulo_publico.dto.CatalogResponse;
import laboratorioxyz.com.ZoneControl.modulo_publico.dto.ContactResponse;
import laboratorioxyz.com.ZoneControl.modulo_publico.dto.InstitutionalResponse;
import laboratorioxyz.com.ZoneControl.modulo_publico.dto.OfficeResponse;
import org.springframework.core.io.Resource;

import java.util.List;

public interface PublicService {
    InstitutionalResponse getInstitutionalInfo();
    ContactResponse getContactInfo();
    List<OfficeResponse> getOffices();
    List<CatalogResponse> getCatalog();
    Resource getBrochure();
}
