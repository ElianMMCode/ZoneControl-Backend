# HU-21 - CONSULTAR OCUPACIÓN EN TIEMPO REAL

| Campo | Valor |
|---|---|
| **Código** | HU-21 |
| **Nombre** | Consultar Ocupación en Tiempo Real |
| **Complejidad** | Media |
| **HU Relacionada** | HU-03, HU-18 |
| **Módulo** | Módulo de Control de Acceso |

## Descripción

**Yo como** supervisor / administrador
**Requiero** consultar quién está dentro de cada zona restringida en tiempo real
**Para** vigilar la ocupación y detectar irregularidades

## Requerimiento

El sistema debe mantener una sesión de ocupación por empleado+zona que se abre al validar un acceso autorizado y se cierra al registrar la salida. Permite consultar el aforo por área y registrar salidas. Roles: ADMIN y SUPERVISOR_AUDITOR (sin rol SEGURIDAD, decisión 2026-08-04).

## Criterios de Aceptación

Condición 01

Dado: que un acceso es validado como autorizado

Cuando: el empleado ingresa a la zona

Entonces: el sistema abre una sesión de ocupación para ese empleado+zona

Condición 02

Dado: que el supervisor registra la salida de un empleado

Cuando: envía POST /api/access/exit

Entonces: el sistema cierra la sesión activa; si no existe sesión activa responde HTTP 400

Condición 03

Dado: que un empleado entra dos veces a la misma zona

Cuando: el segundo acceso es autorizado

Entonces: el sistema cierra la sesión anterior y abre una nueva

Condición 04

Dado: que el supervisor consulta la ocupación

Cuando: llama a GET /api/access/occupancy

Entonces: el sistema devuelve el aforo y las personas por área

## Tareas

| No | Descripción |
|---|---|
| 1 | Entidad AccessSession + repositorio |
| 2 | Abrir/cerrar sesión en la validación (2.1 §9) |
| 3 | POST /api/access/exit y GET /api/access/occupancy |

## Estado de Implementación

- **Backend**: ✓ — sesiones, salida y ocupación (2.1 §9). Tests en `AccessMonitoringControllerTest`.
- **Frontend**: ✓ — panel de zonas en `/supervisor/zones`.

## Control de Versiones

| Versión | Fecha | Autor | Revisión | Descripción | Aprobador |
|---|---|---|---|---|---|
| 1.0 | 2026-08-04 | | | Versión inicial (HU nueva §9.5) | |
