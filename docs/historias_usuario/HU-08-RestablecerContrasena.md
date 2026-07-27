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
**Requiero** generar una contraseña temporal que cumpla con los requisitos de seguridad para un usuario que ha olvidado la suya
**Para** permitirle acceder nuevamente al sistema con una credencial robusta y obligarlo a cambiarla en su próximo inicio de sesión

## Requerimiento

El sistema debe permitir al administrador restablecer la contraseña de un usuario generando una contraseña temporal aleatoria que cumpla con los siguientes requisitos de seguridad: mínimo 8 caracteres de longitud, al menos una letra mayúscula (A-Z), al menos una letra minúscula (a-z), al menos un dígito numérico (0-9) y al menos un carácter especial (@$!%*?&). La contraseña temporal debe encriptarse con BCrypt antes de guardarse, mostrarse al administrador una sola vez y el sistema debe forzar al usuario a cambiarla en su primer inicio de sesión posterior al restablecimiento.

## Criterios de Aceptación

Condición 01

Dado: que el administrador está autenticado y selecciona un usuario de la lista

Cuando: presiona "Restablecer Contraseña", confirma la acción en el diálogo y el sistema procesa la solicitud

Entonces: el sistema genera una contraseña temporal aleatoria que cumple con los requisitos de seguridad (mínimo 8 caracteres, 1 mayúscula, 1 minúscula, 1 dígito, 1 carácter especial), la encripta con BCrypt, la guarda en PostgreSQL, la muestra al administrador en pantalla e internamente marca la cuenta del usuario con el flag "requiere cambio de contraseña"

Condición 02

Dado: que el administrador selecciona un usuario

Cuando: presiona "Restablecer Contraseña"

Entonces: el sistema muestra un diálogo de confirmación con el texto "Se generará una contraseña temporal para el usuario [nombre]. La contraseña actual dejará de ser válida y el usuario deberá cambiarla en su próximo inicio de sesión"

Condición 03

Dado: que el administrador cierra el diálogo de confirmación de restablecimiento

Cuando: la contraseña temporal ya fue mostrada en pantalla

Entonces: el sistema no permite volver a ver la contraseña generada en ninguna otra pantalla ni en el historial, por razones de seguridad

Condición 04

Dado: que el usuario intenta iniciar sesión con la contraseña temporal generada por el administrador

Cuando: el sistema detecta el flag "requiere cambio de contraseña" activo

Entonces: el sistema redirige al usuario a una pantalla de cambio de contraseña obligatorio donde debe ingresar una nueva contraseña que cumpla con los requisitos de seguridad antes de acceder al dashboard de su rol

## Tareas

| No | Descripción |
|---|---|
| 1 | Implementar generador de contraseña temporal aleatoria que cumpla los requisitos: mínimo 8 caracteres, 1 mayúscula, 1 minúscula, 1 dígito, 1 carácter especial (@$!%*?&) |
| 2 | Implementar endpoint POST /api/admin/users/{id}/reset-password en Spring Boot |
| 3 | Encriptar la nueva contraseña temporal con BCrypt antes de guardar en PostgreSQL |
| 4 | Agregar flag booleano "requiereCambioContrasena" en la tabla de usuarios y activarlo al restablecer |
| 5 | Mostrar diálogo de confirmación en el frontend antes de ejecutar el restablecimiento |
| 6 | Mostrar la contraseña temporal generada al administrador una sola vez y no almacenarla en texto plano en ningún log ni historial |
| 7 | Implementar pantalla de cambio obligatorio de contraseña en el flujo de inicio de sesión cuando el flag esté activo |
| 8 | Validar que la nueva contraseña ingresada por el usuario cumpla con los mismos requisitos de seguridad antes de guardarla |

## Control de Versiones

| Versión | Fecha | Autor | Revisión | Descripción | Aprobador |
|---|---|---|---|---|---|
| 1.0 | 2026-07-26 | | | Versión inicial | |
