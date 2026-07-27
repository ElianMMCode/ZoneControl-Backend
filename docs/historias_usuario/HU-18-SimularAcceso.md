# HU-18 - SIMULAR LECTURA DE CREDENCIAL

| Campo | Valor |
|---|---|
| **Código** | HU-18 |
| **Nombre** | Simular Lectura de Credencial |
| **Complejidad** | Alta |
| **HU Relacionada** | HU-09, HU-11 |
| **Módulo** | Módulo de Control de Acceso Físico |

## Descripción

**Yo como** personal de seguridad o empleado
**Requiero** simular la lectura de una credencial ingresando un número de identificación en un formulario
**Para** validar si el ingreso a un área restringida de producción es autorizado, denegado o si la persona no está registrada

## Requerimiento

Simulación del mecanismo de lectura de credenciales mediante un formulario que reciba el número de identificación interno y determine si el ingreso es autorizado, denegado, o si la persona no se encuentra registrada. Cada intento debe registrarse en el historial con su marca de tiempo, independientemente del resultado. Este módulo no requiere autenticación previa por ser una simulación transaccional.

## Criterios de Aceptación

Condición 01

Dado: que un empleado válido con permiso vigente ingresa su número de identificación

Cuando: el sistema valida la credencial

Entonces: el sistema muestra "INGRESO AUTORIZADO" en color verde y registra el intento en la tabla historial_accesos

Condición 02

Dado: que un empleado inactivo ingresa su número de identificación

Cuando: el sistema valida la credencial

Entonces: el sistema muestra "INGRESO DENEGADO" en color rojo y registra el intento en la tabla historial_accesos

Condición 03

Dado: que se ingresa un número de identificación que no existe en el sistema

Cuando: el sistema busca en la base de datos

Entonces: el sistema muestra "NO REGISTRADO" en color amarillo y registra el intento en la tabla historial_accesos

Condición 04

Dado: que un empleado válido sin permiso vigente (suspendido) ingresa su número de identificación

Cuando: el sistema valida la credencial

Entonces: el sistema muestra "ACCESO SUSPENDIDO" en color rojo y registra el intento en la tabla historial_accesos

## Tareas

| No | Descripción |
|---|---|
| 1 | Diseñar formulario de simulación de lectura de credencial con campo de número de identificación |
| 2 | Implementar endpoint POST /api/access/simulate en Spring Boot (sin autenticación JWT) |
| 3 | Consultar existencia del empleado por número de identificación en PostgreSQL |
| 4 | Validar estado del empleado: activo, inactivo o no registrado |
| 5 | Verificar permisos de acceso vigentes para empleados activos (fecha actual dentro del rango, estado no suspendido) |
| 6 | Registrar cada intento en la tabla historial_accesos con timestamp, resultado e identificación del empleado |
| 7 | Implementar alertas visuales en el frontend: verde (AUTORIZADO), rojo (DENEGADO/SUSPENDIDO), amarillo (NO REGISTRADO) |
| 8 | Simular activación de mecanismo de apertura cuando el resultado sea AUTORIZADO |

## Control de Versiones

| Versión | Fecha | Autor | Revisión | Descripción | Aprobador |
|---|---|---|---|---|---|
| 1.0 | 2026-07-26 | | | Versión inicial | |
