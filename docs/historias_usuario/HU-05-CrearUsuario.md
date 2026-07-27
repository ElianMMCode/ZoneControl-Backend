# HU-05 - CREAR USUARIO INTERNO

| Campo | Valor |
|---|---|
| **Código** | HU-05 |
| **Nombre** | Crear Usuario Interno |
| **Complejidad** | Alta |
| **HU Relacionada** | HU-03, HU-06, HU-07, HU-08 |
| **Módulo** | Módulo de Administración |

## Descripción

**Yo como** administrador del sistema
**Requiero** crear nuevos usuarios internos con roles específicos, datos completos y una contraseña que cumpla con los requisitos de seguridad establecidos
**Para** que otros usuarios puedan acceder al sistema según sus funciones asignadas con credenciales robustas

## Requerimiento

El sistema debe permitir al administrador crear usuarios internos del sistema, asignándoles un rol (Administrador, Gestor de Personal o Supervisor/Auditor) y un estado inicial (Activo/Inactivo). El email debe ser único en el sistema y la contraseña debe cumplir con los siguientes requisitos de seguridad antes de ser encriptada y almacenada: mínimo 8 caracteres de longitud, al menos una letra mayúscula (A-Z), al menos una letra minúscula (a-z), al menos un dígito numérico (0-9) y al menos un carácter especial (@$!%*?&).

## Criterios de Aceptación

Condición 01

Dado: que el administrador está autenticado y completa el formulario con todos los datos válidos, un email único y una contraseña que cumple con los requisitos de seguridad

Cuando: envía el formulario de creación

Entonces: el sistema valida los datos, verifica los requisitos de la contraseña, la encripta con BCrypt, guarda el usuario en PostgreSQL, retorna HTTP 201 y actualiza la lista de usuarios en el frontend

Condición 02

Dado: que el administrador ingresa un email

Cuando: el email ya está registrado en el sistema

Entonces: el sistema retorna HTTP 409 y muestra el mensaje "El email ingresado ya se encuentra registrado en el sistema"

Condición 03

Dado: que el administrador ingresa una contraseña

Cuando: la contraseña no cumple con alguno de los requisitos de seguridad (menos de 8 caracteres, sin mayúscula, sin minúscula, sin dígito o sin carácter especial)

Entonces: el sistema rechaza el envío del formulario y muestra un mensaje detallando los requisitos que no se cumplen: "La contraseña debe tener mínimo 8 caracteres, al menos una letra mayúscula, una letra minúscula, un número y un carácter especial (@$!%*?&)"

Condición 04

Dado: que el administrador envía el formulario de creación

Cuando: hay campos obligatorios vacíos (nombre completo, email, contraseña, confirmar contraseña, rol, estado) o las contraseñas no coinciden

Entonces: el sistema muestra los mensajes de error específicos debajo de cada campo inválido e impide el envío hasta que todos los errores sean corregidos

## Tareas

| No | Descripción |
|---|---|
| 1 | Diseñar formulario de creación de usuario con campos: nombre completo, email, contraseña, confirmar contraseña, rol (select), estado (select) |
| 2 | Implementar validación de requisitos de contraseña en frontend: mínimo 8 caracteres, al menos 1 mayúscula, 1 minúscula, 1 dígito, 1 carácter especial (@$!%*?&) |
| 3 | Implementar validación de coincidencia entre contraseña y confirmar contraseña en frontend |
| 4 | Implementar endpoint POST /api/admin/users en Spring Boot con validación de requisitos de contraseña en backend |
| 5 | Validar unicidad del email consultando PostgreSQL antes de la inserción |
| 6 | Encriptar la contraseña con BCrypt una vez superadas todas las validaciones |
| 7 | Retornar HTTP 201 con datos del usuario creado y mostrar notificación de éxito en el frontend |
| 8 | Manejar respuestas de error: 409 para email duplicado y 400 para contraseña inválida o campos faltantes |

## Control de Versiones

| Versión | Fecha | Autor | Revisión | Descripción | Aprobador |
|---|---|---|---|---|---|
| 1.0 | 2026-07-26 | | | Versión inicial | |
