# HU-31 - Consultar el Detalle del Empleado

| Campo | Valor |
|---|---|
| **Código** | HU-31 |
| **Nombre** | Consultar el detalle del empleado |
| **Complejidad** | Media |
| **HU Relacionada** | HU-09, HU-14, HU-11, HU-15 |
| **Módulo** | Módulo de Gestión de Personal |
| **Rol** | Gestor de Personal |

## Descripción

**Yo como** gestor de personal
**Requiero** ver una ficha completa de cada empleado que reúna su información, su fotografía, sus permisos de acceso y su historial de accesos
**Para** conocer de un vistazo todo lo relacionado con una persona sin buscarlo en varias secciones

## Requerimiento

Desde los resultados de búsqueda de personal, el gestor debe poder abrir el detalle de un empleado. La ficha muestra los datos personales (código, documento, nombres, apellidos, cargo, departamento, estado, correo, sede y fecha de ingreso), la fotografía si la tiene, el listado de sus permisos de acceso (con su estado y vigencia) y su historial de accesos recientes. Desde la ficha también se puede editar al empleado, cambiar la fotografía, y otorgar, editar, suspender o reactivar permisos.

Al editar al empleado, el cargo se selecciona del catálogo de cargos (HU-32) mediante una lista desplegable y, según el cargo elegido, el sistema muestra el rol de sistema que le corresponde al empleado (derivado del cargo, HU-05).

## Criterios de Aceptación

Condición 01

Dado: que el gestor busca a un empleado

Cuando: selecciona su detalle

Entonces: el sistema muestra la ficha con los datos personales completos del empleado

Condición 02

Dado: que el empleado tiene fotografía

Cuando: se consulta su detalle

Entonces: la ficha muestra la fotografía; si no tiene, se muestra un lugar sin imagen

Condición 03

Dado: que el empleado tiene permisos de acceso

Cuando: se consulta su detalle

Entonces: se muestran sus permisos con el área, el estado (activo o suspendido), la vigencia y los turnos por día

Condición 04

Dado: que el empleado tiene historial de accesos

Cuando: se consulta su detalle

Entonces: se muestra una lista de sus accesos recientes con fecha, área y resultado

Condición 05

Dado: que el gestor está en el detalle del empleado

Cuando: quiere modificar sus datos o su estado

Entonces: puede hacerlo desde la misma ficha

Condición 06

Dado: que el gestor está en el detalle del empleado

Cuando: quiere gestionar un permiso (otorgar, editar, suspender o reactivar)

Entonces: puede hacerlo desde la misma ficha, con las mismas validaciones que la gestión de permisos (HU-11, HU-13)

## Tareas

| No | Descripción |
|---|---|
| 1 | Mostrar la ficha completa del empleado (datos, foto, permisos, historial) |
| 2 | Permitir editar los datos y el estado desde la ficha |
| 3 | Permitir cambiar la fotografía |
| 4 | Permitir otorgar y gestionar permisos desde la ficha |

## Control de Versiones

| Versión | Fecha | Autor | Revisión | Descripción | Aprobador |
|---|---|---|---|---|---|
| 1.0 | 2026-08-06 | | | Historia nueva: detalle del empleado implementado | |

## Estado de Implementación

- **Backend**: ✓ — `GET /api/personal/{id}`, `GET /api/personal/{id}/permisos`, `GET /api/personal/{id}/accesos`, foto (`POST/GET/DELETE /api/personal/{id}/photo`) y permisos.
- **Frontend**: ✓ — vista de detalle del empleado (`/personal/:id`).
- **Tests**: ✓ — detalle, permisos, historial y foto en los tests del módulo de personal.
