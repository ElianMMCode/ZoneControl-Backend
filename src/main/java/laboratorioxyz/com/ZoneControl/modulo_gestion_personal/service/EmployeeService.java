package laboratorioxyz.com.ZoneControl.modulo_gestion_personal.service;

import laboratorioxyz.com.ZoneControl.modulo_gestion_personal.dto.RegisterEmployeeRequest;
import laboratorioxyz.com.ZoneControl.modulo_gestion_personal.dto.RegisterEmployeeResponse;

public interface EmployeeService {
    RegisterEmployeeResponse register(RegisterEmployeeRequest request);
}
