# Análisis Técnico Integral — ZoneControl

> **Proyecto:** Sistema de control de acceso físico para un laboratorio farmacéutico (caso de estudio en `docs/zonecontrol.pdf`).
> **Fecha:** 2026-08-06
> **Alcance:** Análisis completo de lógica de negocio, tecnologías, estructura y patrones de arquitectura del backend y el frontend.

---

## Índice

1. [Visión general](#1-visión-general)
2. [Stack tecnológico](#2-stack-tecnológico)
3. [Arquitectura del monorepo](#3-arquitectura-del-monorepo)
4. [Arquitectura backend](#4-arquitectura-backend)
5. [Seguridad y autenticación](#5-seguridad-y-autenticación)
6. [Modelo de datos y enums](#6-modelo-de-datos-y-enums)
7. [Módulos de negocio — lógica detallada](#7-módulos-de-negocio--lógica-detallada)
8. [Arquitectura frontend](#8-arquitectura-frontend)
9. [Testing](#9-testing)
10. [Infraestructura, configuración y operación](#10-infraestructura-configuración-y-operación)
11. [Decisiones de diseño y gaps conocidos](#11-decisiones-de-diseño-y-gaps-conocidos)
12. [Glosario de rutas de API](#12-glosario-de-rutas-de-api)

---

## 1. Visión general

ZoneControl es un sistema completo de **control de acceso físico** y **gestión administrativa** para un laboratorio farmacéutico. Cubre dos grandes frentes:

- **Sitio público corporativo** (landing): información institucional, contacto, sedes, catálogo de productos y folleto descargable, sin autenticación.
- **Plataforma interna** por roles: administración de usuarios y contenido, gestión de personal (empleados, cargos, permisos y turnos), control de acceso físico en tiempo real (validación de credenciales, ocupación por zona, emergencias, alertas, SSE) y reportes de auditoría (historial, exportación y archivo periódico).

### 1.1 Módulos funcionales

| Módulo | Paquete Java | Frontend | Rol |
|---|---|---|---|
| Módulo Público | `modulo_publico` | `/` (LandingView) | Público |
| Módulo Autenticación | `modulo_autenticacion` | `/login`, `/configurar-contrasena` | Público + autenticado |
| Módulo Administración | `modulo_administracion` | `/admin/**` | ADMIN |
| Módulo Gestión de Personal | `modulo_gestion_personal` | `/personal`, `/permisos` | ADMIN, GESTOR_PERSONAL |
| Módulo Control de Acceso | `modulo_control_acceso` | `/supervisor/validar`, `/supervisor/zones` | ADMIN, SUPERVISOR_AUDITOR |
| Módulo Reportes | `modulo_reportes` | `/supervisor/reportes` | ADMIN, SUPERVISOR_AUDITOR |

### 1.2 Roles del sistema

- **ADMIN**: administración completa (usuarios, contenido público, áreas, cargos, matriz de roles) + acceso a supervisión y reportes.
- **GESTOR_PERSONAL**: gestión de empleados, cargos y permisos de acceso.
- **SUPERVISOR_AUDITOR**: validación de credenciales, zonas en vivo y reportes (solo lectura sobre personal/áreas).

> **Decisión clave:** el rol de un usuario de sistema **no se elige manualmente**; se deriva del **cargo** (`Position.systemRole`) del empleado asociado. Quien tiene un cargo sin rol definido es solo personal de acceso físico y no es candidato a usuario del sistema.

---

## 2. Stack tecnológico

### 2.1 Backend

| Capa | Tecnología | Versión | Uso |
|---|---|---|---|
| Framework | Spring Boot (starter-web, data-jpa, security, validation) | 3.4.4 | Aplicación completa |
| Lenguaje | Java | 21 | — |
| Build | Maven (`mvnw`) | — | — |
| ORM | Spring Data JPA / Hibernate | — | Persistencia |
| BD | PostgreSQL | — | BD relacional (`zonecontrol`, `ddl-auto=update`) |
| Seguridad | Spring Security + jjwt (api/impl/jackson) | jjwt 0.13.0 | JWT HMAC-SHA, autorización por roles |
| Documentación API | springdoc-openapi (swagger-ui) | 2.7.0 | Swagger `/swagger-ui.html` |
| Excel | Apache POI (`poi-ooxml`) | 5.5.1 | Exportación `.xlsx` |
| PDF | iText (`itextpdf`, clásico 5) | 5.5.13.5 | Exportación `.pdf` |
| Config | spring-dotenv | 4.0.0 | Carga `.env` con `DB_USERNAME`/`DB_PASSWORD` |
| Código | Lombok | — | DTOs, builders, `@Slf4j` |
| Test | spring-boot-starter-test + spring-security-test | — | MockMvc, JUnit, `@WithMockUser` |

### 2.2 Frontend

| Capa | Tecnología | Versión | Uso |
|---|---|---|---|
| Framework | React | 19.2.0 | UI |
| Lenguaje | TypeScript | ~6.0.2 | Tipado estricto (`strict`, `verbatimModuleSyntax`) |
| Bundler | Vite | 8.2.0 | Dev server + build |
| Estilos | Tailwind CSS | 4.3.0 | CSS-first (`@tailwindcss/vite`, sin config JS) |
| Routing | react-router-dom | 7.18.0 | `createBrowserRouter` + `RouterProvider` |
| Estado global | Zustand (con `persist`) | 5.0.14 | Solo sesión de autenticación |
| Formularios | react-hook-form + zod | 7.84 / 4.4 | Formularios + esquemas de validación |
| Toasts | sonner | 2.0.7 | Feedback de éxito/error |
| Iconos | @fontsource/material-symbols-outlined | 5.2.5 | Material Symbols |
| Fuentes | @fontsource/inter, @fontsource/jetbrains-mono | 5.2.5 | Inter + JetBrains Mono locales |
| Lint | oxlint | 1.75.0 | Linter (Rust/oxc) |
| HTTP | **Cliente propio sobre `fetch`** | — | `lib/api.ts` (sin Axios) |
| UI kit | **Componentes propios** sobre Tailwind | — | Sin shadcn/chakra/antd |

> **Observaciones de diseño frontend:** no hay Redux, ni Axios, ni TanStack Query/React Query, ni librería de UI de terceros. El data-fetching usa un hook propio `useResource`; el estado global se limita a la sesión; los componentes de UI son un design system interno.

---

## 3. Arquitectura del monorepo

El proyecto es un **monorepo** con dos aplicaciones y un build compartido:

```
/home/elian/Documents/ZoneControl/
├── pom.xml                        # Backend Spring Boot (Maven)
├── mvnw                           # Wrapper Maven
├── .env                           # Credenciales BD (gitignored)
├── src/main/
│   ├── java/laboratorioxyz/com/ZoneControl/   # Backend (130 clases)
│   ├── frontend/                             # Frontend React+Vite (96 archivos)
│   └── resources/
│       ├── application.properties            # Config Spring
│       ├── employee-default.png              # Foto por defecto del empleado
│       └── static/                           # Build de Vite (outDir) servido por Spring
├── src/test/                       # Tests backend (23 clases, 215 @Test)
├── docs/                           # HUs, PLAN_IMPLEMENTACION, diagramas PlantUML
└── uploads/                        # Folleto PDF y fotos (gitignored)
```

**Flujo del build frontend:** `npm run build` (en `src/main/frontend/`) ejecuta `tsc -b && vite build` con `outDir: "../resources/static/"` y `emptyOutDir: true`. Spring Boot sirve ese estático same-origin; los controllers siguen montados en `/api/*`. En desarrollo, Vite (`:5173`) proxifica `/api` → `http://localhost:8080` sin reescribir.

**Nota operativa:** tras `npm run build`, el backend debe arrancarse siempre con `./mvnw clean spring-boot:run` (no solo `spring-boot:run`) porque Maven copia `src/main/resources/static` a `target/classes/static` **sin borrar** archivos obsoletos, y los assets viejos acumulados hacen que los navegadores (con `index.html` cacheado) sigan cargando la UI anterior.

---

## 4. Arquitectura backend

### 4.1 Patrón por capas por módulo

Cada módulo de negocio sigue una estructura uniforme:

```
modulo_X/
├── controller/      # @RestController: rutas HTTP, @Valid, delega al servicio
├── service/         # Interfaz + *ServiceImpl (@Service, @Transactional)
├── dto/             # Records de entrada/salida (inmutables)
├── model/           # Entidades JPA propias del módulo
└── repository/      # Spring Data JPA
```

**Regla arquitectónica del proyecto:** los controllers **no contienen lógica de negocio**; solo orquestan la capa HTTP y delegan al servicio. Las excepciones son puntuales y documentadas:

- `PermissionController.suspend` parsea `LocalDate`.
- `CargoController.roleOrNull` parsea `Role`.
- `AccessController.stream` arma el snapshot inicial del SSE.
- `modulo_autenticacion` tiene `SetupPasswordService` y `MagicLinkNotifier` como servicios **sin interfaz** (excepción documentada en AGENTS.md).

### 4.2 Patrones transversales

- **DTOs record**: todos los objetos de transferencia son `record` de Java (inmutables), con anidamiento para estructuras complejas (p. ej. `PeriodicReportPreviewResponse` con `AreaRow`/`DayRow`).
- **Errores**: `ResponseStatusException(HttpStatus.X, "mensaje")` lanzada desde los servicios; capturada por `GlobalExceptionHandler` (`@RestControllerAdvice`).
- **Búsquedas paginadas dinámicas**: repositorios que extienden `JpaSpecificationExecutor<T>` + `Specification<T>` construida con `cb.conjunction()` y predicados condicionales (empleados, permisos, historial, usuarios).
- **Transacciones**: `@Transactional` en métodos de escritura y `@Transactional(readOnly = true)` en consultas.
- **Cascadas transversales entre módulos**: p. ej. desactivar un usuario desactiva el empleado y suspende sus permisos; un empleado inactivo desactiva su usuario. Estos flujos cruzan `modulo_administracion`, `modulo_autenticacion` y `modulo_gestion_personal` a través de sus servicios.

### 4.3 Caché

`config/CacheConfig.java` define `@EnableCaching` con `ConcurrentMapCacheManager` (caché en memoria local) y cuatro caches: `institutional`, `contact`, `offices`, `catalog`. Los consumen los `@Cacheable` de `PublicServiceImpl`. `AdminContentServiceImpl` invalida el cache correspondiente tras cada modificación (`CacheManager.evictCache(...)`).

### 4.4 Documentación API

`config/OpenApiConfig.java` configura OpenAPI 3 (springdoc) con título "ZoneControl API", servidores local (8080) y producción (`https://api.laboratorioxzy.com.co`), y el security scheme `bearer-jwt` (HTTP Bearer JWT) aplicado globalmente. Swagger disponible en `/swagger-ui.html`.

### 4.5 SPA forward

`config/SpaForwardController.java` es un `@Controller` que hace `forward:/index.html` para las rutas del router (`/`, `/login`, `/personal/**`, `/supervisor/**`, `/admin/**`, etc.), de modo que el refresh directo en una ruta anidada sirva la SPA. Excluye `/api/**` y `/assets/**`.

---

## 5. Seguridad y autenticación

### 5.1 SecurityConfig

`config/SecurityConfig.java` (`@Configuration`, `@EnableWebSecurity`):

| Aspecto | Configuración |
|---|---|
| CSRF | Deshabilitado (API stateless JWT) |
| Sesiones | `SessionCreationPolicy.STATELESS` |
| PasswordEncoder | `BCryptPasswordEncoder` |
| Filtro | `jwtAuthenticationFilter` añadido **antes** de `UsernamePasswordAuthenticationFilter` |

**Matchers de autorización (en orden de precedencia):**

1. `/error` → `permitAll`.
2. `POST /api/auth/change-password`, `PUT /api/auth/profile` → `authenticated`.
3. `/api/public/**`, `/api/auth/**`, `/api/setup-password/**`, Swagger (`/v3/api-docs/**`, `/swagger-ui/**`, `/webjars/**`) → `permitAll`.
4. Rutas SPA (solo GET): `/`, `/index.html`, `/assets/**`, `/favicon.*`, `/vite.svg`, `/*.svg`, y las rutas del router (`/login`, `/configurar-contrasena`, `/ajustes`, `/personal/**`, `/permisos`, `/supervisor/**`, `/admin/**`) → `permitAll` (los **datos** siguen protegidos bajo `/api/**`).
5. `/api/admin/**` → `ADMIN`.
6. Excepciones de lectura ampliadas para `SUPERVISOR_AUDITOR`:
   - `GET /api/permisos/areas`, `GET /api/permisos/areas/*/empleados`, `GET /api/permisos/areas/*/autorizaciones`.
   - `GET /api/personal/departamentos`, `GET /api/personal/sedes`, `GET /api/personal/cargos`.
   - (Estas reglas van **antes** de la regla general de `/api/permisos/**` y `/api/personal/**`.)
7. `POST /api/personal/cargos` → `ADMIN` (matcher específico antes de `/api/personal/**`).
8. `/api/personal/**`, `/api/permisos/**` → `ADMIN, GESTOR_PERSONAL`.
9. `/api/access/**` → `ADMIN, SUPERVISOR_AUDITOR`.
10. `/api/historial/**`, `/api/reportes/**` → `ADMIN, SUPERVISOR_AUDITOR`.
11. `.anyRequest().authenticated()`.

**CORS:** no configurado en Spring. En desarrollo lo resuelve el proxy de Vite; en producción Spring sirve la SPA same-origin.

### 5.2 JWT — JwtTokenProvider

`security/JwtTokenProvider.java` (jjwt 0.13.0):

- **Clave**: `Keys.hmacShaKeyFor(secret.getBytes(UTF_8))` → HMAC simétrico (HS256/384/512 según longitud). Valor de `app.jwt.secret` hardcodeado en `application.properties` (61 chars, 488 bits).
- **Claims**: `subject` = `userId` (UUID string), `email`, `role` (nombre del enum `Role`), `issuedAt`, `expiration`.
- **Expiración**: `app.jwt.expiration-ms = 86400000` (24 h).
- **Validación**: `Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token)`; cualquier `JwtException`/`IllegalArgumentException` → `validateToken` = `false`.

### 5.3 Filtro JWT — JwtAuthenticationFilter

`security/JwtAuthenticationFilter.java` (`OncePerRequestFilter`):

1. Extrae el token del header `Authorization: Bearer <token>`.
2. **Excepción SSE**: si la URI es `/api/access/stream`, acepta `?token=...` (query param) porque `EventSource` no permite headers personalizados.
3. Si el token es válido, **consulta la BD por request** (`userRepository.findById(userId)`): si el usuario existe y `status == ACTIVO` → establece `UsernamePasswordAuthenticationToken(userId, null, [ROLE_<role>])` en el `SecurityContextHolder`. El **principal es el UUID** del usuario.
4. Si el usuario no existe o está `INACTIVO` → responde **401** directamente (sin continuar la cadena): `{"error":"Cuenta desactivada: inicie sesión de nuevo"}`. Esto implementa **HU-07**: al desactivar un usuario, sus JWT dejan de ser válidos de inmediato.

> El rol del token se confía (no se re-valida contra BD); solo se re-chequea el `status`. Un cambio de rol no se propaga hasta expirar el token.

### 5.4 CustomUserDetailsService

`security/CustomUserDetailsService.java` carga por email: username = `user.getId()`, password = hash BCrypt, `enabled = (status == ACTIVO)`, authority `ROLE_<Role>`. Es el mecanismo que usa el `AuthenticationManager` (DAO provider auto-configurado al haber un único `UserDetailsService` + `PasswordEncoder`).

### 5.5 Flujo de login

`POST /api/auth/login` (`AuthController`):

1. `findByEmail` → 404 si no existe.
2. `status != ACTIVO` → 403.
3. `authenticationManager.authenticate(...)` → `BadCredentialsException` → 401.
4. Genera token y responde `LoginResponse(token, Usuario{id, nombre, email, rol}, requirePasswordChange)`.

### 5.6 Magic link — creación de usuarios sin contraseña (HU-05/08)

Flujo central del onboarding:

1. El ADMIN crea el usuario vía `POST /api/admin/users` con solo `{ employeeCode, status }` (el email y el rol se derivan del empleado/cargo).
2. `SetupPasswordService.generateRawToken()` genera **48 bytes** con `SecureRandom` → hex de 96 chars.
3. **No se guarda el token crudo**: se almacena `hashToken(rawToken)` = **SHA-256 hex (64 chars)** en la columna `setup_token` (única), con `setup_token_expiry = now + 24h`.
4. `MagicLinkNotifier` construye `setupUrl = appUrl + "/configurar-contrasena?token=<raw>"` y lo **loguea en consola** (demo sin SMTP).
5. El frontend abre `setupUrl` en una ventana nueva; `GET /api/setup-password?token=` valida el token y `POST /api/setup-password` { token, newPassword } lo completa (setea `password`, limpia `setupToken`/`setupTokenExpiry` — **single-use**).
6. `resetPassword(id)` regenera el token, pone `password = null` (invalida la actual) y reenvía el link.

**Validación de token** (`findByTokenOrThrow`): hash → `findBySetupToken` → 404 si no existe → **410 GONE** si expiró (24 h).

**Política de contraseña**: 8–60 chars, al menos una mayúscula, una minúscula, un dígito y un símbolo `@$!%*?&` (`@Pattern` en `ChangePasswordRequest`/`SetupPasswordRequest`).

### 5.7 Cambio de contraseña y perfil

- `POST /api/auth/change-password` (autenticado): valida contraseña actual, rechaza nueva = actual, encripta con BCrypt y pone `requirePasswordChange = false`.
- `PUT /api/auth/profile` (autenticado): actualiza nombre/apellido/email (409 si el email ya pertenece a otro usuario).

### 5.8 Manejo global de errores

`common/exception/GlobalExceptionHandler.java` (`@RestControllerAdvice`):

| Excepción | HTTP | Body |
|---|---|---|
| `ResponseStatusException` | su status | `{"error": reason}` |
| `MethodArgumentNotValidException` | 400 | `{"errors": {campo: mensaje}}` |
| `AccessDeniedException` | 403 | `{"error":"Acceso denegado: no tiene permisos para este recurso"}` |
| `MaxUploadSizeExceededException` | 400 | límite de 10 MB |
| `DataIntegrityViolationException` | 409 | `{"error":"Conflicto de datos: ya existe un registro con esos datos"}` |
| `Exception` | 500 | `{"error":"Error interno del servidor"}` |

Convención: 400 = validación, 403 = acceso; 404/400/409/401 se lanzan con `ResponseStatusException` desde los servicios.

---

## 6. Modelo de datos y enums

### 6.1 Entidades base (`model/entity/`)

| Entidad | Tabla | Campos clave |
|---|---|---|
| `Department` | `departments` | id UUID, name (30, unique), description |
| `Office` | `offices` | id UUID, name (30), address (50), openingHours, latitude, longitude |
| `ProductionArea` | `production_areas` | id UUID, name (30, unique), description, `emergencyClosed` (boolean, default false) |

### 6.2 Entidades de dominio

**`User`** (`users`) — módulo autenticación:
- `firstName`, `lastName`, `email` (100, unique), `password` (60, **nullable** hasta completar magic link), `setupToken` (64, unique, nullable — hash SHA-256), `setupTokenExpiry`, `role` (enum STRING), `status` (enum STRING), `requirePasswordChange`, `employee` (`@OneToOne` unique → `Employee`).

**`Employee`** (`employees`) — gestión personal:
- Unicidad compuesta `(document_type, document_number)`. `employeeCode` (12, unique), `documentType`, `documentNumber`, `firstName`, `lastName`, `position` (30, **denormalizado**), `cargo` (`@ManyToOne` → `Position`, fuente de verdad), `email` (nullable — destino del magic link), `status` (default ACTIVO), `department`, `systemRole` (nullable — **rol derivado del cargo**), `contractType`, `baseOffice`, `workShift`, `hireDate`, `contractEndDate`, `photoUrl`.

**`Position`** (`positions`) — catálogo de cargos: `name` (40, unique), `systemRole` (nullable). **Fuente de verdad del rol del usuario.**

**`AccessPermission`** (`access_permissions`): `employee`, `productionArea`, `status` (default ACTIVO), `startDate`/`expirationDate` (not null), `reactivationDate` (nullable), `startTime`/`endTime` (not null). Un permiso por `(empleado, área)`.

**`PermissionSchedule`** (`permission_schedules`): `permission`, `dayOfWeek` (`WeekDay`, LUN..DOM), `startTime`, `endTime`. **Turnos por día.**

**`AccessSession`** (`access_sessions`): `employee`, `productionArea`, `entryTime` (not null), `exitTime` (nullable). Representa la **ocupación actual** de un área.

**`AccessHistory`** (`access_history`): `employee` (nullable FK), `department` (80, **denormalizado** en el momento del evento), `productionAreaName` (30, denormalizado), `timestamp`, `result` (`AccessResult`).

**`AccessAlert`** (`access_alerts`): `tipo` (`DENEGACIONES_REPETIDAS`, `ZONA_EMERGENCIA`), `severidad` (`LOW/MEDIUM/HIGH`), `employeeCode`, `productionAreaName`, `message` (300), `timestamp`, `leido` (default false).

**`ProductCatalog`** (`product_catalog`): `name` (80), `description` (TEXT), `activeIngredient`, `presentation`, `productionArea` (30, String desnormalizado).

**`PublicContent`** (`public_contents`): pares clave-valor `{section, key, value}` para `INSTITUTIONAL` y `CONTACT`.

### 6.3 Enums

| Enum | Valores | Uso |
|---|---|---|
| `Role` | `ADMIN`, `GESTOR_PERSONAL`, `SUPERVISOR_AUDITOR` | Rol de usuario |
| `UserStatus` | `ACTIVO`, `INACTIVO` | Cuenta |
| `EmployeeStatus` | `ACTIVO`, `INACTIVO`, `SUSPENDIDO` | Empleado |
| `PermissionStatus` | `ACTIVO`, `SUSPENDIDO` | Permiso |
| `AccessResult` | `AUTHORIZED`, `DENIED`, `UNREGISTERED`, `SUSPENDED`, `EXIT` | Resultado de validación |
| `ContentSection` | `INSTITUTIONAL`, `CONTACT` | Contenido público |
| `ContractType` | `TIEMPO_COMPLETO`, `MEDIO_TIEMPO`, `TEMPORAL`, `CONTRATISTA`, `PRACTICANTE` | Contrato |
| `DocumentType` | `CC`, `CE`, `TI`, `PA`, `RC` | Doc. identidad |
| `WorkShift` | `DIURNO`, `NOCTURNO`, `MIXTO` | Turno base |
| `WeekDay` | `LUN..DOM` (+ `from(DayOfWeek)`, `today()`) | Días de turnos |
| `AccessLevel` | `NINGUNO`, `LECTURA`, `ESCRITURA` | Matriz de roles |

---

## 7. Módulos de negocio — lógica detallada

### 7.1 Módulo público (`modulo_publico`)

**Controller**: `PublicController` (`/api/public`, todo `permitAll`).

| Endpoint | Lógica |
|---|---|
| `GET /api/public/institucional` | `@Cacheable("institutional")`. Lee pares clave-valor `INSTITUTIONAL` con **defaults**: `companyName` → "Laboratorio XYZ"; si no existe `productionAreas` lo reconstruye desde `ProductionAreaRepository.findAll()` uniendo los nombres. |
| `GET /api/public/contacto` | `@Cacheable("contact")`. Pares clave-valor `CONTACT` → `ContactResponse`. |
| `GET /api/public/sedes` | `@Cacheable("offices")`. `officeRepository.findAll()` → `OfficeResponse(id, name, address, openingHours, latitude, longitude)`. El `id` se expone **deliberadamente** para que el panel admin referencie cada sede (los GET públicos son la única fuente de lectura). |
| `GET /api/public/catalogo` | `@Cacheable("catalog")`. `productCatalogRepository.findAll()` → `CatalogResponse(...)`. El DTO lleva `productionArea` e `id`, pero el **landing no los muestra** (decisión de UX: el visitante no ve datos internos). |
| `GET /api/public/folleto` | **No cacheado**. Lee `File(brochurePath, "Folleto_Laboratorio_XYZ.pdf")` desde `app.brochure.path` (default `uploads/folleto`). Si no existe → 404; si existe → `application/pdf` con `Content-Disposition: attachment`. El PDF se genera **offline** con Chromium headless desde `docs/folleto/...html` (no en runtime). |

**Caché**: 4 caches en `ConcurrentMapCacheManager`, invalidados por `AdminContentServiceImpl`.

### 7.2 Módulo autenticación (`modulo_autenticacion`)

Detallado en la [sección 5](#5-seguridad-y-autenticación). Resumen de flujos:

- **Login** con verificación explícita de estado + `AuthenticationManager`.
- **Magic link** de alta entropía (48 B, SHA-256, 24 h, single-use).
- **Cascadas de estado**: `UserService.deactivateByEmployeeId/reactivateByEmployeeId` usados por la gestión de personal.

### 7.3 Módulo administración (`modulo_administracion`) — solo ADMIN

#### Usuarios — `AdminUserController` (`/api/admin/users`)

| Método | Ruta | Lógica |
|---|---|---|
| GET | `/users` | `Page<UserResponse>` con `Specification` (búsqueda LIKE sobre nombre/apellido/email/código + filtros rol/estado/pendientes de configuración) |
| GET | `/users/candidatos` | Empleados activables: `findActivationCandidates` (JPQL: `systemRole IS NOT NULL AND email IS NOT NULL AND NOT EXISTS usuario`) |
| POST | `/users` | Crea usuario sin contraseña (flujo magic link, §5.6). Valida: empleado existe → tiene `systemRole` → sin usuario previo → tiene email → email único. |
| PATCH | `/users/{id}/status` | `applyStatusChange` con **cascada**: rechaza desactivar la propia cuenta; `INACTIVO` → empleado `INACTIVO` + permisos `SUSPENDIDO`; `ACTIVO` → restaura empleado y permisos. |
| PUT | `/users/{id}` | Edita email + estado (rol/nombre/apellido reflejan al empleado, no editables). |
| POST | `/users/{id}/reset-password` | Regenera magic link (HU-08). |

> `findCurrentAdmin(String principal)` resuelve el admin actual: intenta `UUID.fromString(principal)` (flujo JWT) y cae a `findByEmail` (tests con `@WithMockUser`).

#### Contenido público — `AdminPublicContentController` (`/api/admin/contenido-publico`)

- `PUT /{section}`: reemplaza la sección `INSTITUTIONAL`/`CONTACT` (delete-all + save-all de `PublicContent`) e invalida cache.
- `POST /folleto`: upload `.pdf` ≤ 10 MB → `uploads/folleto/Folleto_Laboratorio_XYZ.pdf`. `DELETE /folleto`: borra el archivo.
- `POST/PUT/DELETE /productos[/{id}]` y `/sedes[/{id}]`: CRUD completo con `evictCache("catalog"/"offices")`.

#### Stats — `AdminStatsController` (`GET /api/admin/stats`)

9 contadores: usuarios totales/activos/inactivos/sin configuración, empleados totales/activos, permisos totales/activos/suspendidos.

#### Matriz de roles — `RoleMatrixController` (`GET /api/admin/role-matrix`)

`RoleMatrixServiceImpl.getMatrix()` **no consulta BD**: reconstruye la matriz "verdad por construcción" desde los matchers de `SecurityConfig` (snapshot manual que hay que mantener alineado). Devuelve 9 módulos × 3 roles con `AccessLevel {NINGUNO, LECTURA, ESCRITURA}`. **Es solo lectura**: los roles son fijos en `SecurityConfig`, sin edición ni enforcement en BD. Los niveles LECTURA del `SUPERVISOR_AUDITOR` sobre áreas/cargos/personal se corresponden con las excepciones reales de seguridad.

### 7.4 Módulo gestión personal (`modulo_gestion_personal`)

#### Empleados — `EmployeeController` (`/api/personal`)

| Método | Ruta | Lógica |
|---|---|---|
| POST | `/personal` | `register` (validaciones + generación de código) |
| GET | `/personal` | `search` paginado con `Specification` (documentType, documentNumber, firstName, lastName, departmentName, cargoName, status) |
| GET | `/personal/departamentos`, `/sedes`, `/cargos` | Catálogos (también para SUPERVISOR_AUDITOR) |
| GET | `/personal/bulk/plantilla` | CSV de plantilla de carga masiva |
| POST | `/personal/bulk` | Carga masiva CSV |
| GET/PATCH | `/personal/{id}` | Detalle / actualización parcial |
| GET | `/personal/{id}/permisos` | Permisos del empleado |
| GET | `/personal/{id}/accesos` | Historial de accesos (limit 1..200) |
| POST/GET/DELETE | `/personal/{id}/photo` | Foto (2 MB, jpg/jpeg/png/webp) |

**`EmployeeServiceImpl` (el más grande, ~740 líneas):**

- **`register`**: valida tipo documento, unicidad `(documentType, documentNumber)` → 409, resuelve departamento (obligatorio), sede (opcional), cargo (`resolveCargo`), y genera código con `generateEmployeeCode()`: `findMaxEmployeeCode()` (JPQL `MAX`) → `EMP-%06d` secuencial. Copia `position = cargo.name` (denormalizado) y `systemRole = cargo.systemRole` (rol derivado).
- **`update`** (PATCH parcial, solo campos no-null): al cambiar `cargoId` actualiza los tres (`cargo`, `position`, `systemRole`); cambio de documento valida unicidad → 409; **estado con cascada**: `INACTIVO`/`SUSPENDIDO` → `cascadeDeactivate` (permisos → SUSPENDIDO + `userService.deactivateByEmployeeId`); `ACTIVO` → `cascadeReactivate`.
- **Bulk upload**: **NO usa Apache POI** — parsea CSV separado por `;` con `BufferedReader`. Valida extensión (`.csv`/`.txt`), tamaño ≤ 10 MB y ≤ 1001 filas, encabezados exactos (`tipo_documento;documento_identidad;nombres;apellidos;cargo;departamento;estado;fecha_ingreso`), y por fila valida tipo doc, número, nombres/apellidos (≥ 2), cargo (debe existir, con cache en `HashMap`), departamento, estado y fecha `YYYY-MM-DD`. Errores por fila → `BulkUploadError(row, field, reason)`; duplicados contra BD y contra el propio archivo. Devuelve `BulkUploadResult(total, successes, errors, errorReportUrl)` donde `errorReportUrl` es en realidad el **CSV de errores en crudo** (`fila;campo;motivo\n`).
- **Fotos**: `uploads/photos/<employeeCode>.<ext>`, fallback a `employee-default.png` del classpath; `@PostConstruct initPhotoDir()` crea el directorio.

#### Permisos — `PermissionController` (`/api/permisos`)

| Método | Ruta | Lógica |
|---|---|---|
| POST | `/permisos` | `grant` |
| DELETE | `/permisos/{id}` | `revoke` (borrado físico; schedules en cascada por FK) |
| PATCH | `/permisos/{id}/suspend` | body `{reactivationDate}` obligatorio → status SUSPENDIDO (sin auto-reactivación) |
| PATCH | `/permisos/{id}/reactivate` | status ACTIVO, limpia `reactivationDate` |
| GET | `/permisos` | búsqueda paginada (código, nombre, área) |
| GET | `/permisos/areas` | Lista de áreas (también SV) |
| GET | `/permisos/areas/{name}/empleados` | Solo lectura (vista por sala) |
| GET | `/permisos/areas/{name}/autorizaciones` | Solo lectura, con turnos por día |
| POST/PUT/DELETE | `/permisos/areas[/{id}]` | CRUD de áreas (`deleteArea` → 409 si hay permisos ACTIVO) |
| PATCH | `/permisos/{id}` | `update` (solo si ACTIVO) |

**`grant`**: empleado existe (404) → `status == ACTIVO` (400) → área existe (400) → **unicidad `(empleado, área)`** (409) → crea permiso ACTIVO → `applySchedules`.

**`applySchedules`** (turnos LUN-DOM, HU-26): si `schedules` es null/vacío genera 7 `PermissionSchedule` con los horarios base (migración implícita); si vienen, guarda cada día. **Turnos nocturnos que cruzan medianoche** se representan con `startTime > endTime`.

**`hasValidPermission`** — la consulta JPQL clave de la validación de acceso:

```sql
SELECT COUNT(ap) > 0 FROM AccessPermission ap
WHERE ap.employee.id = :employeeId AND ap.productionArea.id = :areaId
  AND ap.status = 'ACTIVO'
  AND ap.startDate <= :today AND ap.expirationDate >= :today
  AND (
    EXISTS (SELECT 1 FROM PermissionSchedule ps
        WHERE ps.permission.id = ap.id AND ps.dayOfWeek = :dayOfWeek
        AND ( (ps.startTime <= :now AND ps.endTime >= :now)
              OR (ps.startTime > ps.endTime AND (:now >= ps.startTime OR :now <= ps.endTime)) ) )
    OR ( NOT EXISTS (SELECT 1 FROM PermissionSchedule ps WHERE ps.permission.id = ap.id)
         AND ap.startTime <= :now AND ap.endTime >= :now )
  )
```

Evalúa: permiso ACTIVO + vigencia de fechas + **horario por día** (turno normal o nocturno que cruza medianoche) + fallback al horario base si no hay schedules.

#### Cargos — `CargoController` (`/api/personal/cargos`)

| Método | Ruta | Lógica |
|---|---|---|
| GET | `/cargos` | `List<PositionResponse>` (ADMIN/GP/SV) |
| POST | `/cargos` | `create(name, systemRole?)` (solo ADMIN) |
| PUT | `/cargos/{id}` | `update` → **sincroniza en cascada** `position` y `systemRole` en los empleados vinculados |
| DELETE | `/cargos/{id}` | 409 si hay empleados vinculados |

El rol del sistema nace aquí: `Position.systemRole` nullable = el empleado con ese cargo **no es candidato** a usuario (solo acceso físico).

### 7.5 Módulo control de acceso (`modulo_control_acceso`)

#### Validación de credencial — `AccessValidationServiceImpl`

`POST /api/access/validate` con `{ employeeCode, productionAreaName }` — flujo en cascada:

1. **Área** existe (400).
2. **Kill switch de emergencia**: si `area.isEmergencyClosed()` → `DENIED` con mensaje "ZONA CERRADA POR EMERGENCIA" (no consulta al empleado).
3. **Empleado** existe; si no → `UNREGISTERED` (registra historial con employee null).
4. **Estado**: `status != ACTIVO` → `DENIED`.
5. **Permiso vigente**: `hasValidPermission(...)` (status + fechas + día + horario). Si false → `SUSPENDED` ("ACCESO SUSPENDIDO" — cubre permiso suspendido, vencido o fuera de horario).
6. **Autorizado**: cierra sesión abierta previa del mismo `(empleado, área)` si existe (`closeOpenSession`), guarda nueva `AccessSession(entryTime=now)`, registra historial `AUTHORIZED`, publica eventos SSE, devuelve `AUTHORIZED`.

`AccessResult`: `AUTHORIZED`, `DENIED`, `UNREGISTERED`, `SUSPENDED`, `EXIT`.

**Denormalización**: `AccessHistory.department` se escribe en el momento del evento (si el empleado cambia de depto después, el histórico conserva el original).

**Alertas de anomalías** — `maybeAlertRepeatedDenials`: si `countByEmployeeAndResultSince(employeeId, DENIED, now-15min) >= 3` → `createAlert(DENEGACIONES_REPETIDAS, MEDIUM, ...)` + publica `alert.created` por SSE.

#### Salida — `AccessMonitoringServiceImpl`

`POST /api/access/exit` con `{ employeeCode, productionAreaName }`:
- 404 si empleado no existe; 400 si área no existe; **400 si no hay sesión activa** para `(empleado, área)`.
- Cierra la sesión (`exitTime = now`), registra `AccessHistory(result=EXIT)`, publica `access.validated` (con `EXIT`) y `occupancy.updated`. Devuelve `ExitResponse`.

#### Ocupación y zonas

- `GET /api/access/occupancy` → `OccupancyResponse(areas[])`: agrupa `findByExitTimeIsNull()` por área con `AreaOccupancy(area, aforo, people)`. **El aforo es un conteo de sesiones activas** — no hay límite máximo configurado.
- `POST /api/access/zones/{name}/emergency` body `{cerrada: bool}` → toggle `emergencyClosed` + `AccessAlert(ZONA_EMERGENCIA, MEDIUM, ...)` + publica `zone.updated` y `alert.created`.

#### Alertas

- `GET /api/access/alerts?desde=&leido=` (filtros en memoria, orden desc).
- `PATCH /api/access/alerts/{id}/leido` → marca leída (404 si no existe).

#### Tiempo real — `RealtimeEventPublisher` (SSE)

- `CopyOnWriteArrayList<SseEmitter>`; `subscribe()` crea `SseEmitter(300_000L)` (timeout 5 min) con auto-remoción en completion/timeout/error.
- **Heartbeat cada 15 s** (`ScheduledExecutorService` daemon) con evento `heartbeat` (keep-alive).
- `publish(type, data)` envía el evento a todos; fallos → remueve el emitter.
- `sendSnapshot(emitter, snapshot)` envía el estado inicial.
- Eventos: `snapshot`, `access.validated`, `occupancy.updated`, `zone.updated`, `alert.created`, `heartbeat`.
- Endpoint: `GET /api/access/stream` (autenticado por **token en query param**, excepción del filtro JWT).

### 7.6 Módulo reportes (`modulo_reportes`)

#### Historial — `HistoryController` (`/api/historial`)

| Método | Ruta | Lógica |
|---|---|---|
| GET | `/historial` | `fechaInicio`/`fechaFin` obligatorios (400 si inicio > fin); filtros `employeeCode`, `department`, `productionAreaName`, `resultado`; paginado (default size 20, timestamp desc). |
| POST | `/historial/export` | `ExportRequest{formato, fechaInicio, fechaFin, employeeCode, departamentoName, productionAreaName, resultado}` → **400 si no hay datos**; genera CSV/EXCEL/PDF. |
| GET | `/historial/stats` | `SupervisorStatsResponse` (KPIs). |

**Exportaciones:**
- **CSV**: separador `;`, 8 columnas (`Fecha;Hora;ID Empleado;Nombre;Cargo;Departamento;Área;Resultado`), resumen al final (Total/Autorizados/Otros). UTF-8.
- **EXCEL**: Apache POI `XSSFWorkbook`, hoja "Historial de Accesos".
- **PDF**: `PdfExporter.exportTable(...)` (iText 5).

**KPIs** (`getStats`):
- `totalAccesosHoy` = `countTodayByResultIsNot(EXIT)` — **las salidas no suman** (decisión 2026-08-05).
- `accesosAutorizadosHoy/Denegados/NoRegistrados/Suspendidos`, `totalPermisosActivos/Suspendidos`, `empleadosConAcceso` (distinct con permiso activo).

#### Archivo periódico — `PeriodicReportController` (`/api/reportes`)

| Método | Ruta | Lógica |
|---|---|---|
| POST | `/archivo-periodico/preview` | `PeriodicReportRequest{mes, anio, formato, departmentNames?}` → JSON con `areaRows`/`dayRows`. |
| POST | `/archivo-periodico` | Genera `archivo_periodico_{mes}_{anio}.{ext}`. |

**`PeriodicReportServiceImpl` (HU-17) — reglas de negocio:**
- `records`: `findByPeriod(mes, anio)` (JPQL `EXTRACT`), filtra por `departmentNames` si viene, **excluye `EXIT`** (solo ingresos/intentos), 400 si vacío.
- **Sección 1** — resumen por `departamento × área` (Total, Autorizados, Denegados, No Registrados, Suspendidos, % Autorizados) + fila TOTAL. Defaults "Sin departamento"/"Sin área".
- **Sección 2** — distribución por día (Día; Total; Autorizados; Denegados; No Registrados; Suspendidos) + TOTAL.
- **Sin datos personales** (solo agregados). `departmentNames` opcional.
- **`preview`**: misma agregación devuelta en JSON para el modal "Enviar a Socio Internacional" (el envío externo queda fuera del sistema).

**`PdfExporter`** — **iText 5 clásico** (`com.itextpdf.text`): `Document`, `PdfWriter`, `PdfPTable`, `PdfPCell`; fuentes Helvetica (WinAnsi soporta acentos). API: `exportTable(title, subtitle, headers, rows)` y `exportSections(title, subtitle, List<PdfTable>)`.

---

## 8. Arquitectura frontend

### 8.1 Estructura de `src/`

```
src/main/frontend/src/
├── main.tsx                # Entry (ReactDOM.createRoot + StrictMode + Toaster)
├── App.tsx                 # Componente raíz → <AppRoutes />
├── routes/
│   ├── index.tsx           # createBrowserRouter + RouterProvider (18 rutas)
│   └── guards.tsx          # RequireAuth + RequireRole
├── lib/
│   ├── api.ts              # Cliente HTTP propio sobre fetch (apiFetch, apiDownload, ApiError)
│   ├── cn.ts               # clsx + tailwind-merge
│   └── format.ts           # formatDate, formatDateTime, formatTime, timeAgo, fullName, ...
├── stores/
│   └── authStore.ts        # Zustand + persist (localStorage "zc.auth")
├── hooks/                  # useAuth, useResource, useUsers, useEmployees, useGestor,
│                           #   usePublicData, useContentMutations, useZoneStream
├── components/
│   ├── ui/                 # Alert, Badge, Button, EmptyState, Icon, Input, Modal,
│   │                       #   Select, Skeleton, Tabs, Tooltip
│   ├── common/             # ConfirmDialog, DataTable, PageHeader, Pagination,
│   │                       #   PasswordField, QuickActions, RolePill, SearchInput,
│   │                       #   StatCard, StatusPill
│   ├── domain/             # CandidateEmployeesPanel, PendingUsersPanel, PermissionFormModal,
│   │                       #   RecentActivityList, SecurityAlertsPanel, UserFormModal, UserTable
│   ├── layout/             # AppShell, AuthLayout, Sidebar, TopNavbar
│   ├── admin/content/      # BrochureManager, ContactForm, InstitutionalForm,
│   │                       #   OfficeFormModal, ProductFormModal
│   └── public/             # Secciones del landing (Hero, About, Catalog, Contact, ...)
├── views/
│   ├── admin/              # DashboardView, UsersView, CreateUserView, PublicContentView,
│   │                       #   AdminAreasView, AdminCargosView, RoleMatrixView
│   ├── auth/               # LoginView, SetupPasswordView
│   ├── personal/           # EmployeeListView, RegisterEmployeeView, BulkUploadView,
│   │                       #   EmployeeDetailView, PermissionsView
│   ├── supervisor/         # DashboardView, AccessValidationView, ReportsView, ZonesView
│   ├── settings/           # SettingsView
│   ├── public/             # LandingView
│   ├── ForbiddenView.tsx   # /403
│   └── NotFoundView.tsx    # 404
├── styles/tailwind.css     # @theme tokens + @layer components
└── types/                  # Tipos de dominio (index barrel, common, auth, user, employee,
                            #   permission, access, history, report, public, admin)
```

### 8.2 Router y guards

- `createBrowserRouter` con layouts anidados: raíz pública (`LandingView`), `AuthLayout` (login + magic link) y `ShellWithTitle` (envuelve `RequireAuth` + `AppShell`).
- **Títulos por `handle`**: cada ruta declara `handle: { title }`; `ShellWithTitle` deriva el título del match más profundo con `useMatches()` y se lo pasa a `TopNavbar`.
- **`RequireAuth`**: si `!hydrated` → spinner; si `!isAuthed` → redirige a `/login`.
- **`RequireRole roles`**: si el rol no está en la lista → `/403`.
- **Sin lazy loading**: todas las vistas se importan estáticamente; el bundle se carga completo en la primera visita.
- 18 rutas con roles por vista (tabla completa en la [sección 12](#12-glosario-de-rutas-de-api) del frontend / rutas del router).

**Layout**: `AppShell` = `Sidebar` colapsable (`w-16`/`w-70`) + `TopNavbar` + `<main>` con `Outlet`. El sidebar filtra los items por rol (`visible = items.filter(i => role && i.roles.includes(role))`). `TopNavbar` muestra título, usuario y logout.

### 8.3 Estado y data-fetching

**Estado global (Zustand)**: solo `authStore` con `persist` en `localStorage` bajo la clave **`zc.auth`**. Estado: `{ token, usuario, requirePasswordChange, isHydrated }`; `partialize` persiste solo token/usuario/flag; `onRehydrateStorage` marca `hydrated`. Selectores: `selectToken`, `selectUser`, `selectRole`, `selectIsAuthed`. Acciones: `login`, `logout`, `updateUsuario`.

**Data-fetching (patrón propio `useResource<T>`)**: ejecuta `apiFetch` en `useEffect` con `AbortController` (cancela en unmount), estados `{ data, loading, error, refresh }` con un `tick` interno para re-fetch. `path === null` → no ejecuta (recursos dependientes de selección). No hay caché de servidor ni librería de query.

**Hooks por módulo** (`hooks/`):
- `useUsers` → `useUserMutations` (create/update/updateStatus/resetPassword) + `userListQuery`.
- `useGestor` → `useDepartments`, `useCargos`, `useOffices`, `useEmployeePermissions`, `useEmployeeAccessHistory`, `useEmployeeMutations`, `usePermissionMutations`, `useAreas`, `useBulkUpload`.
- `usePublicData` → compone los 4 recursos públicos + `HEAD /api/public/folleto` para `BrochureStatus`.
- `useContentMutations` → CRUD de contenido público + folleto.
- `useZoneStream` → SSE (ver §8.5).

### 8.4 Cliente HTTP — `lib/api.ts`

- Wrapper propio sobre `fetch` (sin Axios).
- `apiFetch<T>(path, opts)`: `buildUrl` (omite params undefined/null/vacío), `buildHeaders` (`Accept`, `Content-Type` salvo `FormData`, `Authorization: Bearer` si hay token), maneja `204`, parsea JSON o texto según Content-Type.
- `apiDownload`: devuelve `{ blob, filename }` parseando `Content-Disposition`.
- `ApiErrorImpl` con `status` y `fieldErrors`; `parseError` extrae `body.error` (mensaje) y `body.errors` (errores de campo).
- **Sin interceptores** de red ni manejo global de 401: el token lo inyecta `useAuth` vía `setAuthTokenGetter(() => useAuthStore.getState().token)` (registrado una sola vez con flag módulo-level).

### 8.5 Consumo del SSE — `useZoneStream`

- Conecta `new EventSource('/api/access/stream?token=' + token)` (query param porque `EventSource` no permite headers).
- `es.onopen` → `connected=true`; `es.onerror` → `connected=false` + "Reconectando…" (reconexión automática nativa de `EventSource`).
- Eventos manejados:
  - `snapshot` → setea `zones` y `occupancy`.
  - `occupancy.updated` → re-fetch `GET /api/access/occupancy` con header Bearer.
  - `access.validated` → antepone a `validations` (máx. 30).
  - `zone.updated` → actualiza `emergencyClosed`.
  - `alert.created` → antepone a `alerts` (máx. 50).
- **Historial persistente**: al montar carga `GET /api/historial?fechaInicio=<hoy-30d>&...` para que "Validaciones recientes" arranque con datos aunque se recargue.
- Cleanup: `es.close()` en el return del `useEffect`.

### 8.6 Design system — `styles/tailwind.css`

Tailwind 4 **CSS-first** (`@import "tailwindcss"`, sin `tailwind.config.js`):

**Tokens `@theme`** (inspirados en el mockup `33_design-md.html`, Material 3):
- `--color-primary: #0052cc`, `--color-secondary: #006c4a`, `--color-error: #ba1a1a`, superficies M3 (`surface`, `surface-dim`, `surface-container-*`, `on-surface`, `surface-variant`, `outline`, ...), `--color-public-primary: #00346f` (paleta pública del landing).
- Tipografía: `--font-sans: Inter`, `--font-mono: JetBrains Mono`; text tokens `--text-label-caps`, `--text-body-sm/md`, `--text-heading-md/lg/xl`.
- `--radius-*` M3 (4/8/12/16/full).

**Clases utilitarias en `@layer components`** (definidas una vez para evitar bloat):
- Botones: `.btn-primary/secondary/danger/ghost` + `.btn-sm/md/lg`.
- Formularios: `.input`, `.input-error`, `.label-caps`, `.field-label/error/help`.
- Tarjetas: `.card`, `.card-header`; badges: `.badge` + `.badge-active/inactive/warning/error/info`.
- Tablas: `.data-table` (thead/tbody/hover); `.page-header*`; `.stat-card*`; `.sidebar-link`.

**Iconos**: clase global `.material-symbols-outlined` (variable `FILL`), componente `<Icon name size filled>`. Fuentes importadas en `main.tsx` con `@fontsource/...`.

### 8.7 Componentes compartidos clave

- **`DataTable<T>`**: tabla genérica tipada con `Column<T>[]` — estándar de todos los listados.
- **`Modal`**: elemento nativo `<dialog>` (`showModal()`/`close()`), tamaños sm/md/lg, cierra al clic en backdrop.
- **`PermissionFormModal`**: el más complejo — `react-hook-form` + `zod` con `refine` de fecha/hora, selector de días (chips LUN..DOM) y `EmployeeSelector` (autocomplete remoto contra `/api/personal` con debounce ≥ 2 chars y `AbortController`).
- **`StatCard`**: KPI con delta, icono, tone y barra de progreso accesible.
- **`SecurityAlertsPanel`**: consume `GET /api/access/alerts?leido=false` y `PATCH /leido`.
- **`QuickActions`**: enlaces-botón por dashboard.

### 8.8 Vistas más complejas

- **`ZonesView`** (`/supervisor/zones`): SSE + `useResource("/api/access/alerts")` + recursos dependientes de la zona seleccionada (`/api/permisos/areas/{name}/empleados` y `/autorizaciones`). Tarjetas por zona con badge de emergencia/aforo, modal de personal/autorizaciones con Tabs, filtros client-side (Área/Resultado) sobre el feed en vivo.
- **`ReportsView`** (`/supervisor/reportes`): 3 bloques — KPIs (`/api/historial/stats`), historial filtrable con exportación (`apiDownload` + `URL.createObjectURL`), y archivo periódico con modal de vista previa (JSON `areaRows`/`dayRows`) y botón descargar.
- **`PermissionsView`** (`/permisos`): lista paginada + KPIs ligeros (`size: 1` para `totalElements`) + acciones por estado + modales de suspensión/creación/edición + confirmación de revocación.
- **`PublicContentView`** (`/admin/contenido-publico`): 5 tabs (Institucional/Contacto/Sedes/Catálogo/Folleto), paneles con CRUD y refresh.
- **`EmployeeDetailView`** (`/personal/:id`): detalle con foto (query param `?t=` para romper caché), edición, permisos y modal de historial completo (`/api/personal/{id}/accesos?limit=200`).

### 8.9 Formularios

Dos estilos:
1. `react-hook-form` + `zod` (validación manual con `z.schema`, **sin** `zodResolver`): Login, SetupPassword, Settings, UserFormModal, PermissionFormModal, formularios de contenido, CreateUserView.
2. `useState` controlado con `fieldErrors` manual: RegisterEmployeeView, EditEmployeeForm, AdminAreasView, AdminCargosView.

Feedback: `sonner` toast para éxito/error; `isApiError(e)` para mensajes; `ErrorState` con reintento; `EmptyState` para vacíos.

---

## 9. Testing

- **23 clases** de test en `src/test/java/...` con **215 métodos `@Test`** (suite verde).
- **Estrategia**: pruebas de controllers con MockMvc + `@WebMvcTest`/`@SpringBootTest`, autorización con `@WithMockUser(roles=...)`, y `GlobalExceptionHandlerTest` para el contrato de errores.
- Cobertura por módulo:
  - `modulo_publico`: `PublicControllerTest`.
  - `modulo_autenticacion`: `AuthControllerTest`, `SetupPasswordControllerTest`.
  - `modulo_administracion`: `AdminUserControllerTest`, `AdminPublicContentControllerTest`, `AdminStatsControllerTest`, `AdminDashboardDataTest`, `RoleMatrixControllerTest`.
  - `modulo_gestion_personal`: `EmployeeControllerTest`, `EmployeeSearchControllerTest`, `GestorEmployeeControllerTest`, `EmployeeCascadeTest`, `PermissionControllerTest`, `PermissionScheduleControllerTest`, `BulkUploadControllerTest`, `CargoControllerTest`.
  - `modulo_control_acceso`: `AccessControllerTest`, `AccessMonitoringControllerTest`.
  - `modulo_reportes`: `HistoryControllerTest`, `PeriodicReportControllerTest`.
  - Seguridad: `JwtInvalidationTest` (HU-07), `ZoneControlApplicationTests`.
- **Metodología**: TDD por criterio de aceptación de cada HU (documentado en AGENTS.md como obligatorio).
- Los tests del folleto usan `app.brochure.path` apuntando a `target/test-uploads/folleto` para NO borrar el PDF real de `uploads/`.

---

## 10. Infraestructura, configuración y operación

### 10.1 Configuración

`src/main/resources/application.properties`:

```properties
spring.application.name=ZoneControl
spring.datasource.url=jdbc:postgresql://localhost:5432/zonecontrol
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.servlet.multipart.max-file-size=10MB
spring.servlet.multipart.max-request-size=10MB
server.error.include-stacktrace=never
app.jwt.secret=ZONE_CONTROL_SECRET_KEY_MIN_256_BITS_LONG_FOR_HS256_ALGORITHM
app.jwt.expiration-ms=86400000
app.app-url=http://localhost:5173
app.brochure.path=uploads/folleto
springdoc.api-docs.path=/v3/api-docs
springdoc.swagger-ui.path=/swagger-ui.html
```

- **spring-dotenv**: carga `.env` de la raíz (gitignored) con `DB_USERNAME`/`DB_PASSWORD`. Sin defaults → obligatorio.
- **`ddl-auto=update`**: Hibernate crea/actualiza el esquema (sin Flyway/Liquibase; las migraciones estructurales se hacen por código idempotente en `DataInitializer`).
- **JWT**: 24 h, clave HMAC.

### 10.2 Seeds y migraciones — `DataInitializer`

`CommandLineRunner` que ejecuta en orden ~20 seeds idempotentes:

- **Departamentos** (6), **áreas** (5: Sala Blanca A/B, Laboratorio QC, Almacén Controlado, Zona de Empaque), **cargos**.
- **Usuarios demo**: `admin@zonecontrol.com` / `Admin123!` (ADMIN) y `gestor@zonecontrol.com` / `Gestor123!` (GESTOR_PERSONAL). **Auto-reparación del gestor**: si quedó con `password == null` (tras un reset de prueba), restaura `Gestor123!` y limpia el `setupToken` pendiente.
- **Empleados** (demo + 6 candidatos con `systemRole`), **usuarios extra** (Sandra GP, Javier SV, Miguel GP inactivo, Ricardo/Ana con setupToken pendiente).
- **Permisos** y **autorizaciones por área** (`seedAreaAuthorizations`, idempotente por empleado) para que el modal por sala y los filtros tengan contenido real.
- **Sesiones activas** (7 en 4 áreas), **historial de hoy** (7 registros con AUTHORIZED/UNREGISTERED/DENIED/SUSPENDED + previos), **alertas de ejemplo** (2 sin leer) y limpieza de `ACCESO_NOCTURNO` obsoleto.
- **Migraciones**: `migrateEmployeeCargos` (vincula empleados sin cargo por nombre y sincroniza `systemRole`) y `migratePermissionSchedules` (crea schedules LUN-DOM a permisos sin ellos).

### 10.3 Comandos

```bash
./mvnw test                           # suite backend
./mvnw test -Dtest=AuthControllerTest # test concreto
./mvnw clean spring-boot:run          # arranque (SIEMPRE con clean tras npm run build)
# dentro de src/main/frontend/
npm install
npm run dev                           # Vite :5173 (proxy /api → :8080)
npm run build                         # tsc -b && vite build → ../resources/static/
npm run lint                          # oxlint
npm run typecheck                     # tsc -b
```

### 10.4 Artefactos y directorios de datos

- `uploads/folleto/Folleto_Laboratorio_XYZ.pdf`: folleto generado offline con `chromium --headless --disable-gpu --print-to-pdf` desde `docs/folleto/...html` (gitignored).
- `uploads/photos/`: fotos de empleados (gitignored).
- `src/main/resources/static/`: build de Vite commiteado y servido por Spring.

---

## 11. Decisiones de diseño y gaps conocidos

### Decisiones de negocio documentadas

1. **Rol derivado del cargo** (`Position.systemRole`): no se elige manualmente al crear usuario; el empleado sin rol definido es solo personal de acceso físico (HU-05 reescrita).
2. **Magic link sin contraseña inicial**: el administrador nunca conoce la contraseña; se genera un setupToken de 48 B (SHA-256, 24 h, single-use) entregado por consola (sin SMTP aún).
3. **Sin auto-reactivación de permisos suspendidos**: el job `@Scheduled` se eliminó (commit fc377cf); la reactivación es manual vía `PATCH /api/permisos/{id}/reactivate`.
4. **EXIT fuera de KPIs y del archivo periódico**: las salidas se registran para auditoría/historial/exportes pero no suman al "Accesos hoy" ni al archivo periódico (solo ingresos).
5. **Archivo periódico sin datos personales**: solo agregados por departamento × área y por día; el botón "Enviar a Socio Internacional" abre un modal con preview y descarga (el envío externo queda fuera del sistema).
6. **Cascadas de estado transversales**: usuario ↔ empleado ↔ permisos se sincronizan al activar/desactivar.
7. **Matriz de roles solo lectura**: reconstruida desde `SecurityConfig` (verdad por construcción), sin edición ni enforcement en BD.
8. **Aforo = conteo**: la ocupación es el número de sesiones activas; no hay límite de aforo máximo validado.
9. **Landing sin datos internos**: el catálogo no expone `productionArea`/`id` al visitante; los GET públicos sí llevan `id` para el panel admin.

### Gaps / observaciones técnicas

1. **Clave JWT hardcodeada** en `application.properties` (no en `.env`). HMAC simétrico → cualquiera con la clave puede forjar tokens.
2. **Rol confiado del token**: solo se re-valida el `status` por request, no el rol.
3. **Token por query param** en `/api/access/stream` (limitación de `EventSource`); puede quedar en logs/proxies.
4. **CORS no configurado** en Spring (proxy Vite en dev, same-origin en prod).
5. **Magic link sin SMTP real**: solo log en consola.
6. **Asimetría de matchers**: `PUT/DELETE /api/personal/cargos/{id}` caen en `/api/personal/**` → alcanzables por `GESTOR_PERSONAL`, mientras que `POST /api/personal/cargos` es solo ADMIN.
7. **Frontend sin manejo global de 401**: si el JWT expira o el usuario se desactiva, cada vista muestra el error pero la app no cierra sesión automáticamente.
8. **Sin lazy loading** en el router (bundle completo en la primera visita).
9. **Paginación duplicada**: `PermissionsView`/`EmployeeListView` repiten navegación manual que `Pagination` ya encapsula.
10. **`@hookform/resolvers` instalado pero no usado**.
11. **`department` denormalizado** en `AccessHistory` (intencional: conserva el valor histórico).
12. **`AccessResult.SUSPENDED`** cubre también permiso vencido/fuera de horario, no solo el estado suspendido.
13. **Bulk upload por CSV `;`** (no Excel, pese a que Apache POI existe en el proyecto para reportes).

---

## 12. Glosario de rutas de API

### Backend — inventario por módulo

**Público (permitAll):**
```
GET  /api/public/institucional · /contacto · /sedes · /catalogo · /folleto
POST /api/auth/login
GET  /api/setup-password?token= · POST /api/setup-password
```

**Autenticado (cualquier sesión):**
```
POST /api/auth/change-password · PUT /api/auth/profile
```

**Administración (ADMIN):**
```
GET    /api/admin/users · /api/admin/users/candidatos · /api/admin/users/{id}
POST   /api/admin/users · /api/admin/users/{id}/reset-password
PATCH  /api/admin/users/{id}/status
PUT    /api/admin/users/{id}
GET    /api/admin/stats · /api/admin/role-matrix
PUT    /api/admin/contenido-publico/{INSTITUTIONAL|CONTACT}
POST   /api/admin/contenido-publico/folleto · DELETE /api/admin/contenido-publico/folleto
POST   /api/admin/contenido-publico/productos
PUT    /api/admin/contenido-publico/productos/{id} · DELETE …
POST   /api/admin/contenido-publico/sedes
PUT    /api/admin/contenido-publico/sedes/{id} · DELETE …
```

**Gestión personal (ADMIN, GESTOR_PERSONAL; GETs de catálogo + vistas de sala también SUPERVISOR_AUDITOR):**
```
POST /api/personal · GET /api/personal
GET  /api/personal/departamentos · /sedes · /cargos
GET  /api/personal/bulk/plantilla · POST /api/personal/bulk
GET  /api/personal/{id} · PATCH /api/personal/{id}
GET  /api/personal/{id}/permisos · /{id}/accesos
POST /api/personal/{id}/photo · GET /api/personal/{id}/photo · DELETE /api/personal/{id}/photo
POST /api/personal/cargos (ADMIN) · PUT /api/personal/cargos/{id} · DELETE /api/personal/cargos/{id}
POST /api/permisos · GET /api/permisos
DELETE /api/permisos/{id} · PATCH /api/permisos/{id}
PATCH /api/permisos/{id}/suspend · /{id}/reactivate
GET  /api/permisos/areas · POST /api/permisos/areas
PUT  /api/permisos/areas/{id} · DELETE /api/permisos/areas/{id}
GET  /api/permisos/areas/{name}/empleados · /{name}/autorizaciones
```

**Control de acceso (ADMIN, SUPERVISOR_AUDITOR):**
```
POST /api/access/validate · /api/access/exit
GET  /api/access/occupancy · /api/access/alerts
POST /api/access/zones/{name}/emergency
PATCH /api/access/alerts/{id}/leido
GET  /api/access/stream        # SSE, token por query param
```

**Reportes (ADMIN, SUPERVISOR_AUDITOR):**
```
GET  /api/historial · /api/historial/stats
POST /api/historial/export
POST /api/reportes/archivo-periodico · /api/reportes/archivo-periodico/preview
```

### Frontend — rutas del router

| Ruta | Vista | Rol |
|---|---|---|
| `/` | LandingView | público |
| `/login` | LoginView | público |
| `/configurar-contrasena` | SetupPasswordView | público |
| `/admin/dashboard` | AdminDashboard | ADMIN |
| `/admin/usuarios` · `/admin/usuarios/nuevo` | UsersView · CreateUserView | ADMIN |
| `/admin/contenido-publico` | PublicContentView | ADMIN |
| `/admin/areas` · `/admin/cargos` · `/admin/matriz-roles` | AdminAreasView · AdminCargosView · RoleMatrixView | ADMIN |
| `/personal` · `/personal/nuevo` · `/personal/carga-masiva` · `/personal/:id` | EmployeeListView · RegisterEmployeeView · BulkUploadView · EmployeeDetailView | GESTOR_PERSONAL |
| `/permisos` | PermissionsView | GESTOR_PERSONAL |
| `/supervisor` · `/supervisor/validar` · `/supervisor/zones` · `/supervisor/reportes` | SupervisorDashboard · AccessValidationView · ZonesView · ReportsView | SUPERVISOR_AUDITOR (validar/zones/reportes también ADMIN) |
| `/ajustes` | SettingsView | los 3 roles |
| `/403` · `*` | ForbiddenView · NotFoundView | — |
