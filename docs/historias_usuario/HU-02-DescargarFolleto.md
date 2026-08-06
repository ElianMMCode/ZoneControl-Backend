# HU-02 - DESCARGAR FOLLETO INFORMATIVO

| Campo | Valor |
|---|---|
| **Código** | HU-02 |
| **Nombre** | Descargar Folleto Informativo |
| **Complejidad** | Media |
| **HU Relacionada** | HU-01, HU-19 |
| **Módulo** | Módulo Público |
| **Rol** | Público general (visitante) |

## Descripción

**Yo como** visitante del sitio público de Laboratorio XYZ
**Requiero** descargar un folleto informativo de la empresa en formato PDF
**Para** tener información descargable de la compañía y consultarla sin conexión a internet

## Requerimiento

El sitio público ofrece un botón llamado "Descargar Folleto" que pone a disposición del visitante el folleto de Laboratorio XYZ como un archivo PDF. La descarga se realiza sin necesidad de iniciar sesión.

El folleto es un documento preparado por el equipo de administración que resume la información de la empresa. Cuando existe un folleto cargado, el visitante puede descargarlo y abrirlo en su dispositivo. Cuando aún no hay un folleto disponible, el sistema no muestra el botón de descarga y tampoco presenta errores de página, simplemente omite esta opción.

De esta forma, el sitio público se adapta solo: si hay folleto, se ofrece la descarga; si no lo hay, el visitante nunca ve un aviso de error ni un enlace roto.

## Criterios de Aceptación

Condición 01

Dado: que existe un folleto cargado en el sistema

Cuando: el visitante entra al sitio público

Entonces: el sistema muestra el botón "Descargar Folleto" en la sección correspondiente

Condición 02

Dado: que el visitante presiona el botón "Descargar Folleto"

Cuando: el folleto está disponible

Entonces: el sistema descarga un archivo PDF con nombre descriptivo (Folleto_Laboratorio_XYZ.pdf) que se guarda en el dispositivo del visitante

Condición 03

Dado: que el visitante descargó el folleto

Cuando: abre el archivo en su dispositivo

Entonces: el archivo es un PDF válido que se visualiza correctamente con la información de la empresa

Condición 04

Dado: que no existe un folleto cargado en el sistema

Cuando: el visitante entra al sitio público

Entonces: el sistema no muestra el botón "Descargar Folleto" ni presenta ningún mensaje de error

Condición 05

Dado: que el visitante descarga el folleto

Cuando: realiza la descarga

Entonces: el sistema no solicita inicio de sesión y no modifica ningún dato del sistema

## Tareas

| No | Descripción |
|---|---|
| 1 | Generar el folleto de Laboratorio XYZ en formato PDF |
| 2 | Preparar el sitio público para ofrecer la descarga del folleto |
| 3 | Mostrar el botón "Descargar Folleto" solo cuando el folleto está disponible |
| 4 | Verificar que el archivo descargado se abre correctamente como PDF |
| 5 | Comprobar que cuando no hay folleto, el botón se oculta y no se muestran errores |

## Control de Versiones

| Versión | Fecha | Autor | Revisión | Descripción | Aprobador |
|---|---|---|---|---|---|
| 1.0 | 2026-08-06 | | | Revisión de lenguaje y criterios detallados | |

## Estado de Implementación

- **Backend**: ✓ — `GET /api/public/folleto` sirve `uploads/folleto/Folleto_Laboratorio_XYZ.pdf` como binario; responde 404 si el archivo no existe (el front lo detecta y oculta el botón). El PDF se genera desde `docs/folleto/Folleto_Laboratorio_XYZ.html` con Chromium headless. Tests verdes (`FolletoPublicoTest` operan sobre `target/test-uploads/folleto` para no tocar el PDF real).
- **Frontend**: ✓ — `LandingView`: botón "Descargar Folleto" visible solo cuando la comprobación (`HEAD /api/public/folleto`) responde 200.
