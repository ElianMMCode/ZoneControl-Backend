# HU-29 - GESTIONAR EL CATÁLOGO DE CARGOS

| Campo | Valor |
|---|---|
| **Código** | HU-29 |
| **Nombre** | Gestionar el catálogo de cargos |
| **Complejidad** | Media |
| **HU Relacionada** | HU-04, HU-08, HU-28 |
| **Módulo** | Módulo de Gestión de Personal |
| **Rol** | Administrador |

## Descripción

**Yo como** administrador del sistema
**Requiero** mantener un catálogo de cargos de la compañía, indicando para cada uno si otorga un rol de sistema y cuál
**Para** que al registrar a un empleado se le asigne un cargo del catálogo (sin escribir textos libres) y que el rol de usuario de cada persona se derive de su cargo

## Requerimiento

El sistema cuenta con un catálogo de cargos. Cada cargo tiene un nombre único y, opcionalmente, un **rol de sistema** (Administrador, Gestor de Personal o Supervisor / Auditor). El rol de sistema del cargo es la base para decidir si un empleado puede tener usuario del sistema y con qué rol (HU-04).

El administrador puede crear, editar y eliminar cargos. Al crear o editar un cargo, define su nombre y, si corresponde, el rol de sistema que otorga. Si el rol de un cargo cambia, el sistema actualiza el rol derivado en los empleados que tienen ese cargo (los que pasan a tener rol se convierten en candidatos; los que dejan de tenerlo dejan de serlo). Un cargo con empleados vinculados no se puede eliminar; el sistema lo informa.

Este catálogo es la única fuente para el campo "cargo" de los empleados: al registrar o editar un empleado (HU-08, HU-28) el cargo se selecciona de aquí, no se escribe libremente.

## Criterios de Aceptación

Condición 01

Dado: que el administrador abre la sección de cargos

Cuando: consulta el catálogo

Entonces: el sistema muestra la lista de cargos con su nombre y el rol de sistema que cada uno otorga (o indica que no otorga ninguno)

Condición 02

Dado: que el administrador crea un cargo

Cuando: ingresa un nombre y, opcionalmente, un rol de sistema

Entonces: el sistema guarda el cargo y aparece en el catálogo

Condición 03

Dado: que el administrador intenta crear un cargo con un nombre que ya existe

Cuando: confirma la creación

Entonces: el sistema muestra el mensaje "Ya existe un cargo con el nombre: X" y no lo crea

Condición 04

Dado: que el administrador edita un cargo

Cuando: cambia el nombre o el rol de sistema

Entonces: el sistema guarda el cambio y actualiza el rol derivado en los empleados que tienen ese cargo

Condición 05

Dado: que un empleado tiene un cargo cuyo rol cambia

Cuando: el administrador guarda la edición del cargo

Entonces: el sistema actualiza el rol de sistema del empleado: con rol pasa a ser candidato a usuario, sin rol deja de serlo

Condición 06

Dado: que el administrador intenta eliminar un cargo

Cuando: el cargo no tiene empleados vinculados

Entonces: el sistema lo elimina

Condición 07

Dado: que el administrador intenta eliminar un cargo

Cuando: el cargo tiene empleados vinculados

Entonces: el sistema muestra un mensaje indicando que no se puede eliminar porque tiene empleados vinculados

Condición 08

Dado: que el gestor registra o edita un empleado

Cuando: elige el cargo

Entonces: selecciona un cargo del catálogo mediante una lista desplegable (no se escribe el cargo libremente)

## Tareas

| No | Descripción |
|---|---|
| 1 | Crear el modelo de cargo con nombre único y rol de sistema opcional |
| 2 | Listar el catálogo de cargos |
| 3 | Crear, editar y eliminar cargos |
| 4 | Sincronizar el rol de sistema en los empleados vinculados al editar un cargo |
| 5 | Bloquear la eliminación de cargos con empleados vinculados |
| 6 | Usar el catálogo en los formularios de registro y edición de empleados |

## Control de Versiones

| Versión | Fecha | Autor | Revisión | Descripción | Aprobador |
|---|---|---|---|---|---|
| 1.0 | 2026-08-06 | | | Historia nueva: modelo de cargos con rol de sistema derivado | |

## Estado de Implementación

- **Backend**: ✓ — entidad `Position` (nombre único + `systemRole`), `GET/POST/PUT/DELETE /api/personal/cargos` (mutaciones solo ADMIN; DELETE con 409 si hay empleados vinculados; PUT sincroniza el rol en los empleados del cargo). `RegisterEmployeeRequest`/`UpdateEmployeeRequest` usan `cargoId` y derivan `position` y `systemRole` del cargo. Tests en `CargoControllerTest` y de registro/edición de empleados.
- **Frontend**: ✓ — vista `AdminCargosView` (`/admin/cargos`, rol ADMIN) con CRUD de cargos; los formularios de registro y edición de empleados usan el desplegable de cargos (`useCargos`).
