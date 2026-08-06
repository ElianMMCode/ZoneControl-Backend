# HU-26 - Gestionar mi Perfil y Cambiar mi Contraseña

| Campo | Valor |
|---|---|
| **Código** | HU-26 |
| **Nombre** | Gestionar mi perfil y cambiar mi contraseña |
| **Complejidad** | Baja |
| **HU Relacionada** | HU-03 |
| **Módulo** | Módulo de Autenticación |
| **Rol** | Administrador, Gestor de Personal, Supervisor / Auditor |

## Descripción

**Yo como** usuario interno
**Requiero** consultar mis datos personales y cambiar mi contraseña cuando lo necesite
**Para** mantener mi información actualizada y proteger el acceso a mi cuenta

## Requerimiento

Todo usuario del sistema (administrador, gestor o supervisor) debe poder acceder a una sección de ajustes desde la que ve sus datos (nombre, correo y rol) y puede cambiar su contraseña. Para cambiar la contraseña debe indicar su contraseña actual y confirmar la nueva; la nueva contraseña debe cumplir con las reglas de seguridad definidas por el sistema. Si la contraseña actual es incorrecta, el sistema no permite el cambio y lo indica claramente.

## Criterios de Aceptación

Condición 01

Dado: que un usuario inicia sesión

Cuando: ingresa a la sección de ajustes

Entonces: puede ver sus datos personales (nombre, correo y rol) sin posibilidad de editarlos desde aquí

Condición 02

Dado: que el usuario quiere cambiar su contraseña

Cuando: ingresa su contraseña actual, la contraseña nueva y la confirmación

Entonces: el sistema valida que la contraseña actual sea correcta y que la nueva cumpla las reglas de seguridad

Condición 03

Dado: que la contraseña actual es incorrecta

Cuando: el usuario intenta cambiar la contraseña

Entonces: el sistema muestra el mensaje de que la contraseña actual no es correcta y no realiza el cambio

Condición 04

Dado: que la contraseña actual es correcta y la nueva cumple las reglas

Cuando: el usuario confirma el cambio

Entonces: el sistema guarda la nueva contraseña y a partir de ese momento es la que se usa para iniciar sesión

Condición 05

Dado: que el usuario cambió su contraseña

Cuando: vuelve a iniciar sesión

Entonces: debe usar la contraseña nueva

## Tareas

| No | Descripción |
|---|---|
| 1 | Mostrar los datos personales del usuario en la sección de ajustes |
| 2 | Validar la contraseña actual antes de permitir el cambio |
| 3 | Validar que la nueva contraseña cumpla las reglas de seguridad |
| 4 | Guardar la nueva contraseña de forma segura |

## Control de Versiones

| Versión | Fecha | Autor | Revisión | Descripción | Aprobador |
|---|---|---|---|---|---|
| 1.0 | 2026-08-06 | | | Historia nueva: ajustes de perfil y cambio de contraseña implementados | |

## Estado de Implementación

- **Backend**: ✓ — `POST /api/auth/change-password` (requiere la contraseña actual) y consulta del perfil.
- **Frontend**: ✓ — sección de ajustes (`/ajustes`) accesible para los tres roles.
- **Tests**: ✓ — cambio de contraseña correcto e incorrecto en `AuthControllerTest`.
