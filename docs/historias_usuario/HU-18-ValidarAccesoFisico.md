# HU-18 - VALIDAR ACCESO FÍSICO

| Campo | Valor |
|---|---|
| **Código** | HU-18 |
| **Nombre** | Validar Acceso Físico |
| **Complejidad** | Alta |
| **HU Relacionada** | HU-09, HU-11 |
| **Módulo** | Módulo de Control de Acceso Físico |

## Descripción

**Yo como** supervisor o auditor
**Requiero** validar el acceso a áreas restringidas ingresando el código de un empleado
**Para** determinar si el ingreso a un área restringida de producción es autorizado, denegado o si la persona no está registrada

## Requerimiento

Mecanismo de validación de acceso que recibe el código interno del empleado y determina si el ingreso es autorizado, denegado, o si la persona no se encuentra registrada. Cada intento debe registrarse en el historial con su marca de tiempo, independientemente del resultado.

## Criterios de Aceptación

Condición 01

Dado: que un empleado válido con permiso vigente ingresa su código

Cuando: el sistema valida el acceso

Entonces: el sistema muestra "INGRESO AUTORIZADO" en color verde y registra el intento en la tabla historial_accesos

Condición 02

Dado: que un empleado inactivo ingresa su código

Cuando: el sistema valida el acceso

Entonces: el sistema muestra "INGRESO DENEGADO" en color rojo y registra el intento en la tabla historial_accesos

Condición 03

Dado: que se ingresa un código que no existe en el sistema

Cuando: el sistema busca en la base de datos

Entonces: el sistema muestra "NO REGISTRADO" en color amarillo y registra el intento en la tabla historial_accesos

Condición 04

Dado: que un empleado válido sin permiso vigente (suspendido) ingresa su código

Cuando: el sistema valida el acceso

Entonces: el sistema muestra "ACCESO SUSPENDIDO" en color rojo y registra el intento en la tabla historial_accesos

## Tareas

| No | Descripción |
|---|---|
| 1 | Diseñar formulario de validación de acceso con campo de código de empleado |
| 2 | Implementar endpoint POST /api/access/validate en Spring Boot (con autenticación JWT) |
| 3 | Consultar existencia del empleado por código en PostgreSQL |
| 4 | Validar estado del empleado: activo, inactivo o no registrado |
| 5 | Verificar permisos de acceso vigentes para empleados activos (fecha actual dentro del rango, estado no suspendido) |
| 6 | Registrar cada intento en la tabla historial_accesos con timestamp, resultado e identificación del empleado |
| 7 | Implementar alertas visuales en el frontend: verde (AUTORIZADO), rojo (DENEGADO/SUSPENDIDO), amarillo (NO REGISTRADO) |
| 8 | Simular activación de mecanismo de apertura cuando el resultado sea AUTORIZADO |

## Estado de Implementación

- **Backend**: ✓ — `POST /api/access/validate` (AUTHORIZED / DENIED / UNREGISTERED / SUSPENDED) con registro en `AccessHistory`. Tests verdes.
- **Frontend**: ✓ — `AccessValidationView` (`/supervisor/validar`, mockup 44) con selector de zona **cargado dinámicamente desde `GET /api/permisos/areas`** (`useAreas`), código de empleado y alerta de color por resultado (verde autorizado / rojo denegado-suspendido / amarillo no registrado).
- **Notas**: `SecurityConfig` permite a `SUPERVISOR_AUDITOR` hacer `GET /api/permisos/areas` (regla específica antes del `hasAnyRole(ADMIN, GESTOR_PERSONAL)` de `/api/permisos/**`), por lo que las zonas reflejan el catálogo real en vez de una lista estática. Test: `supervisor_canListProductionAreas`.

## Control de Versiones

| Versión | Fecha | Autor | Revisión | Descripción | Aprobador |
|---|---|---|---|---|---|
| 1.2 | 2026-08-05 | | | Zonas dinámicas: supervisor accede a `GET /api/permisos/areas` y el frontend deja de usar la lista hardcodeada | |
| 1.1 | 2026-07-29 | | | Renombre: simulación → validación, actor cambia a supervisor/auditor, endpoint ahora requiere JWT | |
| 1.0 | 2026-07-26 | | | Versión inicial | |
