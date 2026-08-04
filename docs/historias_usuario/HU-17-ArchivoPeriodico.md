# HU-17 - GENERAR ARCHIVO PERIÓDICO PARA SOCIOS

| Campo | Valor |
|---|---|
| **Código** | HU-17 |
| **Nombre** | Generar Archivo Periódico para Socios |
| **Complejidad** | Media |
| **HU Relacionada** | HU-03, HU-15 |
| **Módulo** | Módulo de Reportes y Auditoría |

## Descripción

**Yo como** supervisor o auditor
**Requiero** generar un archivo periódico con los accesos agrupados por departamento de producción, sin incluir datos personales que permitan identificar a los empleados
**Para** compartir la información de actividad con los socios internacionales cumpliendo con la protección de datos sensibles según lo establecido en el plan de expansión

## Requerimiento

Generación periódica de un archivo de intercambio de información con el socio internacional, que resuma la actividad de acceso por departamento. El supervisor debe poder seleccionar el período (mes/año), los departamentos a incluir y el formato de salida. El archivo generado no debe contener ningún dato personal sensible ni información que permita identificar individualmente a los empleados. Solo debe incluir datos agregados por departamento: nombre del departamento, cantidad total de accesos registrados, cantidad de accesos autorizados, cantidad de accesos denegados, cantidad de accesos no registrados y cantidad de accesos suspendidos durante el período seleccionado.

## Criterios de Aceptación

Condición 01

Dado: que el supervisor está autenticado y accede a la sección "Archivo Periódico"

Cuando: configura el período (mes y año), selecciona uno o más departamentos de producción, elige el formato de salida (CSV o Excel) y presiona "Generar Archivo"

Entonces: el sistema consulta el historial de accesos del período, agrupa la información por departamento, calcula las estadísticas agregadas sin incluir ningún dato de identificación personal y genera el archivo de intercambio que se descarga automáticamente en el navegador

Condición 02

Dado: que el supervisor selecciona departamentos específicos para el reporte

Cuando: genera el archivo periódico

Entonces: el sistema incluye exclusivamente los accesos de los departamentos seleccionados, ignorando los accesos de los departamentos no marcados en la configuración

Condición 03

Dado: que el supervisor configura un período para el reporte

Cuando: no existen accesos registrados en el período y departamentos seleccionados

Entonces: el sistema muestra el mensaje "No se encontraron registros de acceso para el período y departamentos seleccionados" y no genera ningún archivo vacío

Condición 04

Dado: que el sistema genera el archivo de intercambio para el socio internacional

Cuando: el archivo es creado en cualquiera de los formatos disponibles (CSV o Excel)

Entonces: el contenido del archivo no incluye en ninguna de sus columnas nombres de empleados, apellidos de empleados, tipos de documento de identidad, números de documento de identidad ni números de identificación internos de los empleados. El archivo contiene exclusivamente las siguientes columnas: Nombre del Departamento, Período, Total de Accesos, Accesos Autorizados, Accesos Denegados, Accesos No Registrados, Accesos Suspendidos

Condición 05

Dado: que el supervisor intenta enviar el archivo al socio internacional

Cuando: presiona la opción "Enviar a Socio Internacional"

Entonces: el sistema registra la acción en los logs de auditoría con la fecha, hora, usuario que generó el envío, período del reporte, departamentos incluidos y formato del archivo, garantizando la trazabilidad de cada intercambio de información externa

## Tareas

| No | Descripción |
|---|---|
| 1 | Diseñar interfaz de configuración del archivo periódico con selector de período (mes y año), selector múltiple de departamentos de producción y selector de formato (CSV, Excel) |
| 2 | Implementar endpoint POST /api/reportes/archivo-periodico en Spring Boot que reciba período, lista de departamentos y formato |
| 3 | Implementar consulta SQL que agrupe los accesos por departamento en el período seleccionado, excluyendo explícitamente las columnas de datos personales (nombres, apellidos, tipo_documento, documento_identidad, identificacion_interna) |
| 4 | Generar archivo CSV con columnas: Departamento, Período, Total Accesos, Autorizados, Denegados, No Registrados, Suspendidos |
| 5 | Generar archivo Excel con las mismas columnas usando Apache POI e incluir formato de tabla y autoajuste de columnas |
| 6 | Implementar validación que impida la generación del archivo si no hay registros en el período seleccionado |
| 7 | Registrar en logs de auditoría cada generación y envío de archivo periódico con todos los metadatos relevantes |
| 8 | Permitir la descarga local del archivo generado y opcionalmente registrar el envío al socio internacional |

## Estado de Implementación

- **Backend**: ✓ — `POST /api/reportes/archivo-periodico` con **agregación por departamento SIN datos personales** (columnas: Departamento, Período, Total, Autorizados, Denegados, No Registrados, Suspendidos) y filtro opcional `departmentNames` (gap 1.2 §9 implementado). Formatos CSV/EXCEL/PDF. Tests en `PeriodicReportControllerTest`.
- **Frontend**: ✓ — panel "Archivo periódico para socios" en `ReportsView` (`/supervisor/reportes`, mockup 37) con selector de mes/año/formato (CSV/Excel/PDF) y descarga automática.
- **Notas**: cumple la normativa del socio internacional al excluir datos personales.

## Control de Versiones

| Versión | Fecha | Autor | Revisión | Descripción | Aprobador |
|---|---|---|---|---|---|
| 1.0 | 2026-07-26 | | | Versión inicial | |
| 1.1 | 2026-08-04 | | | Reescritura: agregación por departamento sin datos personales + `departmentNames` (gap 1.2 §9) | |
