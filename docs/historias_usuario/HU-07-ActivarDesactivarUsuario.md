# HU-07 - ACTIVAR/DESACTIVAR USUARIO

| Campo | Valor |
|---|---|
| **Código** | HU-07 |
| **Nombre** | Activar/Desactivar Usuario |
| **Complejidad** | Media |
| **HU Relacionada** | HU-03, HU-05, HU-06 |
| **Módulo** | Módulo de Administración |

## Descripción

**Yo como** administrador del sistema
**Requiero** cambiar el estado de un usuario entre activo e inactivo
**Para** controlar el acceso de los usuarios al sistema sin necesidad de eliminar sus cuentas

## Requerimiento

El sistema debe permitir al administrador activar o desactivar cuentas de usuario. Un usuario desactivado no debe poder iniciar sesión. **Nota:** los tokens JWT ya emitidos no se invalidan al desactivar (siguen válidos hasta su expiración); la desactivación se valida en el login y en `CustomUserDetailsService`. El sistema debe impedir que el administrador se desactive a sí mismo.

## Criterios de Aceptación

Condición 01

Dado: que el administrador selecciona un usuario activo

Cuando: confirma la desactivación

Entonces: el sistema cambia el estado a "INACTIVO", el usuario no puede iniciar sesión y se registra la acción en logs

Condición 02

Dado: que el administrador selecciona un usuario inactivo

Cuando: confirma la activación

Entonces: el sistema cambia el estado a "ACTIVO" y el usuario puede iniciar sesión nuevamente

Condición 03

Dado: que el administrador intenta cambiar el estado de un usuario

Cuando: presiona el toggle de activar/desactivar

Entonces: el sistema muestra un diálogo de confirmación antes de ejecutar el cambio

Condición 04

Dado: que el administrador intenta desactivar su propia cuenta

Cuando: ejecuta la acción

Entonces: el sistema rechaza la operación y muestra el mensaje "No puede desactivar su propia cuenta"

## Tareas

| No | Descripción |
|---|---|
| 1 | Implementar toggle de activar/desactivar en la lista de usuarios del frontend |
| 2 | Implementar endpoint PATCH /api/admin/users/{id}/status en Spring Boot |
| 3 | Agregar validación que impida al administrador desactivarse a sí mismo |
| 4 | ~~Invalidar token JWT del usuario al ser desactivado~~ — gap conocido (HU-07): la desactivación no invalida tokens existentes |
| 5 | Mostrar diálogo de confirmación antes de ejecutar el cambio de estado |
| 6 | Registrar la acción de cambio de estado en logs del sistema |

## Control de Versiones

| Versión | Fecha | Autor | Revisión | Descripción | Aprobador |
|---|---|---|---|---|---|
| 1.0 | 2026-07-26 | | | Versión inicial | |
