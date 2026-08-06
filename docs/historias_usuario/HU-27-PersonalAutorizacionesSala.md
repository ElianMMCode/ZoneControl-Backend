# HU-27 - Consultar el Personal y las Autorizaciones de una Sala

| Campo | Valor |
|---|---|
| **Código** | HU-27 |
| **Nombre** | Consultar el personal y las autorizaciones de una sala |
| **Complejidad** | Media |
| **HU Relacionada** | HU-20, HU-10, HU-23 |
| **Módulo** | Módulo de Control de Acceso Físico |
| **Rol** | Supervisor / Auditor, Administrador |

## Descripción

**Yo como** supervisor o auditor
**Requiero** consultar, desde cada sala del panel de zonas, quiénes tienen acceso a esa sala y los permisos otorgados
**Para** verificar rápidamente la dotación y las autorizaciones de cada área sin salir del panel de zonas

## Requerimiento

Cada zona del panel de zonas en vivo debe ofrecer la opción de abrir un detalle con dos pestañas: "Empleados asignados", que muestra la lista de personas con permiso sobre esa sala (código, nombre, cargo, departamento y estado del empleado), y "Autorizaciones", que muestra cada permiso con su información completa (vigencia, horario, turnos por día de la semana y estado del permiso). Además, el detalle muestra la ocupación actual de la sala (quiénes están dentro y desde qué hora). Esta consulta es de solo lectura: no permite modificar permisos.

## Criterios de Aceptación

Condición 01

Dado: que el supervisor está en el panel de zonas en vivo

Cuando: abre el detalle de una sala con la opción "Personal / Autorizaciones"

Entonces: el sistema muestra la ocupación actual de la sala y dos pestañas: "Empleados asignados" y "Autorizaciones"

Condición 02

Dado: que se consulta la pestaña "Empleados asignados"

Cuando: la sala tiene empleados con permiso

Entonces: se muestran el código, el nombre, el cargo, el departamento y el estado de cada empleado asignado

Condición 03

Dado: que se consulta la pestaña "Autorizaciones"

Cuando: la sala tiene permisos otorgados

Entonces: cada permiso muestra el empleado, el estado del permiso, la fecha de inicio y de vencimiento, el horario base y los turnos por día de la semana

Condición 04

Dado: que una sala no tiene empleados asignados ni autorizaciones

Cuando: se abre su detalle

Entonces: el sistema lo indica con el mensaje de que no hay información para esa sala

Condición 05

Dado: que el supervisor consulta el detalle de una sala

Cuando: termina la revisión

Entonces: puede cerrar el detalle sin haber realizado ningún cambio (la consulta es de solo lectura)

## Tareas

| No | Descripción |
|---|---|
| 1 | Mostrar la ocupación actual de la sala en el detalle |
| 2 | Listar los empleados asignados a la sala |
| 3 | Mostrar las autorizaciones con vigencia, horario y turnos por día |
| 4 | Mantener la consulta de solo lectura |

## Control de Versiones

| Versión | Fecha | Autor | Revisión | Descripción | Aprobador |
|---|---|---|---|---|---|
| 1.0 | 2026-08-06 | | | Historia nueva: panel por sala implementado | |

## Estado de Implementación

- **Backend**: ✓ — `GET /api/permisos/areas/{nombre}/empleados` y `GET /api/permisos/areas/{nombre}/autorizaciones` (solo lectura, accesibles para ADMIN y SUPERVISOR_AUDITOR).
- **Frontend**: ✓ — botón "Personal / Autorizaciones" por sala en `/supervisor/zones` con las dos pestañas y la ocupación detallada.
- **Tests**: ✓ — `listAreaEmployees_*` y `listAreaAuthorizations_*` en `PermissionControllerTest`.
