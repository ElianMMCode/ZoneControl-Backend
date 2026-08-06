# HU-01 - CONSULTA DE INFORMACIÓN PÚBLICA

| Campo | Valor |
|---|---|
| **Código** | HU-01 |
| **Nombre** | Consulta de Información Pública |
| **Complejidad** | Alta |
| **HU Relacionada** | HU-02, HU-19 |
| **Módulo** | Módulo Público |
| **Rol** | Público general (visitante) |

## Descripción

**Yo como** visitante del sitio público de Laboratorio XYZ
**Requiero** consultar información de la empresa, sus datos de contacto, la ubicación de sus sedes, su catálogo de productos y un folleto descargable
**Para** conocer la compañía farmacéutica, sus productos y cómo contactarla sin necesidad de iniciar sesión

## Requerimiento

El sitio público del sistema le permite a cualquier persona, sin necesidad de identificarse, conocer a Laboratorio XYZ. En la primera pantalla se presenta la información institucional: el nombre de la empresa, su misión, su visión y una descripción de la organización.

Además, el visitante puede ver los datos de contacto de la empresa (números de teléfono, correo electrónico y redes sociales), la ubicación de las sedes con sus direcciones y horarios de atención, y el catálogo de productos farmacéuticos. En el catálogo se muestra de cada producto su nombre, su descripción, su principio activo y su presentación.

El sistema también ofrece la descarga de un folleto informativo (historia HU-02).

## Criterios de Aceptación

Condición 01

Dado: que el visitante abre el sitio público de Laboratorio XYZ

Cuando: revisa la sección de información institucional

Entonces: el sistema muestra el nombre de la empresa, una descripción de la compañía, su misión y su visión

Condición 02

Dado: que el visitante se encuentra en el sitio público

Cuando: consulta la sección de datos de contacto

Entonces: el sistema muestra los números de teléfono, el correo electrónico de contacto y las redes sociales de Laboratorio XYZ

Condición 03

Dado: que el visitante desea conocer dónde se ubican las sedes

Cuando: consulta la sección de sedes

Entonces: el sistema muestra la dirección, los horarios de atención y las coordenadas de cada sede

Condición 04

Dado: que el visitante desea conocer los productos de la empresa

Cuando: consulta el catálogo de productos

Entonces: el sistema muestra el nombre, la descripción, el principio activo y la presentación de cada producto farmacéutico

Condición 05

Dado: que el visitante navega por las secciones del sitio público

Cuando: consulta información institucional, contacto, sedes o catálogo

Entonces: el sistema muestra la información sin solicitar inicio de sesión y sin modificar ningún dato

## Tareas

| No | Descripción |
|---|---|
| 1 | Diseñar el sitio público con menú de navegación entre las secciones (Institucional, Contacto, Sedes, Catálogo) |
| 2 | Implementar la sección de información institucional con nombre, misión, visión y descripción |
| 3 | Implementar la sección de datos de contacto con teléfonos, correo y redes sociales |
| 4 | Implementar la sección de sedes con direcciones, horarios y coordenadas |
| 5 | Implementar el catálogo de productos con nombre, descripción, principio activo y presentación, sin datos internos |
| 6 | Preparar la sección para el botón de descarga del folleto de la historia HU-02 |
| 7 | Verificar que el sitio público se consulta sin iniciar sesión y que no altera datos del sistema |

## Control de Versiones

| Versión | Fecha | Autor | Revisión | Descripción | Aprobador |
|---|---|---|---|---|---|
| 1.0 | 2026-08-06 | | | Revisión de lenguaje y criterios detallados | |

## Estado de Implementación

- **Backend**: ✓ — `PublicController` expone `GET /api/public/institucional`, `GET /api/public/contacto`, `GET /api/public/sedes`, `GET /api/public/catalogo` y `GET /api/public/folleto`; `CacheConfig` habilita caché para la información estática. Tests verdes (`PublicControllerTest`).
- **Frontend**: ✓ — `LandingView` en la ruta `/` replica el mockup 27: hero, secciones Institucional/Contacto/Sedes/Catálogo y botón de folleto. El catálogo no muestra `productionArea` ni `id`. La sección de sedes muestra direcciones, horarios y coordenadas.
