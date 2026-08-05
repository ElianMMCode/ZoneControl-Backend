package laboratorioxyz.com.ZoneControl.config;

import laboratorioxyz.com.ZoneControl.model.entity.Department;
import laboratorioxyz.com.ZoneControl.model.entity.Office;
import laboratorioxyz.com.ZoneControl.model.entity.ProductionArea;
import laboratorioxyz.com.ZoneControl.model.enums.ContentSection;
import laboratorioxyz.com.ZoneControl.model.enums.ContractType;
import laboratorioxyz.com.ZoneControl.model.enums.DocumentType;
import laboratorioxyz.com.ZoneControl.model.enums.EmployeeStatus;
import laboratorioxyz.com.ZoneControl.model.enums.Role;
import laboratorioxyz.com.ZoneControl.model.enums.AccessResult;
import laboratorioxyz.com.ZoneControl.model.enums.PermissionStatus;
import laboratorioxyz.com.ZoneControl.model.enums.UserStatus;
import laboratorioxyz.com.ZoneControl.model.enums.WeekDay;
import laboratorioxyz.com.ZoneControl.model.enums.WorkShift;
import laboratorioxyz.com.ZoneControl.model.repository.DepartmentRepository;
import laboratorioxyz.com.ZoneControl.model.repository.ProductionAreaRepository;
import laboratorioxyz.com.ZoneControl.modulo_autenticacion.model.User;
import laboratorioxyz.com.ZoneControl.modulo_autenticacion.repository.UserRepository;
import laboratorioxyz.com.ZoneControl.modulo_control_acceso.model.AccessAlert;
import laboratorioxyz.com.ZoneControl.modulo_control_acceso.model.AccessHistory;
import laboratorioxyz.com.ZoneControl.modulo_control_acceso.model.AccessSession;
import laboratorioxyz.com.ZoneControl.modulo_control_acceso.repository.AccessAlertRepository;
import laboratorioxyz.com.ZoneControl.modulo_control_acceso.repository.AccessHistoryRepository;
import laboratorioxyz.com.ZoneControl.modulo_control_acceso.repository.AccessSessionRepository;
import laboratorioxyz.com.ZoneControl.modulo_gestion_personal.model.AccessPermission;
import laboratorioxyz.com.ZoneControl.modulo_gestion_personal.model.Employee;
import laboratorioxyz.com.ZoneControl.modulo_gestion_personal.model.PermissionSchedule;
import laboratorioxyz.com.ZoneControl.modulo_gestion_personal.repository.AccessPermissionRepository;
import laboratorioxyz.com.ZoneControl.modulo_gestion_personal.repository.EmployeeRepository;
import laboratorioxyz.com.ZoneControl.modulo_gestion_personal.repository.PermissionScheduleRepository;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/**
 * Inicializador de datos semilla para la base de datos.
 * Se ejecuta al arrancar la aplicación y siembra datos iniciales
 * solo si las tablas correspondientes están vacías.
 *
 * Incluye:
 * - 6 departamentos de producción
 * - 5 áreas restringidas de producción
 * - 1 usuario administrador por defecto
 * - 1 usuario gestor de personal por defecto (gestor@zonecontrol.com)
 * - Contenido público de ejemplo (misión, visión, contacto)
 * - 2 sedes físicas
 * - 2 productos del catálogo
 * - Empleados de prueba con distintos estados y perfiles reales
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
    private final AccessSessionRepository accessSessionRepository;
    private final AccessAlertRepository accessAlertRepository;
    private final PermissionScheduleRepository permissionScheduleRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    private Department adminDepartment;

    @Override
    public void run(String... args) {
        log.info("Seeding initial data...");
        seedDepartments();
        seedProductionAreas();
        seedAdminUser();
        seedPublicContent();
        seedOffices();
        seedProductCatalog();
        seedGestorUser();
        seedExtraEmployees();
        seedExtraUsers();
        seedAccessPermissions();
        seedCandidateEmployees();
        seedGestorSampleData();
        seedAreaAuthorizations();
        seedAccessSessions();
        seedAccessHistory();
        seedAccessAlerts();
        migratePermissionSchedules();
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
        seedArea("Sala Blanca A", "Zona aséptica para el llenado de productos estériles");
        seedArea("Sala Blanca B", "Área controlada para producción de líquidos y semisólidos");
        seedArea("Laboratorio QC", "Laboratorio de control de calidad y análisis microbiológico");
        seedArea("Almacén Controlado", "Bodega con clima controlado para materias primas y producto terminado");
        seedArea("Zona de Empaque", "Área de acondicionamiento y empaque final de medicamentos");
        log.info("Production areas seed finished");
    }

    private void seedArea(String name, String description) {
        ProductionArea existing = productionAreaRepository.findByName(name).orElse(null);
        if (existing == null) {
            productionAreaRepository.save(ProductionArea.builder()
                    .name(name)
                    .description(description)
                    .build());
        } else if (existing.getDescription() == null || existing.getDescription().isBlank()) {
            existing.setDescription(description);
            productionAreaRepository.save(existing);
        }
    }

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

    /**
     * Crea un empleado y usuario de tipo GESTOR_PERSONAL dedicado al
     * dashboard del gestor. Credenciales: gestor@zonecontrol.com /
     * Gestor123!. Es idempotente.
     */
    private void seedGestorUser() {
        // Auto-reparación: si el usuario demo existe pero quedó sin contraseña
        // (p. ej. un reset de prueba sin completar el magic link), se restaura
        // la credencial por defecto y se limpia el setupToken pendiente.
        var existing = userRepository.findByEmail("gestor@zonecontrol.com");
        if (existing.isPresent()) {
            User gestor = existing.get();
            if (gestor.getPassword() == null) {
                gestor.setPassword(passwordEncoder.encode("Gestor123!"));
                gestor.setSetupToken(null);
                gestor.setSetupTokenExpiry(null);
                gestor.setRequirePasswordChange(false);
                userRepository.save(gestor);
                log.info("Gestor user password restored to default (was null)");
            } else {
                log.info("Gestor user already exists — skipping");
            }
            return;
        }
        Department calidad = departmentRepository.findByName("Control de Calidad")
                .orElseGet(() -> departmentRepository.findByName("Control de Calidad").orElseThrow());

        Employee gestorEmployee = employeeRepository.findByEmployeeCode("EMP-000050")
                .orElseGet(() -> employeeRepository.save(Employee.builder()
                        .employeeCode("EMP-000050")
                        .documentType(DocumentType.CC)
                        .documentNumber("0000000050")
                        .firstName("María")
                        .lastName("Pérez")
                        .position("Gestora de Personal")
                        .email("gestor@zonecontrol.com")
                        .department(calidad)
                        .status(EmployeeStatus.ACTIVO)
                        .contractType(ContractType.TIEMPO_COMPLETO)
                        .workShift(WorkShift.DIURNO)
                        .hireDate(LocalDate.now().minusYears(2))
                        .build()));

        User gestor = User.builder()
                .firstName("María")
                .lastName("Pérez")
                .email("gestor@zonecontrol.com")
                .password(passwordEncoder.encode("Gestor123!"))
                .role(Role.GESTOR_PERSONAL)
                .status(UserStatus.ACTIVO)
                .requirePasswordChange(false)
                .employee(gestorEmployee)
                .build();
        userRepository.save(gestor);
        log.info("Seeded gestor user: gestor@zonecontrol.com");
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
        seedOffice(Office.builder()
                .name("Sede Principal Bogotá")
                .address("Cra 45 # 26-85, Bogotá D.C.")
                .openingHours("Lun-Vie 8:00-18:00")
                .latitude(4.7110)
                .longitude(-74.0721)
                .build());
        seedOffice(Office.builder()
                .name("Planta de Producción Medellín")
                .address("Cl 10 # 20-30, Medellín, Antioquia")
                .openingHours("Lun-Sáb 6:00-20:00")
                .latitude(6.2476)
                .longitude(-75.5658)
                .build());
        seedOffice(Office.builder()
                .name("Sede Regional Cali")
                .address("Cl 18 # 122-30, Cali, Valle del Cauca")
                .openingHours("Lun-Vie 8:00-17:00")
                .latitude(3.4516)
                .longitude(-76.5320)
                .build());
        log.info("Offices seed finished");
    }

    private void seedOffice(Office office) {
        officeRepository.findByName(office.getName()).orElseGet(() -> officeRepository.save(office));
    }

    private void seedProductCatalog() {
        seedProduct(ProductCatalog.builder()
                .name("Ácido Acetilsalicílico Genfar")
                .description("Analgésico y antiinflamatorio no esteroideo")
                .activeIngredient("Ácido Acetilsalicílico")
                .presentation("Tabletas 500mg x 30")
                .productionArea("Sala Blanca A")
                .build());
        seedProduct(ProductCatalog.builder()
                .name("Omeprazol MK")
                .description("Inhibidor de la bomba de protones para tratamiento de acidez gástrica")
                .activeIngredient("Omeprazol")
                .presentation("Cápsulas 20mg x 14")
                .productionArea("Sala Blanca B")
                .build());
        seedProduct(ProductCatalog.builder()
                .name("Losartán LabX 50mg")
                .description("Antihipertensivo para el control de la presión arterial")
                .activeIngredient("Losartán Potásico")
                .presentation("Tabletas 50mg x 30")
                .productionArea("Zona de Empaque")
                .build());
        log.info("Product catalog seed finished");
    }

    private void seedProduct(ProductCatalog product) {
        productCatalogRepository.findByName(product.getName())
                .orElseGet(() -> productCatalogRepository.save(product));
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

        saveEmployee("EMP-000030", "100000030", "Eduardo", "Vega",
                "Jefe de Sistemas", null, calidad, EmployeeStatus.ACTIVO, Role.ADMIN);
        saveEmployee("EMP-000031", "100000031", "Daniela", "Torres",
                "Analista de Producción", "daniela.torres@laboratorioxzy.com.co",
                produccion, EmployeeStatus.ACTIVO, null);
        saveEmployee("EMP-000032", "100000032", "Felipe", "Mora",
                "Auxiliar de Empaque", "felipe.mora@laboratorioxzy.com.co",
                empaque, EmployeeStatus.SUSPENDIDO, null);
    }

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
        createUser(ricardo, "Ricardo", "Díaz", ricardo.getEmail(), Role.GESTOR_PERSONAL, UserStatus.ACTIVO,
                "pending-ricardo-hash", LocalDateTime.now().plusHours(24));
        createUser(ana, "Ana", "Martínez", ana.getEmail(), Role.ADMIN, UserStatus.ACTIVO,
                "pending-ana-hash", LocalDateTime.now().plusHours(24));

        log.info("Seeded 5 extra users (Sandra, Javier, Miguel, Ricardo, Ana)");
    }

    private void createUser(Employee employee, String firstName, String lastName, String email,
                            Role role, UserStatus status, String setupToken,
                            LocalDateTime setupTokenExpiry) {
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
     * para que las KPI y vistas tengan contenido.
     */
    private void seedAccessPermissions() {
        ProductionArea salaBlancaA = productionAreaRepository.findByName("Sala Blanca A").orElseThrow();
        ProductionArea salaBlancaB = productionAreaRepository.findByName("Sala Blanca B").orElseThrow();

        Employee admin = employeeRepository.findByEmployeeCode("EMP-000001").orElse(null);
        Employee sandra = employeeRepository.findByEmployeeCode("EMP-000040").orElse(null);
        Employee javier = employeeRepository.findByEmployeeCode("EMP-000041").orElse(null);
        Employee miguel = employeeRepository.findByEmployeeCode("EMP-000042").orElse(null);

        LocalDate today = LocalDate.now();
        if (admin != null && accessPermissionRepository.findByEmployee_Id(admin.getId()).isEmpty()) {
            accessPermissionRepository.save(AccessPermission.builder()
                    .employee(admin).productionArea(salaBlancaA)
                    .status(PermissionStatus.ACTIVO)
                    .startDate(today).expirationDate(today.plusYears(1))
                    .startTime(LocalTime.of(0, 0))
                    .endTime(LocalTime.of(23, 59))
                    .build());
        }
        if (sandra != null && accessPermissionRepository.findByEmployee_Id(sandra.getId()).isEmpty()) {
            accessPermissionRepository.save(AccessPermission.builder()
                    .employee(sandra).productionArea(salaBlancaA)
                    .status(PermissionStatus.ACTIVO)
                    .startDate(today).expirationDate(today.plusMonths(6))
                    .startTime(LocalTime.of(7, 0))
                    .endTime(LocalTime.of(17, 0))
                    .build());
        }
        if (javier != null && accessPermissionRepository.findByEmployee_Id(javier.getId()).isEmpty()) {
            accessPermissionRepository.save(AccessPermission.builder()
                    .employee(javier).productionArea(salaBlancaB)
                    .status(PermissionStatus.ACTIVO)
                    .startDate(today).expirationDate(today.plusMonths(6))
                    .startTime(LocalTime.of(6, 0))
                    .endTime(LocalTime.of(22, 0))
                    .build());
        }
        if (miguel != null && accessPermissionRepository.findByEmployee_Id(miguel.getId()).isEmpty()) {
            accessPermissionRepository.save(AccessPermission.builder()
                    .employee(miguel).productionArea(salaBlancaA)
                    .status(PermissionStatus.SUSPENDIDO)
                    .startDate(today).expirationDate(today.plusMonths(3))
                    .startTime(LocalTime.of(8, 0))
                    .endTime(LocalTime.of(17, 0))
                    .build());
        }
        log.info("Seeded access permissions (3 activos, 1 suspendido)");
    }

    private void seedCandidateEmployees() {
        Department calidad = departmentRepository.findByName("Control de Calidad").orElseThrow();
        Department produccion = departmentRepository.findByName("Producción Sólidos").orElseThrow();
        Department empaque = departmentRepository.findByName("Empaque").orElseThrow();

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
     * Empleados de prueba para que el dashboard del gestor tenga
     * contenido realista con distintos estados y perfiles de
     * "empleado real" (tipo de contrato, sede, turno, fechas de
     * vigencia, foto). Es idempotente.
     */
    private void seedGestorSampleData() {
        // Empleado del departamento Esterilización: idempotente, garantiza que
        // el departamento tenga datos reales en los filtros y vistas.
        Department esterilizacion = departmentRepository.findByName("Esterilización").orElseThrow();
        saveEmployee("EMP-000106", "200000106", "Estefanía", "Londoño",
                "Operadora de Esterilización", "estefania.londono@laboratorioxzy.com.co",
                esterilizacion, EmployeeStatus.ACTIVO, null);

        if (employeeRepository.findByEmployeeCode("EMP-000100").isPresent()) {
            log.info("Gestor sample employees already exist — skipping");
            return;
        }
        Department calidad = departmentRepository.findByName("Control de Calidad").orElseThrow();
        Department produccion = departmentRepository.findByName("Producción Sólidos").orElseThrow();
        Department empaque = departmentRepository.findByName("Empaque").orElseThrow();
        Department almacenamiento = departmentRepository.findByName("Almacenamiento").orElseThrow();
        Office bogota = officeRepository.findByName("Sede Principal Bogotá").orElseThrow();
        Office medellin = officeRepository.findByName("Planta de Producción Medellín").orElseThrow();

        saveRichEmployee("EMP-000100", "200000100", "Camila", "Rojas",
                "Analista de Calidad Senior", "camila.rojas@laboratorioxzy.com.co",
                calidad, EmployeeStatus.ACTIVO, ContractType.TIEMPO_COMPLETO,
                bogota, WorkShift.DIURNO,
                LocalDate.now().minusYears(3), null);

        saveRichEmployee("EMP-000101", "200000101", "Andrés", "Salazar",
                "Operario de Producción", "andres.salazar@laboratorioxzy.com.co",
                produccion, EmployeeStatus.ACTIVO, ContractType.TIEMPO_COMPLETO,
                medellin, WorkShift.NOCTURNO,
                LocalDate.now().minusYears(1), null);

        saveRichEmployee("EMP-000102", "200000102", "Valentina", "Castro",
                "Auxiliar de Empaque", "valentina.castro@laboratorioxzy.com.co",
                empaque, EmployeeStatus.SUSPENDIDO, ContractType.TEMPORAL,
                bogota, WorkShift.DIURNO,
                LocalDate.now().minusMonths(8), LocalDate.now().plusMonths(4));

        saveRichEmployee("EMP-000103", "200000103", "Sergio", "Ortega",
                "Coordinador de Almacén", "sergio.ortega@laboratorioxzy.com.co",
                almacenamiento, EmployeeStatus.INACTIVO, ContractType.CONTRATISTA,
                medellin, WorkShift.MIXTO,
                LocalDate.now().minusYears(5), null);

        saveRichEmployee("EMP-000104", "200000104", "Juliana", "Paredes",
                "Practicante de Calidad", "juliana.paredes@laboratorioxzy.com.co",
                calidad, EmployeeStatus.ACTIVO, ContractType.PRACTICANTE,
                bogota, WorkShift.DIURNO,
                LocalDate.now().minusMonths(3), LocalDate.now().plusMonths(3));

        saveRichEmployee("EMP-000105", "200000105", "Tomás", "Vargas",
                "Inspector QC", "tomas.vargas@laboratorioxzy.com.co",
                calidad, EmployeeStatus.ACTIVO, ContractType.MEDIO_TIEMPO,
                bogota, WorkShift.DIURNO,
                LocalDate.now().minusYears(2), null);

        log.info("Seeded 6 rich sample employees for gestor dashboard (various statuses)");
    }

    /**
     * Autorizaciones por área (vista por sala del panel de zonas): garantiza
     * que las 5 áreas de producción tengan empleados asignados y permisos con
     * turnos por día para mostrar. Idempotente por empleado (un empleado que ya
     * tiene permiso se omite, incluidos los sembrados en seedAccessPermissions).
     */
    private void seedAreaAuthorizations() {
        LocalDate today = LocalDate.now();
        seedAreaPermission("EMP-000100", "Laboratorio QC", PermissionStatus.ACTIVO, today);
        seedAreaPermission("EMP-000104", "Laboratorio QC", PermissionStatus.ACTIVO, today);
        seedAreaPermission("EMP-000105", "Laboratorio QC", PermissionStatus.ACTIVO, today);
        seedAreaPermission("EMP-000101", "Sala Blanca B", PermissionStatus.ACTIVO, today);
        seedAreaPermission("EMP-000106", "Sala Blanca A", PermissionStatus.ACTIVO, today);
        seedAreaPermission("EMP-000102", "Zona de Empaque", PermissionStatus.ACTIVO, today);
        seedAreaPermission("EMP-000032", "Zona de Empaque", PermissionStatus.SUSPENDIDO, today);
        seedAreaPermission("EMP-000103", "Almacén Controlado", PermissionStatus.SUSPENDIDO, today);
        log.info("Seeded area authorizations for all production areas");
    }

    private void seedAreaPermission(String employeeCode, String areaName,
                                    PermissionStatus status, LocalDate today) {
        Employee employee = employeeRepository.findByEmployeeCode(employeeCode).orElse(null);
        if (employee == null || !accessPermissionRepository.findByEmployee_Id(employee.getId()).isEmpty()) {
            return;
        }
        ProductionArea area = productionAreaRepository.findByName(areaName).orElse(null);
        if (area == null) {
            return;
        }
        accessPermissionRepository.save(AccessPermission.builder()
                .employee(employee).productionArea(area)
                .status(status)
                .startDate(today).expirationDate(today.plusYears(1))
                .startTime(LocalTime.of(6, 0))
                .endTime(LocalTime.of(22, 0))
                .build());
    }

    /**
     * Sesiones activas (aforos) para el panel de zonas: crea empleados "dentro"
     * de algunas salas, todos con permiso ACTIVO vigente, para que la ocupación
     * muestre aforos cargados en varias áreas. Idempotente: solo si no existen
     * sesiones activas (no compite con el flujo entrada/salida real).
     */
    private void seedAccessSessions() {
        if (!accessSessionRepository.findByExitTimeIsNull().isEmpty()) {
            log.info("Access sessions already exist — skipping");
            return;
        }
        LocalDateTime now = LocalDateTime.now();
        seedSession("EMP-000001", "Sala Blanca A", now.minusHours(2).minusMinutes(10));
        seedSession("EMP-000106", "Sala Blanca A", now.minusHours(1).minusMinutes(35));
        seedSession("EMP-000041", "Sala Blanca B", now.minusHours(3));
        seedSession("EMP-000101", "Sala Blanca B", now.minusHours(1).minusMinutes(5));
        seedSession("EMP-000100", "Laboratorio QC", now.minusHours(2).minusMinutes(40));
        seedSession("EMP-000105", "Laboratorio QC", now.minusMinutes(30));
        seedSession("EMP-000102", "Zona de Empaque", now.minusHours(1).minusMinutes(50));
        log.info("Seeded 7 active access sessions across 4 areas");
    }

    private void seedSession(String employeeCode, String areaName, LocalDateTime entryTime) {
        Employee employee = employeeRepository.findByEmployeeCode(employeeCode).orElse(null);
        ProductionArea area = productionAreaRepository.findByName(areaName).orElse(null);
        if (employee == null || area == null) {
            return;
        }
        if (accessSessionRepository
                .findByEmployee_IdAndProductionArea_IdAndExitTimeIsNull(employee.getId(), area.getId())
                .isPresent()) {
            return;
        }
        accessSessionRepository.save(AccessSession.builder()
                .employee(employee)
                .productionArea(area)
                .entryTime(entryTime)
                .build());
    }

    private void saveRichEmployee(String code, String doc, String firstName, String lastName,
                                  String position, String email, Department department,
                                  EmployeeStatus status, ContractType contractType,
                                  Office baseOffice, WorkShift workShift,
                                  LocalDate hireDate, LocalDate contractEndDate) {
        if (employeeRepository.findByEmployeeCode(code).isPresent()) return;
        employeeRepository.save(Employee.builder()
                .employeeCode(code)
                .documentType(DocumentType.CC)
                .documentNumber(doc)
                .firstName(firstName)
                .lastName(lastName)
                .position(position)
                .email(email)
                .department(department)
                .status(status)
                .contractType(contractType)
                .baseOffice(baseOffice)
                .workShift(workShift)
                .hireDate(hireDate)
                .contractEndDate(contractEndDate)
                .build());
    }

    private void seedAccessHistory() {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        boolean todayHasHistory = accessHistoryRepository.findAll().stream()
                .anyMatch(h -> h.getTimestamp() != null
                        && h.getTimestamp().toLocalDate().isEqual(LocalDate.now()));
        if (todayHasHistory) {
            log.info("Access history for today already exists — skipping");
            return;
        }
        Employee admin = employeeRepository.findByEmployeeCode("EMP-000001").orElse(null);
        Employee sandra = employeeRepository.findByEmployeeCode("EMP-000040").orElse(null);
        Employee javier = employeeRepository.findByEmployeeCode("EMP-000041").orElse(null);
        Employee camila = employeeRepository.findByEmployeeCode("EMP-000100").orElse(null);

        saveHistory(admin, "Control de Calidad", "Sala Blanca A",
                startOfDay.plusHours(8).plusMinutes(12), AccessResult.AUTHORIZED);
        saveHistory(sandra, "Control de Calidad", "Sala Blanca A",
                startOfDay.plusHours(9).plusMinutes(3), AccessResult.AUTHORIZED);
        saveHistory(javier, "Producción Sólidos", "Sala Blanca B",
                startOfDay.plusHours(9).plusMinutes(47), AccessResult.AUTHORIZED);
        saveHistory(camila, "Control de Calidad", "Laboratorio QC",
                startOfDay.plusHours(10).plusMinutes(20), AccessResult.AUTHORIZED);
        saveHistory(null, "Producción Sólidos", "Sala Blanca B",
                startOfDay.plusHours(10).plusMinutes(15), AccessResult.UNREGISTERED);
        saveHistory(null, "Almacenamiento", "Almacén Controlado",
                startOfDay.plusHours(11).plusMinutes(22), AccessResult.DENIED);
        saveHistory(null, "Producción Sólidos", "Sala Blanca B",
                startOfDay.plusHours(12).plusMinutes(5), AccessResult.SUSPENDED);

        // Para el historial del empleado EMP-000100 (Camila) sembramos
        // algunos registros anteriores también, para que la vista de
        // detalle tenga una mini-timeline coherente.
        if (camila != null) {
            saveHistory(camila, "Control de Calidad", "Laboratorio QC",
                    startOfDay.minusDays(1).plusHours(8), AccessResult.AUTHORIZED);
            saveHistory(camila, "Control de Calidad", "Sala Blanca A",
                    startOfDay.minusDays(1).plusHours(14), AccessResult.AUTHORIZED);
            saveHistory(camila, "Control de Calidad", "Laboratorio QC",
                    startOfDay.minusDays(2).plusHours(9), AccessResult.AUTHORIZED);
        }

        log.info("Seeded access history records");
    }

    private void saveHistory(Employee employee, String department, String area,
                             LocalDateTime timestamp, AccessResult result) {
        accessHistoryRepository.save(AccessHistory.builder()
                .employee(employee)
                .department(department)
                .productionAreaName(area)
                .timestamp(timestamp)
                .result(result)
                .build());
    }

    /**
     * Siembra alertas de seguridad de ejemplo (sin leer) para que el panel
     * "Alertas de seguridad" del dashboard del admin tenga contenido. Son
     * datos transaccionales: solo se siembran si la tabla está vacía.
     * Previamente se limpian las alertas ACCESO_NOCTURNO obsoletas (tipo
     * eliminado de la lógica de negocio) para no romper la deserialización.
     */
    private void seedAccessAlerts() {
        accessAlertRepository.deleteNocturnalAlerts();
        if (accessAlertRepository.count() > 0) {
            log.info("Access alerts already exist — skipping");
            return;
        }
        LocalDateTime now = LocalDateTime.now();
        accessAlertRepository.save(AccessAlert.builder()
                .tipo(AccessAlert.AlertType.DENEGACIONES_REPETIDAS)
                .severidad(AccessAlert.AlertSeverity.MEDIUM)
                .employeeCode("EMP-000042")
                .productionAreaName("Sala Blanca A")
                .message("≥3 intentos denegados del empleado EMP-000042 en 15 min")
                .timestamp(now.minusHours(5))
                .build());
        accessAlertRepository.save(AccessAlert.builder()
                .tipo(AccessAlert.AlertType.ZONA_EMERGENCIA)
                .severidad(AccessAlert.AlertSeverity.MEDIUM)
                .productionAreaName("Sala Blanca B")
                .message("Zona Sala Blanca B CERRADA POR EMERGENCIA")
                .timestamp(now.minusHours(26))
                .build());
        log.info("Seeded 2 access alerts");
    }

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

    /**
     * Migración idempotente de turnos (3.2 §9): todo permiso existente sin
     * schedules recibe el schedule LUN-DOM con sus horarios base.
     */
    private void migratePermissionSchedules() {
        List<AccessPermission> permissions = accessPermissionRepository.findAll();
        int migrated = 0;
        for (AccessPermission p : permissions) {
            if (!permissionScheduleRepository.existsByPermission_Id(p.getId())) {
                for (WeekDay day : WeekDay.values()) {
                    permissionScheduleRepository.save(PermissionSchedule.builder()
                            .permission(p)
                            .dayOfWeek(day)
                            .startTime(p.getStartTime())
                            .endTime(p.getEndTime())
                            .build());
                }
                migrated++;
            }
        }
        if (migrated > 0) {
            log.info("Permission schedules migrated: {} permisos con schedule LUN-DOM", migrated);
        }
    }
}
