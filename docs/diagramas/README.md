# Estructura de Diagramas - Zone Control

## Diagramas de Casos de Uso (`casos_uso/`)

| Archivo | Descripción | Actores |
|---------|-------------|---------|
| `00_diagrama_general.puml` | Diagrama general con jerarquía de actores y todos los casos de uso | Todos |
| `01_modulo_publico.puml` | Módulo público - información institucional (sin auth) | Público General |
| `02_modulo_autenticacion.puml` | Módulo de autenticación (login JWT + cambio de contraseña) | Usuario Autenticado |
| `03_modulo_administracion.puml` | Módulo de administración del sistema | Administrador (hereda de Usuario Autenticado). CU-03a: CRUD usuarios · CU-03b: Contenido público · CU-03c: Matriz de roles y permisos (consulta, pendiente) |
| `04_modulo_gestion_personal.puml` | Gestión de personal (registro, carga masiva, permisos, búsqueda, edición, áreas, fotografía) | Gestor de Personal (hereda de Usuario Autenticado) |
| `05_modulo_control_acceso_fisico.puml` | Control de acceso físico | Supervisor/Auditor + Empleado |
| `06_modulo_reportes_auditoria.puml` | Reportes y auditoría | Supervisor/Auditor (hereda de Usuario Autenticado) |

---

## Diagramas de Flujo (`flujo/`)

| # | Archivo | CU | Descripción |
|---|---------|-----|-------------|
| 01 | `01_flujo_autenticacion.puml` | CU-02 | Autenticación (transversal) |
| 02 | `02_flujo_publico.puml` | CU-01 | Módulo Público |
| 03 | `03_flujo_crear_usuario.puml` | CU-03 | Crear Usuario |
| 04 | `04_flujo_editar_usuario.puml` | CU-03 | Editar Usuario |
| 05 | `05_flujo_activar_desactivar.puml` | CU-03 | Activar/Desactivar |
| 06 | `06_flujo_restablecer_password.puml` | CU-03 | Restablecer Contraseña |
| 07 | `07_flujo_registrar_personal.puml` | CU-04 | Registrar Personal |
| 08 | `08_flujo_carga_masiva.puml` | CU-05 | Carga Masiva |
| 09 | `09_flujo_otorgar_acceso.puml` | CU-06 | Otorgar Acceso |
| 10 | `10_flujo_revocar_acceso.puml` | CU-06 | Revocar Acceso |
| 11 | `11_flujo_suspender_acceso.puml` | CU-06 | Suspender Acceso |
| 12 | `12_flujo_buscar_personal.puml` | CU-07 | Buscar Personal |
| 13 | `13_flujo_historial.puml` | CU-08 | Consultar Historial |
| 14 | `14_flujo_documento_descargable.puml` | CU-09 | Generar Documento |
| 15 | `15_flujo_archivo_periodico.puml` | CU-10 | Archivo Periódico |
| 16 | `16_flujo_control_acceso.puml` | CU-11 | Control de Acceso |
| 17 | `17_flujo_gestion_contenido_publico.puml` | CU-03b | Gestionar Contenido Público |
| 18 | `18_flujo_editar_empleado.puml` | CU-07a | Editar Empleado |
| 19 | `19_flujo_gestion_areas_produccion.puml` | CU-06a | Gestionar Áreas de Producción (implementado backend) |

---

## Dependencias entre Casos de Uso

| Caso de Uso | Dependencia | Motivo |
|-------------|-----------|--------|
| CU-03a (Crear Usuario) | CU-04 (Registrar Personal) | Prerrequisito: el Employee debe existir antes de crear el User. Relación @OneToOne obligatoria vía employeeCode. |
| CU-05 (Carga Masiva) | CU-05a (Validar estructura) | Incluido (<<include>>): validación de encabezados y datos por fila antes de insertar. |

## Flujo Transversal

Todos los módulos internos requieren autenticación. La herencia de actores refleja esto:

```
Usuario Autenticado ─── CU-02 (Login JWT)
    ├── Administrador      → CU-03 (Admin)
    ├── Gestor Personal    → CU-04-07 (Personal)
    └── Supervisor/Auditor → CU-08-11 (Reportes + Acceso)
```

---

## Asignación de Vistas a Dashboards

Cada vista de los mockups pertenece al actor principal definido en los casos de uso.
El Administrador tiene acceso total por la matriz de permisos (SecurityConfig), pero
cada vista debe aparecer en el dashboard de su actor. Las rutas corresponden a las
definidas en `src/main/frontend/src/routes/index.tsx` (React Router).

| Dashboard | Actor (CU) | Vistas |
|-----------|------------|--------|
| **Público** | Público General (CU-01) | `/` (`LandingView`) |
| **Compartido** | Usuario Autenticado (CU-02) | `/login`, `/configurar-contrasena`, `/ajustes` (`SettingsView` — perfil + cambio de contraseña) |
| **Admin** | Administrador (CU-03a/03b/03c) | `/admin/dashboard`, `/admin/usuarios`, `/admin/usuarios/nuevo`, `/admin/contenido-publico`, `/admin/areas` (CU-06a) |
| **Gestor** | Gestor de Personal (CU-04/05/06/06a/07/07a/07b) | `/personal`, `/personal/nuevo`, `/personal/carga-masiva`, `/personal/:id`, `/permisos` |
| **Supervisor** | Supervisor/Auditor (CU-08/09/10/11) | `/supervisor` |

> **Nota**: `settings.html`/`/ajustes` es transversal porque el perfil y el cambio de contraseña
> (`POST /api/auth/change-password`) aplican a los tres roles autenticados (CU-02).
> La vista de validación de acceso (`access-validation.html`, mockup 44) y la de reportes
> (`audit-reports.html`, mockup 37) pertenecen al Supervisor según CU-11/CU-08-10 — pendientes frontend.
> La matriz de roles (mockup 16) pertenece al Administrador (CU-03c, solo consulta) — pendiente.
> `CU-06a` (áreas) y `CU-07b` (fotografía) están implementados en backend y frontend.

---

## Stack Tecnológico

| Capa | Tecnología |
|------|-----------|
| Frontend | React o Vue.js |
| Backend | Spring Boot (Java) |
| Base de datos | PostgreSQL |
| Autenticación | JWT |
| Documentación | PlantUML |
