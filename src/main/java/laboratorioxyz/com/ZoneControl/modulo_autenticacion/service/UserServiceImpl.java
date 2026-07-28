package laboratorioxyz.com.ZoneControl.modulo_autenticacion.service;

import laboratorioxyz.com.ZoneControl.model.enums.UserStatus;
import laboratorioxyz.com.ZoneControl.modulo_autenticacion.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    @Transactional
    public void deactivateByEmployeeId(UUID employeeId) {
        userRepository.findByEmployee_Id(employeeId).ifPresent(user -> {
            user.setStatus(UserStatus.INACTIVO);
            userRepository.save(user);
            log.info("User {} deactivated due to employee {} status change", user.getId(), employeeId);
        });
    }
}
