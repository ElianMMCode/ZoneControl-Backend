# HU-05 - EDITAR USUARIO INTERNO

| Campo | Valor |
|---|---|
| **Código** | HU-05 |
| **Nombre** | Editar Usuario Interno |
| **Complejidad** | Media |
| **HU Relacionada** | HU-03, HU-04, HU-06 |
| **Módulo** | Módulo de Administración |
| **Rol** | Administrador |

## Descripción

**Yo como** administrador del sistema
**Requiero** corregir el correo electrónico de un usuario o cambiar su estado (activo o inactivo)
**Para** mantener actualizados los datos de acceso del personal

## Requerimiento

El administrador puede editar dos datos de un usuario existente: el correo electrónico con el que inicia sesión y su estado (activo o inactivo). Al guardar, el sistema verifica que el nuevo correo no le pertenezca a otro usuario; si ya está en uso, avisa con un mensaje claro y no guarda el cambio.

Los demás datos de la cuenta no se editan aquí. El rol se elige solo al momento de crear la cuenta y no puede cambiarse después. El nombre, los apellidos y el cargo del usuario provienen del empleado vinculado y se gestionan en Gestión de Personal, no en esta pantalla.

Cambiar el estado a "inactivo" desactiva la cuenta de inmediato y sigue las mismas reglas de la historia HU-06. Si el administrador desactiva su propia cuenta, el sistema se lo impide.

## Criterios de Aceptación

Condición 01

Dado: que el administrador abre la edición de un usuario

Cuando: modifica el correo electrónico o el estado y guarda los cambios

Entonces: el sistema guarda el cambio, muestra una confirmación y actualiza la lista de usuarios

Condición 02

Dado: que el administrador cambia el correo de un usuario

Cuando: el nuevo correo ya pertenece a otro usuario

Entonces: el sistema muestra el mensaje "El email ya está registrado", no guarda el cambio y deja el correo anterior

Condición 03

Dado: que el administrador abre la edición de un usuario

Cuando: observa el formulario

Entonces: el sistema muestra en solo lectura el nombre, los apellidos, el cargo, el rol y el código del empleado, y solo permite editar el correo y el estado

Condición 04

Dado: que el administrador intenta editar el rol de un usuario

Cuando: busca la opción para cambiarlo

Entonces: el sistema no ofrece esa opción, ya que el rol solo se define al crear la cuenta

Condición 05

Dado: que el administrador cambia el estado de un usuario a inactivo

Cuando: guarda el cambio

Entonces: el sistema desactiva la cuenta de inmediato con las mismas reglas de la historia HU-06 (el usuario deja de poder entrar y debe volver a iniciar sesión)

Condición 06

Dado: que el administrador intenta desactivar su propia cuenta

Cuando: guarda el cambio de estado

Entonces: el sistema se lo impide y muestra el mensaje "No puede desactivar su propia cuenta"

## Tareas

| No | Descripción |
|---|---|
| 1 | Diseñar el formulario de edición con el correo y el estado editables y el resto de los datos en solo lectura |
| 2 | Permitir modificar el correo electrónico del usuario |
| 3 | Permitir cambiar el estado activo / inactivo |
| 4 | Validar que el nuevo correo no esté en uso por otro usuario |
| 5 | Impedir que el administrador desactive su propia cuenta |
| 6 | Mostrar confirmación al guardar y actualizar la lista de usuarios |

## Control de Versiones

| Versión | Fecha | Autor | Revisión | Descripción | Aprobador |
|---|---|---|---|---|---|
| 1.0 | 2026-08-06 | | | Revisión de lenguaje y criterios detallados | |

## Estado de Implementación

- **Backend**: ✓ — `PUT /api/admin/users/{id}` con `UpdateUserRequest { email, status }`; valida el correo duplicado (409) y aplica el cambio de estado con la misma cascada y el guard de auto-desactivación de HU-06. Tests verdes (`AdminUserControllerTest`).
- **Frontend**: ✓ — `UserFormModal` con nombre/apellido/cargo/rol/código en solo lectura y campos de correo y estado editables; `UsersView` incluye la columna "Activación" (Pendiente/Completada).
