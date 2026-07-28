package laboratorioxyz.com.ZoneControl.config;

import laboratorioxyz.com.ZoneControl.model.enums.PermissionStatus;
import laboratorioxyz.com.ZoneControl.modulo_gestion_personal.model.AccessPermission;
import laboratorioxyz.com.ZoneControl.modulo_gestion_personal.repository.AccessPermissionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class PermissionScheduler {

    private final AccessPermissionRepository accessPermissionRepository;

    @Scheduled(fixedRate = 3600000)
    @Transactional
    public void reactivateExpiredSuspensions() {
        List<AccessPermission> expired = accessPermissionRepository
                .findByStatusAndReactivationDateBefore(PermissionStatus.SUSPENDIDO, LocalDate.now());

        for (AccessPermission permission : expired) {
            permission.setStatus(PermissionStatus.ACTIVO);
            permission.setReactivationDate(null);
            accessPermissionRepository.save(permission);
            log.info("Auto-reactivated permission id={} for employee={}",
                    permission.getId(), permission.getEmployee().getEmployeeCode());
        }

        if (!expired.isEmpty()) {
            log.info("Auto-reactivated {} suspended permissions", expired.size());
        }
    }
}
