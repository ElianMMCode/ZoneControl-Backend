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
**Requiero** modificar el correo de un usuario interno existente
**Para** corregir el email de acceso al sistema

## Requerimiento

El sistema debe permitir al administrador modificar **únicamente el correo electrónico** de un usuario interno existente, validando el nuevo email contra duplicados. El resto de los datos del usuario **no son editables aquí**:

- **Nombre, apellido y cargo**: reflejan al `Employee` vinculado y se gestionan en **Gestión de Personal** (HU-09/14). No le corresponden al administrador modificarlos.
- **Rol**: se asigna en la creación del usuario (HU-05).
- **Estado**: se cambia con el toggle Activar/Desactivar (`PATCH /api/admin/users/{id}/status`, HU-07).

## Criterios de Aceptación

Condición 01

Dado: que el administrador selecciona un usuario de la lista

Cuando: modifica el correo y guarda los cambios

Entonces: el sistema valida el nuevo email, actualiza el registro en PostgreSQL, retorna HTTP 200 y muestra confirmación de actualización

Condición 02

Dado: que el administrador modifica el email

Cuando: el nuevo email ya pertenece a otro usuario

Entonces: el sistema retorna error 409 con el mensaje "El email ya está registrado"

Condición 03

Dado: que el administrador intenta editar un usuario

Cuando: el usuario ya no existe en el sistema

Entonces: el sistema retorna error 404 con el mensaje "Usuario no encontrado"

Condición 04

Dado: que el administrador abre el formulario de edición

Cuando: observa los campos

Entonces: nombre, apellido, rol, estado y código del empleado se muestran en **solo lectura** y únicamente el campo email es editable

## Tareas

| No | Descripción |
|---|---|
| 1 | Diseñar formulario de edición con datos del usuario en solo lectura (nombre, apellido, rol, estado, código) y solo el campo email editable |
| 2 | Implementar endpoint PUT /api/admin/users/{id} que únicamente actualiza el email |
| 3 | Validar que el nuevo email no esté en uso por otro usuario antes de actualizar |
| 4 | Implementar validaciones en frontend (formato de email) |
| 5 | Actualizar la fila correspondiente en la tabla de usuarios tras edición exitosa |
| 6 | Manejar respuestas de error 404 (usuario no encontrado) y 409 (email duplicado) |

## Estado de Implementación

- **Backend**: ✓ — `PUT /api/admin/users/{id}` con `UpdateUserRequest` que solo acepta `email` (`@NotBlank @Email`). El servicio únicamente actualiza el correo.
- **Frontend**: ✓ — `UserFormModal` con datos en solo lectura + campo email editable.
- **Nota relacionada**: la tabla de usuarios (`UserTable`) incluye una columna **"Activación"** (Pendiente/Completada según `setupToken`) y la tarjeta de candidatos se llama **"Pendientes de activación de usuario"** (empleados sin cuenta de sistema).

## Control de Versiones

| Versión | Fecha | Autor | Revisión | Descripción | Aprobador |
|---|---|---|---|---|---|
| 1.0 | 2026-07-26 | | | Versión inicial | |
| 1.1 | 2026-08-04 | | | Solo el correo es editable: nombre/apellido/cargo vienen del Employee (Gestión de Personal); rol solo en creación; estado vía toggle HU-07 | |
