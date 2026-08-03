# HU-12 - REVOCAR ACCESO

| Campo | Valor |
|---|---|
| **Código** | HU-12 |
| **Nombre** | Revocar Acceso |
| **Complejidad** | Alta |
| **HU Relacionada** | HU-03, HU-11 |
| **Módulo** | Módulo de Gestión de Personal |

## Descripción

**Yo como** gestor de personal
**Requiero** eliminar permanentemente un permiso de acceso de un empleado
**Para** revocar privilegios de ingreso a áreas restringidas que ya no son necesarios

## Requerimiento

El sistema debe permitir al gestor de personal revocar (eliminar permanentemente) la autorización de ingreso de un empleado a áreas restringidas. La revocación debe ser inmediata, irreversible y quedar registrada en los logs del sistema.

## Criterios de Aceptación

Condición 01

Dado: que el gestor selecciona un empleado con permisos activos

Cuando: selecciona un permiso específico y confirma la revocación

Entonces: el sistema elimina el permiso de la base de datos, el empleado pierde acceso inmediatamente a las áreas revocadas y la acción se registra en logs

Condición 02

Dado: que el gestor va a revocar un permiso

Cuando: presiona "Revocar Acceso"

Entonces: el sistema muestra un diálogo de confirmación indicando que el empleado perderá acceso a las áreas y que la acción es permanente

Condición 03

Dado: que el gestor intenta revocar un permiso

Cuando: el permiso ya no existe en el sistema

Entonces: el sistema retorna error 404 con el mensaje "Permiso no encontrado"

## Tareas

| No | Descripción |
|---|---|
| 1 | Diseñar la vista de permisos activos del empleado con opción de revocar cada permiso |
| 2 | Implementar diálogo de confirmación de revocación con advertencia de que la acción es permanente |
| 3 | Implementar endpoint DELETE /permisos/{id} en Spring Boot |
| 4 | Eliminar el registro de permiso en PostgreSQL |
| 5 | Registrar la acción de revocación en logs del sistema |
| 6 | Actualizar la lista de permisos en el frontend tras la revocación exitosa |
| 7 | Manejar respuesta de error 404 para permisos que ya no existen |

## Control de Versiones

| Versión | Fecha | Autor | Revisión | Descripción | Aprobador |
|---|---|---|---|---|---|
| 1.0 | 2026-07-26 | | | Versión inicial | |
