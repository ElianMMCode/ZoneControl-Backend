# HU-10 - Otorgar Acceso a Áreas

| Campo | Valor |
|---|---|
| **Código** | HU-10 |
| **Nombre** | Otorgar Acceso a Áreas |
| **Complejidad** | Alta |
| **HU Relacionada** | HU-03, HU-08, HU-11, HU-12 |
| **Módulo** | Módulo de Gestión de Personal |
| **Rol** | Gestor de personal |

## Descripción

**Yo como** gestor de personal
**Requiero** asignar a un empleado activo un permiso de acceso a un área restringida, definiendo horarios y vigencia
**Para** controlar qué zonas puede visitar cada empleado, en qué momentos y durante qué períodos

## Requerimiento

El sistema debe permitir al gestor de personal autorizar la entrada de un empleado a un área restringida. Para ello selecciona al empleado, elige el área, define el horario base de acceso (hora de inicio y hora de fin) y las fechas de inicio y vencimiento del permiso. De forma opcional, puede indicar horarios diferentes para cada día de la semana (lunes a domingo).

Solo se puede tener un permiso por cada combinación de empleado y área. Si el empleado ya tiene un permiso para esa área, el sistema no crea uno nuevo: lo indica y sugiere editar el permiso existente. Tampoco se permite otorgar acceso a un empleado que no está activo.

El permiso otorgado se puede editar después (por ejemplo, para ajustar el horario o la vigencia), siempre y cuando el permiso esté activo. Los permisos suspendidos no se pueden editar.

## Criterios de Aceptación

Condición 01

Dado: que el gestor selecciona un empleado activo y un área restringida

Cuando: define el horario base, las fechas de inicio y vencimiento, y confirma la operación

Entonces: el sistema guarda el permiso y lo muestra en la lista de permisos del empleado, de modo que la validación de acceso en esa área lo considere autorizado dentro del horario y las fechas definidos

Condición 02

Dado: que el empleado ya tiene un permiso para la misma área

Cuando: el gestor intenta otorgar un nuevo acceso para esa misma combinación de empleado y área

Entonces: el sistema no crea el permiso y muestra el mensaje "El empleado ya tiene un permiso para esta área. Edite el permiso existente"

Condición 03

Dado: que el gestor selecciona un empleado inactivo

Cuando: intenta otorgarle acceso a un área

Entonces: el sistema rechaza la operación y muestra el mensaje "No se puede otorgar acceso a empleado inactivo"

Condición 04

Dado: que el gestor desea horarios distintos según el día de la semana

Cuando: configura turnos diferentes para algunos días (lunes a domingo)

Entonces: el sistema guarda el horario general y, además, los horarios particulares de cada día, y la validación de acceso respeta el horario del día correspondiente

Condición 05

Dado: que un permiso está activo

Cuando: el gestor lo edita para cambiar el horario o las fechas de vigencia

Entonces: el sistema guarda los cambios y la validación de acceso utiliza los nuevos valores

Condición 06

Dado: que el gestor omite un dato obligatorio (empleado, área, horario o fechas)

Cuando: intenta otorgar el permiso

Entonces: el sistema indica el campo que falta, no guarda el permiso y le pide completar la información

## Tareas

| No | Descripción |
|---|---|
| 1 | Diseñar el formulario de otorgamiento de permiso con búsqueda del empleado y selector de área restringida |
| 2 | Implementar los selectores de horario (inicio y fin) y de fechas de inicio y vencimiento |
| 3 | Implementar el selector de turnos por día de la semana (opcional) |
| 4 | Guardar el permiso validando que el empleado esté activo |
| 5 | Verificar la regla de un permiso por empleado y área, impidiendo duplicados con el mensaje de la historia |
| 6 | Permitir la edición del permiso solo cuando está activo |
| 7 | Mostrar los mensajes de error de forma clara en la interfaz |

## Control de Versiones

| Versión | Fecha | Autor | Revisión | Descripción | Aprobador |
|---|---|---|---|---|---|
| 1.0 | 2026-07-26 | | | Versión inicial | |
| 1.1 | 2026-07-31 | | | Regla un-permiso-por-área; mensaje 409 actualizado; +PATCH editar permiso | |
| 1.2 | 2026-08-06 | | | Revisión de lenguaje y criterios detallados | |

## Estado de Implementación

- **Backend**: ✓ — `POST /api/permisos` (201 / 400 empleado inactivo / 404 / 409 duplicado empleado+área) y `PATCH /api/permisos/{id}` (edición de fechas y horarios de permisos ACTIVO). `PermissionSchedule` LUN..DOM (HU-23). Tests verdes.
- **Frontend**: ✓ — modal "Otorgar permiso" en `PermissionsView` (`/permisos`, mockup 45) y "Asignar nueva área" en `EmployeeDetailView` (`/personal/:id`, mockup 41). Incluye selector de empleado (búsqueda mientras se escribe), selector de área, turnos por día y manejo del 409.
