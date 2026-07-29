# Estructura de Diagramas - Zone Control

## Diagramas de Casos de Uso (`casos_uso/`)

| Archivo | Descripción | Actores |
|---------|-------------|---------|
| `00_diagrama_general.puml` | Diagrama general con todos los actores y casos de uso | Todos |
| `01_modulo_publico.puml` | Módulo público - información institucional | Público General |
| `02_modulo_autenticacion.puml` | Módulo de autenticación (login) | Usuarios Internos |
| `03_modulo_administracion.puml` | Módulo de administración del sistema | Administrador |
| `04_modulo_gestion_personal.puml` | Gestión de personal (registro, carga masiva, permisos, búsqueda, edición con cascade) | Gestor de Personal |
| `05_modulo_control_acceso_fisico.puml` | Control de acceso físico | Supervisor/Auditor |
| `06_modulo_reportes_auditoria.puml` | Reportes y auditoría | Supervisor/Auditor |

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

---

## Flujo Transversal

Todos los módulos internos requieren autenticación:

```
Usuario → Login (CU-02) → Token JWT → Módulo correspondiente
```

---

## Stack Tecnológico

| Capa | Tecnología |
|------|-----------|
| Frontend | React o Vue.js |
| Backend | Spring Boot (Java) |
| Base de datos | PostgreSQL |
| Autenticación | JWT |
| Documentación | PlantUML |
