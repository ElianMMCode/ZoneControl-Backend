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
    <version>0.12.6</version>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-impl</artifactId>
    <version>0.12.6</version>
    <scope>runtime</scope>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-jackson</artifactId>
    <version>0.12.6</version>
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
    <version>5.3.0</version>
</dependency>

<!-- PDF generation -->
<dependency>
    <groupId>com.itextpdf</groupId>
    <artifactId>itext7-core</artifactId>
    <version>8.0.4</version>
    <type>pom</type>
</dependency>
```

Configurar `application.properties`:

```properties
spring.application.name=ZoneControl
spring.datasource.url=jdbc:postgresql://localhost:5432/zonecontrol
spring.datasource.username=postgres
spring.datasource.password=postgres
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
spring.servlet.multipart.max-file-size=10MB
spring.servlet.multipart.max-request-size=10MB

# JWT
app.jwt.secret=ZONE_CONTROL_SECRET_KEY_MIN_256_BITS_LONG_FOR_HS256_ALGORITHM
app.jwt.expiration-ms=86400000
```

### 1.2 Estructura de paquetes backend

```
laboratorioxyz.com.ZoneControl
├── config/           # SecurityConfig, JwtConfig, WebConfig
├── security/         # JwtTokenProvider, JwtAuthenticationFilter, CustomUserDetailsService
├── controller/       # REST controllers
├── service/          # Business logic
├── repository/       # JPA repositories
├── model/            # Entities
│   └ entity/
│   └ enums/
├── dto/              # Request/Response DTOs
├── exception/        # Custom exceptions + GlobalExceptionHandler
└── util/             # PasswordGenerator, FileValidator, etc.
```

### 1.3 Proyecto Frontend — React

Crear fuera del backend (directorio hermano):

```bash
npx create-react-app zonecontrol-frontend --template typescript
cd zonecontrol-frontend
npm install axios react-router-dom @mui/material @emotion/react @emotion/styled
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
| id | Long (PK) | Autogenerado |
| nombre | String | Único, not null |
| descripcion | String | Opcional |

### 2.2 `Usuario`

| Campo | Tipo | Restricción |
|-------|------|-------------|
| id | UUID (PK) | Autogenerado |
| firstName | String | Not null |
| lastName | String | Not null |
| email | String | Único, not null |
| password | String | BCrypt, nullable hasta completar el setup por magic link (HU-05) |
| rol | Enum(ADMIN, GESTOR_PERSONAL, SUPERVISOR_AUDITOR) | Not null |
| estado | Enum(ACTIVO, INACTIVO) | Default ACTIVO |
| requiereCambioPassword | boolean | Default false |
| employee | @OneToOne(Employee) | Not null, unique |

### 2.3 `Personal` (Empleado)

| Campo | Tipo | Restricción |
|-------|------|-------------|
| id | UUID (PK) | Autogenerado |
| identificacionInterna | String | Único, formato EMP-XXXXXX, generado automáticamente |
| tipoDocumento | Enum(CC, CE, TI, PA, RC) | Not null |
| numeroDocumento | String | Not null |
| nombres | String | Not null, min 2 chars |
| apellidos | String | Not null, min 2 chars |
| cargo | String | Not null |
| estado | Enum(ACTIVO, INACTIVO, SUSPENDIDO) | Default ACTIVO |
| departamento | @ManyToOne(Departamento) | Not null |

Unique constraint: (tipoDocumento, numeroDocumento)

**Nota:** El estado del empleado usa `EmployeeStatus` (ACTIVO, INACTIVO, SUSPENDIDO), distinto de `UserStatus` que solo tiene ACTIVO/INACTIVO. Si un empleado pasa a INACTIVO o SUSPENDIDO, sus permisos de acceso se marcan como SUSPENDIDO y su usuario de sistema (si existe) se marca como INACTIVO en cascada.

### 2.4 `AreaProduccion`

| Campo | Tipo |
|-------|------|
| id | Long (PK) |
| nombre | String, único |
| descripcion | String |

### 2.5 `PermisoAcceso`

| Campo | Tipo |
|-------|------|
| id | Long (PK) |
| personal | @ManyToOne(Personal) |
| areaProduccion | @ManyToOne(AreaProduccion) |
| estado | Enum(ACTIVO, SUSPENDIDO) |
| fechaInicio | LocalDate |
| fechaExpiracion | LocalDate |
| fechaReactivacion | LocalDate (nullable, solo si SUSPENDIDO) |
| horarioInicio | LocalTime |
| horarioFin | LocalTime |
| diaSemana | Integer (1=Lun…7=Dom) |

### 2.6 `HistorialAcceso`

| Campo | Tipo |
|-------|------|
| id | Long (PK) |
| personal | @ManyToOne(Personal) |
| departamento | String (denormalizado para reportes) |
| timestamp | LocalDateTime |
| resultado | Enum(AUTORIZADO, DENEGADO, NO_REGISTRADO, SUSPENDIDO) |

### 2.7 `ContenidoPublico`

Tabla clave-valor para contenido del módulo público:

