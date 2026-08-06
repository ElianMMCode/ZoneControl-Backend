# HU-25 - Registrar la Salida de un Área

| Campo | Valor |
|---|---|
| **Código** | HU-25 |
| **Nombre** | Registrar la salida de un área restringida |
| **Complejidad** | Media |
| **HU Relacionada** | HU-17 |
| **Módulo** | Módulo de Control de Acceso Físico |
| **Rol** | Supervisor / Auditor, Administrador |

## Descripción

**Yo como** supervisor o auditor
**Requiero** registrar la salida de un empleado de un área restringida
**Para** mantener la ocupación de cada zona actualizada y dejar constancia de cuándo la persona dejó el área

## Requerimiento

Cuando una persona autorizada ingresa a un área, el sistema abre una sesión de ocupación (HU-17). Para mantener los datos correctos, debe existir la opción de registrar la salida: el sistema cierra esa sesión, guarda en el historial la salida con su hora, y actualiza en tiempo real la ocupación de la zona. La salida solo se puede registrar si la persona tenía una sesión abierta en esa zona; de lo contrario, el sistema lo indica como un error.

## Criterios de Aceptación

Condición 01

Dado: que un empleado tiene una sesión abierta en una zona

Cuando: el supervisor registra su salida indicando el código del empleado y la zona

Entonces: el sistema cierra la sesión, guarda la salida en el historial con la fecha y hora, y la persona deja de contar en la ocupación de la zona

Condición 02

Dado: que el supervisor registra una salida

Cuando: el empleado indicado no tiene una sesión abierta en la zona indicada

Entonces: el sistema muestra el mensaje de que no hay una sesión activa y no registra la salida

Condición 03

Dado: que se registró la salida

Cuando: el sistema actualiza la información

Entonces: la ocupación de la zona se reduce en una persona y el panel de zonas en vivo se actualiza al instante

Condición 04

Dado: que se registró la salida

Cuando: se consulta el historial o un reporte

Entonces: la salida aparece como un registro independiente (resultado "Salida") con el empleado, la zona, la fecha y la hora

Condición 05

Dado: que la salida queda registrada en el historial

Cuando: se calculan los indicadores de "accesos del día" o se genera el archivo periódico

Entonces: las salidas no se cuentan como accesos de entrada (los indicadores y el archivo periódico reflejan solo ingresos)

## Tareas

| No | Descripción |
|---|---|
| 1 | Registrar la salida cerrando la sesión abierta del empleado en la zona |
| 2 | Guardar la salida en el historial con el resultado "Salida" |
| 3 | Actualizar la ocupación en tiempo real |
| 4 | Validar que exista una sesión abierta antes de registrar la salida |

## Control de Versiones

| Versión | Fecha | Autor | Revisión | Descripción | Aprobador |
|---|---|---|---|---|---|
| 1.0 | 2026-08-06 | | | Historia nueva: flujo de salida implementado junto a la entrada | |

## Estado de Implementación

- **Backend**: ✓ — `POST /api/access/exit` cierra la sesión y guarda el registro con resultado EXIT; la salida no suma a los indicadores de acceso ni al archivo periódico.
- **Frontend**: ✓ — pestaña "Salida" en la validación de credencial (`/supervisor/validar`).
- **Tests**: ✓ — `exit_registersExitHistory` y cierre de sesión en `AccessMonitoringControllerTest`.
