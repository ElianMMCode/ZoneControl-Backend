# HU-09 - Carga Masiva de Personal

| Campo | Valor |
|---|---|
| **Código** | HU-09 |
| **Nombre** | Carga Masiva de Personal |
| **Complejidad** | Alta |
| **HU Relacionada** | HU-03, HU-08 |
| **Módulo** | Módulo de Gestión de Personal |
| **Rol** | Gestor de personal |

## Descripción

**Yo como** gestor de personal
**Requiero** registrar a varios empleados a la vez cargando un archivo de texto con los datos de todos ellos
**Para** ahorrar tiempo y evitar errores al incorporar grupos grandes de personal, garantizando que la información quede completa y consistente desde el primer momento

## Requerimiento

El sistema debe permitir cargar muchos empleados de una sola vez mediante un archivo de texto plano (CSV o TXT). Para que el usuario lo llene correctamente, el sistema ofrece una plantilla descargable que muestra las columnas esperadas y un ejemplo de cómo completarlas. Las columnas son: tipo de documento, número de documento, nombres, apellidos, cargo, departamento, estado y fecha de ingreso.

Antes de registrar a cualquier empleado, el sistema revisa el archivo completo y cada fila por separado. Si una fila tiene errores (por ejemplo, un tipo de documento no admitido, un número de documento que ya existe, un departamento que no existe o un cargo que no está en el catálogo de cargos), esa fila se rechaza, pero el proceso no se detiene: las filas correctas sí se registran.

Al terminar, se presenta un resumen con la cantidad total de filas, cuántas se registraron y cuántas fallaron, además de una lista detallada de los errores para que se puedan corregir y volver a subir el archivo. A cada empleado registrado se le asigna su código interno de forma automática. El archivo tiene límites de tamaño y de cantidad de filas para evitar cargas excesivas.

## Criterios de Aceptación

Condición 01

Dado: que el gestor de personal está dentro del sistema y abre la sección "Carga Masiva"

Cuando: presiona el botón "Descargar Plantilla"

Entonces: el sistema descarga un archivo llamado "plantilla_carga_masiva_personal.csv" cuya primera fila contiene las columnas esperadas (tipo de documento, número de documento, nombres, apellidos, cargo, departamento, estado y fecha de ingreso) e incluye una segunda fila con datos de ejemplo para orientar el llenado correcto del archivo

Condición 02

Dado: que el gestor selecciona un archivo CSV o TXT

Cuando: el archivo tiene la extensión permitida y sus columnas coinciden con las de la plantilla

Entonces: el sistema revisa cada fila, registra todos los empleados que cumplen las reglas, les asigna su código interno automáticamente y muestra el resumen del proceso

Condición 03

Dado: que el archivo contiene filas con datos inválidos

Cuando: el sistema revisa cada fila una por una

Entonces: rechaza únicamente las filas con problemas e indica el motivo: tipo de documento no admitido (solo se aceptan CC, CE, TI, PA o RC), número de documento repetido dentro del mismo archivo o ya existente en el sistema, campos obligatorios incompletos o departamento que no existe. Las filas correctas se registran igualmente

Condición 04

Dado: que termina el procesamiento del archivo

Cuando: hubo filas correctas e incorrectas

Entonces: el sistema muestra un resumen con el total de filas procesadas, la cantidad de registros creados y la cantidad de errores, junto con una tabla que detalla cada error indicando la fila, el campo y el motivo, de modo que el gestor pueda corregirlos y volver a subir el archivo

Condición 05

Dado: que el gestor intenta cargar un archivo

Cuando: el archivo pesa más de 10 MB o contiene más de 1000 filas

Entonces: el sistema rechaza la carga y muestra un mensaje indicando que se debe dividir el archivo en partes más pequeñas

Condición 06

Dado: que el gestor sube un archivo con formato incorrecto

Cuando: la extensión no es CSV ni TXT, la cantidad de columnas no coincide o los encabezados de la primera fila no son los esperados

Entonces: el sistema muestra un mensaje claro que explica el problema detectado y sugiere descargar la plantilla para ver el formato admitido

## Tareas

| No | Descripción |
|---|---|
| 1 | Diseñar la pantalla de carga masiva con el selector de archivo, el botón "Descargar Plantilla" y el botón "Subir Archivo" |
| 2 | Crear la plantilla descargable con los encabezados correctos y una fila de ejemplo |
| 3 | Implementar el proceso de subida y procesamiento del archivo |
| 4 | Validar la extensión del archivo (solo CSV y TXT) y rechazar otros formatos con un mensaje claro |
| 5 | Validar que la estructura de columnas y los encabezados coincidan con la plantilla |
| 6 | Validar cada fila: tipo de documento admitido (CC, CE, TI, PA, RC), documento no repetido dentro del archivo ni existente en el sistema, campos obligatorios completos y departamento existente |
| 7 | Asignar automáticamente el código interno a cada empleado válido y guardar los registros |
| 8 | Construir el resumen con total, registrados y errores, y la tabla de errores por fila con campo y motivo |
| 9 | Aplicar el límite de tamaño (10 MB) y de cantidad (1000 registros) por archivo |

## Control de Versiones

| Versión | Fecha | Autor | Revisión | Descripción | Aprobador |
|---|---|---|---|---|---|
| 1.0 | 2026-07-26 | | | Versión inicial | |
| 1.1 | 2026-08-06 | | | Revisión de lenguaje y criterios detallados | |
| 1.2 | 2026-08-06 | | | El cargo de cada fila debe existir en el catálogo de cargos (HU-29) | |

## Estado de Implementación

- **Backend**: ✓ — `GET /api/personal/bulk/plantilla` (CSV con encabezados de 8 columnas —incluye `fecha_ingreso`— y fila de ejemplo) y `POST /api/personal/bulk` (validación por fila que incluye que el cargo exista en el catálogo, inserción por lotes y reporte de errores en `errorReportUrl` como CSV inline). Límite de **10 MB explícito** en el servicio (mensaje de la HU) y de **1000 registros**. Tests verdes.
- **Frontend**: ✓ — `BulkUploadView` (`/personal/carga-masiva`, mockup 10) con descarga de plantilla, subida y **tabla de errores inline** (Fila/Campo/Detalle) además del reporte descargable.
