# HU-03 - INICIAR SESIÓN

| Campo | Valor |
|---|---|
| **Código** | HU-03 |
| **Nombre** | Iniciar Sesión |
| **Complejidad** | Alta |
| **HU Relacionada** | HU-05, HU-09, HU-15 |
| **Módulo** | Módulo de Autenticación |

## Descripción

**Yo como** usuario interno del sistema (Administrador, Gestor de Personal o Supervisor/Auditor)
**Requiero** autenticarme con mi correo electrónico y contraseña
**Para** acceder a las funcionalidades del sistema según el rol que tengo asignado

## Requerimiento

Control de acceso al sistema mediante autenticación de usuario y contraseña, con permisos diferenciados según el rol asignado. El sistema debe validar credenciales contra la base de datos, generar token JWT y redirigir al dashboard correspondiente.

## Criterios de Aceptación

Condición 01

Dado: que el usuario tiene credenciales válidas

Cuando: ingresa email y contraseña correctos

Entonces: el sistema valida las credenciales contra PostgreSQL, genera un token JWT y redirige al dashboard según el rol (Administrador, Gestor de Personal, Supervisor/Auditor)

Condición 02

Dado: que el usuario ingresa credenciales

Cuando: el email o la contraseña son incorrectos

Entonces: el sistema muestra el mensaje "Credenciales incorrectas"

Condición 03

Dado: que el usuario ingresa un email

Cuando: el email no existe en el sistema

Entonces: el sistema muestra el mensaje "Usuario no registrado"

Condición 04

Dado: que el usuario tiene una cuenta desactivada

Cuando: intenta iniciar sesión

Entonces: el sistema muestra el mensaje "Cuenta desactivada, contacte al administrador"

## Tareas

| No | Descripción |
|---|---|
| 1 | Diseñar formulario de login con campos de email y contraseña en el frontend |
| 2 | Implementar endpoint POST /api/auth/login en Spring Boot |
| 3 | Configurar validación de credenciales contra tabla de usuarios en PostgreSQL |
| 4 | Implementar encriptación de contraseñas con BCrypt |
| 5 | Implementar generación de token JWT con clave secreta y tiempo de expiración |
| 6 | Implementar redirección a dashboard según rol del usuario (Admin, Gestor, Supervisor) |
| 7 | Manejar códigos de respuesta HTTP: 200 (éxito), 401 (credenciales incorrectas), 403 (cuenta desactivada), 404 (usuario no encontrado) |

## Control de Versiones

| Versión | Fecha | Autor | Revisión | Descripción | Aprobador |
|---|---|---|---|---|---|
| 1.0 | 2026-07-26 | | | Versión inicial | |
