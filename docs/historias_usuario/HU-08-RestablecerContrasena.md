# HU-08 - RESTABLECER CONTRASEÑA

| Campo | Valor |
|---|---|
| **Código** | HU-08 |
| **Nombre** | Restablecer Contraseña |
| **Complejidad** | Media |
| **HU Relacionada** | HU-03, HU-05 |
| **Módulo** | Módulo de Administración |

## Descripción

**Yo como** administrador del sistema
**Requiero** restablecer la contraseña de un usuario que ha olvidado la suya
**Para** permitirle acceder nuevamente al sistema con una credencial robusta que él mismo elija

## Requerimiento

El sistema debe permitir al administrador restablecer la contraseña de un usuario sin conocer ni generar la nueva contraseña. Al restablecer, el sistema invalida la contraseña actual (password = null), genera un token de un solo uso (setupToken) con expiración de 24h, lo almacena hasheado en BD y envía un **magic link** al correo personal del empleado para que el propio usuario establezca su nueva contraseña. Se alinea con la misma política de seguridad de la creación de usuarios (HU-05) y con NIST SP 800-63B: la contraseña nunca es conocida por el administrador ni viaja por email.

## Criterios de Aceptación

Condición 01

Dado: que el administrador está autenticado y selecciona un usuario de la lista

Cuando: presiona "Restablecer Contraseña" y confirma la acción en el diálogo

Entonces: el sistema invalida la contraseña actual (password = null), genera un setupToken criptográfico aleatorio de 96 caracteres hex, lo hashea con SHA-256, lo almacena en la columna setupToken con setupTokenExpiry = now() + 24h, envía el magic link al correo personal del empleado y muestra el mensaje "Enlace de configuración enviado al correo del usuario"

Condición 02

Dado: que el administrador selecciona un usuario

Cuando: presiona "Restablecer Contraseña"

Entonces: el sistema muestra un diálogo de confirmación con el texto "Se enviará un enlace de configuración al correo [email] del usuario [nombre]. La contraseña actual dejará de ser válida y el enlace expirará en 24 horas"

Condición 03

Dado: que el usuario recibe el email con el magic link

Cuando: hace clic en el enlace dentro de las 24 horas

Entonces: el sistema valida el setupToken contra el hash almacenado en BD, verifica que setupTokenExpiry no haya vencido, redirige al usuario a la pantalla de configuración de contraseña donde debe ingresar una nueva contraseña que cumpla los requisitos (mínimo 8 caracteres, al menos 1 mayúscula, 1 minúscula, 1 dígito, 1 carácter especial @$!%*?&). Al enviar, el sistema encripta la contraseña con BCrypt, la guarda en la columna password, limpia setupToken y setupTokenExpiry, marca requirePasswordChange = false y permite al usuario iniciar sesión con su nueva contraseña

Condición 04

Dado: que el usuario intenta usar el magic link de restablecimiento

Cuando: han pasado más de 24 horas desde el restablecimiento o el enlace ya fue usado

Entonces: el sistema muestra el mensaje "El enlace de configuración ha expirado. Contacte al administrador para generar un nuevo enlace" y el usuario debe solicitar un nuevo restablecimiento al administrador

Condición 05

Dado: que el administrador solicita restablecer la contraseña de un usuario

Cuando: el empleado vinculado al usuario no tiene un correo personal registrado en Gestión Personal

Entonces: el sistema retorna HTTP 400 y muestra el mensaje "El empleado no tiene un correo registrado. Regístrelo en Gestión Personal para restablecer la contraseña"

## Tareas

| No | Descripción |
|---|---|
| 1 | Implementar endpoint POST /admin/users/{id}/reset-password en Spring Boot |
| 2 | Al restablecer: validar que el empleado tenga email, generar setupToken criptográfico de 96 caracteres hex, hashearlo con SHA-256, guardarlo con setupTokenExpiry = now() + 24h, anular password y no enviar ninguna contraseña temporal al administrador |
| 3 | Reutilizar el servicio MagicLinkNotifier para enviar el enlace al correo personal del empleado; la contraseña nunca viaja por email ni es visible para el administrador |
| 4 | Mostrar diálogo de confirmación en el frontend antes de ejecutar el restablecimiento y notificación de éxito con "Enlace de configuración enviado al correo del usuario" |
| 5 | Reutilizar la pantalla de configuración de contraseña del flujo de creación (HU-05) para completar el restablecimiento; validar que la nueva contraseña cumpla los mismos requisitos de seguridad antes de guardarla con BCrypt |
| 6 | Manejar respuestas de error: 404 para usuario inexistente, 400 para empleado sin correo, 410 para token expirado, 404 para token inválido |

## Control de Versiones

| Versión | Fecha | Autor | Revisión | Descripción | Aprobador |
|---|---|---|---|---|---|
| 1.0 | 2026-07-26 | | | Versión inicial (contraseña temporal visible al admin) | |
| 1.1 | 2026-07-31 | | | Alineada con HU-05: restablecimiento vía magic link, sin contraseña temporal visible al administrador | |
