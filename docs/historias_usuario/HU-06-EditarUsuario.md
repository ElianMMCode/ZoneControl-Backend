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

El sistema debe permitir al administrador modificar el **correo electrónico** y el **estado (Activo/Inactivo)** de un usuario interno existente, validando el nuevo email contra duplicados. El resto de los datos del usuario **no son editables aquí**:

- **Nombre, apellido y cargo**: reflejan al `Employee` vinculado y se gestionan en **Gestión de Personal** (HU-09/14). No le corresponden al administrador modificarlos.
- **Rol**: se asigna en la creación del usuario (HU-05).

## Criterios de Aceptación

Condición 01

Dado: que el administrador selecciona un usuario de la lista

Cuando: modifica el correo o el estado y guarda los cambios

Entonces: el sistema valida los nuevos datos, actualiza el registro en PostgreSQL, retorna HTTP 200 y muestra confirmación de actualización

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

Entonces: nombre, apellido, rol y código del empleado se muestran en **solo lectura** y únicamente los campos email y estado son editables

## Tareas

| No | Descripción |
|---|---|
| 1 | Diseñar formulario de edición con datos del usuario en solo lectura (nombre, apellido, rol, código) y campos email + estado editables |
| 2 | Implementar endpoint PUT /api/admin/users/{id} que actualiza email y estado (con guard de auto-desactivación y cascada a empleado/permisos) |
| 3 | Validar que el nuevo email no esté en uso por otro usuario antes de actualizar |
| 4 | Implementar validaciones en frontend (formato de email) |
| 5 | Actualizar la fila correspondiente en la tabla de usuarios tras edición exitosa |
| 6 | Manejar respuestas de error 404 (usuario no encontrado) y 409 (email duplicado) |

## Estado de Implementación

- **Backend**: ✓ — `PUT /api/admin/users/{id}` con `UpdateUserRequest { email, status }`. El servicio actualiza el correo y aplica el cambio de estado con la misma cascada del toggle (HU-07): rechaza desactivar la propia cuenta y suspende/restaura empleado y permisos.
- **Frontend**: ✓ — `UserFormModal` con datos en solo lectura + campos email y estado editables.
- **Nota relacionada**: la tabla de usuarios (`UserTable`) incluye una columna **"Activación"** (Pendiente/Completada según `setupToken`) y la tarjeta de candidatos se llama **"Pendientes de activación de usuario"** (empleados sin cuenta de sistema).

## Control de Versiones

| Versión | Fecha | Autor | Revisión | Descripción | Aprobador |
|---|---|---|---|---|---|
| 1.0 | 2026-07-26 | | | Versión inicial | |
| 1.1 | 2026-08-04 | | | Solo correo editable (nombre/apellido/cargo del Employee; rol solo en creación) | |
| 1.2 | 2026-08-04 | | | El estado vuelve a ser editable (email + estado); la edición reutiliza la cascada y el guard de auto-desactivación del toggle | |
