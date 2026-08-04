# HU-10 - CARGA MASIVA DE PERSONAL

| Campo | Valor |
|---|---|
| **Código** | HU-10 |
| **Nombre** | Carga Masiva de Personal |
| **Complejidad** | Alta |
| **HU Relacionada** | HU-03, HU-09 |
| **Módulo** | Módulo de Gestión de Personal |

## Descripción

**Yo como** gestor de personal
**Requiero** cargar múltiples registros de personal desde un archivo plano (CSV/TXT) con una plantilla descargable que contenga la estructura admitida, y que el sistema valide cada registro antes de insertarlo
**Para** ahorrar tiempo al registrar grandes volúmenes de empleados asegurando que todos los datos sean correctos y consistentes desde el inicio

## Requerimiento

Carga masiva de personal a partir de un archivo plano, con validación previa de la extensión del archivo, los encabezados y la información fila por fila antes de su incorporación al sistema. El número de identificación interno de cada empleado debe ser generado automáticamente por el sistema. El archivo debe contener los campos: tipo de documento de identidad colombiano, número de documento de identidad, nombres, apellidos, cargo, departamento y estado. El sistema debe ofrecer un botón para descargar una plantilla CSV con los encabezados correctos y una fila de ejemplo para que el usuario la complete correctamente.

## Criterios de Aceptación

Condición 01

Dado: que el gestor de personal está autenticado y accede a la sección "Carga Masiva"

Cuando: presiona el botón "Descargar Plantilla"

Entonces: el sistema genera y descarga un archivo CSV con el nombre "plantilla_carga_masiva_personal.csv" que contiene los encabezados exactos en la primera fila (tipo_documento;documento_identidad;nombres;apellidos;cargo;departamento;estado;fecha_ingreso) y una segunda fila con datos de ejemplo (CC;1234567890;Juan;Pérez;Analista;Control de Calidad;ACTIVO;2026-01-15) para guiar al usuario en el llenado correcto del archivo

Condición 02

Dado: que el gestor selecciona un archivo CSV o TXT con extensión, estructura y encabezados válidos

Cuando: sube el archivo al servidor y el sistema lo procesa completamente

Entonces: el sistema valida la extensión del archivo (.csv o .txt), verifica que los encabezados coincidan exactamente con los esperados, valida cada fila (tipo de documento colombiano válido, documento de identidad no duplicado, campos obligatorios completos, departamento existente, estado válido), genera automáticamente el número de identificación interno para cada registro válido, inserta los registros correctos en PostgreSQL y muestra un resumen con el conteo total de registros procesados, éxitos y errores

Condición 03

Dado: que el gestor sube un archivo a la plataforma

Cuando: la extensión del archivo no es .csv ni .txt, la estructura de columnas no coincide con la esperada o los encabezados de la primera fila no son exactamente los requeridos

Entonces: el sistema retorna un error específico según el caso detectado: "Extensión de archivo no permitida. Solo se aceptan archivos .csv y .txt", "La estructura del archivo no coincide con el formato esperado" o "Los encabezados del archivo son incorrectos. Descargue la plantilla para ver el formato admitido"

Condición 04

Dado: que el archivo contiene una mezcla de registros válidos e inválidos

Cuando: el sistema procesa la carga completa

Entonces: el sistema inserta únicamente los registros que pasan todas las validaciones, genera un reporte de errores detallado y descargable donde cada error indica el número de fila, el campo problemático y el motivo del rechazo, y muestra el resumen final: "Registros procesados: [total]. Éxitos: [X]. Errores: [Y]. Descargar detalle de errores"

Condición 05

Dado: que el gestor intenta cargar un archivo

Cuando: el archivo pesa más de 10MB o contiene más de 1000 registros

Entonces: el sistema rechaza la carga y muestra el mensaje "El archivo excede el límite permitido de 10MB o 1000 registros. Por favor, divida el archivo en partes más pequeñas"

## Tareas

| No | Descripción |
|---|---|
| 1 | Diseñar interfaz de carga masiva con selector de archivo, botón "Descargar Plantilla" y botón "Subir Archivo" |
| 2 | Crear archivo plantilla CSV de referencia en el servidor con encabezados correctos y fila de ejemplo, servido mediante endpoint GET /api/personal/bulk/plantilla |
| 3 | Implementar endpoint POST /api/personal/bulk (multipart/form-data) en Spring Boot |
| 4 | Validar extensión del archivo recibido: solo .csv y .txt permitidos |
| 5 | Validar encabezados del archivo contra los esperados (tipo_documento, documento_identidad, nombres, apellidos, cargo, departamento, estado) |
| 6 | Validar cada fila individualmente: tipo de documento colombiano válido (CC, CE, TI, PA, RC), documento de identidad no duplicado en BD ni dentro del mismo archivo, campos obligatorios completos, departamento existente en BD, estado válido (ACTIVO/INACTIVO) |
| 7 | Generar automáticamente el número de identificación interno (EMP-XXXXXX) para cada registro válido e insertar en PostgreSQL mediante batch insert |
| 8 | Construir reporte de errores detallado (fila, campo, motivo) para registros inválidos y retornarlo como archivo descargable |
| 9 | Limitar tamaño máximo de archivo a 10MB y máximo 1000 registros por carga |

## Estado de Implementación

- **Backend**: ✓ — `GET /api/personal/bulk/plantilla` (CSV con encabezados de 8 columnas —incluye `fecha_ingreso`— y fila ejemplo) y `POST /api/personal/bulk` (validación por fila, batch insert, reporte de errores en `errorReportUrl` como CSV inline). Límite de **10MB explícito** en el servicio (mensaje de la HU) y de **1000 registros**. Tests verdes.
- **Frontend**: ✓ — `BulkUploadView` (`/personal/carga-masiva`, mockup 10) con descarga de plantilla, upload y **tabla de errores inline** (Fila/Campo/Detalle) además del reporte descargable.

## Control de Versiones

| Versión | Fecha | Autor | Revisión | Descripción | Aprobador |
|---|---|---|---|---|---|
| 1.0 | 2026-07-26 | | | Versión inicial | |
