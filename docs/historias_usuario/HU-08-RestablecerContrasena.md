# HU-08 - RESTABLECER CONTRASEÑA

| Campo | Valor |
|---|---|
| **Código** | HU-08 |
| **Nombre** | Restablecer Contraseña |
| **Complejidad** | Media |
| **HU Relacionada** | HU-03, HU-05 |
| **Módulo** | Módulo de Administración |
| **Rol** | Administrador |

## Descripción

**Yo como** administrador del sistema
**Requiero** restablecer la contraseña de un usuario que olvidó la suya o la quiere cambiar
**Para** que la persona recupere el acceso al sistema eligiendo su propia contraseña

## Requerimiento

Cuando una persona no puede entrar porque olvidó su contraseña, el administrador puede elegir la opción "Restablecer contraseña" en la cuenta de ese usuario. El sistema deja sin efecto la contraseña actual y genera un enlace de activación temporal que expira a las 24 horas.

El administrador nunca ve ni recibe la contraseña nueva. El enlace se abre en una ventana nueva con la página de configuración, donde la persona define su nueva contraseña. Cuando la persona guarda su nueva contraseña, ya puede iniciar sesión con su correo y esa contraseña.

Si el enlace no se usa dentro de las 24 horas, deja de ser válido y la persona debe pedirle al administrador que genere uno nuevo. El sistema no ofrece un flujo público de "olvidé mi contraseña": solo el administrador puede iniciar un restablecimiento.

## Criterios de Aceptación

Condición 01

Dado: que el administrador selecciona un usuario en la lista

Cuando: elige "Restablecer contraseña" y confirma la acción en el diálogo

Entonces: el sistema deja sin efecto la contraseña actual, genera un enlace de activación temporal que expira en 24 horas y muestra el mensaje "Enlace de configuración enviado al correo del usuario"

Condición 02

Dado: que el administrador elige "Restablecer contraseña"

Cuando: presiona la opción

Entonces: el sistema muestra un diálogo de confirmación indicando que se enviará un enlace de configuración al correo del usuario, que la contraseña actual dejará de ser válida y que el enlace expirará en 24 horas

Condición 03

Dado: que el usuario abre el enlace de restablecimiento

Cuando: lo hace dentro de las 24 horas y define su nueva contraseña

Entonces: el sistema guarda la nueva contraseña y a partir de ese momento el usuario inicia sesión con su correo y esa contraseña

Condición 04

Dado: que el usuario intenta usar el enlace de restablecimiento

Cuando: han pasado más de 24 horas desde que se generó o el enlace ya fue usado

Entonces: el sistema muestra el mensaje "El enlace de configuración ha expirado. Contacte al administrador para generar un nuevo enlace" y el usuario debe pedirle un nuevo enlace al administrador

Condición 05

Dado: que el administrador elige restablecer la contraseña de un usuario

Cuando: el empleado vinculado a esa cuenta no tiene un correo electrónico registrado en Gestión de Personal

Entonces: el sistema muestra el mensaje "El empleado no tiene un correo registrado. Regístrelo en Gestión Personal para restablecer la contraseña" y no genera el enlace

Condición 06

Dado: que un usuario olvidó su contraseña

Cuando: busca en el sistema una opción pública para recuperarla por su cuenta

Entonces: el sistema no ofrece esa opción: el restablecimiento solo lo puede iniciar el administrador

## Tareas

| No | Descripción |
|---|---|
| 1 | Agregar la opción "Restablecer contraseña" en la ficha de cada usuario |
| 2 | Mostrar un diálogo de confirmación antes de restablecer |
| 3 | Dejar sin efecto la contraseña actual y generar un enlace temporal de 24 horas |
| 4 | Abrir la página de configuración de contraseña en una ventana nueva |
| 5 | Reutilizar la página donde la persona define su nueva contraseña |
| 6 | Validar que el empleado tenga correo registrado antes de generar el enlace |

## Control de Versiones

| Versión | Fecha | Autor | Revisión | Descripción | Aprobador |
|---|---|---|---|---|---|
| 1.0 | 2026-08-06 | | | Revisión de lenguaje y criterios detallados | |

## Estado de Implementación

- **Backend**: ✓ — `POST /api/admin/users/{id}/reset-password` invalida la contraseña (`password=null`), genera un `setupToken` (48 bytes aleatorios, hash SHA-256, expiración 24 h) y devuelve el campo `setupUrl`; reutiliza `GET/POST /api/setup-password` y `MagicLinkNotifier` (sin SMTP). No existe flujo público de recuperación. Tests verdes (`AdminUserControllerTest`).
- **Frontend**: ✓ — `UsersView` (mockup 31): "Restablecer" abre un diálogo de confirmación y luego `SetupPasswordView` (`/configurar-contrasena?token=`) en una ventana nueva.
