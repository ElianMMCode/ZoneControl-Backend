package laboratorioxyz.com.ZoneControl.modulo_control_acceso.service;

import laboratorioxyz.com.ZoneControl.modulo_control_acceso.dto.ValidateAccessResponse;

import java.util.UUID;

public interface AccessValidationService {
    ValidateAccessResponse validate(String employeeCode, UUID productionAreaId);
}
