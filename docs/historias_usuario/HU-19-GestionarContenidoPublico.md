# HU-19 - GESTIONAR CONTENIDO DEL MÓDULO PÚBLICO

| Campo | Valor |
|---|---|
| **Código** | HU-19 |
| **Nombre** | Gestionar Contenido del Módulo Público |
| **Complejidad** | Alta |
| **HU Relacionada** | HU-01, HU-02, HU-03 |
| **Módulo** | Módulo de Administración |

## Descripción

**Yo como** administrador del sistema
**Requiero** gestionar todo el contenido que se muestra en el módulo público del sitio web, incluyendo información institucional, datos de contacto, ubicación de sedes, catálogo de servicios/productos y el folleto informativo en PDF
**Para** mantener actualizada la información que los visitantes consultan sin necesidad de involucrar al equipo de desarrollo

## Requerimiento

El sistema debe permitir al administrador editar la información institucional, los datos de contacto, la ubicación de sedes, el catálogo de servicios y productos farmacéuticos, así como cargar y actualizar el folleto informativo en formato PDF. El folleto debe cumplir con restricciones de formato y peso. Si no hay folleto cargado, el botón de descarga no debe mostrarse en el módulo público.

## Criterios de Aceptación

Condición 01

Dado: que el administrador está autenticado

Cuando: accede a la sección "Gestionar Contenido Público" y modifica la información institucional (misión, visión, descripción de la empresa, áreas de producción)

Entonces: el sistema guarda los cambios en la base de datos y los visitantes del módulo público ven la información actualizada inmediatamente

Condición 02

Dado: que el administrador está en la sección "Gestionar Contenido Público"

Cuando: modifica los datos de contacto (teléfonos, correo electrónico, redes sociales) y guarda los cambios

Entonces: el sistema actualiza la información de contacto y esta se refleja en el módulo público sin necesidad de reiniciar el servidor

Condición 03

Dado: que el administrador está en la sección "Gestionar Contenido Público"

Cuando: modifica la ubicación de sedes (direcciones, mapa, horarios de atención) y guarda los cambios

Entonces: el sistema actualiza la información de sedes en la base de datos y los visitantes visualizan los nuevos datos al recargar la página

Condición 04

Dado: que el administrador está en la sección "Gestionar Contenido Público"

Cuando: agrega, modifica o elimina productos del catálogo de servicios y productos farmacéuticos (nombre del medicamento, descripción, principio activo, presentación, área de producción asociada)

Entonces: el sistema actualiza el catálogo y los visitantes del módulo público ven los cambios reflejados en la sección "Catálogo de Servicios/Productos"

Condición 05

Dado: que el administrador está en la sección de gestión del folleto

Cuando: selecciona un archivo PDF válido que no excede los 10MB y presiona "Cargar Folleto"

Entonces: el sistema almacena el archivo en el servidor, habilita el botón "Descargar Folleto" en el módulo público y muestra confirmación de carga exitosa

Condición 06

Dado: que el administrador intenta cargar un folleto

Cuando: el archivo seleccionado no tiene extensión .pdf

Entonces: el sistema rechaza la carga y muestra el mensaje "Formato no permitido. Solo se aceptan archivos PDF"

Condición 07

Dado: que el administrador intenta cargar un folleto

Cuando: el archivo PDF excede los 10MB

Entonces: el sistema rechaza la carga y muestra el mensaje "El archivo excede el tamaño máximo permitido de 10MB"

Condición 08

Dado: que el administrador elimina el folleto actualmente cargado

Cuando: confirma la eliminación

Entonces: el sistema borra el archivo del servidor y oculta el botón "Descargar Folleto" del módulo público

Condición 09

Dado: que el administrador intenta editar contenido público

Cuando: deja campos obligatorios vacíos en cualquiera de las secciones (información institucional, contacto, sedes, catálogo)

Entonces: el sistema muestra los errores de validación correspondientes a los campos incompletos e impide guardar hasta que se corrijan

## Tareas

| No | Descripción |
|---|---|
| 1 | Diseñar interfaz de administración de contenido público con pestañas: Información Institucional, Datos de Contacto, Ubicación de Sedes, Catálogo de Productos, Folleto |
| 2 | Implementar formularios de edición para información institucional, datos de contacto y ubicación de sedes |
| 3 | Implementar CRUD del catálogo de servicios/productos (nombre, descripción, principio activo, presentación, área de producción) |
| 4 | Implementar carga de archivo PDF para el folleto con validación de formato (.pdf) y tamaño máximo (10MB) |
| 5 | Implementar endpoints PUT/POST /api/admin/contenido-publico en Spring Boot para cada sección |
| 6 | Implementar endpoint POST /api/admin/contenido-publico/folleto (multipart/form-data) con validaciones de formato y peso |
| 7 | Implementar endpoint DELETE /api/admin/contenido-publico/folleto para eliminar el folleto actual |
| 8 | Condicionar la visibilidad del botón "Descargar Folleto" en el módulo público a la existencia del archivo en el servidor |
| 9 | Configurar almacenamiento de archivos en el servidor (directorio uploads/folleto/) |

## Control de Versiones

| Versión | Fecha | Autor | Revisión | Descripción | Aprobador |
|---|---|---|---|---|---|
| 1.0 | 2026-07-26 | | | Versión inicial | |
