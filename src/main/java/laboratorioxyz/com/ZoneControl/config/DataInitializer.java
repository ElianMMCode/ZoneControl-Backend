package laboratorioxyz.com.ZoneControl.config;

import laboratorioxyz.com.ZoneControl.model.entity.Department;
import laboratorioxyz.com.ZoneControl.model.entity.Office;
import laboratorioxyz.com.ZoneControl.model.entity.ProductionArea;
import laboratorioxyz.com.ZoneControl.model.enums.ContentSection;
import laboratorioxyz.com.ZoneControl.model.enums.DocumentType;
import laboratorioxyz.com.ZoneControl.model.enums.EmployeeStatus;
import laboratorioxyz.com.ZoneControl.model.enums.Role;
import laboratorioxyz.com.ZoneControl.model.enums.UserStatus;
import laboratorioxyz.com.ZoneControl.model.repository.DepartmentRepository;
import laboratorioxyz.com.ZoneControl.model.repository.ProductionAreaRepository;
import laboratorioxyz.com.ZoneControl.modulo_autenticacion.model.User;
import laboratorioxyz.com.ZoneControl.modulo_autenticacion.repository.UserRepository;
import laboratorioxyz.com.ZoneControl.modulo_gestion_personal.model.Employee;
import laboratorioxyz.com.ZoneControl.modulo_gestion_personal.repository.EmployeeRepository;
import laboratorioxyz.com.ZoneControl.modulo_publico.model.ProductCatalog;
import laboratorioxyz.com.ZoneControl.modulo_publico.model.PublicContent;
import laboratorioxyz.com.ZoneControl.modulo_publico.repository.OfficeRepository;
import laboratorioxyz.com.ZoneControl.modulo_publico.repository.ProductCatalogRepository;
import laboratorioxyz.com.ZoneControl.modulo_publico.repository.PublicContentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Inicializador de datos semilla para la base de datos.
 * Se ejecuta al arrancar la aplicación y siembra datos iniciales
 * solo si las tablas correspondientes están vacías.
 *
 * Incluye:
 * - 6 departamentos de producción
 * - 5 áreas restringidas de producción
 * - 1 usuario administrador por defecto
 * - Contenido público de ejemplo (misión, visión, contacto)
 * - 2 sedes físicas
 * - 2 productos del catálogo
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final DepartmentRepository departmentRepository;
    private final ProductionAreaRepository productionAreaRepository;
    private final UserRepository userRepository;
    private final EmployeeRepository employeeRepository;
    private final PublicContentRepository publicContentRepository;
    private final OfficeRepository officeRepository;
    private final ProductCatalogRepository productCatalogRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    private Department adminDepartment;

    /**
     * Cada método de seed verifica si ya existen datos antes de insertar,
     * permitiendo reinicios del servidor sin duplicar registros.
     */
    @Override
    public void run(String... args) {
        log.info("Seeding initial data...");
        seedDepartments();
        seedProductionAreas();
        seedAdminUser();
        seedPublicContent();
        seedOffices();
        seedProductCatalog();
        seedCandidateEmployees();
        log.info("Seed completed successfully");
    }

    private void seedDepartments() {
        if (departmentRepository.count() > 0) {
            adminDepartment = departmentRepository.findByName("Control de Calidad").orElseThrow();
            log.info("Departments already exist — skipping");
            return;
        }
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
        adminDepartment = departmentRepository.findByName("Control de Calidad").orElseThrow();
        log.info("Seeded {} departments", names.length);
    }

    private void seedProductionAreas() {
        if (productionAreaRepository.count() > 0) {
            log.info("Production areas already exist — skipping");
            return;
        }
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

    /**
     * Crea el empleado administrador y su usuario de sistema asociado.
     * Primero se crea el Employee (EMP-000001) y luego el User vinculado,
     * cumpliendo la regla @OneToOne obligatoria (todo User debe tener un Employee).
     */
    private void seedAdminUser() {
        if (userRepository.count() > 0) {
            log.info("Users already exist — skipping");
            return;
        }
        Employee adminEmployee = Employee.builder()
                .employeeCode("EMP-000001")
                .documentType(DocumentType.CC)
                .documentNumber("0000000001")
                .firstName("Admin")
                .lastName("ZoneControl")
                .position("Administrador del Sistema")
                .department(adminDepartment)
                .status(EmployeeStatus.ACTIVO)
                .build();
        adminEmployee = employeeRepository.save(adminEmployee);

        User admin = User.builder()
                .firstName("Admin")
                .lastName("ZoneControl")
                .email("admin@zonecontrol.com")
                .password(passwordEncoder.encode("Admin123!"))
                .role(Role.ADMIN)
                .status(UserStatus.ACTIVO)
                .requirePasswordChange(false)
                .employee(adminEmployee)
                .build();

        userRepository.save(admin);
        log.info("Seeded admin user: admin@zonecontrol.com");
    }

    private void seedPublicContent() {
        if (publicContentRepository.count() > 0) {
            log.info("Public content already exists — skipping");
            return;
        }
        publicContentRepository.save(PublicContent.builder()
                .section(ContentSection.INSTITUTIONAL).key("mission")
                .value("Proveer medicamentos de alta calidad que mejoren la calidad de vida de los pacientes colombianos").build());
        publicContentRepository.save(PublicContent.builder()
                .section(ContentSection.INSTITUTIONAL).key("vision")
                .value("Ser líderes en la producción farmacéutica en Latinoamérica para 2030").build());
        publicContentRepository.save(PublicContent.builder()
                .section(ContentSection.INSTITUTIONAL).key("description")
                .value("Laboratorio XYZ es una compañía farmacéutica colombiana especializada en la producción de medicamentos de alto costo para enfermedades crónicas y huérfanas").build());

        publicContentRepository.save(PublicContent.builder()
                .section(ContentSection.CONTACT).key("phone")
                .value("+57 601 234 5678").build());
        publicContentRepository.save(PublicContent.builder()
                .section(ContentSection.CONTACT).key("email")
                .value("contacto@laboratoriosxyz.com").build());
        publicContentRepository.save(PublicContent.builder()
                .section(ContentSection.CONTACT).key("socialMedia")
                .value("@LaboratorioXYZ").build());
        log.info("Seeded public content");
    }

    private void seedOffices() {
        if (officeRepository.count() > 0) {
            log.info("Offices already exist — skipping");
            return;
        }
        officeRepository.save(Office.builder()
                .name("Sede Principal Bogotá")
                .address("Cra 45 # 26-85, Bogotá D.C.")
                .openingHours("Lun-Vie 8:00-18:00")
                .latitude(4.7110)
                .longitude(-74.0721)
                .build());
        officeRepository.save(Office.builder()
                .name("Planta de Producción Medellín")
                .address("Cl 10 # 20-30, Medellín, Antioquia")
                .openingHours("Lun-Sáb 6:00-20:00")
                .latitude(6.2476)
                .longitude(-75.5658)
                .build());
        log.info("Seeded {} offices", 2);
    }

    private void seedProductCatalog() {
        if (productCatalogRepository.count() > 0) {
            log.info("Product catalog already exists — skipping");
            return;
        }
        productCatalogRepository.save(ProductCatalog.builder()
                .name("Ácido Acetilsalicílico Genfar")
                .description("Analgésico y antiinflamatorio no esteroideo")
                .activeIngredient("Ácido Acetilsalicílico")
                .presentation("Tabletas 500mg x 30")
                .productionArea("Sala Blanca A")
                .build());
        productCatalogRepository.save(ProductCatalog.builder()
                .name("Omeprazol MK")
                .description("Inhibidor de la bomba de protones para tratamiento de acidez gástrica")
                .activeIngredient("Omeprazol")
                .presentation("Cápsulas 20mg x 14")
                .productionArea("Sala Blanca B")
                .build());
        log.info("Seeded {} products", 2);
    }

    /**
     * Siembra 2 empleados candidatos a ser activados como usuarios del sistema:
     * tienen systemRole + email, pero no están vinculados a un User. Sirven para
     * que el Admin los vea en el panel "Empleados pendientes de activación" y en
     * la página de Gestión de Usuarios.
     */
    private void seedCandidateEmployees() {
        long existing = employeeRepository.findAll().stream()
                .filter(e -> e.getSystemRole() != null)
                .filter(e -> userRepository.findByEmployee_Id(e.getId()).isEmpty())
                .count();
        if (existing > 0) {
            log.info("Candidate employees already exist — skipping");
            return;
        }
        Department calidad = departmentRepository.findByName("Control de Calidad").orElseThrow();
        employeeRepository.save(Employee.builder()
                .employeeCode("EMP-000002")
                .documentType(DocumentType.CC)
                .documentNumber("0000000002")
                .firstName("Lucía")
                .lastName("Fernandez")
                .position("Coordinadora de Personal")
                .email("lucia.fernandez@laboratorioxzy.com.co")
                .department(calidad)
                .status(EmployeeStatus.ACTIVO)
                .systemRole(Role.GESTOR_PERSONAL)
                .build());
        employeeRepository.save(Employee.builder()
                .employeeCode("EMP-000003")
                .documentType(DocumentType.CC)
                .documentNumber("0000000003")
                .firstName("Roberto")
                .lastName("Gómez")
                .position("Auditor Interno")
                .email("roberto.gomez@laboratorioxzy.com.co")
                .department(calidad)
                .status(EmployeeStatus.ACTIVO)
                .systemRole(Role.SUPERVISOR_AUDITOR)
                .build());
        log.info("Seeded 2 candidate employees for activation");
    }
}