| Campo | Tipo |
|-------|------|
| id | Long (PK) |
| seccion | String (INSTITUCIONAL, CONTACTO, SEDES) |
| clave | String |
| valor | TEXT |

### 2.8 `CatalogoProducto`

| Campo | Tipo |
|-------|------|
| id | Long (PK) |
| nombre | String |
| descripcion | TEXT |
| principioActivo | String |
| presentacion | String |
| areaProduccion | String |

### 2.9 `Sede`

| Campo | Tipo |
|-------|------|
| id | Long (PK) |
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
  - `GET /api/public/institucional` — misión, visión, descripción, áreas de producción
  - `GET /api/public/contacto` — teléfonos, email, redes sociales
  - `GET /api/public/sedes` — direcciones, mapa, horarios
  - `GET /api/public/catalogo` — lista de productos farmacéuticos
- Cachear respuestas (Spring Cache con ConcurrentMapCacheManager)
- TDD: tests de integración verificando HTTP 200 y estructura JSON

**HU-02: Descargar Folleto**
- `GET /api/public/folleto` — servir PDF estático
- Frontend: botón "Descargar Folleto" condicionado a que exista archivo
- TDD: test de descarga exitosa y error 404 si no hay folleto

**HU-19: Gestionar Contenido Público (requiere Fase 2 — auth)**
- CRUD de contenido público (solo ADMIN)
- PUT/POST `/api/admin/contenido-publico/{seccion}`
- POST `/api/admin/contenido-publico/folleto` (multipart, validar .pdf, max 10MB)
- DELETE `/api/admin/contenido-publico/folleto`
- Frontend: pestañas por sección, cargador de PDF
- TDD: tests de creación, actualización, validación de formato y tamaño

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

