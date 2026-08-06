# HU-19 - GESTIONAR ÁREAS DE PRODUCCIÓN

| Campo | Valor |
|---|---|
| **Código** | HU-19 |
| **Nombre** | Gestionar Áreas de Producción |
| **Complejidad** | Media |
| **HU Relacionada** | HU-03, HU-10 |
| **Módulo** | Módulo de Gestión de Personal |
| **Rol** | Administrador, Gestor de Personal |

## Descripción

**Yo como** gestor de personal o administrador
**Requiero** crear, modificar y eliminar las áreas de producción a las que se asignan permisos de acceso
**Para** mantener al día el catálogo de zonas restringidas del laboratorio

## Requerimiento

El sistema debe permitir registrar una nueva área de producción indicando su nombre, que debe ser único y de hasta 30 caracteres, y una descripción opcional. También debe permitir modificar el nombre o la descripción de un área existente y eliminar un área siempre que no esté comprometida con permisos de acceso vigentes.

Cuando un área tiene permisos activos asociados, el sistema no permite eliminarla y explica el motivo indicando cuántos permisos activos tiene. Si se intenta registrar un área con un nombre que ya existe, el sistema lo rechaza e informa el conflicto.

El historial de accesos conserva el nombre que tenía el área en el momento de cada evento, de modo que renombrar un área no altera los registros históricos. Las áreas son gestionadas tanto por el administrador como por el gestor de personal.

## Criterios de Aceptación

Condición 01

Dado: que el gestor o administrador desea crear un área de producción

Cuando: ingresa un nombre nuevo (de hasta 30 caracteres) y una descripción opcional y envía el formulario

Entonces: el sistema registra el área y la muestra en el listado de áreas disponibles para asignar permisos

Condición 02

Dado: que el gestor o administrador intenta crear un área

Cuando: el nombre ingresado ya está registrado

Entonces: el sistema rechaza el registro y muestra el mensaje "Ya existe un área con ese nombre", sin crear duplicados

Condición 03

Dado: que el gestor o administrador modifica un área existente

Cuando: cambia el nombre o la descripción y envía el formulario

Entonces: el sistema actualiza los datos del área; si el nuevo nombre ya pertenece a otra área, el sistema rechaza el cambio e informa el conflicto

Condición 04

Dado: que el gestor o administrador elimina un área

Cuando: el área no tiene permisos de acceso activos

Entonces: el sistema elimina el área y esta deja de aparecer en el listado

Condición 05

Dado: que el gestor o administrador elimina un área

Cuando: el área tiene permisos de acceso activos

Entonces: el sistema no elimina el área y muestra el mensaje "No se puede eliminar el área porque tiene N permiso(s) activo(s)", indicando la cantidad de permisos involucrados

Condición 06

Dado: que se renombra un área que ya tiene accesos registrados en el historial

Cuando: se consulta el historial de accesos

Entonces: los registros anteriores conservan el nombre original del área, sin verse alterados por el cambio

## Tareas

| No | Descripción |
|---|---|
| 1 | Registrar un área con nombre único (máximo 30 caracteres) y descripción opcional |
| 2 | Modificar el nombre o la descripción de un área existente |
| 3 | Eliminar un área cuando no tenga permisos activos, rechazando la eliminación e informando el motivo cuando sí los tenga |
| 4 | Conservar el nombre histórico del área en el registro de accesos aunque esta se renombre |
| 5 | Pantalla de administración de áreas con listado y formulario de alta/edición |

## Control de Versiones

| Versión | Fecha | Autor | Revisión | Descripción | Aprobador |
|---|---|---|---|---|---|
| 1.0 | 2026-08-06 | | | Revisión de lenguaje y criterios detallados | |

## Estado de Implementación

- **Backend**: ✓ — `POST/PUT/DELETE /api/permisos/areas` (nombre único ≤ 30, 409 si hay permisos activos; gap 1.4 §9). Tests en `PermissionControllerTest`.
- **Frontend**: ✓ — `AdminAreasView` en `/admin/areas` (rol ADMIN). Seed de áreas con descripción en `DataInitializer`.
