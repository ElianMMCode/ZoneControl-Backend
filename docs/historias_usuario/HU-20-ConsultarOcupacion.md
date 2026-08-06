# HU-20 - CONSULTAR OCUPACIÓN EN TIEMPO REAL

| Campo | Valor |
|---|---|
| **Código** | HU-20 |
| **Nombre** | Consultar Ocupación en Tiempo Real |
| **Complejidad** | Media |
| **HU Relacionada** | HU-03, HU-17, HU-25, HU-27 |
| **Módulo** | Módulo de Control de Acceso |
| **Rol** | Administrador, Supervisor/Auditor |

## Descripción

**Yo como** supervisor o administrador
**Requiero** ver, en tiempo real, cuántas personas hay dentro de cada zona restringida y quiénes son
**Para** vigilar la ocupación y detectar irregularidades a tiempo

## Requerimiento

El sistema debe mostrar un panel "Zonas en vivo" que se actualiza automáticamente, sin que el usuario tenga que recargar la página. El panel presenta cada zona del laboratorio con su estado (operativa o en emergencia) y su aforo total, es decir, el número de personas que hay dentro en ese momento.

Desde cada zona se puede abrir un detalle que muestra quiénes están dentro y desde qué hora ingresaron, junto con dos pestañas de consulta: los empleados asignados a esa zona y las autorizaciones vigentes con su horario.

El panel también incluye una tabla de "Validaciones recientes" con el historial de entradas y salidas de los últimos 30 días, que se puede filtrar por área y por resultado (autorizado, denegado, etc.), mostrando el empleado en cada fila. La entrada de una persona se registra cuando se valida su credencial, y la salida cuando se registra su egreso.

## Criterios de Aceptación

Condición 01

Dado: que el supervisor o administrador abre el panel "Zonas en vivo"

Cuando: se produce un ingreso o una salida en cualquier zona

Entonces: el panel se actualiza por sí solo y muestra en cada tarjeta de zona el aforo total (número de personas dentro) y el estado de la zona (operativa o en emergencia)

Condición 02

Dado: que un empleado ingresa a una zona con una validación autorizada

Cuando: se registra la entrada

Entonces: el sistema abre el registro de ocupación de ese empleado en esa zona y el aforo de la tarjeta aumenta en uno

Condición 03

Dado: que el supervisor o administrador registra la salida de un empleado de una zona

Cuando: se confirma la salida

Entonces: el sistema cierra el registro de ocupación del empleado en esa zona y el aforo disminuye en uno; si el empleado no tenía una ocupación registrada, el sistema informa que no se puede registrar la salida

Condición 04

Dado: que un empleado ingresa dos veces a la misma zona

Cuando: se valida el segundo ingreso como autorizado

Entonces: el sistema da por cerrado el registro anterior y abre uno nuevo, de modo que la persona solo cuenta una vez en el aforo

Condición 05

Dado: que el supervisor o administrador abre el detalle de una zona

Cuando: pulsa el botón "Personal / Autorizaciones" de la zona

Entonces: el sistema muestra la ocupación actual detallada (quiénes están dentro y desde qué hora) y las pestañas "Empleados asignados" y "Autorizaciones"

Condición 06

Dado: que el supervisor o administrador consulta la tabla de validaciones recientes

Cuando: aplica un filtro por área o por resultado, o revisa el feed en vivo

Entonces: el sistema muestra los registros de los últimos 30 días correspondientes al filtro, incluyendo la columna de empleado, y los nuevos eventos aparecen sin recargar la página

## Tareas

| No | Descripción |
|---|---|
| 1 | Registro de ocupación por empleado y zona que se abre al validar un ingreso y se cierra al registrar la salida |
| 2 | Gestión de ingresos repetidos (cerrar el registro anterior y abrir uno nuevo) |
| 3 | Consulta del aforo total y del detalle de personas dentro por zona |
| 4 | Registro de salida con control del caso en que no exista ocupación activa |
| 5 | Panel "Zonas en vivo" con actualización automática y detalle por zona |
| 6 | Tabla de validaciones recientes con filtros de área y resultado sobre el feed en vivo y el historial de 30 días |

## Control de Versiones

| Versión | Fecha | Autor | Revisión | Descripción | Aprobador |
|---|---|---|---|---|---|
| 1.0 | 2026-08-06 | | | Revisión de lenguaje y criterios detallados | |

## Estado de Implementación

- **Backend**: ✓ — entidad `AccessSession`, apertura/cierre en `POST /api/access/validate` y `POST /api/access/exit`, `GET /api/access/occupancy`, SSE `RealtimeEventPublisher` (2.1 §9). Tests en `AccessMonitoringControllerTest`.
- **Frontend**: ✓ — panel de zonas en `/supervisor/zones` (roles ADMIN y SUPERVISOR_AUDITOR). Entrada vía `/supervisor/validar` (HU-17) y salida vía pestaña Salida (HU-25).
