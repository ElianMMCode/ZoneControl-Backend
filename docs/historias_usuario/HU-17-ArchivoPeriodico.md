# HU-17 - Generar Archivo Periódico para Socios

| Campo | Valor |
|---|---|
| **Código** | HU-17 |
| **Nombre** | Generar Archivo Periódico para Socios |
| **Complejidad** | Media |
| **HU Relacionada** | HU-03, HU-15 |
| **Módulo** | Módulo de Reportes y Auditoría |
| **Rol** | Supervisor o auditor |

## Descripción

**Yo como** supervisor o auditor
**Requiero** generar un archivo periódico con la actividad de ingresos a las áreas, sin ningún dato personal
**Para** compartir información agregada con el socio internacional cumpliendo la protección de datos establecida en el plan de expansión

## Requerimiento

El sistema debe permitir al supervisor o auditor generar un archivo periódico para compartir información con el socio internacional. Se configura el período (mes y año), de forma opcional los departamentos a incluir (si no se elige ninguno, se incluyen todos) y el formato de salida (PDF, Excel o CSV).

Este archivo NO contiene datos personales: no incluye nombres, números de documento ni códigos de empleado, porque es información agregada. Además, solo considera los ingresos a las áreas; las salidas quedan fuera del reporte.

El archivo tiene dos secciones. La primera resume los accesos por departamento y área, con las columnas: Departamento, Área, Total, Autorizados, Denegados, No Registrados, Suspendidos y % de Autorizados. La segunda muestra la distribución por día, con las columnas: Día, Total, Autorizados, Denegados, No Registrados y Suspendidos.

El botón "Enviar a Socio Internacional" abre una vista previa del contenido del archivo con un botón para descargarlo; el envío en sí se realiza fuera del sistema (no hay envío por correo ni registro de envío). Si el período elegido no tiene registros, el sistema no genera el archivo y lo indica.

## Criterios de Aceptación

Condición 01

Dado: que el supervisor configura el período (mes y año), los departamentos a incluir y el formato

Cuando: genera el archivo

Entonces: el sistema produce el archivo con las dos secciones descritas y permite descargarlo

Condición 02

Dado: que el archivo se genera

Cuando: se revisa la primera sección

Entonces: muestra el resumen por departamento y área con las columnas Departamento, Área, Total, Autorizados, Denegados, No Registrados, Suspendidos y % de Autorizados

Condición 03

Dado: que el archivo se genera

Cuando: se revisa la segunda sección

Entonces: muestra la distribución por día con las columnas Día, Total, Autorizados, Denegados, No Registrados y Suspendidos

Condición 04

Dado: que el archivo se genera

Cuando: se revisa su contenido

Entonces: no incluye nombres de empleados, apellidos, tipos o números de documento ni códigos; solo contiene información agregada que no permite identificar a ninguna persona

Condición 05

Dado: que el archivo se genera para un período

Cuando: se contabilizan los accesos

Entonces: solo se incluyen los ingresos a las áreas; las salidas no forman parte del archivo

Condición 06

Dado: que el supervisor elige un período y unos departamentos

Cuando: no hay registros de ingreso en ese período

Entonces: el sistema muestra un mensaje indicando que no hay registros y no genera un archivo vacío

Condición 07

Dado: que el supervisor presiona "Enviar a Socio Internacional"

Cuando: quiere compartir el archivo con el socio

Entonces: el sistema abre una vista previa del contenido del archivo con un botón "Descargar" para adjuntarlo al envío por el canal externo; el envío en sí queda fuera del sistema

## Tareas

| No | Descripción |
|---|---|
| 1 | Diseñar la pantalla de configuración con selector de mes y año, selector de departamentos y selector de formato (PDF, Excel, CSV) |
| 2 | Implementar la generación del archivo con el período, los departamentos y el formato elegidos |
| 3 | Implementar la sección de resumen por departamento y área con sus columnas y el porcentaje de autorizados |
| 4 | Implementar la sección de distribución por día con sus columnas |
| 5 | Excluir explícitamente cualquier dato personal e incluir solo los ingresos (no las salidas) |
| 6 | Validar que existan registros en el período antes de generar el archivo |
| 7 | Implementar la vista previa con el botón "Descargar" para el envío al socio, quedando el envío real fuera del sistema |

## Control de Versiones

| Versión | Fecha | Autor | Revisión | Descripción | Aprobador |
|---|---|---|---|---|---|
| 1.1 | 2026-08-04 | | | Reescritura: agregación por departamento sin datos personales + `departmentNames` (gap 1.2 §9) | |
| 1.2 | 2026-08-05 | | | Selección de varios departamentos + modal de vista previa con Descargar para el envío al socio | |
| 1.3 | 2026-08-06 | | | Revisión de lenguaje y criterios detallados (dos secciones, solo ingresos, envío fuera del sistema) | |

## Estado de Implementación

- **Backend**: ✓ — `POST /api/reportes/archivo-periodico` con agregación **por departamento × área** SIN datos personales (columnas: Departamento, Área, Total, Autorizados, Denegados, No Registrados, Suspendidos, % Autorizados) y **distribución por día**; filtro opcional `departmentNames`; **solo ingresos** (las salidas EXIT quedan fuera). Formatos CSV/EXCEL/PDF. `POST /api/reportes/archivo-periodico/preview` devuelve `areaRows`/`dayRows` en JSON para la vista previa. Error si el período no tiene registros. Tests en `PeriodicReportControllerTest`.
- **Frontend**: ✓ — panel "Archivo periódico para socios" en `ReportsView` (`/supervisor/reportes`, mockup 37) con selector de mes/año/formato (CSV/Excel/PDF), **chips de selección multi-departamento** (envía `departmentNames`) y descarga automática. El botón "Enviar a socio internacional" abre un **modal de vista previa** (de `archivo-periodico/preview`, sin datos personales) con botón **Descargar** para adjuntarlo al envío por el canal externo; el envío en sí queda fuera del sistema.