- **Adicional (dashboard admin)**: `GET /admin/stats` — contadores agregados para las tarjetas KPI del dashboard del administrador (usuarios por estado, pendientes de configuración de contraseña, empleados, permisos). Solo ADMIN. TDD: 1 test de conteos delta.
- **Adicional (ajustes)**: `POST /auth/change-password` — cambio de contraseña voluntario por el usuario autenticado. Requiere token JWT válido (SecurityConfig: regla auth/change-password → authenticated() antes del permitAll de /auth/**). TDD: 5 tests (éxito, actual incorrecta, misma contraseña, validación, sin token).

### Fase 3 — Administración (HU-05, HU-06, HU-07, HU-08)

**HU-05: Crear Usuario Interno**
- `POST /api/admin/users`
- Validar email único (HTTP 409 si duplicado)
- Validar password: min 8 chars, 1 mayúscula, 1 minúscula, 1 dígito, 1 especial (@$!%*?&)
- Encriptar con BCrypt antes de persistir
- Frontend: formulario con campos y validación en tiempo real
- TDD: test creación exitosa, email duplicado, password inválida

**HU-06: Editar Usuario**
- `PUT /api/admin/users/{id}`
- Validar nuevo email no duplicado (excluyendo al mismo usuario)
- HTTP 404 si usuario no existe
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
- Perfil del usuario autenticado + cambio de contraseña (`POST /auth/change-password`)
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
- `GET /api/personal?tipoDocumento=&numeroDocumento=&nombres=&apellidos=&departamentoId=&page=&size=`
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
- Job programado (@Scheduled) que verifica cada hora permisos SUSPENDIDOS cuya fechaReactivacion ya pasó y los reactiva
- TDD: test suspender, test reactivación automática (mock @Scheduled)

### Fase 5 — Control de Acceso Físico (HU-18)

**HU-18: Validar Acceso Físico**
- `POST /access/validate` — body: { "employeeCode": "EMP-000001" }
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
- `GET /api/historial?fechaInicio=&fechaFin=&personalId=&departamentoId=&resultado=&page=&size=`
- Fecha inicio y fin obligatorias. Validar fechaInicio ≤ fechaFin
- Filtros opcionales: personal, departamento, resultado (AND)
- Frontend: date pickers, tabla paginada con resultados
- TDD: test consulta con datos, sin datos, rango inválido

**HU-16: Generar Documento Descargable**
- `POST /api/historial/export` — body: { "formato": "PDF"|"CSV"|"EXCEL", "filtros": {...} }
- Generar archivo con encabezado, fecha, filtros, tabla de datos, resumen estadístico
- PDF: iText 7
- CSV: OpenCSV o manual
- Excel: Apache POI (.xlsx)
- Frontend: botón "Exportar" con selector de formato
- TDD: test cada formato, test sin datos (no permitir exportar)

**HU-17: Archivo Periódico para Socios**
- `POST /api/reportes/archivo-periodico` — body: { "mes": 7, "anio": 2026, "departamentosIds": [...], "formato": "CSV"|"EXCEL" }
- Consulta SQL agregada por departamento: total, autorizados, denegados, no registrados, suspendidos
- SIN datos personales (solo columnas: Departamento, Período, Total, Autorizados, Denegados, No Registrados, Suspendidos)
- Botón "Enviar a Socio Internacional" → log de auditoría
- Frontend: selector de mes/año, multiselect departamentos, formato
- TDD: test generación con datos, sin datos, sin datos personales

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

Crear `@RestControllerAdvice` con:
- `MethodArgumentNotValidException` → HTTP 400 con lista de errores por campo
- `DataIntegrityViolationException` → HTTP 409 (violación unique constraint)
- `EntityNotFoundException` → HTTP 404
- `AccessDeniedException` → HTTP 403
- `Exception` → HTTP 500 genérico (no exponer stack trace)

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
| GET | /api/public/sedes | No | Público | 01 |
| GET | /api/public/catalogo | No | Público | 01 |
| GET | /api/public/folleto | No | Público | 02 |
| POST | /api/auth/login | No | Autenticación | 03 |
| POST | /api/admin/users | Admin | Administración | 05 |
| PUT | /api/admin/users/{id} | Admin | Administración | 06 |
| PATCH | /api/admin/users/{id}/status | Admin | Administración | 07 |
| POST | /api/admin/users/{id}/reset-password | Admin | Administración | 08 |
| PUT/POST | /api/admin/contenido-publico/{seccion} | Admin | Administración | 19 |
| POST | /api/admin/contenido-publico/folleto | Admin | Administración | 19 |
| DELETE | /api/admin/contenido-publico/folleto | Admin | Administración | 19 |
| POST | /api/personal | Gestor/Admin | Gestión Personal | 09 |
| GET | /api/personal | Gestor/Admin | Gestión Personal | 14 |
| GET | /api/personal/{id} | Gestor/Admin | Gestión Personal | 14 |
| PATCH | /api/personal/{id} | Gestor/Admin | Gestión Personal | 14 |
| GET | /api/personal/bulk/plantilla | Gestor/Admin | Gestión Personal | 10 |
| POST | /api/personal/bulk | Gestor/Admin | Gestión Personal | 10 |
| POST | /api/permisos | Gestor/Admin | Gestión Personal | 11 |
| DELETE | /api/permisos/{id} | Gestor/Admin | Gestión Personal | 12 |
| PATCH | /api/permisos/{id}/suspend | Gestor/Admin | Gestión Personal | 13 |
| POST | /access/validate | Admin, Supervisor | Control Acceso | 18 |
| GET | /api/historial | Supervisor/Admin | Reportes | 15 |
| POST | /api/historial/export | Supervisor/Admin | Reportes | 16 |
| POST | /api/reportes/archivo-periodico | Supervisor/Admin | Reportes | 17 |

---

## 6. Orden de Implementación (actualizado)

Se construye toda la lógica de negocio primero con seguridad abierta (`permitAll`). La autenticación JWT se agrega al final como capa transversal.

```
Fase 0: Schema BD + DataInitializer                          ✓
Fase 1: HU-01 → HU-02 (módulo público, sin auth)            ✓
Fase 2: HU-09 → HU-14 → HU-10 (gestión personal)
Fase 3: HU-11 → HU-12 → HU-13 (permisos de acceso)
Fase 4: HU-18 (control acceso físico)
Fase 5: HU-05 → HU-06 → HU-07 → HU-08 → HU-19 (admin usuarios + contenido)
Fase 6: HU-15 → HU-16 → HU-17 (reportes y auditoría)
Fase 7: HU-03 (autenticación JWT + seguridad y roles)
```

Cada HU = al menos 1 commit atómico. Cada HU completa sus tests TDD antes de pasar a la siguiente.

---

## 7. Resolución de identificadores por texto

Todos los endpoints que reciben UUIDs para referenciar entidades serán migrados para aceptar identificadores legibles y que el backend resuelva la entidad internamente.

| DTO / Controller | Campo UUID actual | Nuevo campo String | Resuelve con |
|-----------------|-------------------|-------------------|-------------|
| `RegisterEmployeeRequest` | `departmentId` | `departmentName` | `DepartmentRepository.findByName()` |
| `UpdateEmployeeRequest` | `departmentId` | `departmentName` | `DepartmentRepository.findByName()` |
| `CreateUserRequest` | `employeeId` | `employeeCode` | `EmployeeRepository.findByEmployeeCode()` |
| `CreatePermissionRequest` | `employeeId` + `productionAreaId` | `employeeCode` + `productionAreaName` | `findByEmployeeCode()` + `findByName()` |
| `ValidateAccessRequest` | `productionAreaId` | `productionAreaName` | `ProductionAreaRepository.findByName()` |
| `ExportRequest` | `personalId` + `departamentoId` | `employeeCode` + `departamentoName` | `findByEmployeeCode()` + `findByName()` |
| `GET /personal` | `departmentId` (filtro) | `departmentName` | JPA Criteria `root.get("department").get("name")` |
| `GET /historial` | `personalId` (filtro) | `employeeCode` | JPA Criteria `root.get("employee").get("employeeCode")` |

**Beneficio:** el frontend envía texto plano (código de empleado, nombre de departamento, nombre de área) y el backend traduce a la entidad correspondiente, eliminando el flujo de 2 pasos (buscar UUID → usar UUID).