# HU-23 - ALERTAS DE ANOMALÍAS DE ACCESO

| Campo | Valor |
|---|---|
| **Código** | HU-23 |
| **Nombre** | Alertas de Anomalías de Acceso |
| **Complejidad** | Media |
| **HU Relacionada** | HU-18, HU-22 |
| **Módulo** | Módulo de Control de Acceso |
| **Rol** | Administrador, Supervisor/Auditor |

## Descripción

**Yo como** administrador o supervisor/auditor
**Requiero** recibir alertas automáticas cuando el sistema detecta anomalías en los accesos
**Para** detectar patrones de riesgo y actuar a tiempo

## Requerimiento

El sistema genera automáticamente alertas en dos situaciones únicamente. La primera, "denegaciones repetidas": cuando el mismo empleado acumula tres o más intentos de acceso denegados dentro de una ventana de 15 minutos. La segunda, "emergencia de zona": cuando una zona se cierra o se reabre por emergencia. No existen otros tipos de alerta en el sistema.

Cada alerta queda guardada y se muestra tanto en el panel "Zonas en vivo" como en el panel "Alertas de seguridad" que ve el administrador. Las alertas nuevas aparecen por sí solas, sin que el usuario tenga que recargar la pantalla.

El administrador puede marcar una alerta como leída; una vez marcada, deja de contarse como pendiente. El resto del personal no modifica las alertas; solo las consulta.

## Criterios de Aceptación

Condición 01

Dado: que un mismo empleado acumula tres o más intentos de acceso denegados dentro de 15 minutos

Cuando: se registra el tercer intento denegado

Entonces: el sistema crea una alerta de "denegaciones repetidas" que identifica al empleado y el momento de la anomalía

Condición 02

Dado: que una zona se cierra o se reabre por emergencia

Cuando: se ejecuta el cierre o la reapertura

Entonces: el sistema crea una alerta de "emergencia de zona" indicando la zona y la acción realizada

Condición 03

Dado: que el administrador o supervisor/auditor revisa las alertas

Cuando: abre el panel de zonas en vivo o el panel de alertas de seguridad

Entonces: el sistema muestra las alertas ordenadas por fecha, con las más recientes primero, y las nuevas aparecen sin recargar la pantalla

Condición 04

Dado: que el administrador marca una alerta como leída

Cuando: confirma la acción sobre esa alerta

Entonces: el sistema la deja de mostrar como pendiente en los paneles correspondientes

Condición 05

Dado: que se revisa el historial del sistema

Cuando: se buscan anomalías de otro tipo (por ejemplo, accesos fuera del horario laboral)

Entonces: no se generan alertas para esas situaciones, porque el sistema solo contempla las dos anomalías definidas: denegaciones repetidas y emergencia de zona

## Tareas

| No | Descripción |
|---|---|
| 1 | Detección automática de tres o más denegaciones del mismo empleado en 15 minutos |
| 2 | Detección automática de cierre y reapertura de zona por emergencia |
| 3 | Guardado y consulta de las alertas ordenadas por fecha |
| 4 | Presentación de las alertas nuevas en los paneles sin recargar la pantalla |
| 5 | Marcar una alerta como leída por el administrador |

## Control de Versiones

| Versión | Fecha | Autor | Revisión | Descripción | Aprobador |
|---|---|---|---|---|---|
| 1.0 | 2026-08-06 | | | Revisión de lenguaje y criterios detallados | |

## Estado de Implementación

- **Backend**: ✓ — detección on-write en `POST /api/access/validate` (≥3 denegaciones en 15 min) y en el toggle de emergencia; `GET /api/access/alerts`, `PATCH /api/access/alerts/{id}/leido`, emisión SSE `alert.created` (2.4 §9). Solo tipos `DENEGACIONES_REPETIDAS` y `ZONA_EMERGENCIA`; `ACCESO_NOCTURNO` eliminado (decisión 2026-08-05). Tests en `AccessMonitoringControllerTest`.
- **Frontend**: ✓ — panel de alertas en `/supervisor/zones` y `SecurityAlertsPanel` en el dashboard del admin (`/admin/dashboard`).
