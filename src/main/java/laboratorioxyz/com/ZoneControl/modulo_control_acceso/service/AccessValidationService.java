package laboratorioxyz.com.ZoneControl.modulo_control_acceso.service;

import laboratorioxyz.com.ZoneControl.modulo_control_acceso.dto.ValidateAccessResponse;

public interface AccessValidationService {
    ValidateAccessResponse validate(String employeeCode, String productionAreaName);
}
