# HU-17 - Validar Acceso Físico

| Campo | Valor |
|---|---|
| **Código** | HU-17 |
| **Nombre** | Validar Acceso Físico |
| **Complejidad** | Alta |
| **HU Relacionada** | HU-08, HU-10 |
| **Módulo** | Módulo de Control de Acceso Físico |
| **Rol** | Público (modo autoservicio, sin inicio de sesión) |

## Descripción

**Yo como** persona que llega a una zona restringida
**Requiero** validar mi ingreso usando mi código de empleado desde una vista pública de autoservicio
**Para** registrar mi entrada o salida y conocer de inmediato si estoy autorizado, denegado, si no estoy registrado o si mi permiso está suspendido

## Requerimiento

El sistema debe permitir a cualquier persona validar su ingreso a un área restringida desde la vista pública `/validar`, sin iniciar sesión. La pantalla muestra todas las zonas como tarjetas; al seleccionar una, se habilita el formulario con el código del empleado y las pestañas "Entrada" y "Salida".

Al validar una entrada, el sistema revisa en orden: si el área está en emergencia (de ser así, el ingreso se deniega por emergencia); si el empleado existe (si no existe, el resultado es "no registrado"); si el empleado está activo (si no lo está, se deniega); y si tiene un permiso vigente para esa área, dentro del horario y del día correspondiente (si no, el resultado es "suspendido"). Si todo está correcto, el acceso se autoriza y se abre la sesión del empleado en el área.

Cada intento queda registrado en el historial con la fecha y hora y con su resultado, sin importar el desenlace. La respuesta muestra los datos del empleado (código, nombre, cargo y departamento). Después de cada validación, el campo del código se limpia para evitar reenvíos accidentales.

## Criterios de Aceptación

Condición 01

Dado: que un empleado activo tiene un permiso vigente para el área, dentro del horario y del día correspondientes, y el área no está en emergencia

Cuando: se valida su código en la pestaña de entrada

Entonces: el sistema muestra "Ingreso autorizado" en verde junto con los datos del empleado (código, nombre, cargo y departamento), abre la sesión en el área y registra el intento en el historial

Condición 02

Dado: que el área está en estado de emergencia

Cuando: se intenta validar el ingreso de cualquier persona a esa área

Entonces: el sistema deniega el acceso indicando que es por emergencia y registra el intento con su resultado

Condición 03

Dado: que se ingresa un código que no corresponde a ningún empleado del sistema

Cuando: se valida el acceso

Entonces: el sistema muestra "No registrado" en color amarillo y registra el intento con su resultado

Condición 04

Dado: que el empleado existe pero no está activo

Cuando: se valida su acceso

Entonces: el sistema deniega el ingreso y registra el intento con su resultado

Condición 05

Dado: que el empleado está activo pero no tiene un permiso vigente para el área, o se encuentra fuera del horario o del día permitido

Cuando: se valida su acceso

Entonces: el sistema muestra "Acceso suspendido" y registra el intento con su resultado

Condición 06

Dado: que se realiza cualquier validación, con cualquier resultado

Cuando: se obtiene el desenlace

Entonces: el intento queda registrado en el historial con la fecha y hora, el área, el empleado y el resultado, ya sea autorizado, denegado, no registrado o suspendido

Condición 07

Dado: que termina una validación

Cuando: se muestra el resultado

Entonces: el campo del código queda vacío y listo para el siguiente intento, evitando reenvíos accidentales

## Tareas

| No | Descripción |
|---|---|
| 1 | Diseñar la pantalla de validación con pestañas "Entrada" y "Salida", selector de área y campo de código del empleado |
| 2 | Implementar el proceso de validación de entrada |
| 3 | Implementar el proceso de validación de salida (HU-25) |
| 4 | Verificar, en orden: emergencia del área, existencia del empleado, estado activo y permiso vigente con horario y día |
| 5 | Registrar cada intento en el historial con fecha, hora, área, empleado y resultado |
| 6 | Mostrar el resultado con colores (verde autorizado, rojo denegado o suspendido, amarillo no registrado) junto con los datos del empleado |
| 7 | Limpiar el campo de código después de cada validación |

## Control de Versiones

| Versión | Fecha | Autor | Revisión | Descripción | Aprobador |
|---|---|---|---|---|---|
| 1.0 | 2026-07-26 | | | Versión inicial | |
| 1.1 | 2026-07-29 | | | Renombre: simulación → validación, actor cambia a supervisor/auditor | |
| 1.2 | 2026-08-05 | | | Zonas dinámicas: el selector de áreas deja de usar una lista fija y refleja las áreas reales | |
| 1.3 | 2026-08-06 | | | Revisión de lenguaje y criterios detallados (emergencia, horario/día, limpieza del campo) | |
| 1.4 | 2026-08-23 | | | Modo autoservicio público: vista /validar sin login con tarjetas por zona (GET /api/public/zonas); se retira /supervisor/validar; el supervisor deja de operar la validación directamente y recibe reportes internos en Reportes | |

## Estado de Implementación

- **Backend**: ✓ — `POST /api/access/validate` (AUTHORIZED / DENIED / UNREGISTERED / SUSPENDED) con apertura de sesión (`AccessSession`), registro en `AccessHistory`, zona en emergencia (`emergencyClosed`), horario/día (`PermissionSchedule`) y resultado `EXIT` vía `POST /api/access/exit`. Ambos endpoints abiertos a `permitAll()` para el modo autoservicio. `GET /api/public/zonas` lista las zonas activas sin autenticación. Tests verdes.
- **Frontend**: ✓ — vista pública `ValidateAccessView` en `/validar` (sin login, enlazada desde el navbar del landing): tarjetas por zona con indicador de emergencia, pestañas **Entrada**/**Salida**, resultado visual por color con datos del empleado y limpieza del código tras cada intento. Se retiró la ruta interna `/supervisor/validar`; el supervisor conserva la auditoría mediante el "Reporte interno de validaciones de acceso" en `/supervisor/reportes`.
- **Notas**: el supervisor ya no opera la validación directamente; cualquier persona registra su entrada/salida en modo autoservicio y cada intento queda auditado en el historial.
