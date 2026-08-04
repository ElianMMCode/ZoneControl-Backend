# HU-27 - CONSULTAR MATRIZ DE ROLES Y PERMISOS

| Campo | Valor |
|---|---|
| **Código** | HU-27 |
| **Nombre** | Consultar Matriz de Roles y Permisos |
| **Complejidad** | Baja |
| **HU Relacionada** | HU-03 |
| **Módulo** | Módulo de Administración |

## Descripción

**Yo como** administrador del sistema
**Requiero** consultar la matriz de permisos por módulo y rol
**Para** conocer qué acciones puede realizar cada rol del sistema

## Requerimiento

El sistema debe permitir al administrador consultar una matriz módulo × rol → booleano. Es **solo lectura**: los roles son fijos en `SecurityConfig` y no hay edición ni enforcement en BD. La matriz se reconstruye a partir de las reglas de acceso reales.

## Criterios de Aceptación

Condición 01

Dado: que el administrador consulta la matriz

Cuando: accede a la vista de roles y permisos

Entonces: el sistema muestra la matriz con los 3 roles (ADMIN, GESTOR_PERSONAL, SUPERVISOR_AUDITOR) y los módulos del sistema

Condición 02

Dado: que un usuario sin rol ADMIN consulta la matriz

Cuando: llama a GET /api/admin/role-matrix

Entonces: el sistema retorna HTTP 403

## Tareas

| No | Descripción |
|---|---|
| 1 | Implementar GET /api/admin/role-matrix (solo ADMIN) |
| 2 | Reconstruir la matriz desde las reglas de SecurityConfig |
| 3 | Frontend: vista de matriz de solo lectura consumiendo el endpoint |

## Estado de Implementación

- **Backend**: ✓ — `GET /api/admin/role-matrix` (gap 1.5 §9). Test: `RoleMatrixControllerTest`.
- **Frontend**: ✓ — `RoleMatrixView` en `/admin/matriz-roles` consume el endpoint (con fallback estático).

## Control de Versiones

| Versión | Fecha | Autor | Revisión | Descripción | Aprobador |
|---|---|---|---|---|---|
| 1.0 | 2026-08-04 | | | Versión inicial (HU nueva §9.5) | |
