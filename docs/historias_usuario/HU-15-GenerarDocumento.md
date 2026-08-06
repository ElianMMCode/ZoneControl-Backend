# HU-15 - Generar Documento Descargable

| Campo | Valor |
|---|---|
| **Código** | HU-15 |
| **Nombre** | Generar Documento Descargable |
| **Complejidad** | Media |
| **HU Relacionada** | HU-03, HU-14 |
| **Módulo** | Módulo de Reportes y Auditoría |
| **Rol** | Supervisor o auditor |

## Descripción

**Yo como** supervisor o auditor
**Requiero** exportar el historial de accesos consultado a un documento descargable en PDF, Excel o CSV
**Para** contar con documentación formal para reportes internos o auditorías externas

## Requerimiento

El sistema debe permitir al supervisor o auditor exportar el historial de accesos que acaba de consultar a un documento descargable, conservando los mismos filtros aplicados en la consulta. Hay tres formatos disponibles: PDF, Excel y CSV.

El documento incluye el encabezado del reporte, el período consultado, los filtros aplicados, la tabla de datos completa y un resumen de los resultados. El archivo CSV utiliza el punto y coma como separador entre columnas, para que los datos sean legibles en programas de hoja de cálculo.

Si la consulta no devolvió resultados, no se puede generar el documento: el sistema lo indica y no crea un archivo vacío.

## Criterios de Aceptación

Condición 01

Dado: que el supervisor ha consultado el historial y obtuvo resultados

Cuando: elige un formato (PDF, Excel o CSV) y solicita la exportación

Entonces: el sistema genera el documento con el encabezado, el período, los filtros aplicados, la tabla de datos y el resumen de resultados, y la descarga comienza automáticamente

Condición 02

Dado: que el supervisor va a exportar el historial

Cuando: elige el formato

Entonces: puede escoger entre PDF (documento formal), Excel (hoja de cálculo) y CSV (datos en texto con punto y coma como separador)

Condición 03

Dado: que el supervisor exporta el historial

Cuando: ya aplicó filtros en la consulta previa

Entonces: el documento incluye únicamente los registros que cumplen esos filtros y el propio documento indica los filtros aplicados

Condición 04

Dado: que la consulta de historial no devolvió resultados

Cuando: el supervisor intenta exportar

Entonces: el sistema muestra el mensaje "No hay datos para exportar" y no genera ningún archivo

## Tareas

| No | Descripción |
|---|---|
| 1 | Implementar el proceso de exportación del historial que reciba el formato elegido y los filtros aplicados |
| 2 | Generar el documento PDF con encabezado, período, filtros, tabla de datos y resumen de resultados |
| 3 | Generar el archivo Excel con la misma información |
| 4 | Generar el archivo CSV con punto y coma como separador |
| 5 | Diseñar en la pantalla la selección de formato (PDF, Excel, CSV) con descarga automática |
| 6 | Validar que existan datos antes de permitir la exportación |

## Control de Versiones

| Versión | Fecha | Autor | Revisión | Descripción | Aprobador |
|---|---|---|---|---|---|
| 1.0 | 2026-07-26 | | | Versión inicial | |
| 1.1 | 2026-08-04 | | | Criterio nuevo: formato PDF (gap 1.1 §9) | |
| 1.2 | 2026-08-06 | | | Revisión de lenguaje y criterios detallados | |

## Estado de Implementación

- **Backend**: ✓ — `POST /api/historial/export` (CSV con separador `;`, EXCEL y **PDF** con `PdfExporter`/itextpdf 5; retorna `byte[]` con `Content-Disposition`). `ExportRequest` aplica los filtros de la consulta (`departmentName`, `productionAreaName`). Tests en `HistoryControllerTest` (PDF no vacío, formato inválido 400).
- **Frontend**: ✓ — botones CSV/Excel/**PDF** en `ReportsView` (`/supervisor/reportes`, mockup 37) con descarga automática.
