# Plan de Implementación — ZoneControl

Guía paso a paso para completar el sistema de control de acceso físico.

---

## 1. Infraestructura y Configuración Inicial

### 1.1 Base de datos — PostgreSQL

Crear base de datos `zonecontrol` y ejecutar el schema inicial:

```sql
CREATE DATABASE zonecontrol;
```

Actualizar `pom.xml`: reemplazar dependencia H2 por driver PostgreSQL y agregar dependencias faltantes:

```xml
<!-- PostgreSQL -->
<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
    <scope>runtime</scope>
</dependency>

<!-- JWT -->
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-api</artifactId>
    <version>0.13.0</version>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-impl</artifactId>
    <version>0.13.0</version>
    <scope>runtime</scope>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-jackson</artifactId>
    <version>0.13.0</version>
    <scope>runtime</scope>
</dependency>

<!-- Spring Security (para BCrypt + JWT filter) -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>

<!-- Validation -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-validation</artifactId>
</dependency>

<!-- Excel/CSV export -->
<dependency>
    <groupId>org.apache.poi</groupId>
    <artifactId>poi-ooxml</artifactId>
    <version>5.5.1</version>
</dependency>

<!-- PDF generation (HU-16/17: pendiente implementar) -->
<dependency>
    <groupId>com.itextpdf</groupId>
    <artifactId>itextpdf</artifactId>
    <version>5.5.13.5</version>
</dependency>
```

Configurar `application.properties`:

```properties
spring.application.name=ZoneControl
spring.datasource.url=jdbc:postgresql://localhost:5432/zonecontrol
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
spring.servlet.multipart.max-file-size=10MB
spring.servlet.multipart.max-request-size=10MB
server.error.include-stacktrace=never

# JWT
app.jwt.secret=ZONE_CONTROL_SECRET_KEY_MIN_256_BITS_LONG_FOR_HS256_ALGORITHM
app.jwt.expiration-ms=86400000

# Frontend (base URL para el magic link de configuración de contraseña)
app.app-url=http://localhost:5173

# Swagger / OpenAPI
springdoc.api-docs.path=/v3/api-docs
springdoc.swagger-ui.path=/swagger-ui.html
```

> Las credenciales se inyectan desde `.env` (spring-dotenv); no hay defaults en el repositorio.

### 1.2 Estructura de paquetes backend

```
laboratorioxyz.com.ZoneControl
├── config/           # SecurityConfig, DataInitializer, CacheConfig, OpenApiConfig
├── security/         # JwtTokenProvider, JwtAuthenticationFilter, CustomUserDetailsService
├── common/exception/ # GlobalExceptionHandler (@RestControllerAdvice)
├── model/            # Compartido: entity/ (Department, ProductionArea, Office), enums/, repository/
└── modulo_*/         # Un paquete por módulo:
    ├── controller/   # REST controllers (sin lógica de negocio)
    ├── service/      # Interface + *ServiceImpl (lógica de negocio)
    ├── dto/          # Request/Response DTOs
    ├── model/        # Entidades del módulo (si aplica)
    └── repository/   # JPA repositories del módulo (si aplica)
```

Módulos: `modulo_publico`, `modulo_autenticacion`, `modulo_administracion`, `modulo_gestion_personal`, `modulo_control_acceso`, `modulo_reportes`.

### 1.3 Proyecto Frontend — React

> **Nota de estado**: la instrucción original era crearlo como directorio hermano. Actualmente el frontend vive **dentro del repo** en `src/main/frontend/` (React 19 + TypeScript 6 + Vite 8 + Tailwind 4), con el build en `src/main/resources/static/`. Ver `AGENTS.md` para el estado detallado de rutas y vistas.

Original (para contexto) — con Vite + React + TypeScript:

```bash
npm create vite@latest ZoneControl-Frontend -- --template react-ts
cd ZoneControl-Frontend
npm install react-router-dom @mui/material @emotion/react @emotion/styled
```

Estructura sugerida:

```
zonecontrol-frontend/
├── public/
├── src/
│   ├── components/       # Componentes reutilizables
│   ├── pages/            # Páginas por módulo
│   ├── services/         # api.ts (cliente axios con interceptor JWT)
│   ├── contexts/         # AuthContext
│   ├── layouts/          # Layouts con/sin sidebar
│   └── types/            # Interfaces TypeScript
```

---

## 2. Modelo de Datos — Entidades JPA

Crear en orden de dependencia:

### 2.1 `Departamento`

| Campo | Tipo | Restricción |
|-------|------|-------------|
| id | UUID (PK) | Autogenerado |
| nombre | String | Único, not null |
| descripcion | String | Opcional |

### 2.2 `Usuario`

| Campo | Tipo | Restricción |
|-------|------|-------------|
| id | UUID (PK) | Autogenerado |
| firstName | String | Not null |
| lastName | String | Not null |
| email | String | Único, not null (se deriva del Employee asociado) |
| password | String | BCrypt, nullable hasta completar setup por magic link (HU-05) |
| setupToken | String | SHA-256 hex (64 chars), único, nullable |
| setupTokenExpiry | LocalDateTime | Nullable |
| rol | Enum(ADMIN, GESTOR_PERSONAL, SUPERVISOR_AUDITOR) | Not null |
| estado | Enum(ACTIVO, INACTIVO) | Default ACTIVO |
| requiereCambioPassword | boolean | Default false |
| employee | @OneToOne(Employee) | Not null, unique |

### 2.3 `Personal` (Empleado)

| Campo | Tipo | Restricción |
|-------|------|-------------|
| id | UUID (PK) | Autogenerado |
| employeeCode | String | Único, formato EMP-XXXXXX, generado automáticamente |
| tipoDocumento | Enum(CC, CE, TI, PA, RC) | Not null |
| numeroDocumento | String | Not null |
| nombres | String | Not null, min 2 chars |
| apellidos | String | Not null, min 2 chars |
| cargo | String | Not null |
| email | String | Opcional; obligatorio para generar magic link (HU-05/08) |
| estado | Enum(ACTIVO, INACTIVO, SUSPENDIDO) | Default ACTIVO |
| departamento | @ManyToOne(Departamento) | Not null |

Unique constraint: (tipoDocumento, numeroDocumento)

**Nota:** El estado del empleado usa `EmployeeStatus` (ACTIVO, INACTIVO, SUSPENDIDO), distinto de `UserStatus` que solo tiene ACTIVO/INACTIVO. El cascade es **bidireccional**: si un empleado pasa a INACTIVO o SUSPENDIDO, sus permisos se marcan SUSPENDIDO y su usuario de sistema se marca INACTIVO; si el empleado vuelve a ACTIVO (o el usuario es reactivado vía `PATCH /api/admin/users/{id}/status`), permisos y usuario se restauran automáticamente.

### 2.4 `AreaProduccion`

| Campo | Tipo |
|-------|------|
| id | UUID (PK) |
| nombre | String, único |
| descripcion | String |

### 2.5 `PermisoAcceso`

| Campo | Tipo |
|-------|------|
| id | UUID (PK) |
| personal | @ManyToOne(Personal) |
| areaProduccion | @ManyToOne(AreaProduccion) |
| estado | Enum(ACTIVO, SUSPENDIDO) |
| fechaInicio | LocalDate |
| fechaExpiracion | LocalDate |
| fechaReactivacion | LocalDate (nullable, solo si SUSPENDIDO) |
| horarioInicio | LocalTime |
| horarioFin | LocalTime |

### 2.6 `HistorialAcceso`

| Campo | Tipo |
|-------|------|
| id | UUID (PK) |
| personal | @ManyToOne(Personal) |
| areaProduccion | String (denormalizado para reportes) |
| timestamp | LocalDateTime |
| resultado | Enum(AUTORIZADO, DENEGADO, NO_REGISTRADO, SUSPENDIDO) |

