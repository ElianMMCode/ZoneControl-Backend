# HU-11 - OTORGAR ACCESO A ÁREAS

| Campo | Valor |
|---|---|
| **Código** | HU-11 |
| **Nombre** | Otorgar Acceso a Áreas |
| **Complejidad** | Alta |
| **HU Relacionada** | HU-03, HU-09, HU-12, HU-13 |
| **Módulo** | Módulo de Gestión de Personal |

## Descripción

**Yo como** gestor de personal
**Requiero** asignar permisos de acceso a un empleado activo para áreas restringidas específicas, definiendo horarios y vigencia
**Para** controlar qué zonas de producción puede visitar cada empleado y durante qué períodos

## Requerimiento

El sistema debe permitir al gestor de personal otorgar autorización de ingreso a áreas restringidas para empleados activos. Se deben poder seleccionar múltiples áreas, definir horarios de acceso, fecha de inicio y fecha de expiración. El sistema debe verificar que no existan conflictos con permisos ya otorgados.

## Criterios de Aceptación

Condición 01

Dado: que el gestor selecciona un empleado activo

Cuando: define las áreas, horarios, fecha de inicio y fecha de expiración

Entonces: el sistema valida que el empleado esté activo, verifica que no haya conflictos de permisos, guarda el permiso en PostgreSQL, retorna HTTP 201 y muestra confirmación

Condición 02

Dado: que el empleado ya tiene un permiso activo para las mismas áreas y horarios

Cuando: el gestor intenta otorgar un acceso duplicado

Entonces: el sistema retorna error 409 con el mensaje "Conflicto de permisos existente"

Condición 03

Dado: que el gestor selecciona un empleado inactivo

Cuando: intenta otorgar acceso

Entonces: el sistema retorna error con el mensaje "No se puede otorgar acceso a empleado inactivo"

## Tareas

| No | Descripción |
|---|---|
| 1 | Diseñar interfaz de gestión de permisos con búsqueda de empleado por ID o nombre |
| 2 | Implementar selector múltiple de áreas restringidas de producción |
| 3 | Implementar definición de horarios de acceso (día de la semana, hora inicio, hora fin) |
| 4 | Implementar selectores de fecha de inicio y fecha de expiración |
| 5 | Implementar endpoint POST /api/permisos en Spring Boot |
| 6 | Validar que el empleado esté activo antes de otorgar el permiso |
| 7 | Verificar y prevenir conflictos de permisos (mismas áreas, mismos horarios, mismo empleado) |
| 8 | Registrar el permiso otorgado en el historial de la base de datos |

## Control de Versiones

| Versión | Fecha | Autor | Revisión | Descripción | Aprobador |
|---|---|---|---|---|---|
| 1.0 | 2026-07-26 | | | Versión inicial | |
