# HU-13 - Suspender Acceso

| Campo | Valor |
|---|---|
| **Código** | HU-13 |
| **Nombre** | Suspender Acceso |
| **Complejidad** | Media |
| **HU Relacionada** | HU-03, HU-11 |
| **Módulo** | Módulo de Gestión de Personal |
| **Rol** | Gestor de personal |

## Descripción

**Yo como** gestor de personal
**Requiero** suspender temporalmente un permiso de acceso indicando una fecha de reactivación de referencia
**Para** bloquear el ingreso de un empleado a un área restringida por un tiempo, sin eliminar el permiso

## Requerimiento

El sistema debe permitir al gestor de personal suspender temporalmente el permiso de acceso de un empleado sin eliminarlo. Al suspender, el gestor indica una fecha de reactivación como referencia y el empleado deja de poder ingresar al área de inmediato. El permiso conserva su información para poder reactivarse después.

Es importante aclarar que la reactivación NO ocurre automáticamente cuando llega esa fecha: el permiso permanece suspendido hasta que el gestor lo reactive de forma manual. La fecha solo sirve como recordatorio de cuándo se previó reactivar. Mientras está suspendido, el permiso no se puede editar.

Si el permiso se suspendió porque el empleado quedó inactivo y después el empleado vuelve a estar activo, los permisos suspendidos por ese motivo se reactivan junto con él. La suspensión y la reactivación también se pueden realizar desde la ficha del empleado.

## Criterios de Aceptación

Condición 01

Dado: que el gestor selecciona un permiso activo de un empleado

Cuando: define la fecha de reactivación de referencia y confirma la suspensión

Entonces: el permiso pasa a estar suspendido, el empleado pierde el acceso a esa área de inmediato y el sistema muestra la confirmación de la suspensión

Condición 02

Dado: que un permiso está suspendido con su fecha de reactivación registrada

Cuando: llega o pasa esa fecha

Entonces: el permiso sigue suspendido; el sistema NO lo reactiva por sí solo, porque la reactivación es siempre manual

Condición 03

Dado: que un permiso está suspendido

Cuando: el gestor lo reactiva de forma manual

Entonces: el permiso vuelve a estar activo y el empleado recupera el acceso al área correspondiente

Condición 04

Dado: que un permiso está suspendido

Cuando: el gestor intenta modificarlo

Entonces: el sistema no permite la edición mientras el permiso esté suspendido y lo indica en la interfaz

Condición 05

Dado: que los permisos de un empleado se suspendieron porque el empleado quedó inactivo

Cuando: el gestor vuelve a activar al empleado

Entonces: los permisos suspendidos por ese motivo se reactivan junto con el empleado

Condición 06

Dado: que el gestor está viendo la ficha del empleado

Cuando: suspende o reactiva un permiso desde allí

Entonces: el sistema aplica el cambio igual que desde la pantalla de permisos y la información queda actualizada en ambos lugares

## Tareas

| No | Descripción |
|---|---|
| 1 | Diseñar la opción de suspender en la lista de permisos con el campo de fecha de reactivación |
| 2 | Implementar el proceso de suspensión que cambia el estado del permiso y guarda la fecha de referencia |
| 3 | Implementar el proceso de reactivación manual |
| 4 | Garantizar que un permiso suspendido no se pueda editar |
| 5 | Implementar la reactivación en cadena al volver a activar al empleado |
| 6 | Incluir la suspensión y la reactivación en la ficha del empleado |
| 7 | Mostrar el diálogo de confirmación con la fecha de reactivación antes de suspender |

## Control de Versiones

| Versión | Fecha | Autor | Revisión | Descripción | Aprobador |
|---|---|---|---|---|---|
| 1.0 | 2026-07-26 | | | Versión inicial | |
| 1.1 | 2026-08-06 | | | Revisión de lenguaje y criterios detallados (se aclara que no hay reactivación automática) | |

## Estado de Implementación

- **Backend**: ✓ — `PATCH /api/permisos/{id}/suspend` (body `{reactivationDate}`) y `PATCH /api/permisos/{id}/reactivate` (reactivación manual). **Sin auto-reactivación** (job `@Scheduled` eliminado, commit fc377cf). Tests verdes.
- **Frontend**: ✓ — modal de suspensión con fecha de reactivación y botón de reactivar en `PermissionsView` (`/permisos`, mockup 45) y desde el detalle del empleado (`/personal/:id`, mockup 41).