### 2.7 `ContenidoPublico`

Tabla clave-valor para contenido del módulo público:

| Campo | Tipo |
|-------|------|
| id | UUID (PK) |
| seccion | Enum(INSTITUTIONAL, CONTACT, LOCATIONS) |
| clave | String |
| valor | TEXT |

### 2.8 `CatalogoProducto`

| Campo | Tipo |
|-------|------|
| id | UUID (PK) |
| nombre | String |
| descripcion | TEXT |
| principioActivo | String |
| presentacion | String |
| areaProduccion | String |

### 2.9 `Sede`

| Campo | Tipo |
|-------|------|
| id | UUID (PK) |
| nombre | String |
| direccion | String |
| horarioAtencion | String |
| latitud | Double |
| longitud | Double |

---

## 3. Fases de Implementación (por orden de ejecución)

### Fase 0 — Semilla de datos

Crear `import.sql` o `DataInitializer` que inserte:
- Departamentos: Control de Calidad, Producción Sólidos, Producción Líquidos, Esterilización, Empaque, Almacenamiento
- Áreas de producción: Sala Blanca A, Sala Blanca B, Laboratorio QC, Almacén Controlado, Zona de Empaque
- Usuario admin por defecto: admin@zonecontrol.com / Admin123!

### Fase 1 — Módulo Público (HU-01, HU-02, HU-19)

**HU-01: Consulta Información Pública**
- Endpoints GET públicos (sin auth):
  - `GET /api/public/institucional` — `{ info: { companyName, mission, vision, description, productionAreas } }`
  - `GET /api/public/contacto` — `{ contact: { phone, email, socialMedia } }`
  - `GET /api/public/sedes` — `[{ id, name, address, openingHours, latitude, longitude }]`
  - `GET /api/public/catalogo` — `[{ id, name, description, activeIngredient, presentation, productionArea }]`
  - Los `id` de sedes y productos se exponen en la respuesta pública para que el panel admin de HU-19 pueda referenciar cada elemento al editar/eliminar sin necesidad de un endpoint admin con id. El landing los ignora; el admin los usa para los `PUT/DELETE /{id}`.
- Cachear respuestas (Spring Cache con ConcurrentMapCacheManager)
- TDD: tests de integración verificando HTTP 200 y estructura JSON (incluyendo el `id`)
- **Pendiente**: el criterio HU-01 cond. 03 pide "un mapa de ubicación" en el landing; por ahora `LocationsSection` muestra lat/long como texto. Implementar mapa embebido (iframe/leaflet) queda como pendiente.

**HU-02: Descargar Folleto**
- `GET /api/public/folleto` — servir PDF estático
- Frontend: botón "Descargar Folleto" condicionado a que exista archivo (HEAD → 200/404)
- TDD: test de descarga exitosa y error 404 si no hay folleto

**HU-19: Gestionar Contenido Público (requiere Fase 2 — auth)**
- CRUD de contenido público (solo ADMIN)
- PUT/POST `/api/admin/contenido-publico/{seccion}` (`INSTITUTIONAL`|`CONTACT`; body `Record<string,string>`; PUT invalida la caché pública). Las sedes se gestionan solo vía el CRUD `sedes[/{id}]`.
- POST `/api/admin/contenido-publico/folleto` (multipart, validar .pdf, max 10MB)
- DELETE `/api/admin/contenido-publico/folleto`
- CRUD de sedes: POST/PUT/DELETE `/api/admin/contenido-publico/sedes[/{id}]` con `OfficeRequest`
- CRUD de productos: POST/PUT/DELETE `/api/admin/contenido-publico/productos[/{id}]` con `ProductRequest`
- Frontend: pestañas por sección, cargador de PDF, tablas editables de sedes y catálogo
- TDD: tests de creación, actualización, validación de formato y tamaño, e invalidación de caché

### Fase 2 — Autenticación (HU-03)

**HU-03: Iniciar Sesión**
- `POST /api/auth/login` — recibe email+password, retorna JWT + datos usuario
- SecurityConfig con Spring Security:
  - `/api/public/**` y `/api/auth/**` y `/api/access/**` → permitAll()
  - `/api/admin/**` → hasRole(ADMIN)
  - `/api/personal/**` → hasAnyRole(ADMIN, GESTOR_PERSONAL)
  - `/api/reportes/**` → hasAnyRole(ADMIN, SUPERVISOR_AUDITOR)
- JwtAuthenticationFilter que extrae token del header Authorization: Bearer
- BCryptPasswordEncoder para validar contraseñas
- Manejar: 200 (éxito con token), 401 (credenciales incorrectas), 403 (cuenta desactivada), 404 (usuario no encontrado)
- Frontend:
  - AuthContext con token almacenado en localStorage
  - axios interceptor que agrega Bearer token a cada request
  - Redirección a dashboard según rol
- TDD: 4 condiciones de aceptación → 4 tests

