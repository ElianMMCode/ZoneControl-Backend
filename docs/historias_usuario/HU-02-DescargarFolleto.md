# HU-02 - DESCARGAR FOLLETO INFORMATIVO

| Campo | Valor |
|---|---|
| **Código** | HU-02 |
| **Nombre** | Descargar Folleto Informativo |
| **Complejidad** | Media |
| **HU Relacionada** | HU-01, HU-19 |
| **Módulo** | Módulo Público |

## Descripción

**Yo como** visitante del sitio web de Laboratorio XYZ
**Requiero** descargar un folleto informativo en formato PDF
**Para** tener información descargable de la compañía farmacéutica para consulta offline

## Requerimiento

Permitir la descarga de un folleto informativo en PDF desde la sección pública del sitio web, sin requerir autenticación.

## Criterios de Aceptación

Condición 01

Dado: que el usuario está en la página de información de Laboratorio XYZ

Cuando: presiona el botón "Descargar Folleto"

Entonces: el sistema genera o sirve el archivo PDF con información de la empresa y el navegador inicia la descarga con un nombre descriptivo (Folleto_Laboratorio_XYZ.pdf)

Condición 02

Dado: que el usuario intenta descargar el folleto

Cuando: hay un error en el servidor o el archivo no existe

Entonces: el sistema muestra un mensaje de error indicando que la descarga no está disponible en ese momento

## Tareas

| No | Descripción |
|---|---|
| 1 | Diseñar y maquetar el folleto informativo de Laboratorio XYZ en PDF |
| 2 | Implementar endpoint GET /api/public/folleto en Spring Boot para servir el archivo PDF |
| 3 | Agregar botón "Descargar Folleto" en la sección de información institucional del frontend |
| 4 | Implementar manejo de errores cuando el archivo no esté disponible |
| 5 | Verificar que el tamaño del PDF no exceda 10MB para descarga rápida (mismo límite que la carga de HU-19) |

## Control de Versiones

| Versión | Fecha | Autor | Revisión | Descripción | Aprobador |
|---|---|---|---|---|---|
| 1.0 | 2026-07-26 | | | Versión inicial | |
