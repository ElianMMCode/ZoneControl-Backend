# HU-05 - CREAR USUARIO INTERNO

| Campo | Valor |
|---|---|
| **Código** | HU-05 |
| **Nombre** | Crear Usuario Interno |
| **Complejidad** | Alta |
| **HU Relacionada** | HU-03, HU-06, HU-07, HU-08 |
| **Módulo** | Módulo de Administración |
| **Rol** | Administrador |

## Descripción

**Yo como** administrador del sistema
**Requiero** crear cuentas de usuario para el personal que necesita entrar al sistema, eligiendo el rol que le corresponde
**Para** que cada persona acceda al sistema con sus propias credenciales y solo a las funciones de su rol

## Requerimiento

Un usuario del sistema es la cuenta con la que una persona entra a trabajar con el sistema. Para crear una cuenta, el administrador selecciona a un empleado de la empresa que ya esté registrado en Gestión de Personal y que tenga un correo electrónico registrado, y le asigna uno de los tres roles disponibles: Administrador, Gestor de Personal o Supervisor / Auditor.

El administrador no escribe ni conoce la contraseña en ningún momento. Al crear la cuenta, el sistema genera un enlace de activación temporal que expira a las 24 horas. Ese enlace se entrega al administrador en la pantalla y se abre en una ventana nueva con la página de configuración de contraseña. La persona abre esa página, define su propia contraseña y desde ese momento puede iniciar sesión.

Si el enlace no se usa dentro de las 24 horas, deja de ser válido y el administrador debe generar uno nuevo. De esta manera, la contraseña solo la conoce la persona dueña de la cuenta.

## Criterios de Aceptación

Condición 01

Dado: que el administrador está identificado y busca un empleado registrado que no tiene cuenta de usuario

Cuando: selecciona al empleado, elige su rol y confirma la creación

Entonces: el sistema crea la cuenta, genera un enlace de activación temporal que expira en 24 horas y muestra el enlace en la pantalla para que se abra la página de configuración

Condición 02

Dado: que la persona recibe el enlace de activación

Cuando: lo abre dentro de las 24 horas y define su nueva contraseña

Entonces: el sistema guarda su contraseña y a partir de ese momento la persona puede iniciar sesión con su correo y esa contraseña

Condición 03

Dado: que una persona intenta usar el enlace de activación

Cuando: han pasado más de 24 horas desde que se generó o el enlace ya fue usado

Entonces: el sistema muestra el mensaje "El enlace de configuración ha expirado. Contacte al administrador para generar un nuevo enlace"

Condición 04

Dado: que el administrador busca al empleado para crear la cuenta

Cuando: el empleado no está registrado en el sistema

Entonces: el sistema muestra un mensaje claro indicando que el empleado no existe y no permite crear la cuenta

Condición 05

Dado: que el administrador selecciona un empleado para crear la cuenta

Cuando: ese empleado ya tiene una cuenta de usuario en el sistema

Entonces: el sistema muestra un mensaje indicando que el empleado ya tiene usuario y no permite crear otra cuenta

Condición 06

Dado: que el administrador selecciona un empleado para crear la cuenta

Cuando: el empleado no tiene un correo electrónico registrado en Gestión de Personal

Entonces: el sistema muestra el mensaje "El empleado no tiene un correo registrado. Regístrelo en Gestión Personal para poder crear el usuario" y no permite crear la cuenta

## Tareas

| No | Descripción |
|---|---|
| 1 | Diseñar el formulario de creación de usuario con la selección del empleado, su rol y la búsqueda de empleados |
| 2 | Permitir buscar y seleccionar el empleado entre los registrados en Gestión de Personal |
| 3 | Generar el enlace de activación temporal de 24 horas al crear la cuenta |
| 4 | Mostrar el enlace en el panel y abrir la página de configuración de contraseña en una ventana nueva |
| 5 | Crear la página donde la persona define su propia contraseña con el enlace |
| 6 | Mostrar los mensajes de error de las validaciones (empleado inexistente, empleado con usuario, empleado sin correo, enlace vencido) |

## Control de Versiones

| Versión | Fecha | Autor | Revisión | Descripción | Aprobador |
|---|---|---|---|---|---|
| 1.0 | 2026-08-06 | | | Revisión de lenguaje y criterios detallados | |

## Estado de Implementación

- **Backend**: ✓ — `POST /api/admin/users` valida que el empleado exista, no tenga usuario previo y tenga correo; deriva nombre y correo del empleado vinculado; genera `setupToken` (48 bytes aleatorios, hash SHA-256, expiración 24 h) y guarda el usuario sin contraseña. `GET/POST /api/setup-password` validan el token y completan la activación (contraseña con BCrypt, limpian `setupToken`). `MagicLinkNotifier` deja el enlace visible en la consola del sistema. Tests verdes (`AdminUserControllerTest`, `SetupPasswordControllerTest`).
- **Frontend**: ✓ — `CreateUserView` (`/admin/usuarios/nuevo`, mockup 21) con selector de empleado; botón "Abrir configuración" que abre `SetupPasswordView` (`/configurar-contrasena?token=`) en ventana nueva.
