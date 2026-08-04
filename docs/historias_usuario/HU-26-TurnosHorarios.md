# HU-26 - TURNOS Y HORARIOS POR DÍA

| Campo | Valor |
|---|---|
| **Código** | HU-26 |
| **Nombre** | Turnos y Horarios por Día |
| **Complejidad** | Alta |
| **HU Relacionada** | HU-11, HU-18 |
| **Módulo** | Módulo de Gestión de Personal |

## Descripción

**Yo como** gestor de personal
**Requiero** definir turnos/horarios por día de la semana en los permisos de acceso
**Para** controlar el ingreso a áreas según el turno laboral del empleado (p. ej. solo días hábiles o con horarios nocturnos)

## Requerimiento

Un permiso puede tener uno o más `PermissionSchedule` (día de la semana + hora inicio + hora fin). La validación de acceso exige que exista un schedule cuyo día coincida con hoy y cuya ventana contenga la hora actual. Si el permiso no tiene schedules (migración), se conserva el comportamiento base (LUN-DOM con los horarios del permiso).

## Criterios de Aceptación

Condición 01

Dado: que un permiso tiene un schedule para el día actual

Cuando: la hora actual está dentro de la ventana del schedule

Entonces: la validación de acceso responde AUTORIZADO

Condición 02

Dado: que un permiso tiene schedules pero ninguno para el día actual

Cuando: se valida un acceso

Entonces: el sistema responde SUSPENDIDO (día sin turno)

Condición 03

Dado: que un permiso tiene un schedule para el día actual

Cuando: la hora actual está fuera de la ventana del schedule

Entonces: el sistema responde SUSPENDIDO (hora fuera de ventana)

Condición 04

Dado: que un permiso existente no tiene schedules (migración)

Cuando: se valida un acceso

Entonces: el sistema usa los horarios base del permiso (equivalente a LUN-DOM)

## Tareas

| No | Descripción |
|---|---|
| 1 | Entidad PermissionSchedule (permission, dayOfWeek, startTime, endTime) |
| 2 | Migración idempotente LUN-DOM para permisos existentes |
| 3 | Modificar hasValidPermission para exigir schedule del día + ventana |
| 4 | CreatePermissionRequest/UpdatePermissionRequest con schedules opcionales |
| 5 | Frontend: selector de días de la semana en el formulario de permiso |

## Estado de Implementación

- **Backend**: ✓ — entidad, migración, query de validación por día y DTOs con schedules (3.2 §9). `PermissionResponse` expone `schedules` (lista `dayOfWeek`/`startTime`/`endTime`) para que el frontend pueda precargar y editar los turnos. Tests en `PermissionScheduleControllerTest`.
- **Frontend**: ✓ — selector de días (turnos) en `PermissionFormModal`. Al **editar** un permiso se preseleccionan los días desde sus `schedules` reales y el PATCH incluye los `schedules` (se reemplazan en backend). Nota de diseño: el modal usa una sola ventana horaria aplicada a los días seleccionados; editar normaliza horarios por-día distintos a ese par.

## Control de Versiones

| Versión | Fecha | Autor | Revisión | Descripción | Aprobador |
|---|---|---|---|---|---|
| 1.0 | 2026-08-04 | | | Versión inicial (HU nueva §9.5) | |
