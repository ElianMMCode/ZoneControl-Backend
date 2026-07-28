# HU-14 - BUSCAR PERSONAL POR FILTROS

| Campo | Valor |
|---|---|
| **Código** | HU-14 |
| **Nombre** | Buscar Personal por Filtros |
| **Complejidad** | Alta |
| **HU Relacionada** | HU-03, HU-09 |
| **Módulo** | Módulo de Gestión de Personal |

## Descripción

**Yo como** gestor de personal
**Requiero** buscar empleados usando filtros por número de identificación, nombre, apellido y departamento, aplicando todos los criterios de forma combinada
**Para** encontrar rápidamente información específica del personal y acceder a sus opciones de gestión

## Requerimiento

Mecanismos de búsqueda del personal por identificación, nombre o apellido, así como filtrado por departamento. Se debe exigir al menos un filtro de búsqueda. Los resultados deben mostrarse en una tabla paginada con opciones de ordenamiento por columna y acciones sobre cada empleado (ver detalle, editar, gestionar permisos).

## Criterios de Aceptación

Condición 01

Dado: que el gestor ingresa al menos un criterio de búsqueda

Cuando: existen empleados que coinciden con los filtros aplicados

Entonces: el sistema muestra una tabla paginada con los resultados, aplicando lógica AND entre todos los filtros

Condición 02

Dado: que el gestor intenta buscar sin seleccionar ningún filtro

Cuando: presiona "Buscar"

Entonces: el sistema muestra el mensaje "Debe seleccionar al menos un filtro de búsqueda"

Condición 03

Dado: que el gestor ingresa criterios de búsqueda

Cuando: no hay empleados que coincidan con los filtros

Entonces: el sistema muestra el mensaje "No se encontraron resultados"

Condición 04

Dado: que se muestran resultados de búsqueda

Cuando: el gestor selecciona un empleado de la lista

Entonces: el sistema ofrece opciones para ver detalle, editar el registro o gestionar sus permisos de acceso

## Criterios de Aceptación (continuación)

Condición 05

Dado: que el gestor selecciona "Editar" en un empleado de la lista

Cuando: modifica sus datos personales o cambia su estado (ACTIVO/INACTIVO/SUSPENDIDO)

Entonces: el sistema guarda los cambios, y si el nuevo estado es INACTIVO o SUSPENDIDO, desactiva en cascada todos sus permisos de acceso (→ SUSPENDIDO) y su usuario de sistema asociado (→ INACTIVO). Si el estado vuelve a ACTIVO, no se restauran automáticamente.

## Tareas

| No | Descripción |
|---|---|
| 1 | Diseñar formulario de búsqueda con filtros: número de identificación, nombre, apellido, departamento (dropdown), estado |
| 2 | Implementar endpoint GET /personal con query parameters dinámicos en Spring Boot |
| 3 | Construir consulta SQL dinámica en el backend aplicando lógica AND entre filtros |
| 4 | Validar en frontend y backend que al menos un filtro esté presente antes de ejecutar la consulta |
| 5 | Implementar paginación de resultados en frontend y backend |
| 6 | Implementar ordenamiento de resultados por columna en la tabla |
| 7 | Agregar acciones por fila: ver detalle (GET /personal/{id}), editar empleado (PATCH /personal/{id}), gestionar permisos |
| 8 | Implementar GET /personal/{id} que retorna detalle completo del empleado con HTTP 200 o 404 |
| 9 | Implementar PATCH /personal/{id} con patch parcial: firstName, lastName, position, documentType, documentNumber, departmentId, status |
| 10 | Validar unicidad (tipoDocumento+numeroDocumento) al cambiar documento |
| 11 | Implementar cascade: al cambiar status a INACTIVO o SUSPENDIDO → permisos → SUSPENDIDO y User vinculado → INACTIVO |
| 12 | Agregar filtro status (ACTIVO/INACTIVO/SUSPENDIDO) en GET /personal |

## Control de Versiones

| Versión | Fecha | Autor | Revisión | Descripción | Aprobador |
|---|---|---|---|---|---|
| 1.0 | 2026-07-26 | | | Versión inicial |
| 1.1 | 2026-07-28 | | | Agregar edición de empleados con cascade de estado | |
