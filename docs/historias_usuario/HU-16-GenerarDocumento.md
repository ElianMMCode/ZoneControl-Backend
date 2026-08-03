# HU-16 - GENERAR DOCUMENTO DESCARGABLE

| Campo | Valor |
|---|---|
| **Código** | HU-16 |
| **Nombre** | Generar Documento Descargable |
| **Complejidad** | Media |
| **HU Relacionada** | HU-03, HU-15 |
| **Módulo** | Módulo de Reportes y Auditoría |

## Descripción

**Yo como** supervisor o auditor
**Requiero** exportar el historial de accesos consultado a un archivo descargable en formato PDF, CSV o Excel
**Para** tener documentación formal para reportes internos o auditorías externas

## Requerimiento

Generación de un documento descargable con el historial de accesos consultado (CU-09 incluye a CU-08). El documento debe incluir encabezado del reporte, fecha de generación, filtros aplicados, tabla de datos y resumen estadístico. Debe ofrecerse en formatos PDF, CSV y Excel.

## Criterios de Aceptación

Condición 01

Dado: que el supervisor ha consultado el historial con resultados

Cuando: selecciona "Exportar" y elige un formato (PDF, CSV o Excel)

Entonces: el sistema genera el archivo en el formato seleccionado, incluyendo encabezado, fecha de generación, filtros aplicados, tabla de datos y resumen estadístico, e inicia la descarga automáticamente

Condición 02

Dado: que el supervisor selecciona exportar

Cuando: el sistema muestra las opciones de formato

Entonces: se ofrecen los formatos PDF (documento formal), CSV (datos crudos) y Excel (.xlsx)

Condición 03

Dado: que la consulta de historial no retornó resultados

Cuando: el supervisor intenta exportar

Entonces: el sistema muestra el mensaje "No hay datos para exportar"

## Tareas

| No | Descripción |
|---|---|
| 1 | Implementar endpoint POST /historial/export en Spring Boot que reciba formato y filtros aplicados |
| 2 | Implementar generación de archivo PDF con librería iText o PDFBox (encabezado, fecha, filtros, tabla, resumen estadístico) |
| 3 | Implementar generación de archivo CSV con separador comma |
| 4 | Implementar generación de archivo Excel (.xlsx) con Apache POI |
| 5 | Diseñar interfaz de selección de formato (PDF, CSV, Excel) en el frontend |
| 6 | Configurar descarga automática del archivo generado en el navegador |
| 7 | Validar que existan datos antes de permitir la exportación |

## Control de Versiones

| Versión | Fecha | Autor | Revisión | Descripción | Aprobador |
|---|---|---|---|---|---|
| 1.0 | 2026-07-26 | | | Versión inicial | |
