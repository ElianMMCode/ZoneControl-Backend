# HU-03 - INICIAR SESIÓN

| Campo | Valor |
|---|---|
| **Código** | HU-03 |
| **Nombre** | Iniciar Sesión |
| **Complejidad** | Alta |
| **HU Relacionada** | HU-04, HU-05, HU-06, HU-07 |
| **Módulo** | Módulo de Autenticación |
| **Rol** | Administrador, Gestor de Personal, Supervisor / Auditor |

## Descripción

**Yo como** usuario interno del sistema (Administrador, Gestor de Personal o Supervisor / Auditor)
**Requiero** identificarme con mi correo electrónico y mi contraseña
**Para** ingresar al sistema y usar las funciones que corresponden a mi rol

## Requerimiento

El sistema controla quién puede ingresar. Los usuarios internos cuentan con un correo electrónico y una contraseña con los que se identifican al iniciar sesión. El ingreso se realiza desde una pantalla de inicio de sesión con dos campos: correo electrónico y contraseña.

Cuando el usuario ingresa sus datos correctamente, el sistema lo deja entrar y lo lleva directamente a su panel de trabajo según su rol: el Administrador llega a su panel de administración, el Gestor de Personal a la gestión de personal y el Supervisor / Auditor a su panel de supervisión. Así, cada quien ve solo lo que le corresponde.

Si los datos no son correctos, el sistema avisa con un mensaje claro y diferenciado: si el correo no está registrado indica que el usuario no está registrado; si la contraseña no coincide indica credenciales incorrectas; y si la cuenta está desactivada lo comunica para que el usuario contacte al administrador. Además, si una persona ya tiene una sesión iniciada y vuelve a la pantalla de inicio de sesión, el sistema la redirige directamente a su panel.

## Criterios de Aceptación

Condición 01

Dado: que el usuario tiene un correo y una contraseña válidos

Cuando: ingresa su correo y su contraseña en la pantalla de inicio de sesión

Entonces: el sistema lo deja entrar y lo lleva a su panel según su rol: el Administrador a su panel de administración, el Gestor de Personal a la gestión de personal y el Supervisor / Auditor a su panel de supervisión

Condición 02

Dado: que el usuario ingresa un correo electrónico

Cuando: el correo no está registrado en el sistema

Entonces: el sistema muestra el mensaje "Usuario no registrado" y no permite el ingreso

Condición 03

Dado: que el usuario ingresa su correo

Cuando: escribe una contraseña que no coincide con la del correo

Entonces: el sistema muestra el mensaje "Credenciales incorrectas" y no permite el ingreso

Condición 04

Dado: que el usuario tiene una cuenta desactivada por el administrador

Cuando: intenta iniciar sesión con su correo y contraseña

Entonces: el sistema muestra el mensaje "Cuenta desactivada, contacte al administrador" y no permite el ingreso

Condición 05

Dado: que el usuario deja vacíos el correo o la contraseña

Cuando: presiona el botón para iniciar sesión

Entonces: el sistema indica debajo de cada campo el dato que falta y no envía el formulario hasta que se complete

Condición 06

Dado: que un usuario ya tiene una sesión iniciada

Cuando: abre la pantalla de inicio de sesión

Entonces: el sistema no le muestra el formulario y lo redirige directamente a su panel según su rol

## Tareas

| No | Descripción |
|---|---|
| 1 | Diseñar la pantalla de inicio de sesión con los campos de correo electrónico y contraseña |
| 2 | Validar las credenciales al enviar el formulario |
| 3 | Mostrar los mensajes de error diferenciados (no registrado, credenciales incorrectas, cuenta desactivada) |
| 4 | Dirigir a cada usuario a su panel según su rol al iniciar sesión correctamente |
| 5 | Redirigir al panel correspondiente cuando el usuario ya tiene una sesión iniciada |
| 6 | Validar que los campos obligatorios no se envíen vacíos |

## Control de Versiones

| Versión | Fecha | Autor | Revisión | Descripción | Aprobador |
|---|---|---|---|---|---|
| 1.0 | 2026-08-06 | | | Revisión de lenguaje y criterios detallados | |

## Estado de Implementación

- **Backend**: ✓ — `POST /api/auth/login` en `AuthController` + `AuthServiceImpl`; emisión de JWT con `JwtTokenProvider` (jjwt 0.13.0) y roles resueltos por `CustomUserDetailsService`; los tres roles están definidos en `SecurityConfig`. Tests verdes (`AuthControllerTest`).
- **Frontend**: ✓ — `LoginView` en la ruta `/login` (mockup 46). El acceso público se hace desde el enlace "Acceso interno" del footer del landing; sin sesión lleva a `/login` y con sesión al panel del rol. `AuthContext` conserva la sesión.
