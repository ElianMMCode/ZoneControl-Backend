# HU-19 - GESTIONAR CONTENIDO PÚBLICO

| Campo | Valor |
|---|---|
| **Código** | HU-19 |
| **Nombre** | Gestionar Contenido Público |
| **Complejidad** | Alta |
| **HU Relacionada** | HU-01, HU-02, HU-03 |
| **Módulo** | Módulo de Administración |
| **Rol** | Administrador |

## Descripción

**Yo como** administrador del sistema
**Requiero** gestionar directamente todo el contenido que ven los visitantes del sitio público de la empresa: información institucional, datos de contacto, sedes, catálogo de productos y folleto informativo
**Para** mantener la información pública actualizada sin depender del equipo de desarrollo

## Requerimiento

El sistema debe ofrecer al administrador una sola pantalla con cinco secciones para administrar el sitio público. En "Información Institucional" se editan la misión, la visión y la descripción de la empresa. En "Datos de Contacto" se actualizan los teléfonos, los correos electrónicos y las redes sociales. En "Sedes" se agregan, modifican y eliminan las sedes indicando ciudad, dirección y coordenadas de ubicación. En "Catálogo de Productos" se agregan, modifican y eliminan los productos farmacéuticos con su nombre, descripción, principio activo, presentación y área de producción.

En la sección "Folleto Informativo" el administrador carga un archivo en formato PDF de máximo 10 MB que reemplaza al folleto vigente, o lo elimina cuando ya no corresponde. El botón de descarga del folleto que ve el visitante solo aparece si existe un folleto cargado; si se elimina, el botón desaparece.

Todos los cambios guardados se reflejan de inmediato en el sitio público, sin necesidad de reiniciar ni intervención técnica. El único encargado de gestionar este contenido es el administrador; los visitantes solo leen lo publicado.

## Criterios de Aceptación

Condición 01

Dado: que el administrador ingresa a la pantalla de gestión de contenido público

Cuando: modifica la información institucional (misión, visión y descripción de la empresa) y guarda

Entonces: el sistema guarda los cambios y los visitantes ven la nueva información institucional en el sitio público de inmediato, sin reiniciar el servicio

Condición 02

Dado: que el administrador está en la pantalla de gestión de contenido público

Cuando: modifica los datos de contacto (teléfonos, correos y redes sociales) y guarda

Entonces: el sistema actualiza la información de contacto y el visitante que recargue el sitio público ve los nuevos datos

Condición 03

Dado: que el administrador agrega una nueva sede con ciudad, dirección y coordenadas de ubicación

Cuando: guarda el registro

Entonces: el sistema agrega la sede al listado y esta aparece en el sitio público; mientras no haya un mapa gráfico disponible, el visitante ve las coordenadas como texto

Condición 04

Dado: que el administrador agrega, modifica o elimina productos del catálogo (nombre, descripción, principio activo, presentación y área de producción)

Cuando: guarda los cambios

Entonces: el sistema actualiza el catálogo y los visitantes del sitio público ven los cambios en la sección de catálogo de productos

Condición 05

Dado: que el administrador elimina una sede o un producto del catálogo

Cuando: confirma la eliminación

Entonces: el sistema elimina el registro y este deja de mostrarse en el sitio público; si lo eliminado estaba en uso en alguna otra parte de la gestión, el sistema informa del impedimento y no completa la eliminación

Condición 06

Dado: que el administrador carga un folleto en formato PDF que no supera los 10 MB

Cuando: confirma la carga

Entonces: el sistema guarda el folleto, reemplaza al anterior si existía y habilita el botón "Descargar Folleto" en el sitio público, mostrando un mensaje de carga exitosa

Condición 07

Dado: que el administrador intenta cargar un folleto

Cuando: el archivo no tiene formato PDF o supera los 10 MB

Entonces: el sistema rechaza la carga y muestra el mensaje correspondiente ("Formato no permitido. Solo se aceptan archivos PDF" o "El archivo excede el tamaño máximo permitido de 10MB"), sin modificar el folleto vigente

Condición 08

Dado: que el administrador elimina el folleto actualmente publicado

Cuando: confirma la eliminación

Entonces: el sistema borra el archivo y oculta el botón "Descargar Folleto" del sitio público

Condición 09

Dado: que el administrador intenta guardar contenido en cualquiera de las secciones (información institucional, contacto, sedes o catálogo)

Cuando: deja campos obligatorios sin completar

Entonces: el sistema señala los campos incompletos e impide guardar hasta que se corrijan

## Tareas

| No | Descripción |
|---|---|
| 1 | Pantalla de administración de contenido público con cinco secciones: Institucional, Contacto, Sedes, Catálogo y Folleto |
| 2 | Formularios de edición para información institucional y datos de contacto |
| 3 | Alta, edición y baja de sedes (ciudad, dirección, coordenadas) |
| 4 | Alta, edición y baja de productos del catálogo |
| 5 | Carga de folleto en PDF con validación de formato y tamaño máximo de 10 MB, reemplazo y eliminación |
| 6 | Mostrar u ocultar el botón "Descargar Folleto" del sitio público según exista un folleto cargado |
| 7 | Reflejar de inmediato en el sitio público todos los cambios guardados |

## Control de Versiones

| Versión | Fecha | Autor | Revisión | Descripción | Aprobador |
|---|---|---|---|---|---|
| 1.0 | 2026-08-06 | | | Revisión de lenguaje y criterios detallados | |

## Estado de Implementación

- **Backend**: ✓ — `PUT /api/admin/contenido-publico/{INSTITUTIONAL|CONTACT}`, `POST/DELETE /api/admin/contenido-publico/folleto`, CRUD `POST/PUT/DELETE /api/admin/contenido-publico/sedes[/{id}]` y `/productos[/{id}]`; caché pública invalidada en cada escritura. Tests en `AdminPublicContentControllerTest`.
- **Frontend**: ✓ — `PublicContentView` en `/admin/contenido-publico` (rol ADMIN) con 5 pestañas. La sección de sedes muestra direcciones, horarios y coordenadas.
