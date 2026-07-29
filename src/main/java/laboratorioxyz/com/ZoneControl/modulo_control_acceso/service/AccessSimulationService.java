package laboratorioxyz.com.ZoneControl.modulo_control_acceso.service;

import laboratorioxyz.com.ZoneControl.modulo_control_acceso.dto.SimulateAccessResponse;

import java.util.UUID;

public interface AccessSimulationService {
    SimulateAccessResponse simulate(String employeeCode, UUID productionAreaId);
}