- **Adicional (dashboard admin)**: `GET /api/admin/stats` — contadores agregados para las tarjetas KPI del dashboard del administrador (usuarios por estado, pendientes de configuración de contraseña, empleados, permisos). Solo ADMIN. TDD: 1 test de conteos delta.
- **Adicional (ajustes)**: `POST /api/auth/change-password` — cambio de contraseña voluntario por el usuario autenticado. Requiere token JWT válido (SecurityConfig: regla auth/change-password → authenticated() antes del permitAll de /api/auth/**). TDD: 5 tests (éxito, actual incorrecta, misma contraseña, validación, sin token).

### Fase 3 — Administración (HU-05, HU-06, HU-07, HU-08)

**HU-05: Crear Usuario Interno**
- `POST /api/admin/users`
- **No recibe password ni email**: ambos se derivan del `Employee` asociado (`employeeCode` obligatorio en el request)
- Validar email único (HTTP 409 si duplicado) y empleado no vinculado a otro usuario (HTTP 409)
- Genera `setupToken` criptográfico (96 hex, hash SHA-256, expiración 24h) y envía magic link al correo del empleado vía `MagicLinkNotifier` (logueado en consola, sin SMTP aún)
- HTTP 400 si el empleado no existe o no tiene email
- Frontend: formulario con selector de empleado existente; sin campos de contraseña
- TDD: test creación exitosa (retorna id + token), email duplicado, empleado sin email, empleado ya vinculado

**HU-06: Editar Usuario**
- `PUT /api/admin/users/{id}` — body `{ email }`: **el admin solo puede editar el correo**
- Nombre/apellido/cargo reflejan al `Employee` vinculado (se gestionan en Gestión de Personal); el rol se asigna en la creación (HU-05) y el estado se cambia vía `PATCH /{id}/status` (HU-07)
- Validar nuevo email no duplicado (HTTP 409, excluyendo al mismo usuario) y HTTP 404 si el usuario no existe
- Frontend: modal de edición con los datos del usuario en solo lectura (nombre, rol, estado, código) + campo email editable
- TDD: test actualización exitosa, email duplicado, usuario inexistente

**HU-07: Activar/Desactivar Usuario**
- `PATCH /api/admin/users/{id}/status` — body: { "estado": "ACTIVO"|"INACTIVO" }
- Impedir auto-desactivación (comparar ID del token con ID del usuario)
- Diálogo de confirmación en frontend
- TDD: test activación, desactivación, auto-desactivación rechazada

**HU-08: Restablecer Contraseña**
- `POST /api/admin/users/{id}/reset-password`
- Alineada con HU-05: invalidar password actual (null), generar setupToken criptográfico de 96 hex, hash SHA-256 con expiración 24h, enviar magic link al correo personal del empleado (sin contraseña temporal visible al admin)
- HTTP 400 si el empleado no tiene correo registrado, 404 si usuario no existe
- Reutilizar pantalla /configurar-contrasena del flujo HU-05 para completar el restablecimiento
- TDD: test envío de magic link, test empleado sin correo, test usuario inexistente

**CU-03c: Gestionar Matriz de Roles y Permisos** (formalizado en diagramas v1.1)
- Vista `admin-roles.html`: matriz de permisos por módulo y rol según SecurityConfig
- Actores: solo ADMIN (ver y consultar la matriz)
- Frontend: tabla con checkmarks por (módulo, rol)

**Vista transversal: Ajustes y Perfil** (`settings.html`)
- Perfil del usuario autenticado + cambio de contraseña (`POST /api/auth/change-password`)
- Aplica a los tres roles (CU-02 transversal); no es exclusiva del admin
- Vista compartida en los 3 dashboards

### Fase 4 — Gestión de Personal (HU-09, HU-14, HU-10, HU-11, HU-12, HU-13)

**HU-09: Registrar Personal Individual**
- `POST /api/personal`
- Generar EMP-XXXXXX automáticamente (secuencia desde 000001)
- Validar tipoDocumento en {CC, CE, TI, PA, RC}
- Validar (tipoDocumento + numeroDocumento) único → HTTP 409
- Validar campos obligatorios y longitud nombres ≥ 2
- Frontend: formulario con select de tipo doc, departamento, etc.
- TDD: 5 condiciones de aceptación → 5 tests

**HU-14: Buscar Personal por Filtros**
- `GET /api/personal?tipoDocumento=&numeroDocumento=&nombres=&apellidos=&departmentName=&status=&page=&size=`
- Al menos 1 filtro obligatorio (HTTP 400 si ninguno)
- Lógica AND entre filtros, paginación, ordenamiento
- Frontend: tabla paginada con acciones (ver detalle, editar, gestionar permisos)
- TDD: test búsqueda con resultados, sin resultados, sin filtros

**HU-10: Carga Masiva de Personal**
- `GET /api/personal/bulk/plantilla` — descargar CSV plantilla con encabezados + fila ejemplo
- `POST /api/personal/bulk` (multipart) — recibir CSV/TXT
- Validar: extensión (.csv/.txt), encabezados exactos, cada fila individualmente
- Batch insert para registros válidos, reporte de errores para inválidos
- Límite: 10MB, 1000 registros
- Frontend: uploader con preview de resultado, descarga de errores
- TDD: test carga exitosa, errores de validación, límites excedidos, plantilla descargable

**HU-11: Otorgar Acceso a Áreas**
- `POST /api/permisos`
- Validar empleado activo, detectar conflictos de permisos (misma área, horario, empleado → HTTP 409)
- Frontend: selector múltiple de áreas, horarios, fechas
- TDD: test otorgar, conflicto, empleado inactivo

**HU-12: Revocar Acceso**
- `DELETE /api/permisos/{id}`
- Diálogo de confirmación "acción permanente"
- HTTP 404 si permiso no existe
- TDD: test revocación exitosa, permiso inexistente

**HU-13: Suspender Acceso**
- `PATCH /api/permisos/{id}/suspend` — body: { "fechaReactivacion": "2026-08-15" }
- La suspensión guarda `fechaReactivacion` para referencia, pero **NO hay job `@Scheduled` de auto-reactivación** (se eliminó en commit fc377cf). El permiso se reactiva manualmente vía `PATCH /api/permisos/{id}` o por el cascade al volver el empleado a ACTIVO.
- TDD: test suspender, test permiso inexistente

### Fase 5 — Control de Acceso Físico (HU-18)

**HU-18: Validar Acceso Físico**
- `POST /api/access/validate` — body: { "employeeCode": "EMP-000001" }
- Requiere rol: ADMIN, SUPERVISOR_AUDITOR
- Lógica:
  1. ¿Empleado existe? No → "NO REGISTRADO" (amarillo)
  2. ¿Empleado activo? No → "INGRESO DENEGADO" (rojo)
  3. ¿Permiso vigente? No → "ACCESO SUSPENDIDO" (rojo)
  4. Sí a todo → "INGRESO AUTORIZADO" (verde)
- Registrar cada intento en historial_accesos con timestamp
- Frontend: formulario simple con alerta de color según resultado
- TDD: 4 condiciones de aceptación → 4 tests

### Fase 6 — Reportes y Auditoría (HU-15, HU-16, HU-17)

**HU-15: Consultar Historial de Accesos**
- `GET /api/historial?fechaInicio=&fechaFin=&employeeCode=&resultado=&page=&size=`
- Fecha inicio y fin obligatorias. Validar fechaInicio ≤ fechaFin
- Filtros opcionales: `employeeCode`, `resultado` (AND). **No hay filtro por departamento.**
- Frontend: date pickers, tabla paginada con resultados
- TDD: test consulta con datos, sin datos, rango inválido

**HU-16: Generar Documento Descargable**
- `POST /api/historial/export` — body: { "formato": "CSV"|"EXCEL"|"PDF", "filtros": {...} }
- Generar archivo con encabezado, fecha, filtros, tabla de datos, resumen estadístico
- **Implementado CSV, EXCEL (Apache POI) y PDF** (itextpdf 5, `PdfExporter` — gap 1.1 §9)
- `ExportRequest` acepta `employeeCode`, `resultado`, `departamentoName`; **el filtro por departamento se aplica** (gap 1.3 §9)
- TDD: test CSV, test Excel, test PDF, test formato inválido, test sin datos (400)

**HU-17: Archivo Periódico para Socios**
- `POST /api/reportes/archivo-periodico` — body: { "mes": 7, "anio": 2026, "formato": "CSV"|"EXCEL"|"PDF", "departmentNames": [...] }
- **Implementada la agregación por departamento SIN datos personales** (columnas: Departamento, Período, Total, Autorizados, Denegados, No Registrados, Suspendidos) — gap 1.2 §9
- TDD: test CSV, test Excel, test PDF, test sin datos (400), test filtro por departamentos

**Dashboard del Supervisor** (adición de coherencia)
- `GET /api/historial/stats` — indicadores agregados: accesos del día por resultado, permisos activos/suspendidos, empleados con acceso vigente
- Tags: **Módulo Reportes**, solo ADMIN y SUPERVISOR_AUDITOR
- Vista `supervisor-dashboard.html`: tarjetas KPI + actividad reciente
- TDD: 1 test de conteos delta

---

## 4. Convenciones y Reglas Transversales

### TDD — Flujo obligatorio

Para cada HU, implementar en este orden:
1. Leer la HU completa (descripción + criterios de aceptación + tareas)
2. Escribir test(s) para cada condición de aceptación
3. Verificar que fallen (red)
4. Implementar la lógica mínima para que pasen (green)
5. Refactorizar si es necesario

### Commits — Conventional Commits

```
feat(public): add institutional info endpoint with caching
feat(auth): add JWT login with role-based redirection
feat(admin): add user creation with BCrypt and password validation
feat(personal): add bulk CSV upload with row-level validation
feat(access): add access validation with historial logging
feat(reports): add periodic partner file generation (CSV/Excel)
fix(auth): prevent self-deactivation for admin users
test(personal): add unit tests for employee search filters
docs: add PlantUML diagram for access control flow
```

### Documentación inline

Comentar exclusivamente decisiones no obvias:
- Por qué se eligió una secuencia numérica vs UUID para EMP-XXXXXX (trazabilidad humana)
- Por qué el acceso físico lo gestiona SUPERVISOR_AUDITOR (unifica responsabilidad de monitoreo y control de acceso en un solo rol)
- Por qué se usa AND entre filtros de búsqueda (precisión > recall para control de acceso)
- Por qué se excluyen datos personales del archivo periódico (protección datos sensibles socio internacional)

### Manejo global de errores

`@RestControllerAdvice` actual implementa:
- `MethodArgumentNotValidException` → HTTP 400 con lista de errores por campo
- `ResponseStatusException` → HTTP 400/404/409/401 según el status lanzado desde servicios/controllers
- `AccessDeniedException` → HTTP 403
- `DataIntegrityViolationException` → HTTP 409 (gap 1.7 §9, implementado)
- `MaxUploadSizeExceededException` → HTTP 400 "El archivo excede el tamaño máximo permitido de 10MB" (folleto HU-19, §9 1.7)
- `Exception` genérica → HTTP 500 (gap 1.7 §9, implementado)

### Logging

Usar SLF4J + Logback. Registrar en cada operación crítica:
- Creación/edición/desactivación de usuarios (admin actions)
- Otorgar/revocar/suspender permisos (gestor actions)
- Cada validación de acceso (independientemente del resultado)
- Generación y envío de archivos periódicos

---

## 5. Resumen de Endpoints

| Método | Endpoint | Auth | Módulo | HU |
|--------|----------|------|--------|-----|
| GET | /api/public/institucional | No | Público | 01 |
| GET | /api/public/contacto | No | Público | 01 |
| GET | /api/public/sedes (respuesta incluye `id` para el panel admin) | No | Público | 01 |
| GET | /api/public/catalogo (respuesta incluye `id` para el panel admin) | No | Público | 01 |
| GET | /api/public/folleto | No | Público | 02 |
| POST | /api/auth/login | No | Autenticación | 03 |
| POST | /api/auth/change-password | Autenticado | Autenticación | 03 |
| GET | /api/setup-password?token= | No | Autenticación | 05/08 |
| POST | /api/setup-password | No | Autenticación | 05/08 |
| GET | /api/admin/users | Admin | Administración | 05 |
| GET | /api/admin/users/candidatos | Admin | Administración | 05 |
| GET | /api/admin/users/{id} | Admin | Administración | 05 |
| POST | /api/admin/users | Admin | Administración | 05 |
| PUT | /api/admin/users/{id} | Admin | Administración | 06 |
| PATCH | /api/admin/users/{id}/status | Admin | Administración | 07 |
| POST | /api/admin/users/{id}/reset-password | Admin | Administración | 08 |
| GET | /api/admin/stats | Admin | Administración | — |
| PUT | /api/admin/contenido-publico/{seccion} | Admin | Administración | 19 |
| POST | /api/admin/contenido-publico/folleto | Admin | Administración | 19 |
| DELETE | /api/admin/contenido-publico/folleto | Admin | Administración | 19 |
| POST/PUT/DELETE | /api/admin/contenido-publico/productos[/{id}] | Admin | Administración | 19 |
| POST/PUT/DELETE | /api/admin/contenido-publico/sedes[/{id}] | Admin | Administración | 19 |
| POST | /api/personal | Gestor/Admin | Gestión Personal | 09 |
| GET | /api/personal | Gestor/Admin | Gestión Personal | 14 |
| GET | /api/personal/{id} | Gestor/Admin | Gestión Personal | 14 |
| PATCH | /api/personal/{id} | Gestor/Admin | Gestión Personal | 14 |
| GET | /api/personal/departamentos | Gestor/Admin | Gestión Personal | 09/14 |
| GET | /api/personal/sedes | Gestor/Admin | Gestión Personal | 09/14 |
| GET | /api/personal/{id}/permisos | Gestor/Admin | Gestión Personal | 11 |
| GET | /api/personal/{id}/accesos | Gestor/Admin | Gestión Personal | 15 |
| POST | /api/personal/{id}/photo | Gestor/Admin | Gestión Personal | 25 (3.1) |
| GET | /api/personal/{id}/photo | Gestor/Admin | Gestión Personal | 25 (3.1) |
| DELETE | /api/personal/{id}/photo | Gestor/Admin | Gestión Personal | 25 (3.1) |
| GET | /api/personal/bulk/plantilla | Gestor/Admin | Gestión Personal | 10 |
| POST | /api/personal/bulk | Gestor/Admin | Gestión Personal | 10 |
| POST | /api/permisos | Gestor/Admin | Gestión Personal | 11 |
| GET | /api/permisos | Gestor/Admin | Gestión Personal | 11 |
| GET | /api/permisos/areas | Gestor/Admin | Gestión Personal | 11/20 |
| POST | /api/permisos/areas | Gestor/Admin | Gestión Personal | 20 (1.4) |
| PUT | /api/permisos/areas/{id} | Gestor/Admin | Gestión Personal | 20 (1.4) |
| DELETE | /api/permisos/areas/{id} | Gestor/Admin | Gestión Personal | 20 (1.4) |
| PATCH | /api/permisos/{id} | Gestor/Admin | Gestión Personal | 11 |
| DELETE | /api/permisos/{id} | Gestor/Admin | Gestión Personal | 12 |
| PATCH | /api/permisos/{id}/suspend | Gestor/Admin | Gestión Personal | 13 |
| PATCH | /api/permisos/{id}/reactivate | Gestor/Admin | Gestión Personal | 13 |
| POST | /api/access/validate | Admin, Supervisor | Control Acceso | 18 |
| GET | /api/historial | Supervisor/Admin | Reportes | 15 |
| GET | /api/historial/stats | Supervisor/Admin | Reportes | — |
| POST | /api/historial/export | Supervisor/Admin | Reportes | 16 |
| POST | /api/reportes/archivo-periodico | Supervisor/Admin | Reportes | 17 |

---

## 6. Orden de Implementación (estado actual)

Todas las fases backend están **implementadas y con tests verdes** (120). El orden histórico de construcción fue:

```
Fase 0: Schema BD + DataInitializer                          ✓
Fase 1: HU-01 → HU-02 → HU-19 (módulo público)               ✓
Fase 2: HU-03 (autenticación JWT + seguridad y roles)         ✓
Fase 3: HU-09 → HU-14 → HU-10 (gestión personal)            ✓
Fase 4: HU-11 → HU-12 → HU-13 (permisos de acceso)          ✓
Fase 5: HU-18 (control acceso físico)                        ✓
Fase 6: HU-05 → HU-06 → HU-07 → HU-08 (admin usuarios)      ✓
Fase 7: HU-15 → HU-16 → HU-17 (reportes y auditoría)        ✓
Fase 8: Frontend React (rama `feat/frontend-gestor`)          en progreso
  ✓ HU-03 login + HU-05/08 magic link
  ✓ Dashboard del Admin (mockup 28): KPIs, candidatos a usuario, usuarios sin configuración, actividad reciente
  ✓ Gestión de usuarios (mockup 31) + Crear usuario (21/20)
  ✓ Gestión de personal (mockup 32): lista + filtros + paginación
  ✓ Registro de personal (mockup 42) con foto opcional (HU-25)
  ✓ Carga masiva (mockup 10) con plantilla, upload y tabla de errores inline
  ✓ Detalle de empleado (mockup 41): edición, foto, asignar área, acciones de permiso e historial completo
  ✓ Gestión de permisos (mockup 45): otorgar/editar/suspender/reactivar/revocar + KPIs derivables
  ✓ Dashboard del supervisor (mockup 09)
  ✓ Validación de credencial (mockup 44) en `/supervisor/validar` — consume `POST /api/access/validate`
  ✓ Reportes / historial (mockup 37) en `/supervisor/reportes` — filtros, export CSV/Excel y archivo periódico
  ✓ Matriz de roles (mockup 16) en `/admin/matriz-roles` — solo lectura, reconstruida de `SecurityConfig`
  ✓ Ajustes/Perfil (mockup 00) con cambio de contraseña (HU-03 adicional)
  ✓ Sitio corporativo público (mockup 27) en `/`
  ✓ Gestión de contenido público (mockup 18) en `/admin/contenido-publico`
  ✓ Gestión de áreas de producción (mockup 22) en `/admin/areas` (backend CRUD 1.4 implementado)
```

Adiciones de coherencia posteriores también implementadas: `GET /api/admin/stats`, `GET /api/historial/stats`, `GET /api/permisos` + `GET /api/permisos/areas` + `PATCH /api/permisos/{id}` + `PATCH /api/permisos/{id}/reactivate` + CRUD `POST/PUT/DELETE /api/permisos/areas`, `POST /api/auth/change-password`, fotografía de empleado (`POST/GET/DELETE /api/personal/{id}/photo`), `GET /api/personal/departamentos`, `GET /api/personal/sedes`, `GET /api/personal/{id}/permisos`, `GET /api/personal/{id}/accesos`, `GET /api/admin/users/candidatos` y flujo de magic link para contraseñas (HU-05/08).

**Pendiente global (frontend):** no quedan vistas pendientes de los mockups finales; todas las rutas documentadas están construidas. Gaps restantes son de **backend** (PDF en export/archivo periódico, agregación por departamento, filtro por departamento, matriz de roles endpoint, invalidación JWT, handlers 409/500, tiempo real/rol SEGURIDAD, turnos) — documentados en §4/§7/§8/§9.12 y en AGENTS.md.

Cada HU = al menos 1 commit atómico. Cada HU completa sus tests TDD antes de pasar a la siguiente.

---

## 7. Resolución de identificadores por texto

Todos los endpoints que reciben UUIDs para referenciar entidades fueron migrados para aceptar identificadores legibles y que el backend resuelva la entidad internamente.

| DTO / Controller | Campo UUID original | Campo String actual | Resuelve con |
|-----------------|-------------------|-------------------|-------------|
| `RegisterEmployeeRequest` | `departmentId` | `departmentName` | `DepartmentRepository.findByName()` |
| `UpdateEmployeeRequest` | `departmentId` | `departmentName` | `DepartmentRepository.findByName()` |
| `CreateUserRequest` | `employeeId` | `employeeCode` | `EmployeeRepository.findByEmployeeCode()` |
| `CreatePermissionRequest` | `employeeId` + `productionAreaId` | `employeeCode` + `productionAreaName` | `findByEmployeeCode()` + `findByName()` |
| `ValidateAccessRequest` | `productionAreaId` | `productionAreaName` | `ProductionAreaRepository.findByName()` |
| `ExportRequest` | `personalId` + `departamentoId` | `employeeCode` + `departamentoName` | `findByEmployeeCode()` + `findByName()` |
| `GET /api/personal` | `departmentId` (filtro) | `departmentName` | JPA Criteria `root.get("department").get("name")` |
| `GET /api/historial` | `personalId` (filtro) | `employeeCode` | JPA Criteria `root.get("employee").get("employeeCode")` |

**Beneficio:** el frontend envía texto plano (código de empleado, nombre de departamento, nombre de área) y el backend traduce a la entidad correspondiente, eliminando el flujo de 2 pasos (buscar UUID → usar UUID).

---

## 8. Gaps pendientes derivados de los mockups finales

Los mockups finales en `.stitch/screens/` (marca Laboratorio XYZ) asumen funcionalidad que el backend aún no implementa. Registro para no re-descubrirlos:

| Mockup | Funcionalidad asumida | Estado en backend |
|--------|----------------------|-------------------|
| `22_...gesti-n-de-reas-de-producci-n` | CRUD de áreas de producción y terminales biométricos | CRUD de áreas **implementado** (`POST/PUT/DELETE /api/permisos/areas`, §9 item 1.4) y frontend en `/admin/areas`. Terminales biométricos: no existen → pendiente de decidir |
| `16_...matriz-de-roles-y-permisos` | Matriz de roles/api/permisos (consulta) | Roles fijos en `SecurityConfig` (ADMIN, GESTOR_PERSONAL, SUPERVISOR_AUDITOR). Endpoint `GET /api/admin/role-matrix` **implementado** (HU-27, §9 item 1.5) y vista frontend `/admin/matriz-roles` que lo consume con fallback estático |
| `28_...dashboard-de-administraci-n` | Mapa de accesos en tiempo real, "Súper Usuario" | Ocupación/zonas en vivo **implementadas** (§9.3 2.1–2.4) para ADMIN/SUPERVISOR (`/supervisor/zones`). El "Súper Usuario" y el mapa geográfico del mockup 28 siguen sin implementar |
| `09_...panel-de-supervisi-n-corporativo` | Estado de zonas (A-12, B-04) y alertas críticas en vivo | Zonas y alertas en vivo **implementadas** (`/supervisor/zones`); las zonas del seed son Sala Blanca A/B, Laboratorio QC, Almacén Controlado, Zona de Empaque (no A-12/B-04). Decorativo en los nombres |
| `42_...registro-de-personal` | Fotografía del empleado (opcional) | **Implementado** (HU-25, §9 item 3.1): `POST/GET/DELETE /api/personal/{id}/photo` + frontend en registro y detalle |
| `46_...inicio-de-sesi-n-interno` | "¿Olvidó su contraseña?" | No hay flujo público de recovery; solo reset vía `POST /api/admin/users/{id}/reset-password` (magic link) |
| `37_...reportes-de-auditor-a` | Exportar PDF | **Implementado** (gap 1.1 §9): PDF en `/api/historial/export` y archivo periódico (itextpdf 5, `PdfExporter`); frontend en `/supervisor/reportes` con botón PDF |
> **Cobertura:** la funcionalidad de los mockups 22 (CRUD áreas — backend ✓, frontend `/admin/areas`), 42 (foto — implementada), 44 (validación — frontend `/supervisor/validar`), 37 (reportes — CSV/Excel/**PDF** + agregación por departamento implementados) y 16 (matriz — endpoint `GET /api/admin/role-matrix` + frontend `/admin/matriz-roles`, §9 item 1.5) está incorporada a la hoja de ruta de la §9. Lo que queda en la §9: 28/09 (mapa y alertas en vivo, §9.3) y turnos (§9.4 3.2). El flujo público de "¿Olvidó su contraseña?" (mockup 46) y la edición real de la matriz de permisos quedan fuera de alcance de §9.

---

## 9. Funcionalidades Extra — Plan de Implementación

Plan complementario a este `PLAN_IMPLEMENTACION.md` y a `AGENTS.md`. Documenta las funcionalidades adicionales acordadas y cómo se integran con el avance del proyecto. Cada feature se implementa con TDD (test rojo → verde) y su documentación (HU, diagramas de casos de uso, diagramas de flujo) se entrega en el mismo commit, siguiendo las convenciones del proyecto.

### 9.1 Alcance y decisiones de diseño

Tres líneas de trabajo acordadas:

1. **Cerrar gaps existentes** (deuda técnica y mockups pendientes).
2. **Acceso en tiempo real** para ADMIN/SUPERVISOR (ocupación, emergencia de zona, SSE/mapa en vivo, alertas). **No se implementa el rol SEGURIDAD** (decisión de proyecto 2026-08-04).
3. **Gestión de personal mejorada** (foto, turnos).

Decisiones de diseño confirmadas:

- **Matriz de roles**: solo consulta/vista (sin edición ni enforcement en BD).
- **Tiempo real**: SSE (Server-Sent Events) con `SseEmitter` de Spring MVC y `EventSource` nativo en el frontend. Token JWT por query param en el endpoint de stream (EventSource no permite headers personalizados).
- **Sin rol SEGURIDAD ni consola de guardia**: los flujos de ocupación/emergencia/alertas quedan para ADMIN y SUPERVISOR_AUDITOR. Se eliminan también `SecurityActionLog` y la HU-24. No se implementa un workflow de aprobación de dos pasos.
- **Implementación por fases** (A → D) de manera que cada fase deja el proyecto compilando y con tests verdes.

### 9.2 Línea 1 — Cerrar gaps existentes

#### 1.1 Export PDF (HU-16/17)
- Aceptar formato `PDF` en `ExportRequest` y `PeriodicReportRequest` (hoy solo CSV/EXCEL).
- Nuevo `modulo_reportes/util/PdfExporter` (compartido por ambos servicios). itextpdf 5 ya está en `pom.xml`.
- PDF con encabezado (título, período/fechas), tabla con las columnas del export y resumen estadístico.
- **Tests**: export PDF con datos (bytes no vacíos, `Content-Type: application/pdf`), formato inválido 400, sin datos 400.
- **Esfuerzo**: bajo (~1 día).

> **Estado 1.1 — IMPLEMENTADO (backend + frontend)**: `PdfExporter` (itextpdf 5) compartido; formato PDF aceptado en `ExportRequest` y `PeriodicReportRequest`; botones CSV/Excel/PDF en `/supervisor/reportes`. Tests en `HistoryControllerTest`/`PeriodicReportControllerTest`.

#### 1.2 Archivo periódico agregado por departamento (HU-17 gap)
- Reemplazar la emisión de filas por empleado con una agregación `GROUP BY department` en `AccessHistoryRepository` (Total, Autorizados, Denegados, No Registrados, Suspendidos).
- **Sin datos personales** (cumple el plan y la normativa para el socio internacional).
- `PeriodicReportRequest`: añadir `List<String> departmentNames` (opcional) para filtrar.
- Columnas resultantes: `Departamento | Período | Total | Autorizados | Denegados | No Registrados | Suspendidos`.
- **Tests**: agregación correcta con varios empleados del mismo depto, filtro por deptos, sin datos 400.
- **Esfuerzo**: bajo (~1 día).

> **Estado 1.2 — IMPLEMENTADO**: `PeriodicReportServiceImpl` agrega `GROUP BY department` sin datos personales (Departamento, Período, Total, Autorizados, Denegados, No Registrados, Suspendidos) + fila TOTAL; `PeriodicReportRequest.departmentNames` opcional. Tests en `PeriodicReportControllerTest`.

#### 1.3 Filtro por departamento en historial y export (HU-15/16)
- `ExportRequest.departamentoName` ya se acepta pero no se aplica. Aplicar predicado `department` en `HistoryServiceImpl.search()` y `export()`.
- `GET /api/historial`: añadir query param `department` (el campo `department` ya está denormalizado en `AccessHistory`).
- **Tests**: historial filtrado por depto, export filtrado, sin resultados 400 en export.
- **Esfuerzo**: bajo (horas).

> **Estado 1.3 — IMPLEMENTADO**: `GET /api/historial?department=` y predicado de `ExportRequest.departamentoName` en `HistoryServiceImpl`. Tests en `HistoryControllerTest`.

#### 1.4 CRUD de áreas de producción (mockup 22)
- Pasar de catálogo de solo lectura (`GET /api/permisos/areas`) a CRUD completo en el mismo controller (mismas reglas de rol que `/api/permisos/**`).
- `POST /api/permisos/areas`: crear; nombre único → 409; longitud ≤30.
- `PUT /api/permisos/areas/{id}`: actualizar nombre/descripción.
- `DELETE /api/permisos/areas/{id}`: 409 si hay `AccessPermission` activos que la referencien.
- Coherencia con `AccessHistory.productionAreaName` (campo denormalizado): el histórico se conserva aunque se renombre el área.
- **Tests**: crear, duplicado 409, editar, borrar, borrar con permisos asociados 409.
- **Esfuerzo**: bajo-medio (1-2 días).

> **Estado 1.4 — IMPLEMENTADO (backend)**: `POST/PUT/DELETE /api/permisos/areas` operativos en `PermissionController`; frontend de CRUD en `/admin/areas` (`AdminAreasView`, solo ADMIN). El histórico denormalizado `AccessHistory.productionAreaName` se conserva. Pendiente: revisar coherencia con §9.7 (flujo 19) y HU-20.

#### 1.5 Matriz de roles — solo consulta (mockup 16)
- `GET /api/admin/role-matrix` (solo ADMIN): devuelve la matriz `módulo × rol → booleano` que ya está implícita en `SecurityConfig`, incluyendo el nuevo rol SEGURIDAD.
- **Sin edición ni enforcement en BD**: los roles siguen fijos en `SecurityConfig`. La matriz se reconstruye a partir de las reglas actuales para que la UI la muestre alineada con el backend.
- **Tests**: devuelve la matriz con los 4 roles; 403 si el rol no es ADMIN.
- **Esfuerzo**: bajo (~1 día).

> **Estado 1.5 — IMPLEMENTADO**: `GET /api/admin/role-matrix` en `RoleMatrixController` (servicio que refleja SecurityConfig, sin rol SEGURIDAD). Vista `/admin/matriz-roles` consume el endpoint. Tests en `RoleMatrixControllerTest`. HU-27 creada.

#### 1.6 Invalidar JWT al desactivar usuario (HU-07 gap)
- `JwtAuthenticationFilter`: tras validar firma/expiración, cargar el usuario por ID y comprobar `status == ACTIVO`. Si no, devolver 401.
- Cachear el estado brevemente (Caffeine 5-10 min) para evitar una consulta a BD por cada request.
- **Tests**: token de usuario recién desactivado → 401; usuario activo → 200.
- **Esfuerzo**: bajo (~1 día).

> **Estado 1.6 — IMPLEMENTADO**: `JwtAuthenticationFilter` consulta el `User` por ID y exige `status == ACTIVO`; si el usuario fue desactivado responde 401. Test: `JwtInvalidationTest`.

#### 1.7 Handlers 409 y 500 en GlobalExceptionHandler
- Añadir `DataIntegrityViolationException` → 409 (mensaje genérico de conflicto de unicidad).
- Añadir `Exception` → 500 (con `log.error` y mensaje genérico, sin filtrar stacktrace al cliente).
- **Tests**: violación de unicidad no mapeada 409; excepción no controlada 500.
- **Esfuerzo**: bajo (horas).

> **Estado 1.7 — IMPLEMENTADO**: `GlobalExceptionHandler` con `DataIntegrityViolationException` → 409 y `Exception` → 500 (log.error). Test: `GlobalExceptionHandlerTest`.

### 9.3 Línea 2 — Acceso en tiempo real (solo ADMIN/SUPERVISOR, sin rol SEGURIDAD)

#### 2.1 Sesiones de ocupación ("quién está dentro")
- Nueva entidad `AccessSession` (id, `employee`, `productionArea`, `entryTime`, `exitTime` nullable).
- En `AccessValidationServiceImpl.validate()`: si el resultado es `AUTHORIZED`, cerrar cualquier sesión abierta previa del mismo empleado+área (si la hay) y crear una nueva sesión. No se modifica `AccessHistory` aquí.
- `POST /api/access/exit` (employeeCode, productionAreaName) → cierra la sesión abierta; 400 si no hay sesión activa.
- `GET /api/access/occupancy` → por área: lista de `{employeeCode, nombre, entryTime, tiempoDentro}` y aforo total por área.
- **Tests**: entrada autorizada crea sesión, salida la cierra, doble entrada cierra la anterior, salida sin sesión 400, ocupación correcta.
- **Esfuerzo**: medio (~2 días).

#### 2.2 Cierre de emergencia de zona (kill switch)
- Campo `emergencyClosed` (boolean, default false) en `ProductionArea`.
- `POST /api/access/zones/{name}/emergency` (ADMIN/SUPERVISOR) con body `{cerrada: true|false}`.
- En `validate()`: si el área está cerrada → `DENIED` con mensaje "ZONA CERRADA POR EMERGENCIA", se registra en `AccessHistory` y se emite evento SSE.
- **Tests**: validar con zona en emergencia → denegado + historial, reabrir zona → flujo normal, sin rol → 403.
- **Esfuerzo**: bajo-medio (1-2 días).

#### 2.3 Mapa de zonas en vivo — SSE
- **`RealtimeEventPublisher`** (singleton `@Service`): mantiene `CopyOnWriteArrayList<SseEmitter>` con timeout (~5 min) y heartbeat cada 15s para evitar cierres por proxies. Método `publish(RealtimeEvent)`.
- Envelope JSON discriminado por `type`:
  - `access.validated` → `{employeeCode, area, result, timestamp}`
  - `occupancy.updated` → `{area, count, people[]}`
  - `zone.updated` → `{area, emergencyClosed}`
  - `alert.created` → `{alert}`
  - `snapshot` (inicial) → `{zones[], occupancy[]}`
- Endpoint `GET /api/access/stream` (ADMIN/SUPERVISOR). Al conectar, envía el snapshot. Al validar/registrar salida/emergencia/alerta, se publica el evento.
- **Autenticación SSE**: `EventSource` del navegador no permite header `Authorization`. Extender `JwtAuthenticationFilter.extractToken` para aceptar también token por query param **únicamente** en `/api/access/stream?token=...`. El resto del API sigue usando el header `Bearer`.
- **Frontend**: hook `useZoneStream` (suscripción con reconexión automática) consumido por el panel de zonas (mockups 09/28).
- **Tests**: unitario del publisher (suscribir → publicar → recibir); integración con MockMvc async (conectar al stream → ejecutar `validate()` → recibir `access.validated` con `getAsyncResult()`).
- **Esfuerzo**: medio (2-3 días).

#### 2.4 Alertas de anomalías
- Nueva entidad `AccessAlert` (id, `tipo` enum: `ACCESO_NOCTURNO`, `DENEGACIONES_REPETIDAS`, `ZONA_EMERGENCIA`, `ACCESO_FUERA_HORARIO`; `severidad`; `employeeCode`; `productionAreaName`; `message`; `timestamp`).
- Detección **on-write** dentro del servicio de validación:
  - ≥3 denegaciones del mismo empleado en 15 min → alerta.
  - Acceso autorizado entre 00:00-05:00 → alerta baja.
  - Cierre/reapertura de zona → alerta media.
- Persistencia + emisión SSE (`alert.created`).
- `GET /api/access/alerts?desde=&leido=` (ADMIN/SUPERVISOR) para el panel.
- **Tests**: disparo por 3 denegaciones, disparo nocturno, no-disparo en condiciones normales, listar alertas.
- **Esfuerzo**: medio (2-3 días).

> **Estado Línea 2 — ELIMINADO el rol SEGURIDAD (decisión 2026-08-04)**: no se implementan `SecurityActionLog`, consola `/guardia` ni la HU-24. Los items 2.1–2.4 quedan PENDIENTES para ADMIN/SUPERVISOR.

### 9.4 Línea 3 — Gestión de personal mejorada

#### 3.1 Fotografía del empleado (mockup 42)
- Campo `photoUrl` (nullable) en `Employee`.
- `POST /api/personal/{id}/photo` (multipart, jpg/png/webp, máx 2MB) → guarda en `uploads/photos/{employeeCode}.{ext}`, setea `photoUrl`.
- `GET /api/personal/{id}/photo` sirve el archivo estático. `DELETE /api/personal/{id}/photo` la elimina (opcional).
- Validación de extensión y tamaño en el servicio (mismo patrón que el folleto HU-19).
- **Tests**: subida válida, extensión inválida 400, tamaño excedido 400, empleado inexistente 404, GET devuelve el archivo.
- **Esfuerzo**: bajo-medio (1-2 días).

> **Estado 3.1 — IMPLEMENTADO (backend + frontend)**: `POST/GET/DELETE /api/personal/{id}/photo` en `EmployeeController`; frontend en `RegisterEmployeeView` (foto opcional) y `EmployeeDetailView` (ver/cambiar/eliminar). `Employee.photoUrl` sembrado en `EmployeeSearchResponse`.

#### 3.2 Turnos y horarios por día (mockup 45)
- Nueva entidad `PermissionSchedule` (id, `permission` FK, `dayOfWeek` enum LUN..DOM, `startTime`, `endTime`).
- Migración: los permisos existentes generan un schedule LUN-DOM con su `startTime`/`endTime` actual (script de datos idempotente en `DataInitializer` o al arranque).
- Modificar `AccessPermissionRepository.hasValidPermission` para exigir, además del rango de fechas vigente, un schedule cuyo `dayOfWeek` coincida con el día actual y cuya hora actual esté dentro de la ventana `startTime`/`endTime`.
- `CreatePermissionRequest` / `UpdatePermissionRequest`: añadir lista opcional de schedules; si no viene, se crea el schedule LUN-DOM con los horarios base.
- **Tests**: validación con turno del día correcto, día sin turno → SUSPENDIDO, hora fuera de ventana → SUSPENDIDO, migración correcta de permisos existentes, criterios ya existentes de HU-18/11 siguen verdes.
- **Esfuerzo**: medio-alto (2-3 días, toca la query de validación núcleo).

> **Estado 3.2 — IMPLEMENTADO**: `PermissionSchedule` (LUN..DOM) + migración idempotente en `DataInitializer`, query `hasValidPermission` exige schedule del día + ventana (con fallback para permisos sin schedules), `Create/UpdatePermissionRequest.schedules` opcionales y selector de días en `PermissionFormModal`. Tests en `PermissionScheduleControllerTest`. HU-26 creada.

### 9.5 Nuevas Historias de Usuario

Las HUs nuevas siguen la numeración de `docs/historias_usuario/HU-*.md` y se crean/actualizan **durante la implementación de cada fase**, no ahora.

| HU | Nombre | Actor principal |
|---|---|---|
| HU-20 | Gestionar Áreas de Producción | Gestor/Admin |
| HU-21 | Consultar Ocupación en Tiempo Real | Supervisor / Admin |
| HU-22 | Cerrar Zona por Emergencia | Supervisor / Admin |
| HU-23 | Alertas de Anomalías de Acceso | Sistema → Supervisor / Admin |
| HU-25 | Fotografía del Empleado | Gestor |
| HU-26 | Turnos y Horarios por Día | Gestor |
| HU-27 | Consultar Matriz de Roles y Permisos | Admin |

> HU-24 (Consola de Seguridad / rol SEGURIDAD) **ELIMINADA** por decisión de proyecto (2026-08-04).

Formato de cada HU (alineado con las existentes): tabla de metadatos, descripción, requerimiento, **criterios de aceptación (uno por test)**, tareas y control de versiones.

### 9.6 HUs a actualizar (no son nuevas)

- **HU-07**: nuevo criterio — al desactivar un usuario, sus tokens JWT dejan de ser válidos inmediatamente (401). ✅ Actualizada (gap 1.6 implementado).
- **HU-15**: nuevo criterio — filtro por `department` en `GET /api/historial`. ✅ Actualizada (gap 1.3 implementado).
- **HU-16**: nuevo criterio — formato `PDF` en el export (junto a CSV/EXCEL). ✅ Actualizada (gap 1.1 implementado).
- **HU-17**: reescritura — archivo periódico agregado por departamento sin datos personales, con `departmentNames` opcional. ✅ Actualizada (gap 1.2 implementado).
- **HU-18**: nuevo criterio — zona en emergencia → INGRESO DENEGADO (depende de §9.3 2.2, pendiente). Sin actor SEGURIDAD.
- **HU-00**: actualizar HU Relacionada (HU-20..27, sin HU-24), actualizar tareas. Sin actor Guardia.

### 9.7 Diagramas

#### Casos de uso (`docs/diagramas/casos_uso/`)
- `00_diagrama_general.puml`: CU nuevos por módulo (sin actor Guardia).
- `03_modulo_administracion.puml`: CU-03c → "Consultar matriz de roles y permisos" (solo lectura).
- `04_modulo_gestion_personal.puml`: CU-06a Gestionar áreas de producción · CU-06b Turnos y horarios · CU-07b Fotografía del empleado.
- `05_modulo_control_acceso_fisico.puml`: CU-11d Consultar ocupación · CU-11e Registrar salida · CU-11f Cerrar zona por emergencia · CU-11g Alertas de anomalías (actores Supervisor/Admin; sin Guardia).
- `06_modulo_reportes_auditoria.puml`: CU-08 + filtro depto · CU-09 + PDF · CU-10 agregación por departamento.

#### Flujos (`docs/diagramas/flujo/`)
- Nuevos: `19_flujo_gestion_areas_produccion.puml` (✓ creado), `20_flujo_consulta_ocupacion.puml`, `21_flujo_cierre_emergencia.puml`, `22_flujo_alertas_anomalias.puml`. **No** se crea el flujo de consola de guardia (rol eliminado).
- Actualizados: `13_flujo_historial.puml` (filtro depto ✓), `14_flujo_documento_descargable.puml` (opción PDF ✓), `15_flujo_archivo_periodico.puml` (agregación por departamento ✓), `16_flujo_control_acceso.puml` (emergencia + ocupación; sin Guardia).

#### Documentos maestros
- `docs/diagramas/README.md`: actualizar tablas de CU y flujos; asignar vistas de áreas/matriz/zonas a sus respectivos dashboards. Sin actor Guardia ni vista `/guardia`.
- `docs/PLAN_IMPLEMENTACION.md`: esta §9 + actualizar §5 (resumen de endpoints) cuando se ejecute cada fase.

### 9.8 Frontend (por fase, con el design system existente)

| Vista | Funcionalidad | Mockup de referencia |
|---|---|---|
| Reportes | Selector formato CSV/Excel/**PDF**; archivo periódico con agregación por departamento y filtro | 37 (✓ implementado) |
| Áreas de producción | CRUD de áreas (tabla + modal) | 22 (✓ implementado, admin) |
| Matriz de roles | Tabla de solo lectura módulo × rol | 16 (✓ implementado; endpoint pendiente) |
| Panel de zonas | Estado por área, aforo en vivo, toggle de emergencia, alertas en vivo vía SSE | 28, 09 |
| Gestión personal | Subida de foto (✓), schedules de turnos en el formulario de permiso | 42, 45 |

Rutas nuevas (protegidas por rol en `src/main/frontend/src/routes/index.tsx`): `/admin/areas` (✓), `/admin/matriz-roles` (✓), `/supervisor/zones`, `/supervisor/reportes` (✓), `/supervisor/validar` (✓). No se implementa `/guardia` (rol SEGURIDAD eliminado).

### 9.9 Fases de implementación

| Fase | Items | Salida esperada |
|---|---|---|
| **A — Gaps fáciles** | 1.7 → 1.3 → 1.6 → 1.1 → 1.2 | ✅ COMPLETADA: handlers 409/500, filtro por depto, invalidación JWT, export PDF y agregación por departamento. HUs 07/15/16/17 y flujos 13-15 actualizados. |
| **B — Áreas y matriz** | 1.4 (HU-20) → 1.5 (HU-27) | ✅ COMPLETADA: CRUD áreas (backend + frontend admin) y endpoint `GET /api/admin/role-matrix` + HU-20/HU-27. |
| **C — Tiempo real** | 2.1 (HU-21) → 2.2 (HU-22) → 2.3 (SSE) → 2.4 (HU-23) | ✅ COMPLETADA: sesiones, emergencia, SSE y alertas para ADMIN/SUPERVISOR; flujos 20-22 y HUs 21-23. Sin rol SEGURIDAD ni `SecurityActionLog`. |
| **D — Personal** | 3.1 (HU-25 ✓) → 3.2 (HU-26) | ✅ COMPLETADA: foto ✓ (CU-07b) y turnos/schedules ✓ (CU-06b, HU-26). |

Frontend integrado por fase, consumiendo los hooks y store ya existentes. Documentos maestros (`docs/diagramas/README.md`, HU-00, esta §9) actualizados al cierre de cada fase.

### 9.10 Convenciones

- **TDD obligatorio**: un test por criterio de aceptación de la HU (test rojo → verde) antes de implementar la lógica.
- **Conventional Commits**: `feat(scope):` y `docs(scope):` por cada feature; las HUs/diagramas se commitean junto al código en el mismo PR o en commits coordinados.
- **Errores vía `@RestControllerAdvice`**: 400 validación, 403 acceso, 404 inexistente, 409 conflicto (incluido `DataIntegrityViolationException` tras 1.7), 500 no controlado.
- **Controllers sin lógica de negocio** (delegan al servicio, validación con Jakarta Validation en DTOs).
- **DTOs con identificadores legibles** (employeeCode, departmentName, productionAreaName) — coherente con la §7.
- **Verificación**: `./mvnw test` debe quedar en verde al cerrar cada feature; el frontend se verifica con `npm run typecheck` y `npm run build` desde `src/main/frontend/`.

### 9.11 Integración con el proyecto

- Esta sección se referencia mutuamente con `AGENTS.md` y con las HUs que se creen durante la implementación.
- Las nuevas entidades (`AccessSession`, `AccessAlert`, `SecurityActionLog`, `PermissionSchedule`) se ubican en los módulos existentes (`modulo_control_acceso`, `modulo_gestion_personal`); no se crean paquetes nuevos salvo que la cohesión lo justifique.
- Cada fase deja el backend compilando, con todos los tests verdes y la documentación sincronizada.
- El plan se ejecuta **después** del cierre de la Fase 7 actual (reportes y auditoría) y se coordina con el avance del frontend React en `src/main/frontend/`.

### 9.12 Estado de implementación de la §9 (fecha: 2026-08-04)

| Item | Descripción | Estado |
|---|---|---|
| 1.1 | Export PDF (HU-16/17) | IMPLEMENTADO (backend + frontend) |
| 1.2 | Archivo periódico agregado por departamento | IMPLEMENTADO |
| 1.3 | Filtro por departamento en historial y export | IMPLEMENTADO |
| 1.4 | CRUD de áreas de producción (HU-20) | IMPLEMENTADO (backend) + frontend `/admin/areas` (admin) |
| 1.5 | Matriz de roles solo consulta (HU-27) | IMPLEMENTADO: `GET /api/admin/role-matrix` + vista `/admin/matriz-roles` consumiendo el endpoint |
| 1.6 | Invalidar JWT al desactivar usuario (HU-07 gap) | IMPLEMENTADO |
| 1.7 | Handlers 409 y 500 en GlobalExceptionHandler | IMPLEMENTADO |
| 2.1–2.4 | Tiempo real para ADMIN/SUPERVISOR (ocupación, emergencia, SSE, alertas) | IMPLEMENTADO (backend + frontend `/supervisor/zones`); sin rol SEGURIDAD |
| 2.5 | Rol SEGURIDAD / consola del guardia / SecurityActionLog | ELIMINADO (decisión de proyecto) |
| 3.1 | Fotografía del empleado (HU-25) | IMPLEMENTADO (backend + frontend) |
| 3.2 | Turnos y horarios por día (HU-26) | IMPLEMENTADO (entidad PermissionSchedule + validación por día + frontend) |
