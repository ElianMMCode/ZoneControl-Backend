# HU-20 - GESTIONAR ÁREAS DE PRODUCCIÓN

| Campo | Valor |
|---|---|
| **Código** | HU-20 |
| **Nombre** | Gestionar Áreas de Producción |
| **Complejidad** | Media |
| **HU Relacionada** | HU-03, HU-11, HU-18 |
| **Módulo** | Módulo de Administración |

## Descripción

**Yo como** administrador del sistema
**Requiero** gestionar el catálogo de áreas restringidas de producción (crear, editar, desactivar)
**Para** mantener actualizadas las zonas físicas que controlan el acceso, sin eliminar áreas que ya tienen permisos o historial asociado

## Requerimiento

El sistema debe permitir al administrador crear y editar áreas de producción, y desactivarlas en lugar de eliminarlas (DELETE duro rompería las FK de AccessPermission y AccessHistory). Las áreas inactivas no deben aparecer en el selector de permisos del gestor ni en la información pública, y los intentos de validación sobre áreas inactivas deben resultar en INGRESO DENEGADO.

## Criterios de Aceptación

Condición 01

Dado: que el administrador accede a la sección de áreas de producción

Cuando: consulta el catálogo

Entonces: el sistema lista todas las áreas (activas e inactivas) con su estado

Condición 02

Dado: que el administrador ingresa un nombre de área único

Cuando: crea una nueva área

Entonces: el sistema guarda el área con estado activo y retorna HTTP 201 con los datos del área creada

Condición 03

Dado: que el administrador intenta crear un área con un nombre que ya existe

Cuando: envía la solicitud

Entonces: el sistema retorna HTTP 409 con el mensaje "Ya existe un área con el nombre 'X'"

Condición 04

Dado: que el administrador edita un área existente

Cuando: modifica el nombre o la descripción y confirma

Entonces: el sistema actualiza el área y retorna HTTP 200. Si el nuevo nombre colisiona con otra área, retorna HTTP 409

Condición 05

Dado: que el administrador desactiva un área

Cuando: cambia el estado a inactivo

Entonces: el sistema preserva el área (no la elimina) y el área deja de aparecer en el selector de permisos del gestor y en la información pública institucional

## Tareas

| No | Descripción |
|---|---|
| 1 | Agregar campo `active` (boolean, default true) a la entidad ProductionArea |
| 2 | Implementar endpoint GET /admin/areas para listar todas las áreas |
| 3 | Implementar endpoint POST /admin/areas para crear una nueva área |
| 4 | Implementar endpoint PUT /admin/areas/{id} para editar nombre/descripción |
| 5 | Implementar endpoint PATCH /admin/areas/{id}/status para desactivar/activar (sin eliminación dura) |
| 6 | Actualizar GET /permisos/areas para excluir áreas inactivas del selector |
| 7 | Rechazar otorgamiento de permisos sobre áreas inactivas (400) |
| 8 | Validar acceso (POST /access/validate) sobre área inactiva → DENIED |
| 9 | Excluir áreas inactivas de la información institucional pública |

## Control de Versiones

| Versión | Fecha | Autor | Revisión | Descripción | Aprobador |
|---|---|---|---|---|---|
| 1.0 | 2026-07-31 | | | Versión inicial (CRUD con desactivación, CU-03d en diagramas) | |
