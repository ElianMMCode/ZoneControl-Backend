package laboratorioxyz.com.ZoneControl.config;

import laboratorioxyz.com.ZoneControl.model.entity.Department;
import laboratorioxyz.com.ZoneControl.model.entity.Office;
import laboratorioxyz.com.ZoneControl.model.entity.ProductionArea;
import laboratorioxyz.com.ZoneControl.model.enums.ContentSection;
import laboratorioxyz.com.ZoneControl.model.enums.DocumentType;
import laboratorioxyz.com.ZoneControl.model.enums.EmployeeStatus;
import laboratorioxyz.com.ZoneControl.model.enums.Role;
import laboratorioxyz.com.ZoneControl.model.enums.AccessResult;
import laboratorioxyz.com.ZoneControl.model.enums.PermissionStatus;
import laboratorioxyz.com.ZoneControl.model.enums.UserStatus;
import laboratorioxyz.com.ZoneControl.model.repository.DepartmentRepository;
import laboratorioxyz.com.ZoneControl.model.repository.ProductionAreaRepository;
import laboratorioxyz.com.ZoneControl.modulo_autenticacion.model.User;
import laboratorioxyz.com.ZoneControl.modulo_autenticacion.repository.UserRepository;
import laboratorioxyz.com.ZoneControl.modulo_control_acceso.model.AccessHistory;
import laboratorioxyz.com.ZoneControl.modulo_control_acceso.repository.AccessHistoryRepository;
import laboratorioxyz.com.ZoneControl.modulo_gestion_personal.model.AccessPermission;
import laboratorioxyz.com.ZoneControl.modulo_gestion_personal.model.Employee;
import laboratorioxyz.com.ZoneControl.modulo_gestion_personal.repository.AccessPermissionRepository;
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
    private final AccessPermissionRepository accessPermissionRepository;
    private final AccessHistoryRepository accessHistoryRepository;
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
        seedExtraEmployees();
        seedExtraUsers();
        seedAccessPermissions();
        seedCandidateEmployees();
        seedAccessHistory();
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
     * Empleados extra (no candidatos) que dan variedad al directorio:
     * algunos tienen rol de sistema pero sin email (no son candidatos,
     * sirven para demostrar que el filtro los excluye), otros no tienen
     * rol de sistema y representan empleados de solo acceso físico, y
     * un par están en estados no-activos.
     *
     * Es idempotente: si el employeeCode ya existe, no duplica.
     */
    private void seedExtraEmployees() {
        Department calidad = departmentRepository.findByName("Control de Calidad").orElseThrow();
        Department produccion = departmentRepository.findByName("Producción Sólidos").orElseThrow();
        Department empaque = departmentRepository.findByName("Empaque").orElseThrow();

        // saveEmployee es idempotente por código: si ya existe, no duplica.
        saveEmployee("EMP-000030", "100000030", "Eduardo", "Vega",
                "Jefe de Sistemas", null, calidad, EmployeeStatus.ACTIVO, Role.ADMIN);
        saveEmployee("EMP-000031", "100000031", "Daniela", "Torres",
                "Analista de Producción", "daniela.torres@laboratorioxzy.com.co",
                produccion, EmployeeStatus.ACTIVO, null);
        saveEmployee("EMP-000032", "100000032", "Felipe", "Mora",
                "Auxiliar de Empaque", "felipe.mora@laboratorioxzy.com.co",
                empaque, EmployeeStatus.SUSPENDIDO, null);
    }

    /**
     * Crea empleados que ya tienen un usuario asociado (varios estados y
     * roles) para que el dashboard muestre variedad: usuarios activos de
     * distintos roles, uno inactivo, y dos con setupToken pendiente
     * (para alimentar el panel "Usuarios sin configuración" y la KPI
     * "Sin configuración" de la página de Gestión de Usuarios).
     *
     * Los empleados se crean en seedExtraEmployees; aquí se crean los
     * usuarios vinculados. Es idempotente: si el email ya existe, no
     * duplica.
     */
    private void seedExtraUsers() {
        if (userRepository.findByEmail("sandra.ruiz@laboratorioxzy.com.co").isPresent()) {
            log.info("Extra users already exist — skipping");
            return;
        }
        Employee sandra = employeeRepository.findByEmployeeCode("EMP-000040").orElse(null);
        if (sandra == null) {
            sandra = saveEmployee("EMP-000040", "100000040", "Sandra", "Ruiz",
                    "Coordinadora de Administración",
                    "sandra.ruiz@laboratorioxzy.com.co",
                    departmentRepository.findByName("Control de Calidad").orElseThrow(),
                    EmployeeStatus.ACTIVO, null);
        }
        Employee javier = employeeRepository.findByEmployeeCode("EMP-000041").orElse(null);
        if (javier == null) {
            javier = saveEmployee("EMP-000041", "100000041", "Javier", "Soto",
                    "Supervisor de Turno",
                    "javier.soto@laboratorioxzy.com.co",
                    departmentRepository.findByName("Producción Sólidos").orElseThrow(),
                    EmployeeStatus.ACTIVO, null);
        }
        Employee miguel = employeeRepository.findByEmployeeCode("EMP-000042").orElse(null);
        if (miguel == null) {
            miguel = saveEmployee("EMP-000042", "100000042", "Miguel", "Ángel",
                    "Gestor de Personal",
                    "miguel.angel@laboratorioxzy.com.co",
                    departmentRepository.findByName("Control de Calidad").orElseThrow(),
                    EmployeeStatus.INACTIVO, null);
        }
        Employee ricardo = employeeRepository.findByEmployeeCode("EMP-000043").orElse(null);
        if (ricardo == null) {
            ricardo = saveEmployee("EMP-000043", "100000043", "Ricardo", "Díaz",
                    "Gestor de Personal",
                    "ricardo.diaz@laboratorioxzy.com.co",
                    departmentRepository.findByName("Control de Calidad").orElseThrow(),
                    EmployeeStatus.ACTIVO, null);
        }
        Employee ana = employeeRepository.findByEmployeeCode("EMP-000044").orElse(null);
        if (ana == null) {
            ana = saveEmployee("EMP-000044", "100000044", "Ana", "Martínez",
                    "Administradora",
                    "ana.martinez@laboratorioxzy.com.co",
                    departmentRepository.findByName("Control de Calidad").orElseThrow(),
                    EmployeeStatus.ACTIVO, null);
        }

        createUser(sandra, "Sandra", "Ruiz", sandra.getEmail(), Role.GESTOR_PERSONAL, UserStatus.ACTIVO, null, null);
        createUser(javier, "Javier", "Soto", javier.getEmail(), Role.SUPERVISOR_AUDITOR, UserStatus.ACTIVO, null, null);
        createUser(miguel, "Miguel", "Ángel", miguel.getEmail(), Role.GESTOR_PERSONAL, UserStatus.INACTIVO, null, null);
        // Ricardo y Ana tienen setupToken pendiente → aparecen en el panel
        // "Usuarios sin configuración" del dashboard.
        createUser(ricardo, "Ricardo", "Díaz", ricardo.getEmail(), Role.GESTOR_PERSONAL, UserStatus.ACTIVO,
                "pending-ricardo-hash", java.time.LocalDateTime.now().plusHours(24));
        createUser(ana, "Ana", "Martínez", ana.getEmail(), Role.ADMIN, UserStatus.ACTIVO,
                "pending-ana-hash", java.time.LocalDateTime.now().plusHours(24));

        log.info("Seeded 5 extra users (Sandra, Javier, Miguel, Ricardo, Ana)");
    }

    private void createUser(Employee employee, String firstName, String lastName, String email,
                            Role role, UserStatus status, String setupToken,
                            java.time.LocalDateTime setupTokenExpiry) {
        User.UserBuilder builder = User.builder()
                .firstName(firstName)
                .lastName(lastName)
                .email(email)
                .password(setupToken == null ? passwordEncoder.encode("Demo1234!") : null)
                .role(role)
                .status(status)
                .requirePasswordChange(false)
                .employee(employee);
        if (setupToken != null) {
            builder.setupToken(setupToken).setupTokenExpiry(setupTokenExpiry);
        }
        userRepository.save(builder.build());
    }

    /**
     * Siembra permisos de acceso (algunos activos, otros suspendidos)
     * para que las KPI "Permisos activos" y "Permisos suspendidos"
     * del dashboard tengan contenido y para que el historial los
     * pueda referenciar. Es idempotente: si ya hay al menos 3
     * permisos no crea más.
     */
    private void seedAccessPermissions() {
        ProductionArea salaBlancaA = productionAreaRepository.findByName("Sala Blanca A").orElseThrow();
        ProductionArea salaBlancaB = productionAreaRepository.findByName("Sala Blanca B").orElseThrow();

        Employee admin = employeeRepository.findByEmployeeCode("EMP-000001").orElse(null);
        Employee sandra = employeeRepository.findByEmployeeCode("EMP-000040").orElse(null);
        Employee javier = employeeRepository.findByEmployeeCode("EMP-000041").orElse(null);
        Employee miguel = employeeRepository.findByEmployeeCode("EMP-000042").orElse(null);

        java.time.LocalDate today = java.time.LocalDate.now();
        // Idempotente: solo crea el permiso si el empleado aún no tiene
        // ninguno para esa área. Así un re-arranque no duplica filas.
        if (admin != null && accessPermissionRepository.findByEmployee_Id(admin.getId()).isEmpty()) {
            accessPermissionRepository.save(AccessPermission.builder()
                    .employee(admin).productionArea(salaBlancaA)
                    .status(PermissionStatus.ACTIVO)
                    .startDate(today).expirationDate(today.plusYears(1))
                    .startTime(java.time.LocalTime.of(0, 0))
                    .endTime(java.time.LocalTime.of(23, 59))
                    .build());
        }
        if (sandra != null && accessPermissionRepository.findByEmployee_Id(sandra.getId()).isEmpty()) {
            accessPermissionRepository.save(AccessPermission.builder()
                    .employee(sandra).productionArea(salaBlancaA)
                    .status(PermissionStatus.ACTIVO)
                    .startDate(today).expirationDate(today.plusMonths(6))
                    .startTime(java.time.LocalTime.of(7, 0))
                    .endTime(java.time.LocalTime.of(17, 0))
                    .build());
        }
        if (javier != null && accessPermissionRepository.findByEmployee_Id(javier.getId()).isEmpty()) {
            accessPermissionRepository.save(AccessPermission.builder()
                    .employee(javier).productionArea(salaBlancaB)
                    .status(PermissionStatus.ACTIVO)
                    .startDate(today).expirationDate(today.plusMonths(6))
                    .startTime(java.time.LocalTime.of(6, 0))
                    .endTime(java.time.LocalTime.of(22, 0))
                    .build());
        }
        if (miguel != null && accessPermissionRepository.findByEmployee_Id(miguel.getId()).isEmpty()) {
            accessPermissionRepository.save(AccessPermission.builder()
                    .employee(miguel).productionArea(salaBlancaA)
                    .status(PermissionStatus.SUSPENDIDO)
                    .startDate(today).expirationDate(today.plusMonths(3))
                    .startTime(java.time.LocalTime.of(8, 0))
                    .endTime(java.time.LocalTime.of(17, 0))
                    .build());
        }
        log.info("Seeded access permissions (3 activos, 1 suspendido)");
    }

    /**
     * Siembra 6 registros de AccessHistory distribuidos a lo largo del
     * día de hoy con resultados variados (autorizado, denegado, no
     * registrado, suspendido) para que el panel "Actividad reciente"
     * del dashboard del Admin y la página del Supervisor tengan
     * contenido realista. Es idempotente: si ya hay al menos un
     * registro con timestamp de hoy, no crea más.
     */
    private void seedAccessHistory() {
        java.time.LocalDateTime startOfDay = java.time.LocalDate.now().atStartOfDay();
        boolean todayHasHistory = accessHistoryRepository.findAll().stream()
                .anyMatch(h -> h.getTimestamp() != null
                        && h.getTimestamp().toLocalDate().isEqual(java.time.LocalDate.now()));
        if (todayHasHistory) {
            log.info("Access history for today already exists — skipping");
            return;
        }
        Employee admin = employeeRepository.findByEmployeeCode("EMP-000001").orElse(null);
        Employee sandra = employeeRepository.findByEmployeeCode("EMP-000040").orElse(null);
        Employee javier = employeeRepository.findByEmployeeCode("EMP-000041").orElse(null);

        saveHistory(admin, "Control de Calidad", "Sala Blanca A", startOfDay.plusHours(8).plusMinutes(12), AccessResult.AUTHORIZED);
        saveHistory(sandra, "Control de Calidad", "Sala Blanca A", startOfDay.plusHours(9).plusMinutes(3), AccessResult.AUTHORIZED);
        saveHistory(javier, "Producción Sólidos", "Sala Blanca B", startOfDay.plusHours(9).plusMinutes(47), AccessResult.AUTHORIZED);
        saveHistory(null, "Producción Sólidos", "Sala Blanca B", startOfDay.plusHours(10).plusMinutes(15), AccessResult.UNREGISTERED);
        saveHistory(null, "Almacenamiento", "Almacén Controlado", startOfDay.plusHours(11).plusMinutes(22), AccessResult.DENIED);
        saveHistory(null, "Producción Sólidos", "Sala Blanca B", startOfDay.plusHours(12).plusMinutes(5), AccessResult.SUSPENDED);
        log.info("Seeded 6 access history records for today");
    }

    private void saveHistory(Employee employee, String department, String area,
                             java.time.LocalDateTime timestamp, AccessResult result) {
        accessHistoryRepository.save(AccessHistory.builder()
                .employee(employee)
                .department(department)
                .productionAreaName(area)
                .timestamp(timestamp)
                .result(result)
                .build());
    }

    /**
     * Empleados candidatos a ser activados como usuarios del sistema:
     * tienen systemRole + email + ACTIVO + sin usuario. Cubren los tres
     * roles del sistema para que el panel "Empleados pendientes de
     * activación" muestre variedad. Es idempotente: si los códigos ya
     * existen, no duplica.
     */
    private void seedCandidateEmployees() {
        Department calidad = departmentRepository.findByName("Control de Calidad").orElseThrow();
        Department produccion = departmentRepository.findByName("Producción Sólidos").orElseThrow();
        Department empaque = departmentRepository.findByName("Empaque").orElseThrow();

        // saveEmployee es idempotente por código.
        saveEmployee("EMP-000002", "0000000002", "Lucía", "Fernandez",
                "Coordinadora de Personal", "lucia.fernandez@laboratorioxzy.com.co",
                calidad, EmployeeStatus.ACTIVO, Role.GESTOR_PERSONAL);
        saveEmployee("EMP-000003", "0000000003", "Roberto", "Gómez",
                "Auditor Interno", "roberto.gomez@laboratorioxzy.com.co",
                calidad, EmployeeStatus.ACTIVO, Role.SUPERVISOR_AUDITOR);
        saveEmployee("EMP-000020", "0000000020", "Diego", "Ramírez",
                "Jefe de Operaciones", "diego.ramirez@laboratorioxzy.com.co",
                produccion, EmployeeStatus.ACTIVO, Role.ADMIN);
        saveEmployee("EMP-000021", "0000000021", "Patricia", "Núñez",
                "Analista de Nómina", "patricia.nunez@laboratorioxzy.com.co",
                empaque, EmployeeStatus.ACTIVO, Role.GESTOR_PERSONAL);
        saveEmployee("EMP-000022", "0000000022", "Andrés", "Castillo",
                "Auditor Senior", "andres.castillo@laboratorioxzy.com.co",
                calidad, EmployeeStatus.ACTIVO, Role.SUPERVISOR_AUDITOR);
        saveEmployee("EMP-000023", "0000000023", "Laura", "Mendoza",
                "Directora Administrativa", "laura.mendoza@laboratorioxzy.com.co",
                calidad, EmployeeStatus.ACTIVO, Role.ADMIN);

        log.info("Seeded 6 candidate employees for activation");
    }

    /**
     * Helper: crea o recupera un empleado por código. Es idempotente.
     */
    private Employee saveEmployee(String code, String doc, String firstName, String lastName,
                                  String position, String email, Department department,
                                  EmployeeStatus status, Role systemRole) {
        return employeeRepository.findByEmployeeCode(code).orElseGet(() -> {
            Employee employee = Employee.builder()
                    .employeeCode(code)
                    .documentType(DocumentType.CC)
                    .documentNumber(doc)
                    .firstName(firstName)
                    .lastName(lastName)
                    .position(position)
                    .email(email)
                    .department(department)
                    .status(status)
                    .systemRole(systemRole)
                    .build();
            return employeeRepository.save(employee);
        });
    }
}
