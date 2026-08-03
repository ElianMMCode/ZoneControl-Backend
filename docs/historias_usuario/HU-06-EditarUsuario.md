# HU-06 - EDITAR USUARIO INTERNO

| Campo | Valor |
|---|---|
| **Código** | HU-06 |
| **Nombre** | Editar Usuario Interno |
| **Complejidad** | Alta |
| **HU Relacionada** | HU-03, HU-05 |
| **Módulo** | Módulo de Administración |

## Descripción

**Yo como** administrador del sistema
**Requiero** modificar los datos de un usuario interno existente
**Para** mantener la información actualizada y corregir errores en los registros

## Requerimiento

El sistema debe permitir al administrador modificar los datos de usuarios internos existentes, incluyendo nombre completo, email, rol y estado. El nuevo email debe validarse contra duplicados y los cambios deben reflejarse en el próximo inicio de sesión del usuario.

## Criterios de Aceptación

Condición 01

Dado: que el administrador selecciona un usuario de la lista

Cuando: modifica campos y guarda los cambios

Entonces: el sistema valida los nuevos datos, actualiza el registro en PostgreSQL, retorna HTTP 200 y muestra confirmación de actualización

Condición 02

Dado: que el administrador modifica el email

Cuando: el nuevo email ya pertenece a otro usuario

Entonces: el sistema retorna error 409 con el mensaje "Email ya registrado"

Condición 03

Dado: que el administrador intenta editar un usuario

Cuando: el usuario ya no existe en el sistema

Entonces: el sistema retorna error 404 con el mensaje "Usuario no encontrado"

## Tareas

| No | Descripción |
|---|---|
| 1 | Diseñar formulario de edición con datos precargados del usuario seleccionado |
| 2 | Implementar endpoint PUT /api/admin/users/{id} en Spring Boot |
| 3 | Validar que el nuevo email no esté en uso por otro usuario antes de actualizar |
| 4 | Implementar validaciones en frontend (campos obligatorios, formato de email) |
| 5 | Actualizar la fila correspondiente en la tabla de usuarios tras edición exitosa |
| 6 | Manejar respuestas de error 404 (usuario no encontrado) y 409 (email duplicado) |

## Control de Versiones

| Versión | Fecha | Autor | Revisión | Descripción | Aprobador |
|---|---|---|---|---|---|
| 1.0 | 2026-07-26 | | | Versión inicial | |
