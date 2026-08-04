# HU-23 - ALERTAS DE ANOMALÍAS DE ACCESO

| Campo | Valor |
|---|---|
| **Código** | HU-23 |
| **Nombre** | Alertas de Anomalías de Acceso |
| **Complejidad** | Media |
| **HU Relacionada** | HU-18, HU-22 |
| **Módulo** | Módulo de Control de Acceso |

## Descripción

**Yo como** sistema
**Requiero** generar alertas cuando se detectan anomalías de acceso
**Para** que el supervisor/auditor pueda actuar sobre patrones de riesgo

## Requerimiento

El sistema detecta y persiste alertas on-write durante la validación de acceso: ≥3 denegaciones del mismo empleado en 15 min, acceso autorizado entre 00:00-05:00, y cierre/reapertura de zona por emergencia. Las alertas se emiten por SSE y se pueden consultar. Roles: ADMIN y SUPERVISOR_AUDITOR.

## Criterios de Aceptación

Condición 01

Dado: que un empleado acumula ≥3 denegaciones en 15 minutos

Cuando: se registra la tercera denegación

Entonces: el sistema crea una alerta DENEGACIONES_REPETIDAS

Condición 02

Dado: que un acceso es autorizado entre 00:00 y 05:00

Cuando: se registra el acceso

Entonces: el sistema crea una alerta ACCESO_NOCTURNO de severidad baja

Condición 03

Dado: que una zona se cierra o reabre por emergencia

Cuando: se ejecuta el toggle

Entonces: el sistema crea una alerta ZONA_EMERGENCIA

Condición 04

Dado: que el supervisor consulta las alertas

Cuando: llama a GET /api/access/alerts

Entonces: el sistema devuelve las alertas ordenadas por fecha

## Tareas

| No | Descripción |
|---|---|
| 1 | Entidad AccessAlert (tipo, severidad, mensaje) |
| 2 | Detección on-write en la validación (2.4 §9) |
| 3 | GET /api/access/alerts + emisión SSE alert.created |

## Estado de Implementación

- **Backend**: ✓ — detección, persistencia y consulta de alertas (2.4 §9). Tests en `AccessMonitoringControllerTest`.
- **Frontend**: ✓ — panel de alertas en `/supervisor/zones`.

## Control de Versiones

| Versión | Fecha | Autor | Revisión | Descripción | Aprobador |
|---|---|---|---|---|---|
| 1.0 | 2026-08-04 | | | Versión inicial (HU nueva §9.5) | |
