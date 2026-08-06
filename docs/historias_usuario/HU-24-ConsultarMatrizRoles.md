# HU-24 - CONSULTAR MATRIZ DE ROLES

| Campo              | Valor                     |
| ------------------ | ------------------------- |
| **Código**         | HU-24                     |
| **Nombre**         | Consultar Matriz de Roles |
| **Complejidad**    | Baja                      |
| **HU Relacionada** | HU-03                     |
| **Módulo**         | Módulo de Administración  |
| **Rol**            | Administrador             |

## Descripción

**Yo como** administrador del sistema
**Requiero** consultar una matriz que muestre qué acciones puede realizar cada rol en cada módulo del sistema
**Para** conocer y explicar con claridad las capacidades de cada perfil de usuario

## Requerimiento

El sistema debe mostrar una tabla de consulta donde cada fila corresponde a un módulo del sistema y cada columna a uno de los tres roles existentes: Administrador, Gestor de Personal y Supervisor/Auditor. En la intersección de cada módulo y rol se indica el nivel de acceso: **Escritura** (el rol puede administrar el módulo), **Lectura** (el rol solo puede consultar información del módulo) o **Sin acceso**. Por ejemplo, el Supervisor/Auditor tiene lectura en áreas, cargos y catálogos de personal (los usa en el panel de zonas y en los reportes) y escritura en control de acceso físico y reportes.

Esta pantalla es únicamente de consulta: desde aquí no se modifican ni los roles ni sus permisos. Solo el administrador puede ver la matriz; cualquier otro perfil que intente consultarla debe ser rechazado sin mostrar información.

## Criterios de Aceptación

Condición 01

Dado: que el administrador ingresa a la pantalla de consulta de roles

Cuando: se abre la vista de la matriz

Entonces: el sistema muestra la tabla con los tres roles (Administrador, Gestor de Personal y Supervisor/Auditor) como columnas y los módulos del sistema como filas, indicando en cada intersección si el rol tiene escritura, solo lectura o sin acceso sobre ese módulo

Condición 02

Dado: que un usuario que no es administrador intenta consultar la matriz de roles

Cuando: intenta acceder a la pantalla

Entonces: el sistema rechaza la solicitud sin mostrar la información de la matriz

Condición 03

Dado: que el administrador revisa la matriz

Cuando: examina el nivel de cualquier módulo y rol

Entonces: el nivel (escritura, solo lectura o sin acceso) coincide con las reglas de acceso reales del sistema, es decir, lo que ese rol puede hacer en ese módulo

Condición 04

Dado: que el administrador abre la pantalla de la matriz

Cuando: el sistema no puede entregar los datos de la matriz en ese momento

Entonces: la pantalla muestra una versión de respaldo previamente preparada con la misma información, y queda claro que se trata de una vista de consulta

Condición 05

Dado: que el administrador consulta la matriz

Cuando: revisa la pantalla en busca de controles para editar o eliminar roles o permisos

Entonces: no encuentra ninguno, porque la matriz es únicamente de consulta y no permite modificar las capacidades de los roles

## Tareas

| No  | Descripción                                                                    |
| --- | ------------------------------------------------------------------------------ |
| 1   | Obtener la matriz módulo × rol según las reglas de acceso reales del sistema   |
| 2   | Pantalla de consulta de la matriz con los tres roles y los módulos del sistema |
| 3   | Restringir la consulta únicamente al administrador                             |
| 4   | Mostrar una versión de respaldo si el servicio no responde                     |
| 5   | Garantizar que la vista no ofrezca opciones de edición ni eliminación          |

## Control de Versiones

| Versión | Fecha      | Autor | Revisión | Descripción                                 | Aprobador |
| ------- | ---------- | ----- | -------- | ------------------------------------------- | --------- |
| 1.0     | 2026-08-06 |       |          | Revisión de lenguaje y criterios detallados |           |
| 1.1     | 2026-08-06 |       |          | Niveles de acceso (Escritura / Lectura / Sin acceso) y módulo Cargos |           |

## Estado de Implementación

- **Backend**: ✓ — `GET /api/admin/role-matrix` (solo ADMIN, 403 para otros roles), matriz módulo × rol → nivel `NINGUNO/LECTURA/ESCRITURA` reconstruida desde `SecurityConfig` vía `RoleMatrixServiceImpl` (gap 1.5 §9). Incluye el módulo Cargos. Test: `RoleMatrixControllerTest`.
- **Frontend**: ✓ — `RoleMatrixView` en `/admin/matriz-roles` (rol ADMIN) con fallback estático y leyenda de niveles (Escritura / Lectura / Sin acceso). Solo lectura; roles fijos en `SecurityConfig`.
