package laboratorioxyz.com.ZoneControl.config;

import laboratorioxyz.com.ZoneControl.model.entity.Department;
import laboratorioxyz.com.ZoneControl.model.entity.ProductionArea;
import laboratorioxyz.com.ZoneControl.model.enums.Role;
import laboratorioxyz.com.ZoneControl.model.enums.UserStatus;
import laboratorioxyz.com.ZoneControl.model.repository.DepartmentRepository;
import laboratorioxyz.com.ZoneControl.model.repository.ProductionAreaRepository;
import laboratorioxyz.com.ZoneControl.modulo_autenticacion.model.User;
import laboratorioxyz.com.ZoneControl.modulo_autenticacion.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final DepartmentRepository departmentRepository;
    private final ProductionAreaRepository productionAreaRepository;
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (departmentRepository.count() > 0) {
            log.info("Database already contains data — skipping seed");
            return;
        }

        log.info("Seeding initial data...");
        seedDepartments();
        seedProductionAreas();
        seedAdminUser();
        log.info("Seed completed successfully");
    }

    private void seedDepartments() {
        String[] names = {
            "Control de Calidad",
            "Producción Sólidos",
            "Producción Líquidos",
            "Esterilización",
            "Empaque",
            "Almacenamiento"
        };

        for (String name : names) {
            departmentRepository.save(Department.builder()
                    .name(name)
                    .build());
        }
        log.info("Seeded {} departments", names.length);
    }

    private void seedProductionAreas() {
        String[] names = {
            "Sala Blanca A",
            "Sala Blanca B",
            "Laboratorio QC",
            "Almacén Controlado",
            "Zona de Empaque"
        };

        for (String name : names) {
            productionAreaRepository.save(ProductionArea.builder()
                    .name(name)
                    .build());
        }
        log.info("Seeded {} production areas", names.length);
    }

    private void seedAdminUser() {
        User admin = User.builder()
                .firstName("Admin")
                .lastName("ZoneControl")
                .email("admin@zonecontrol.com")
                .password(passwordEncoder.encode("Admin123!"))
                .role(Role.ADMIN)
                .status(UserStatus.ACTIVO)
                .requirePasswordChange(false)
                .build();

        userRepository.save(admin);
        log.info("Seeded admin user: admin@zonecontrol.com");
    }
}
