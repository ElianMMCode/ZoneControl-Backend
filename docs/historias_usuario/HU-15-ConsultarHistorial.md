# HU-15 - CONSULTAR HISTORIAL DE ACCESOS

| Campo | Valor |
|---|---|
| **Código** | HU-15 |
| **Nombre** | Consultar Historial de Accesos |
| **Complejidad** | Alta |
| **HU Relacionada** | HU-03, HU-18 |
| **Módulo** | Módulo de Reportes y Auditoría |

## Descripción

**Yo como** supervisor o auditor
**Requiero** consultar el historial de accesos por rango de fechas, con filtros opcionales por empleado, departamento o resultado
**Para** monitorear y auditar los accesos realizados en las áreas restringidas de producción

## Requerimiento

Consulta del historial de accesos de una persona específica, con posibilidad de acotar la consulta a un rango de fechas. El sistema debe permitir filtrar adicionalmente por departamento y por resultado del acceso (autorizado, denegado, no registrado, suspendido). Los resultados deben mostrarse en una tabla con opción de exportación.

## Criterios de Aceptación

Condición 01

Dado: que el supervisor está autenticado

Cuando: selecciona un rango de fechas válido y presiona "Consultar"

Entonces: el sistema consulta el historial en PostgreSQL y muestra los registros en una tabla con columnas: fecha, hora, identificación, empleado, departamento y resultado

Condición 02

Dado: que el supervisor aplica filtros adicionales (empleado, departamento, resultado)

Cuando: ejecuta la consulta

Entonces: el sistema muestra solo los registros que cumplen todos los criterios combinados

Condición 03

Dado: que el supervisor selecciona un rango de fechas

Cuando: la fecha de inicio es posterior a la fecha de fin

Entonces: el sistema muestra el mensaje "Rango de fechas inválido"

Condición 04

Dado: que el supervisor consulta un período

Cuando: no hay accesos registrados para los filtros seleccionados

Entonces: el sistema muestra el mensaje "No hay registros para el período seleccionado"

## Tareas

| No | Descripción |
|---|---|
| 1 | Diseñar formulario de consulta con date pickers de fecha inicio y fecha fin, y filtros opcionales (empleado, departamento, resultado) |
| 2 | Implementar endpoint GET /api/historial con query parameters en Spring Boot |
| 3 | Validar que la fecha de inicio no sea posterior a la fecha de fin |
| 4 | Construir consulta SQL dinámica con filtros opcionales aplicando lógica AND |
| 5 | Mostrar resultados en tabla paginada con columnas: fecha, hora, ID, empleado, departamento, resultado |
| 6 | Agregar opción de exportación desde la vista de resultados (conecta con HU-16) |

## Estado de Implementación

- **Backend**: ✓ — `GET /api/historial` (fechaInicio/fechaFin obligatorias + `employeeCode`, `resultado` opcionales, paginado). Tests verdes.
- **Frontend**: ✓ — `ReportsView` (`/supervisor/reportes`, mockup 37) con KPIs, filtros de fecha/empleado/resultado, tabla paginada y export CSV/Excel.
- **Notas**: el filtro por departamento no está disponible (gap backend §8).

## Control de Versiones

| Versión | Fecha | Autor | Revisión | Descripción | Aprobador |
|---|---|---|---|---|---|
| 1.0 | 2026-07-26 | | | Versión inicial | |
