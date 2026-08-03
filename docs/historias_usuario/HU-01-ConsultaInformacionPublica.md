# HU-01 - CONSULTA DE INFORMACIÓN PÚBLICA

| Campo | Valor |
|---|---|
| **Código** | HU-01 |
| **Nombre** | Consulta de Información Pública |
| **Complejidad** | Alta |
| **HU Relacionada** | HU-02 |
| **Módulo** | Módulo Público |

## Descripción

**Yo como** visitante del sitio web de Laboratorio XYZ
**Requiero** acceder a información de la empresa, su catálogo de servicios y productos farmacéuticos, datos de contacto y ubicación de sedes
**Para** conocer la compañía farmacéutica, sus áreas de producción y los medicamentos que fabrica sin necesidad de autenticación

## Requerimiento

Permitir al público general consultar información institucional básica sin autenticación, incluyendo datos de la empresa, el catálogo de servicios y productos farmacéuticos, información de contacto y ubicación de las sedes de producción.

## Contrato de los endpoints públicos

- `GET /api/public/institucional` → `{ info: { companyName, mission, vision, description, productionAreas } }` (mapa clave-valor).
- `GET /api/public/contacto` → `{ contact: { phone, email, socialMedia } }`.
- `GET /api/public/sedes` → `[{ id, name, address, openingHours, latitude, longitude }]`. El `id` se incluye para que el panel admin de HU-19 pueda referenciar la sede al editar/eliminar sin necesidad de un endpoint admin adicional.
- `GET /api/public/catalogo` → `[{ id, name, description, activeIngredient, presentation, productionArea }]`. El `id` se incluye por la misma razón que en sedes.
- `GET /api/public/folleto` → PDF binario (`Folleto_Laboratorio_XYZ.pdf`) o 404 si no hay folleto cargado.

## Criterios de Aceptación

Condición 01

Dado: que el usuario accede al sitio web

Cuando: selecciona "Información de la Empresa"

Entonces: el sistema muestra el nombre (Laboratorio XYZ), la descripción de la compañía farmacéutica, la misión y visión, y las áreas de producción

Condición 02

Dado: que el usuario está en la página principal

Cuando: selecciona "Datos de Contacto"

Entonces: el sistema muestra los números de teléfono, el correo electrónico de contacto y las redes sociales de Laboratorio XYZ

Condición 03

Dado: que el usuario desea conocer la ubicación

Cuando: selecciona "Ubicación de Sedes"

Entonces: el sistema muestra las direcciones de las sedes, un mapa de ubicación y los horarios de atención

Condición 04

Dado: que el usuario desea conocer los productos de la compañía

Cuando: selecciona "Catálogo de Servicios/Productos"

Entonces: el sistema muestra el listado de medicamentos fabricados por Laboratorio XYZ, con nombre del producto, descripción, principio activo, presentación y área de producción asociada

Condición 05

Dado: que el usuario navega por la información pública

Cuando: accede a cualquier sección del módulo público

Entonces: no se solicita autenticación ni se modifica ningún dato del sistema

## Tareas

| No | Descripción |
|---|---|
| 1 | Diseñar la interfaz del módulo público con menú de navegación (Información, Catálogo, Contacto, Sedes) |
| 2 | Implementar componente de "Información de la Empresa" con datos institucionales de Laboratorio XYZ |
| 3 | Implementar componente de "Catálogo de Servicios/Productos" con listado de medicamentos, descripción, principio activo y presentación |
| 4 | Implementar componente de "Datos de Contacto" con teléfonos, email y redes sociales |
| 5 | Implementar componente de "Ubicación de Sedes" con direcciones y mapa |
| 6 | Configurar endpoints GET públicos sin autenticación en Spring Boot |
| 7 | Implementar cache para optimizar la carga de información estática |

## Control de Versiones

| Versión | Fecha | Autor | Revisión | Descripción | Aprobador |
|---|---|---|---|---|---|
| 1.0 | 2026-07-26 | | | Versión inicial | |
