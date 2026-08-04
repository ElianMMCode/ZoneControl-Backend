# HU-22 - CERRAR ZONA POR EMERGENCIA

| Campo | Valor |
|---|---|
| **Código** | HU-22 |
| **Nombre** | Cerrar Zona por Emergencia |
| **Complejidad** | Media |
| **HU Relacionada** | HU-03, HU-18 |
| **Módulo** | Módulo de Control de Acceso |

## Descripción

**Yo como** supervisor / administrador
**Requiero** cerrar una zona restringida por emergencia
**Para** impedir inmediatamente el ingreso de personal cuando hay un riesgo

## Requerimiento

El sistema debe permitir cerrar y reabrir una zona (kill switch). Mientras está cerrada, toda validación de acceso responde DENIED "ZONA CERRADA POR EMERGENCIA", se registra en el historial y se genera una alerta. Roles: ADMIN y SUPERVISOR_AUDITOR.

## Criterios de Aceptación

Condición 01

Dado: que el supervisor cierra una zona

Cuando: envía POST /api/access/zones/{name}/emergency con {cerrada: true}

Entonces: el área queda marcada como cerrada por emergencia y se genera una alerta

Condición 02

Dado: que una zona está cerrada por emergencia

Cuando: se intenta validar un acceso a esa zona

Entonces: el sistema responde DENIED "ZONA CERRADA POR EMERGENCIA" y registra el intento en el historial

Condición 03

Dado: que la zona se reabre

Cuando: envía POST /api/access/zones/{name}/emergency con {cerrada: false}

Entonces: la validación vuelve al flujo normal

Condición 04

Dado: que un rol sin permisos intenta cerrar una zona

Cuando: llama al endpoint de emergencia

Entonces: el sistema responde HTTP 403

## Tareas

| No | Descripción |
|---|---|
| 1 | Campo emergencyClosed en ProductionArea (2.2 §9) |
| 2 | POST /api/access/zones/{name}/emergency |
| 3 | Bloquear validación en zona cerrada + alerta ZONA_EMERGENCIA |

## Estado de Implementación

- **Backend**: ✓ — kill switch, bloqueo de validación y alerta (2.2 §9). Tests en `AccessMonitoringControllerTest`.
- **Frontend**: ✓ — toggle de emergencia por zona en `/supervisor/zones`.

## Control de Versiones

| Versión | Fecha | Autor | Revisión | Descripción | Aprobador |
|---|---|---|---|---|---|
| 1.0 | 2026-08-04 | | | Versión inicial (HU nueva §9.5) | |
