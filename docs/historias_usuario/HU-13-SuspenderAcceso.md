# HU-13 - SUSPENDER ACCESO

| Campo | Valor |
|---|---|
| **Código** | HU-13 |
| **Nombre** | Suspender Acceso |
| **Complejidad** | Media |
| **HU Relacionada** | HU-03, HU-11 |
| **Módulo** | Módulo de Gestión de Personal |

## Descripción

**Yo como** gestor de personal
**Requiero** suspender temporalmente un permiso de acceso definiendo una fecha de reactivación
**Para** bloquear el ingreso de un empleado a áreas restringidas de forma temporal sin eliminar el permiso

## Requerimiento

El sistema debe permitir al gestor de personal suspender temporalmente la autorización de ingreso de un empleado, estableciendo una fecha programada de reactivación. Mientras el permiso esté suspendido, el empleado no debe poder acceder. **Nota:** la reactivación NO es automática (el job `@Scheduled` se eliminó en commit fc377cf); el permiso queda suspendido hasta que el gestor lo reactive manualmente vía `PATCH /permisos/{id}`.

## Criterios de Aceptación

Condición 01

Dado: que el gestor selecciona un empleado con permisos activos

Cuando: selecciona un permiso, define una fecha de reactivación y confirma la suspensión

Entonces: el sistema cambia el estado del permiso a "SUSPENDIDO", registra la fecha de reactivación, el empleado pierde acceso temporalmente y se muestra confirmación

Condición 02

Dado: que un permiso está en estado "SUSPENDIDO" con una fecha de reactivación registrada

Cuando: el gestor reactiva el permiso manualmente (o el empleado es reactivado en cascada)

Entonces: el sistema cambia el estado a "ACTIVO" y el empleado recupera el acceso a las áreas correspondientes

Condición 03

Dado: que el gestor va a suspender un permiso

Cuando: presiona "Suspender Acceso"

Entonces: el sistema muestra un diálogo de confirmación indicando la fecha hasta la cual estará suspendido el acceso

## Tareas

| No | Descripción |
|---|---|
| 1 | Diseñar la vista de permisos activos del empleado con opción de suspender cada permiso |
| 2 | Implementar campo de fecha de reactivación en el diálogo de suspensión |
| 3 | Implementar endpoint PATCH /permisos/{id}/suspend en Spring Boot |
| 4 | Actualizar estado del permiso a "SUSPENDIDO" y guardar fecha de reactivación en PostgreSQL |
| 5 | ~~Implementar job programado que reactive permisos automáticamente~~ — gap conocido (HU-13): no hay reactivación automática |
| 6 | Mostrar diálogo de confirmación antes de ejecutar la suspensión con la fecha de reactivación |

## Control de Versiones

| Versión | Fecha | Autor | Revisión | Descripción | Aprobador |
|---|---|---|---|---|---|
| 1.0 | 2026-07-26 | | | Versión inicial | |
