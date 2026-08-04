# HU-20 - GESTIONAR ÁREAS DE PRODUCCIÓN

| Campo | Valor |
|---|---|
| **Código** | HU-20 |
| **Nombre** | Gestionar Áreas de Producción |
| **Complejidad** | Media |
| **HU Relacionada** | HU-03, HU-11 |
| **Módulo** | Módulo de Gestión de Personal |

## Descripción

**Yo como** gestor de personal / administrador
**Requiero** crear, editar y eliminar áreas de producción restringidas
**Para** mantener actualizado el catálogo de zonas a las que se asignan permisos de acceso

## Requerimiento

El sistema debe permitir el CRUD de áreas de producción (nombre único, descripción opcional). No se puede eliminar un área que tenga permisos activos asociados. El histórico de accesos conserva el nombre del área aunque se renombre.

## Criterios de Aceptación

Condición 01

Dado: que el gestor/admin crea un área con un nombre nuevo

Cuando: envía el formulario

Entonces: el sistema guarda el área y retorna HTTP 201

Condición 02

Dado: que el gestor/admin crea un área con un nombre ya existente

Cuando: envía el formulario

Entonces: el sistema retorna HTTP 409 "Ya existe un área con ese nombre"

Condición 03

Dado: que el gestor/admin edita un área existente

Cuando: modifica nombre o descripción

Entonces: el sistema actualiza el área y retorna HTTP 200

Condición 04

Dado: que el gestor/admin elimina un área

Cuando: el área no tiene permisos activos asociados

Entonces: el sistema elimina el área y retorna HTTP 200

Condición 05

Dado: que el gestor/admin elimina un área

Cuando: el área tiene permisos activos asociados

Entonces: el sistema retorna HTTP 409 y no permite la eliminación

## Tareas

| No | Descripción |
|---|---|
| 1 | Implementar POST /api/permisos/areas (nombre único, máx 30) |
| 2 | Implementar PUT /api/permisos/areas/{id} |
| 3 | Implementar DELETE /api/permisos/areas/{id} (409 si hay permisos activos) |
| 4 | Frontend: CRUD de áreas (tabla + modal) en `/admin/areas` |

## Estado de Implementación

- **Backend**: ✓ — `POST/PUT/DELETE /api/permisos/areas` (gap 1.4 §9).
- **Frontend**: ✓ — `AdminAreasView` en `/admin/areas` (solo ADMIN). Tabla con conteo de áreas, descripción truncada con tooltip y modal con textos de ayuda.
- **Seed**: las áreas de producción del `DataInitializer` incluyen descripción (idempotente por nombre; actualiza la descripción si el área ya existe sin ella).
- **Flujo**: `19_flujo_gestion_areas_produccion.puml` ✓.

## Control de Versiones

| Versión | Fecha | Autor | Revisión | Descripción | Aprobador |
|---|---|---|---|---|---|
| 1.0 | 2026-08-04 | | | Versión inicial (HU nueva §9.5) | |
