# HU-09 - REGISTRAR PERSONAL INDIVIDUAL

| Campo | Valor |
|---|---|
| **Código** | HU-09 |
| **Nombre** | Registrar Personal Individual |
| **Complejidad** | Alta |
| **HU Relacionada** | HU-03, HU-10 |
| **Módulo** | Módulo de Gestión de Personal |

## Descripción

**Yo como** gestor de personal
**Requiero** registrar un empleado individualmente ingresando su tipo y número de documento de identidad, datos personales, cargo y el departamento de producción al que pertenece, mientras el sistema genera automáticamente su número de identificación interno
**Para** mantener un registro actualizado y trazable del personal autorizado de la organización con documentos de identidad colombianos verificables

## Requerimiento

Registro del personal autorizado, incluyendo sus datos básicos (tipo de documento de identidad colombiano, número de documento de identidad, nombres, apellidos, cargo) y el departamento de producción al que se encuentra asignado. El número de identificación interno debe ser generado automáticamente por el sistema. La combinación de tipo de documento de identidad y número de documento de identidad debe ser única en el sistema. Los tipos de documento de identidad permitidos son los documentos colombianos válidos: Cédula de Ciudadanía (CC), Cédula de Extranjería (CE), Tarjeta de Identidad (TI), Pasaporte (PA) y Registro Civil (RC).

## Criterios de Aceptación

Condición 01

Dado: que el gestor de personal está autenticado y completa el formulario seleccionando un tipo de documento de identidad colombiano válido, ingresando un número de documento de identidad que no existe en el sistema, junto con nombres, apellidos, cargo y departamento

Cuando: envía el formulario de registro

Entonces: el sistema genera automáticamente el número de identificación interno del empleado con el formato EMP-XXXXXX (donde XXXXXX es un número secuencial de 6 dígitos), guarda el registro en PostgreSQL con el departamento asignado, retorna HTTP 201 y muestra el número de identificación interno generado en la pantalla de confirmación

Condición 02

Dado: que el gestor de personal ingresa un tipo y número de documento de identidad

Cuando: el sistema encuentra que la combinación tipo de documento y número de documento de identidad ya está registrada en el sistema

Entonces: el sistema retorna HTTP 409 y muestra el mensaje "Ya existe un empleado registrado con el documento [tipo] número [número]"

Condición 03

Dado: que el gestor de personal selecciona un tipo de documento de identidad

Cuando: el tipo seleccionado no está en la lista de documentos colombianos válidos (CC, CE, TI, PA, RC)

Entonces: el sistema rechaza el envío y muestra el mensaje "Tipo de documento no válido. Los tipos permitidos son: CC, CE, TI, PA, RC"

Condición 04

Dado: que el gestor de personal completa el formulario

Cuando: alguno de los campos obligatorios está vacío (tipo de documento, número de documento, nombres, apellidos, cargo, departamento) o los nombres y apellidos tienen menos de 2 caracteres

Entonces: el sistema muestra los mensajes de error específicos debajo de cada campo inválido e impide el envío hasta que todos los errores sean corregidos

Condición 05

Dado: que el gestor de personal envía el formulario con datos válidos

Cuando: el sistema genera y asigna el número de identificación interno automáticamente

Entonces: el número de identificación interno sigue el formato EMP-XXXXXX, es único en el sistema y no puede ser modificado manualmente por el gestor de personal en ningún momento posterior

## Tareas

| No | Descripción |
|---|---|
| 1 | Diseñar formulario de registro de personal con campos: tipo de documento (select: CC, CE, TI, PA, RC), número de documento de identidad, nombres, apellidos, cargo, departamento (select), estado (select) |
| 2 | Implementar endpoint POST /api/personal en Spring Boot que reciba los datos del empleado sin el ID interno |
| 3 | Implementar lógica de generación automática de número de identificación interno con formato EMP-XXXXXX (secuencial, autoincremental desde 000001) |
| 4 | Validar unicidad de la combinación tipo de documento + número de documento de identidad antes de la inserción |
| 5 | Validar que el tipo de documento sea uno de los documentos colombianos permitidos (CC, CE, TI, PA, RC) |
| 6 | Validar campos obligatorios en frontend y backend (longitud mínima para nombres y apellidos) |
| 7 | Retornar HTTP 201 con el ID interno generado y mostrar notificación de éxito en el frontend |
| 8 | Manejar respuestas de error HTTP 409 para documento de identidad duplicado y HTTP 400 para tipo de documento inválido |

## Estado de Implementación

- **Backend**: ✓ — `POST /api/personal` (201, EMP-XXXXXX secuencial, unicidad tipo+número documento, validación de campos). Tests verdes.
- **Frontend**: ✓ — `RegisterEmployeeView` (`/personal/nuevo`, mockup 42) con foto opcional (HU-25) y notificación con el código generado.
- **Notas**: el formulario incluye campos adicionales del modelo ampliado (tipo de contrato, sede base, turno, fechas de contrato).

## Control de Versiones

| Versión | Fecha | Autor | Revisión | Descripción | Aprobador |
|---|---|---|---|---|---|
| 1.0 | 2026-07-26 | | | Versión inicial | |
