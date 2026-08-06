# HU-09 - REGISTRAR PERSONAL INDIVIDUAL

| Campo | Valor |
|---|---|
| **Código** | HU-09 |
| **Nombre** | Registrar Personal Individual |
| **Complejidad** | Alta |
| **HU Relacionada** | HU-03, HU-10, HU-31 |
| **Módulo** | Módulo de Gestión de Personal |
| **Rol** | Gestor de Personal |

## Descripción

**Yo como** gestor de personal
**Requiero** registrar a un empleado de la empresa con sus datos personales, su cargo y su departamento
**Para** mantener el registro actualizado y trazable del personal autorizado de la organización

## Requerimiento

El gestor de personal registra a cada empleado de forma individual desde un formulario. El formulario pide el tipo de documento de identidad (Cédula de Ciudadanía, Cédula de Extranjería, Tarjeta de Identidad, Pasaporte o Registro Civil), el número de documento, los nombres, los apellidos, el cargo, el departamento y el estado del empleado.

El sistema revisa que todos los campos obligatorios estén completos y que el tipo de documento sea uno de los permitidos. Además, la combinación de tipo y número de documento debe ser única: si ya existe otro empleado con el mismo tipo y número de documento, el sistema lo avisa con un mensaje claro y no guarda el registro.

Al guardar un registro válido, el sistema le asigna automáticamente a cada empleado un código interno con el formato EMP-000001, EMP-000002 y así sucesivamente. Ese código es único y el gestor no puede modificarlo. Después de crear el empleado, el gestor puede añadir de forma opcional una fotografía (imagen en formato JPG, PNG o WebP, de máximo 2 MB).

## Criterios de Aceptación

Condición 01

Dado: que el gestor de personal completa el formulario con un tipo de documento válido, un número de documento que no existe en el sistema, los nombres, los apellidos, el cargo y un departamento

Cuando: envía el formulario de registro

Entonces: el sistema crea el empleado, le asigna automáticamente un código interno con el formato EMP-XXXXXX y muestra ese código en la pantalla de confirmación

Condición 02

Dado: que el gestor de personal ingresa un tipo y un número de documento

Cuando: ya existe otro empleado registrado con ese mismo tipo y número de documento

Entonces: el sistema muestra el mensaje "Ya existe un empleado registrado con el documento [tipo] número [número]" y no guarda el registro

Condición 03

Dado: que el gestor de personal elige un tipo de documento

Cuando: el tipo elegido no está en la lista de documentos permitidos (CC, CE, TI, PA, RC)

Entonces: el sistema rechaza el envío y muestra el mensaje "Tipo de documento no válido. Los tipos permitidos son: CC, CE, TI, PA, RC"

Condición 04

Dado: que el gestor de personal completa el formulario

Cuando: algún campo obligatorio está vacío (tipo de documento, número de documento, nombres, apellidos, cargo o departamento) o los nombres y apellidos tienen menos de 2 caracteres

Entonces: el sistema muestra un mensaje de error debajo de cada campo inválido y no envía el formulario hasta que se corrijan todos los errores

Condición 05

Dado: que el gestor de personal revisa el código asignado al empleado

Cuando: el sistema generó el código interno

Entonces: el código sigue el formato EMP-XXXXXX, es único en el sistema y el gestor no puede modificarlo manualmente en ningún momento posterior

Condición 06

Dado: que el gestor de personal registró al empleado

Cuando: desea añadir una fotografía

Entonces: el sistema permite añadirla después de la creación, acepta imágenes en formato JPG, PNG o WebP de máximo 2 MB y muestra un error si el archivo no cumple esos requisitos

## Tareas

| No | Descripción |
|---|---|
| 1 | Diseñar el formulario de registro de personal con tipo y número de documento, nombres, apellidos, cargo, departamento y estado |
| 2 | Permitir elegir el tipo de documento entre las opciones permitidas (CC, CE, TI, PA, RC) |
| 3 | Generar automáticamente el código interno del empleado con el formato EMP-XXXXXX |
| 4 | Validar que no exista otro empleado con el mismo tipo y número de documento |
| 5 | Validar los campos obligatorios y la longitud mínima de nombres y apellidos |
| 6 | Permitir añadir una fotografía opcional después de crear el empleado |
| 7 | Mostrar el código generado en la pantalla de confirmación y los mensajes de error por campo |

## Control de Versiones

| Versión | Fecha | Autor | Revisión | Descripción | Aprobador |
|---|---|---|---|---|---|
| 1.0 | 2026-08-06 | | | Revisión de lenguaje y criterios detallados | |

## Estado de Implementación

- **Backend**: ✓ — `POST /api/personal` (`EmployeeController`) genera el código secuencial `EMP-000001…`, valida la unicidad de tipo+número de documento, los campos obligatorios y la existencia del departamento. Tests verdes (`EmployeeControllerTest`).
- **Frontend**: ✓ — `RegisterEmployeeView` (`/personal/nuevo`, mockup 42) con validación por campo, notificación con el código generado y carga de fotografía opcional (HU-31). `EmployeeDetailView` (`/personal/:id`, mockup 41) permite gestionar la foto.
