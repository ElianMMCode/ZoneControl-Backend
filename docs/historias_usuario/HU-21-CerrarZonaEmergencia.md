# HU-21 - CERRAR ZONA POR EMERGENCIA

| Campo | Valor |
|---|---|
| **Código** | HU-21 |
| **Nombre** | Cerrar Zona por Emergencia |
| **Complejidad** | Media |
| **HU Relacionada** | HU-03, HU-17, HU-22 |
| **Módulo** | Módulo de Control de Acceso |
| **Rol** | Administrador, Supervisor/Auditor |

## Descripción

**Yo como** supervisor o administrador
**Requiero** cerrar una zona restringida de inmediato ante una emergencia y poder reabrirla cuando el riesgo pase
**Para** impedir al instante el ingreso de personal a una zona donde hay peligro

## Requerimiento

El sistema debe ofrecer, para cada zona del panel "Zonas en vivo", un control de dos estados: "Cerrar por emergencia" y "Reabrir zona". Al cerrar una zona, el sistema la muestra en estado de emergencia, impide toda entrada y genera una alerta automática. Al reabrirla, la zona vuelve a operar con normalidad y se genera una alerta de reapertura.

Mientras una zona está cerrada por emergencia, cualquier intento de validar la entrada de un empleado a esa zona es rechazado con el motivo "ZONA CERRADA POR EMERGENCIA", y ese intento queda registrado en el historial para su revisión posterior.

Solo el administrador y el supervisor/auditor pueden cerrar o reabrir una zona; cualquier otro rol que lo intente debe ser rechazado sin efecto.

## Criterios de Aceptación

Condición 01

Dado: que el supervisor o administrador ve el panel "Zonas en vivo"

Cuando: pulsa "Cerrar por emergencia" en una zona

Entonces: el sistema marca la zona en estado de emergencia, la muestra con ese estado en el panel y crea una alerta de emergencia de zona

Condición 02

Dado: que una zona está cerrada por emergencia

Cuando: se intenta validar la entrada de un empleado a esa zona

Entonces: el sistema rechaza el acceso con el motivo "ZONA CERRADA POR EMERGENCIA" y registra el intento en el historial

Condición 03

Dado: que una zona está cerrada por emergencia

Cuando: el supervisor o administrador pulsa "Reabrir zona"

Entonces: el sistema vuelve la zona al estado operativo, el panel la muestra operativa y crea una alerta de reapertura; desde ese momento las validaciones de entrada vuelven a su flujo normal

Condición 04

Dado: que un rol sin permiso para gestionar emergencias intenta cerrar o reabrir una zona

Cuando: ejecuta la acción

Entonces: el sistema lo rechaza y la zona no cambia de estado

Condición 05

Dado: que una zona cerrada por emergencia recibe intentos de entrada de varios empleados

Cuando: se revisa el historial de validaciones

Entonces: cada intento aparece registrado con el motivo "ZONA CERRADA POR EMERGENCIA", permitiendo identificar qué personas intentaron entrar y a qué hora

## Tareas

| No | Descripción |
|---|---|
| 1 | Marcar una zona como cerrada por emergencia y poder reabrirla |
| 2 | Bloquear toda validación de entrada a una zona cerrada, registrando el intento con el motivo correspondiente |
| 3 | Generar alerta automática al cerrar y al reabrir una zona por emergencia |
| 4 | Restringir la acción de cierre/reapertura a los roles autorizados |
| 5 | Mostrar el estado de emergencia en la tarjeta de la zona del panel en vivo |

## Control de Versiones

| Versión | Fecha | Autor | Revisión | Descripción | Aprobador |
|---|---|---|---|---|---|
| 1.0 | 2026-08-06 | | | Revisión de lenguaje y criterios detallados | |

## Estado de Implementación

- **Backend**: ✓ — campo `emergencyClosed` en `ProductionArea`, `POST /api/access/zones/{name}/emergency`, bloqueo de validación (`DENIED` "ZONA CERRADA POR EMERGENCIA") y alerta (2.2 §9). Tests en `AccessMonitoringControllerTest`.
- **Frontend**: ✓ — toggle "Cerrar por emergencia"/"Reabrir zona" por zona en `/supervisor/zones`.
