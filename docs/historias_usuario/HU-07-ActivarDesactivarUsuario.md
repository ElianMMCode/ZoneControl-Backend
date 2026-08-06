# HU-07 - ACTIVAR/DESACTIVAR USUARIO

| Campo | Valor |
|---|---|
| **Código** | HU-07 |
| **Nombre** | Activar/Desactivar Usuario |
| **Complejidad** | Media |
| **HU Relacionada** | HU-03, HU-05, HU-06 |
| **Módulo** | Módulo de Administración |
| **Rol** | Administrador |

## Descripción

**Yo como** administrador del sistema
**Requiero** activar o desactivar las cuentas de usuario del personal
**Para** controlar quién puede entrar al sistema sin necesidad de eliminar las cuentas

## Requerimiento

El administrador puede cambiar el estado de una cuenta entre activo e inactivo desde la lista de usuarios. Desactivar una cuenta impide que esa persona entre al sistema, pero no borra la cuenta: la persona puede volver a entrar cuando el administrador la reactive.

Cuando el administrador desactiva una cuenta, el acceso de esa persona se corta de inmediato. Si la persona estaba trabajando en el sistema en ese momento, deja de poder usarlo y, si intenta entrar de nuevo, el sistema le indica que su cuenta está desactivada. Para evitar equivocaciones, antes de desactivar se muestra una confirmación.

El sistema protege la cuenta del propio administrador: nadie puede desactivar su propia cuenta. Reactivar una cuenta es inmediato y la persona vuelve a poder iniciar sesión con su correo y su contraseña.

## Criterios de Aceptación

Condición 01

Dado: que el administrador selecciona un usuario con cuenta activa

Cuando: confirma la desactivación en la pantalla de confirmación

Entonces: el sistema cambia la cuenta a inactiva y esa persona ya no puede iniciar sesión

Condición 02

Dado: que el administrador desactivó la cuenta de un usuario

Cuando: ese usuario estaba conectado al sistema en ese momento

Entonces: el sistema le corta el acceso de inmediato y, si intenta entrar de nuevo, le muestra que su cuenta está desactivada

Condición 03

Dado: que el administrador selecciona un usuario con cuenta inactiva

Cuando: confirma la activación

Entonces: el sistema cambia la cuenta a activa y esa persona vuelve a poder iniciar sesión con su correo y su contraseña

Condición 04

Dado: que el administrador presiona el interruptor de activar/desactivar de un usuario

Cuando: ejecuta la acción

Entonces: el sistema muestra un diálogo de confirmación antes de realizar el cambio

Condición 05

Dado: que el administrador intenta desactivar su propia cuenta

Cuando: ejecuta la acción

Entonces: el sistema rechaza la operación y muestra el mensaje "No puede desactivar su propia cuenta"

Condición 06

Dado: que un usuario tiene la cuenta desactivada

Cuando: intenta iniciar sesión con su correo y su contraseña

Entonces: el sistema no le permite entrar y le indica que su cuenta está desactivada y que contacte al administrador

## Tareas

| No | Descripción |
|---|---|
| 1 | Colocar el interruptor de activar/desactivar en la lista de usuarios |
| 2 | Mostrar un diálogo de confirmación antes de desactivar una cuenta |
| 3 | Impedir que el administrador desactive su propia cuenta |
| 4 | Cortar el acceso de la persona de inmediato al desactivar su cuenta |
| 5 | Permitir reactivar la cuenta para que la persona vuelva a entrar |

## Control de Versiones

| Versión | Fecha | Autor | Revisión | Descripción | Aprobador |
|---|---|---|---|---|---|
| 1.0 | 2026-08-06 | | | Revisión de lenguaje y criterios detallados | |

## Estado de Implementación

- **Backend**: ✓ — `PATCH /api/admin/users/{id}/status` con protección de auto-desactivación. La invalidez inmediata de la sesión se logra porque el filtro de autenticación valida `status == ACTIVO` en cada petición (usuario desactivado → 401). Test: `JwtInvalidationTest`. La desactivación también suspende al empleado vinculado y sus permisos.
- **Frontend**: ✓ — `UsersView` con toggle de activar/desactivar y diálogo de confirmación.
