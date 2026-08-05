package laboratorioxyz.com.ZoneControl.modulo_control_acceso.repository;

import laboratorioxyz.com.ZoneControl.modulo_control_acceso.model.AccessAlert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

public interface AccessAlertRepository extends JpaRepository<AccessAlert, UUID> {

    /**
     * Cleanup idempotente de alertas obsoletas ACCESO_NOCTURNO (tipo eliminado
     * del enum). Se ejecuta antes de sembrar/leer alertas para no romper la
     * deserialización de filas que ya no tienen tipo.
     */
    @Modifying
    @Query(value = "DELETE FROM access_alerts WHERE tipo = 'ACCESO_NOCTURNO'", nativeQuery = true)
    @Transactional
    void deleteNocturnalAlerts();
}
