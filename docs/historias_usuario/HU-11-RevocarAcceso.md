# HU-11 - Revocar Acceso

| Campo | Valor |
|---|---|
| **Código** | HU-11 |
| **Nombre** | Revocar Acceso |
| **Complejidad** | Alta |
| **HU Relacionada** | HU-03, HU-10 |
| **Módulo** | Módulo de Gestión de Personal |
| **Rol** | Gestor de personal |

## Descripción

**Yo como** gestor de personal
**Requiero** eliminar de forma definitiva un permiso de acceso de un empleado
**Para** retirar el privilegio de ingreso a un área restringida que ya no es necesario

## Requerimiento

El sistema debe permitir al gestor de personal revocar (eliminar definitivamente) el permiso de acceso de un empleado a un área restringida. Esta acción no se puede deshacer: una vez confirmada, el empleado pierde inmediatamente el acceso a esa área y el permiso deja de existir.

Como es una acción permanente, el sistema pide confirmación antes de ejecutarla y advierte que el empleado perderá el acceso al área. Si el empleado necesita volver a entrar a esa área, debe otorgársele un permiso nuevo. Si por algún motivo el permiso ya no existe al momento de revocarlo, el sistema lo informa y no realiza ninguna acción.

## Criterios de Aceptación

Condición 01

Dado: que el gestor selecciona un permiso de un empleado

Cuando: confirma la revocación

Entonces: el sistema elimina el permiso de forma definitiva, el empleado pierde inmediatamente el acceso a esa área y el permiso desaparece de la lista de permisos

Condición 02

Dado: que el gestor va a revocar un permiso

Cuando: presiona la opción de revocar

Entonces: el sistema muestra un diálogo de confirmación que advierte que la acción es permanente y que el empleado perderá el acceso al área, y solo procede si el gestor confirma

Condición 03

Dado: que el gestor intenta revocar un permiso

Cuando: ese permiso ya no existe en el sistema

Entonces: el sistema muestra el mensaje "Permiso no encontrado" y no realiza ninguna acción

Condición 04

Dado: que se revocó el permiso de un empleado

Cuando: el empleado intenta acceder a esa área

Entonces: el sistema no lo autoriza, y si se necesita recuperar el acceso, se debe otorgar un permiso nuevo

## Tareas

| No | Descripción |
|---|---|
| 1 | Diseñar la opción de revocar en la lista de permisos del empleado |
| 2 | Implementar el diálogo de confirmación con la advertencia de que la acción es permanente |
| 3 | Implementar el proceso de eliminación definitiva del permiso |
| 4 | Actualizar la lista de permisos en pantalla tras la revocación exitosa |
| 5 | Manejar el caso en que el permiso ya no existe y mostrar el mensaje correspondiente |

## Control de Versiones

| Versión | Fecha | Autor | Revisión | Descripción | Aprobador |
|---|---|---|---|---|---|
| 1.0 | 2026-07-26 | | | Versión inicial | |
| 1.1 | 2026-08-06 | | | Revisión de lenguaje y criterios detallados | |

## Estado de Implementación

- **Backend**: ✓ — `DELETE /api/permisos/{id}` (200 / 404). Tests verdes.
- **Frontend**: ✓ — acción de revocar con confirmación ("acción permanente") en `PermissionsView` (`/permisos`, mockup 45) y `EmployeeDetailView` (`/personal/:id`, mockup 41).
