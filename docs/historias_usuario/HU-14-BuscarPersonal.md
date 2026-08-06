# HU-14 - Buscar Personal por Filtros

| Campo | Valor |
|---|---|
| **Código** | HU-14 |
| **Nombre** | Buscar Personal por Filtros |
| **Complejidad** | Alta |
| **HU Relacionada** | HU-03, HU-09 |
| **Módulo** | Módulo de Gestión de Personal |
| **Rol** | Gestor de personal |

## Descripción

**Yo como** gestor de personal
**Requiero** buscar empleados combinando varios filtros opcionales (tipo de documento, número de documento, nombre, apellido, departamento y estado)
**Para** encontrar rápidamente a una persona o a un grupo del personal y acceder a sus opciones de gestión

## Requerimiento

El sistema debe permitir al gestor de personal buscar empleados combinando filtros opcionales: tipo de documento, número de documento, nombre, apellido, departamento y estado. Todos los filtros son opcionales; si no se usa ninguno, el sistema muestra la lista completa de empleados.

Cuando se usan varios filtros a la vez, se aplican todos juntos: un empleado aparece solo si cumple cada uno de los criterios indicados. Los resultados se muestran en una tabla paginada, es decir, se organizan en páginas cuando hay muchos resultados.

Desde cada resultado de la búsqueda se puede abrir la ficha del empleado, editar sus datos o ir a la gestión de sus permisos de acceso.

## Criterios de Aceptación

Condición 01

Dado: que el gestor entra a la sección de personal sin seleccionar ningún filtro

Cuando: ejecuta la búsqueda

Entonces: el sistema muestra todos los empleados en una tabla paginada

Condición 02

Dado: que el gestor usa varios filtros a la vez (por ejemplo, un departamento y un apellido)

Cuando: ejecuta la búsqueda

Entonces: el sistema muestra únicamente los empleados que cumplen todos los criterios indicados, no solo algunos

Condición 03

Dado: que el gestor aplica filtros de búsqueda

Cuando: ningún empleado coincide con ellos

Entonces: el sistema muestra el mensaje "No se encontraron resultados"

Condición 04

Dado: que la búsqueda devuelve muchos empleados

Cuando: el gestor revisa los resultados

Entonces: el sistema los organiza en páginas con controles para avanzar y retroceder, y muestra cuántos resultados hay en total

Condición 05

Dado: que el gestor ve los resultados de la búsqueda

Cuando: selecciona un empleado

Entonces: el sistema ofrece opciones para ver su detalle, editar su información o gestionar sus permisos de acceso

## Tareas

| No | Descripción |
|---|---|
| 1 | Diseñar el formulario de búsqueda con los filtros: tipo de documento, número de documento, nombre, apellido, departamento (menú desplegable) y estado |
| 2 | Implementar la búsqueda combinada de empleados aplicando todos los filtros a la vez |
| 3 | Permitir listar todos los empleados cuando no hay filtros |
| 4 | Permitir pasar de página en los resultados |
| 5 | Agregar acciones por fila: ver detalle, editar y gestionar permisos |
| 6 | Implementar la consulta del detalle completo del empleado |
| 7 | Implementar la edición del empleado validando que el número de documento no esté repetido |
| 8 | Al cambiar el estado del empleado a inactivo o suspendido, suspender en cadena sus permisos y desactivar su usuario; al volver a activarlo, restaurar el estado anterior |

## Control de Versiones

| Versión | Fecha | Autor | Revisión | Descripción | Aprobador |
|---|---|---|---|---|---|
| 1.0 | 2026-07-26 | | | Versión inicial | |
| 1.1 | 2026-07-28 | | | Agregar edición de empleados y la suspensión/activación en cadena del estado | |
| 1.2 | 2026-08-06 | | | Revisión de lenguaje y criterios detallados | |

## Estado de Implementación

- **Backend**: ✓ — `GET /api/personal` con filtros combinados (`documentType`, `documentNumber`, `firstName`, `lastName`, `departmentName`, `status`) + paginación. Sin filtros lista todos los empleados (todos los filtros son opcionales). Edición vía `PATCH /api/personal/{id}` con validación de unicidad de documento y cascada de estado (permisos → SUSPENDIDO, usuario → INACTIVO; reactivación bidireccional). Tests verdes.
- **Frontend**: ✓ — `EmployeeListView` (`/personal`, mockup 32) con filtros, tabla paginada y acceso al detalle. El filtro de departamento es un menú desplegable (`useDepartments`). La edición y la asignación de áreas se gestionan desde `EmployeeDetailView` (`/personal/:id`, mockup 41).
