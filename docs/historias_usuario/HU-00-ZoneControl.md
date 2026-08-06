# HU-00 - ZONE CONTROL

| Campo              | Valor                                              |
| ------------------ | -------------------------------------------------- |
| **Código**         | HU-00                                              |
| **Nombre**         | Zone Control - Sistema de Control de Acceso Físico |
| **Complejidad**    | Alta                                               |
| **HU Relacionada** | HU-01 a HU-29 (ver listado abajo)                  |
| **Módulo**         | General                                            |

## Descripción

**Yo como** Laboratorio XYZ, compañía farmacéutica dedicada a la producción de medicamentos de alto costo
**Requiero** modernizar el control de acceso físico a las áreas restringidas de producción, integrando la administración del personal autorizado, la validación de acceso físico, la trazabilidad de accesos y la generación de reportes periódicos para socios internacionales
**Para** garantizar la seguridad de los procesos productivos sensibles y cumplir con los requisitos normativos de auditoría

## Requerimiento

El sistema debe reflejar la estructura de la compañía y separar claramente lo que puede hacer el público en general de lo que hace el personal interno. El acceso interno tiene tres roles: administrador del sistema, gestor de personal y supervisor/auditor. El sistema incluye el inicio de sesión, la gestión de usuarios, el registro y la administración del personal, la carga masiva de empleados, el control de acceso físico (entrada y salida), la ocupación de las zonas en tiempo real, las alertas de seguridad, el historial de accesos y la generación de reportes (descargables y periódicos) para el socio internacional.

### Listado de historias de usuario

| Código | Historia                                                 |
| ------ | -------------------------------------------------------- |
| HU-01  | Consultar la información pública de la empresa           |
| HU-02  | Descargar el folleto informativo                         |
| HU-03  | Iniciar sesión en el sistema                             |
| HU-04  | Crear usuario del sistema                                |
| HU-05  | Editar usuario del sistema                               |
| HU-06  | Activar o desactivar usuario                             |
| HU-07  | Restablecer la contraseña de un usuario                  |
| HU-08  | Registrar personal                                       |
| HU-09  | Cargar personal de forma masiva                          |
| HU-10  | Otorgar acceso a un área                                 |
| HU-11  | Revocar acceso a un área                                 |
| HU-12  | Suspender el acceso a un área                            |
| HU-13  | Buscar personal                                          |
| HU-14  | Consultar el historial de accesos                        |
| HU-15  | Generar documento descargable del historial              |
| HU-16  | Generar el archivo periódico para el socio internacional |
| HU-17  | Validar la entrada a un área restringida                 |
| HU-18  | Gestionar el contenido público                           |
| HU-19  | Gestionar las áreas de producción                        |
| HU-20  | Consultar la ocupación en tiempo real de las zonas       |
| HU-21  | Cerrar o reabrir una zona por emergencia                 |
| HU-22  | Consultar las alertas de anomalías de acceso             |
| HU-23  | Definir turnos y horarios por día                        |
| HU-24  | Consultar la matriz de roles                             |
| HU-25  | Registrar la salida de un área                           |
| HU-26  | Gestionar mi perfil y cambiar mi contraseña              |
| HU-27  | Consultar el personal y las autorizaciones de una sala   |
| HU-28 | Consultar el detalle del empleado |
| HU-29 | Gestionar el catálogo de cargos |

## Criterios de Aceptación

Condición 01

Dado: que el sistema está en funcionamiento

Cuando: los usuarios acceden según su rol

Entonces: el sistema diferencia entre el acceso público (sin iniciar sesión) y el acceso interno (con sesión y permisos por rol)

Condición 02

Dado: que el sistema está en operación

Cuando: se registran intentos de entrada o salida en áreas restringidas

Entonces: cada intento queda registrado en el historial con fecha y hora, independientemente del resultado

Condición 03

Dado: que existe un socio internacional

Cuando: se requiere reportar la actividad de acceso

Entonces: el sistema permite generar un archivo periódico sin datos personales, con resumen por departamento y área y distribución por día

Condición 04

Dado: que una zona tiene personal autorizado

Cuando: el supervisor consulta el panel de zonas

Entonces: puede ver la ocupación en tiempo real, las alertas y el detalle del personal y las autorizaciones de cada sala

## Tareas

| No  | Descripción                                                                                |
| --- | ------------------------------------------------------------------------------------------ |
| 1   | Configurar la aplicación y su almacenamiento de información                                |
| 2   | Implementar el sitio público (información, contacto, sedes, catálogo y folleto)            |
| 3   | Implementar el inicio de sesión y la gestión de usuarios                                   |
| 4   | Implementar la gestión del personal (registro, carga masiva, búsqueda, detalle y permisos) |
| 5   | Implementar el control de acceso físico (entrada, salida, ocupación, emergencia y alertas) |
| 6   | Implementar los reportes (historial, documentos descargables y archivo periódico)          |
| 7   | Documentar con diagramas (casos de uso y flujos) y historias de usuario                    |

## Control de Versiones

| Versión | Fecha      | Autor | Revisión | Descripción                                                                | Aprobador |
| ------- | ---------- | ----- | -------- | -------------------------------------------------------------------------- | --------- |
| 1.0     | 2026-07-26 |       |          | Versión inicial                                                            |           |
| 1.1     | 2026-08-06 |       |          | Listado completo de historias (HU-01..29) y criterios alineados al sistema |           |

## Estado de Implementación

- **Backend**: todos los módulos implementados y con tests en verde (202 pruebas); `./mvnw test` en verde. Detalle de rutas en `AGENTS.md`.
- **Frontend**: módulo público, autenticación (inicio de sesión y enlace de activación), administración (usuarios, contenido público, áreas, matriz de roles), gestión del gestor (personal, registro, carga masiva, detalle, permisos, ajustes), panel del supervisor (dashboard, validación de credencial con entrada y salida, zonas en vivo, reportes) y ajustes.
- **Estado frontend**: todas las vistas están construidas.
- **Backend §9**: todos los puntos del plan de implementación están implementados (documentos PDF, archivo periódico de dos secciones, filtros por departamento y área en el historial, gestión de áreas, matriz de roles, invalidación de sesiones al desactivar, manejo de errores, tiempo real y alertas, fotografía y turnos).
- **Nota**: los archivos `HU-00- NOMBRE_DEL_PROYETO(5).docx` y `zonecontrol.pdf` son la fuente binaria del caso de estudio (solo lectura); la fuente de verdad editable son los `.md` y `.puml` de `docs/`.
