# HU-05 - CREAR USUARIO INTERNO

| Campo | Valor |
|---|---|
| **Código** | HU-05 |
| **Nombre** | Crear Usuario Interno |
| **Complejidad** | Alta |
| **HU Relacionada** | HU-03, HU-06, HU-07, HU-08, HU-09, HU-31, HU-32 |
| **Módulo** | Módulo de Administración |
| **Rol** | Administrador |

## Descripción

**Yo como** administrador del sistema
**Requiero** crear cuentas de usuario para el personal que necesita entrar al sistema
**Para** que cada persona acceda con sus propias credenciales y solo a las funciones que le corresponden, sin que nadie elija ni conozca su contraseña

## Requerimiento

Una cuenta de usuario nace de un empleado: primero se registra al empleado en Gestión de Personal (HU-09) y se le asigna un **cargo** del catálogo (HU-32). Cada cargo del catálogo define si otorga o no un **rol de sistema** (Administrador, Gestor de Personal o Supervisor / Auditor) y cuál.

Un empleado cuyo cargo tiene un rol de sistema asignado y que tiene un correo registrado se convierte en **candidato** a usuario del sistema: aparece en el panel "Empleados pendientes de activación" del administrador y en el listado para crear usuarios. Los empleados cuyo cargo no define rol de sistema no son candidatos (son de solo acceso físico).

Para crear la cuenta, el administrador selecciona al empleado candidato. El **rol no se elige manualmente**: nace del cargo del empleado (el sistema lo muestra en la pantalla, pero no es editable). El nombre, el apellido, el correo y el rol de la cuenta se heredan del empleado.

El administrador no escribe ni conoce la contraseña en ningún momento. Al crear la cuenta, el sistema genera un enlace de activación temporal que expira a las 24 horas. Ese enlace se entrega al administrador en la pantalla y se abre en una ventana nueva con la página de configuración de contraseña. La persona abre esa página, define su propia contraseña y desde ese momento puede iniciar sesión.

Si el enlace no se usa dentro de las 24 horas, deja de ser válido y el administrador debe generar uno nuevo (HU-08).

## Criterios de Aceptación

Condición 01

Dado: que un empleado está registrado y su cargo define un rol de sistema, y tiene correo registrado

Cuando: el administrador consulta los candidatos a usuario

Entonces: el empleado aparece en el listado de candidatos con su cargo y el rol que su cargo le otorga

Condición 02

Dado: que el administrador selecciona a un empleado candidato para crear la cuenta

Cuando: revisa el formulario de creación

Entonces: el rol se muestra en pantalla derivado del cargo del empleado y no se puede modificar manualmente; el nombre, el apellido y el correo se heredan del empleado

Condición 03

Dado: que el administrador confirma la creación

Cuando: el empleado es válido (candidato con correo y sin cuenta previa)

Entonces: el sistema crea la cuenta, genera un enlace de activación temporal que expira en 24 horas y muestra el enlace en la pantalla para abrir la página de configuración

Condición 04

Dado: que la persona recibe el enlace de activación

Cuando: lo abre dentro de las 24 horas y define su nueva contraseña

Entonces: el sistema guarda su contraseña y a partir de ese momento la persona puede iniciar sesión con su correo y esa contraseña, con el rol que define su cargo

Condición 05

Dado: que una persona intenta usar el enlace de activación

Cuando: han pasado más de 24 horas desde que se generó o el enlace ya fue usado

Entonces: el sistema muestra el mensaje "El enlace de configuración ha expirado. Contacte al administrador para generar un nuevo enlace"

Condición 06

Dado: que el administrador busca a un empleado para crear la cuenta

Cuando: el empleado no está registrado en el sistema

Entonces: el sistema muestra un mensaje claro indicando que el empleado no existe y no permite crear la cuenta

Condición 07

Dado: que el administrador intenta crear la cuenta de un empleado cuyo cargo no define un rol de sistema

Cuando: confirma la creación

Entonces: el sistema muestra el mensaje "El empleado no tiene un rol de sistema asignado. El rol se define a través de su cargo en Gestión de Personal" y no permite crear la cuenta

Condición 08

Dado: que el administrador selecciona un empleado para crear la cuenta

Cuando: ese empleado ya tiene una cuenta de usuario en el sistema

Entonces: el sistema muestra un mensaje indicando que el empleado ya tiene usuario y no permite crear otra cuenta

Condición 09

Dado: que el administrador selecciona un empleado para crear la cuenta

Cuando: el empleado no tiene un correo electrónico registrado en Gestión de Personal

Entonces: el sistema muestra el mensaje "El empleado no tiene un correo registrado. Regístrelo en Gestión Personal para poder crear el usuario" y no permite crear la cuenta

## Tareas

| No | Descripción |
|---|---|
| 1 | Mostrar los empleados candidatos (cargo con rol de sistema + correo, sin cuenta previa) |
| 2 | Crear la cuenta derivando nombre, correo y rol del empleado y su cargo (sin rol manual) |
| 3 | Generar el enlace de activación temporal de 24 horas al crear la cuenta |
| 4 | Mostrar el enlace en el panel y abrir la página de configuración de contraseña en una ventana nueva |
| 5 | Crear la página donde la persona define su propia contraseña con el enlace |
| 6 | Mostrar los mensajes de error de las validaciones (empleado inexistente, sin rol de sistema, empleado con usuario, empleado sin correo, enlace vencido) |

## Control de Versiones

| Versión | Fecha | Autor | Revisión | Descripción | Aprobador |
|---|---|---|---|---|---|
| 1.0 | 2026-08-06 | | | Reescritura completa: el usuario nace del empleado y el rol de su cargo; sin rol manual; candidato = cargo con rol de sistema | |

## Estado de Implementación

- **Backend**: ✓ — `POST /api/admin/users` recibe `employeeCode` + `status`; valida que el empleado tenga rol de sistema (derivado de su cargo), que no tenga usuario previo y que tenga correo; deriva nombre, correo y **rol** del empleado; genera `setupToken` (48 bytes, hash SHA-256, expiración 24 h) y guarda el usuario sin contraseña. `GET /api/admin/users/candidatos` lista los empleados candidatos (`systemRole` no nulo + correo + sin usuario). `GET/POST /api/setup-password` completan la activación. `MagicLinkNotifier` deja el enlace visible en la consola del sistema. Tests verdes (`AdminUserControllerTest`, `SetupPasswordControllerTest`, `CargoControllerTest`).
- **Frontend**: ✓ — `CreateUserView` (`/admin/usuarios/nuevo`) con selector de candidatos; el rol se muestra derivado del cargo (solo lectura, sin selector manual); botón "Abrir configuración" que abre `SetupPasswordView` (`/configurar-contrasena?token=`) en ventana nueva.
- **Relacionado**: el catálogo de cargos (HU-32) define el rol; el gestor asigna el cargo al registrar/editar el empleado (HU-09, HU-31).
