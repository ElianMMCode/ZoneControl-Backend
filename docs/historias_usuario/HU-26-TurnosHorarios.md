# HU-26 - TURNOS Y HORARIOS POR DÍA

| Campo | Valor |
|---|---|
| **Código** | HU-26 |
| **Nombre** | Turnos y Horarios por Día |
| **Complejidad** | Alta |
| **HU Relacionada** | HU-11, HU-18 |
| **Módulo** | Módulo de Gestión de Personal |
| **Rol** | Gestor de Personal |

## Descripción

**Yo como** gestor de personal
**Requiero** definir horarios de acceso distintos para cada día de la semana en los permisos otorgados a los empleados
**Para** controlar el ingreso a las áreas según el turno laboral real de cada persona, incluyendo días sin autorización y horarios nocturnos

## Requerimiento

Al otorgar o modificar un permiso, el sistema permite indicar, para cada día de la semana (lunes, martes, miércoles, jueves, viernes, sábado y domingo), la ventana horaria en la que el empleado puede ingresar. Se puede configurar un mismo horario para varios días, o dejar el permiso con un único horario que se aplica todos los días si no se definen turnos por día.

Un turno puede cruzar la medianoche: por ejemplo, de las 22:00 a las 06:00 del día siguiente, y el sistema debe aceptar esa configuración y validar correctamente los accesos en ambos segmentos.

Cuando se valida el ingreso de un empleado, el sistema toma en cuenta el turno del día actual: si el empleado no tiene turno configurado para ese día, o si la hora actual queda fuera de la ventana, el acceso se rechaza. Al editar un permiso, los turnos enviados reemplazan a los anteriores; si no se envían turnos nuevos, los existentes se conservan.

## Criterios de Aceptación

Condición 01

Dado: que un empleado tiene configurado un turno para el día actual en su permiso

Cuando: intenta ingresar dentro de la ventana horaria de ese turno

Entonces: el sistema autoriza el acceso

Condición 02

Dado: que un empleado tiene turnos configurados por día

Cuando: intenta ingresar en un día que no tiene turno asignado

Entonces: el sistema rechaza el acceso e indica que el día no tiene turno autorizado

Condición 03

Dado: que un empleado tiene un turno configurado para el día actual

Cuando: intenta ingresar fuera de la ventana horaria de ese turno

Entonces: el sistema rechaza el acceso e indica que la hora está fuera del horario permitido

Condición 04

Dado: que un permiso no tiene turnos configurados por día

Cuando: el empleado intenta ingresar cualquier día

Entonces: el sistema aplica el horario general del permiso, equivalente a tener el mismo turno todos los días de la semana

Condición 05

Dado: que un turno cruza la medianoche (por ejemplo, de las 22:00 a las 06:00)

Cuando: el empleado intenta ingresar dentro de la ventana, en cualquiera de los dos segmentos del horario

Entonces: el sistema autoriza el acceso correctamente tanto antes como después de la medianoche

Condición 06

Dado: que el gestor de personal edita un permiso que ya tiene turnos

Cuando: envía el formulario sin modificar los turnos

Entonces: el sistema conserva los turnos existentes; si el formulario incluye turnos nuevos, estos reemplazan por completo a los anteriores

Condición 07

Dado: que el gestor de personal abre el formulario de edición de un permiso

Cuando: revisa la sección de turnos

Entonces: el sistema muestra preseleccionados los días y el horario que el permiso ya tiene configurados, para que el gestor pueda ajustarlos sin perder de vista lo vigente

## Tareas

| No | Descripción |
|---|---|
| 1 | Guardar turnos por día de la semana (día + hora de inicio + hora de fin) para cada permiso |
| 2 | Aplicar el horario general del permiso a todos los días cuando no hay turnos configurados |
| 3 | Soportar turnos que cruzan la medianoche |
| 4 | Validar el acceso considerando el turno del día actual |
| 5 | Reemplazar o conservar los turnos al editar un permiso según lo enviado |
| 6 | Selector de días de la semana en el formulario de permiso con preselección de lo vigente |

## Control de Versiones

| Versión | Fecha | Autor | Revisión | Descripción | Aprobador |
|---|---|---|---|---|---|
| 1.0 | 2026-08-06 | | | Revisión de lenguaje y criterios detallados | |

## Estado de Implementación

- **Backend**: ✓ — entidad `PermissionSchedule` (LUN..DOM) + migración idempotente, validación por día en `hasValidPermission`, `schedules` opcionales en Create/UpdatePermission, `PermissionResponse.schedules` para precargar (3.2 §9). Tests en `PermissionScheduleControllerTest`.
- **Frontend**: ✓ — selector de días (turnos) en `PermissionFormModal`; preselección y reemplazo de `schedules` en edición. Nota: el modal usa una sola ventana horaria aplicada a los días seleccionados.
