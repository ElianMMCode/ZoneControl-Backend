# HU-14 - Consultar Historial de Accesos

| Campo | Valor |
|---|---|
| **Código** | HU-14 |
| **Nombre** | Consultar Historial de Accesos |
| **Complejidad** | Alta |
| **HU Relacionada** | HU-03, HU-17 |
| **Módulo** | Módulo de Reportes y Auditoría |
| **Rol** | Supervisor o auditor, Gestor de Personal (solo consulta) |

## Descripción

**Yo como** supervisor, auditor o gestor de personal
**Requiero** consultar el historial de accesos a las áreas restringidas por un rango de fechas, con filtros opcionales por empleado, departamento, área o resultado
**Para** monitorear y auditar quién ha entrado a las áreas restringidas de producción y con qué resultado; el gestor lo utiliza además para controlar los accesos habilitados por los permisos que otorga

## Requerimiento

El sistema debe permitir al supervisor o auditor consultar el historial de accesos a las áreas restringidas. Para consultar es obligatorio indicar un rango de fechas (fecha de inicio y fecha de fin). Si la fecha de inicio es posterior a la de fin, el sistema rechaza la consulta con un mensaje claro.

Además, se pueden aplicar filtros opcionales: empleado (por su código), departamento, área de producción y resultado del acceso (autorizado, denegado, no registrado, suspendido o salida). Todos los filtros se combinan, de modo que solo se muestran los accesos que cumplen todos los criterios elegidos.

Los resultados se muestran en una tabla con la fecha, el código del empleado, el nombre del empleado, el área, el departamento y el resultado, ordenados del más reciente al más antiguo y con la opción de pasar de página. Desde esta consulta se puede generar un documento con los resultados (HU-15).

## Criterios de Aceptación

Condición 01

Dado: que el supervisor indica una fecha de inicio y una fecha de fin válidas

Cuando: ejecuta la consulta

Entonces: el sistema muestra el historial del período en una tabla con las columnas fecha, código, empleado, área, departamento y resultado

Condición 02

Dado: que el supervisor indica un rango de fechas

Cuando: la fecha de inicio es posterior a la de fin

Entonces: el sistema muestra el mensaje "Rango de fechas inválido" y no realiza la consulta

Condición 03

Dado: que el supervisor aplica filtros opcionales (empleado, departamento, área o resultado)

Cuando: ejecuta la consulta

Entonces: el sistema muestra solo los accesos que cumplen todos los filtros seleccionados

Condición 04

Dado: que el supervisor consulta un período

Cuando: no hay accesos que coincidan con los filtros

Entonces: el sistema muestra un mensaje indicando que no hay registros para el período seleccionado

Condición 05

Dado: que la consulta devuelve varios accesos

Cuando: el supervisor revisa los resultados

Entonces: el sistema los muestra del más reciente al más antiguo y los organiza en páginas

Condición 06

Dado: que el supervisor ve los resultados de la consulta

Cuando: quiere guardar o imprimir la información

Entonces: puede exportar el historial con los mismos filtros a un documento (HU-15)

## Tareas

| No | Descripción |
|---|---|
| 1 | Diseñar el formulario de consulta con selectores de fecha de inicio y fin, y filtros opcionales (empleado, departamento, área y resultado) |
| 2 | Implementar la consulta del historial por rango de fechas |
| 3 | Validar que la fecha de inicio no sea posterior a la de fin |
| 4 | Implementar los filtros opcionales combinados |
| 5 | Mostrar los resultados en una tabla paginada, ordenada del más reciente al más antiguo, con las columnas fecha, código, empleado, área, departamento y resultado |
| 6 | Conectar la vista de resultados con la exportación de documentos (HU-15) |

## Control de Versiones

| Versión | Fecha | Autor | Revisión | Descripción | Aprobador |
|---|---|---|---|---|---|
| 1.1 | 2026-08-04 | | | Criterio nuevo: filtro por departamento (gap 1.3 §9) | |
| 1.2 | 2026-08-05 | | | Filtro de departamento como dropdown (`useDepartments`) y columna Código en la tabla de historial | |
| 1.3 | 2026-08-06 | | | Revisión de lenguaje y criterios detallados (filtro por área y resultado de salida) | |
| 1.4 | 2026-08-23 | | | El gestor de personal obtiene acceso de consulta al historial (vista /personal/historial); se retira del panel del supervisor, que conserva el archivo periódico | |
| 1.5 | 2026-08-23 | | | La vista integra movimientos en tiempo real (SSE) y cada registro enlaza al detalle del empleado | |

## Estado de Implementación

- **Backend**: ✓ — `GET /api/historial` (fechaInicio/fechaFin obligatorias + `employeeCode`, `department`, `productionAreaName`, `resultado` opcionales, paginado y ordenado descendente). Acceso para ADMIN, GESTOR_PERSONAL y SUPERVISOR_AUDITOR (`SecurityConfig`). Tests en `HistoryControllerTest`.
- **Frontend**: ✓ — vista dedicada `AccessHistoryView` en `/personal/historial` (ADMIN y GESTOR_PERSONAL) con movimientos en tiempo real vía SSE (indicador de conexión), KPIs, filtros de fecha/empleado/departamento/área/resultado, tabla paginada y exportación CSV/Excel/PDF; incluye el panel de alertas de seguridad. Cada registro enlaza directamente al detalle del empleado (`employeeId` expuesto en la API y en los eventos SSE).
- **Notas**: los filtros de departamento y área son menús desplegables reales (`useDepartments` y áreas desde `GET /api/permisos/areas`); el export aplica los mismos filtros. La vista anterior dentro de `ReportsView` del supervisor fue retirada; la operación de puertas (validar/salida/emergencia) sigue siendo exclusiva del supervisor.
