# HU-00 - ZONE CONTROL

| Campo | Valor |
|---|---|
| **Código** | HU-00 |
| **Nombre** | Zone Control - Sistema de Control de Acceso Físico |
| **Complejidad** | Alta |
| **HU Relacionada** | HU-01, HU-02, HU-03, HU-05, HU-06, HU-07, HU-08, HU-09, HU-10, HU-11, HU-12, HU-13, HU-14, HU-15, HU-16, HU-17, HU-18, HU-19 (+ HU-20..HU-27 nuevas de la §9 del PLAN_IMPLEMENTACION) |
| **Módulo** | General |

## Descripción

**Yo como** Laboratorio XYZ, compañía farmacéutica dedicada a la producción de medicamentos de alto costo
**Requiero** modernizar el control de acceso físico a las áreas restringidas de producción, integrando la administración del personal autorizado, la validación de acceso físico, la trazabilidad de accesos y la generación de reportes periódicos para socios internacionales
**Para** garantizar la seguridad de los procesos productivos sensibles y cumplir con los requisitos normativos de auditoría

## Requerimiento

El sistema debe reflejar la estructura organizacional de la compañía, diferenciando entre el público general (consulta de información institucional básica) y el personal interno con tres roles: administrador del sistema, gestor de personal y supervisor/auditor. Debe contemplar autenticación, registro y gestión del personal, carga masiva, control de acceso físico, historial de accesos y exportación de información hacia sistemas externos para el socio internacional.

## Criterios de Aceptación

Condición 01

Dado: que el sistema está implementado

Cuando: los usuarios acceden según su rol

Entonces: el sistema diferencia entre acceso público (sin autenticación) y acceso interno (con autenticación JWT y permisos por rol)

Condición 02

Dado: que el sistema está en operación

Cuando: se registran intentos de acceso a áreas restringidas

Entonces: cada intento queda registrado en el historial con marca de tiempo, independientemente del resultado

Condición 03

Dado: que existe un socio internacional

Cuando: se requiere reportar actividad de acceso

Entonces: el sistema permite generar archivos periódicos de intercambio con la actividad agrupada por departamento

## Tareas

| No | Descripción |
|---|---|
| 1 | Configurar proyecto Spring Boot (backend) con estructura modular |
| 2 | Configurar proyecto React/Vue.js (frontend) |
| 3 | Diseñar e implementar modelo de datos en PostgreSQL (usuarios, personal, permisos, historial_accesos, departamentos) |
| 4 | Implementar autenticación con JWT y BCrypt |
| 5 | Implementar módulo público (información institucional, contacto, sedes, folleto) |
| 6 | Implementar módulo de administración (CRUD de usuarios internos) |
| 7 | Implementar módulo de gestión de personal (registro, carga masiva, permisos, búsqueda) |
| 8 | Implementar módulo de control de acceso físico |
| 9 | Implementar módulo de reportes y auditoría (historial, documentos descargables, archivo periódico) |
| 10 | Documentar con diagramas PlantUML (casos de uso y flujos) |

## Control de Versiones

| Versión | Fecha | Autor | Revisión | Descripción | Aprobador |
|---|---|---|---|---|---|
| 1.0 | 2026-07-26 | | | Versión inicial | |

## Estado de Implementación

- **Backend**: todos los módulos implementados y con tests verdes (147 métodos `@Test`); `./mvnw test` en verde. Ver inventario de endpoints en `AGENTS.md`.
- **Frontend (React en `src/main/frontend`)**: módulo público, autenticación (login + magic link), administración (usuarios, contenido público, áreas), **gestión del gestor completado** (personal, registro, carga masiva, detalle, permisos, ajustes) y dashboard del supervisor.
- **Pendiente frontend**: validación de credencial (HU-18, mockup 44), reportes/historial (HU-15/16/17, mockup 37) y matriz de roles (HU-27, mockup 16).
- **Pendientes backend (§9)**: PDF en export/archivo periódico (1.1), agregación por departamento del archivo periódico (1.2), filtro por departamento en historial/export (1.3), matriz de roles endpoint (1.5), invalidación de JWT (1.6), handlers 409/500 (1.7), tiempo real/rol SEGURIDAD (2.1–2.5), turnos (3.2). Detalle en §9.12.
- **Nota**: los archivos `HU-00- NOMBRE_DEL_PROYETO(5).docx` y `zonecontrol.pdf` son la fuente binaria del caso de estudio (solo lectura); la fuente de verdad editable son los `.md` y `.puml` de `docs/`.
