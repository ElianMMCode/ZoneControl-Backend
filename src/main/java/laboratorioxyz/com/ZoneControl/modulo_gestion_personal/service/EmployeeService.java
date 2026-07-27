package laboratorioxyz.com.ZoneControl.modulo_gestion_personal.service;

import laboratorioxyz.com.ZoneControl.modulo_gestion_personal.dto.RegisterEmployeeRequest;
import laboratorioxyz.com.ZoneControl.modulo_gestion_personal.dto.RegisterEmployeeResponse;

/**
 * Servicio de gestión de personal.
 * Define las operaciones sobre empleados, comenzando con el
 * registro individual con generación automática de código EMP-XXXXXX.
 */
public interface EmployeeService {
    RegisterEmployeeResponse register(RegisterEmployeeRequest request);
}
