# Diagramas de Flujo - Zone Control

## Contenido

1. [Flujo General del Sistema](#1-flujo-general-del-sistema)
2. [Módulo Público (CU-01)](#2-módulo-público-cu-01)
3. [Módulo de Autenticación (CU-02)](#3-módulo-de-autenticación-cu-02)
4. [Módulo de Administración (CU-03)](#4-módulo-de-administración-cu-03)
5. [Módulo de Gestión de Personal (CU-04 a CU-07)](#5-módulo-de-gestión-de-personal)
6. [Módulo de Control de Acceso Físico (CU-11)](#6-módulo-de-control-de-acceso-físico-cu-11)
7. [Módulo de Reportes y Auditoría (CU-08 a CU-10)](#7-módulo-de-reportes-y-auditoría)

---

## 1. Flujo General del Sistema

### Diagrama de Actores y Relaciones

```
┌─────────────────────┐
│   ACTORES EXTERNOS  │
├─────────────────────┤
│ • Público General   │──► CU-01 (Información pública)
│ • Empleado          │──► CU-11 (Solicitar acceso)
│ • Socio Internacional│──► Recibe archivo periódico (CU-10)
└─────────────────────┘

┌─────────────────────┐
│  ACTORES INTERNOS   │
│  (requieren login)  │
├─────────────────────┤
│ • Administrador     │──► CU-03 (Administrar sistema)
│ • Gestor de Personal│──► CU-04, CU-05, CU-06, CU-07
│ • Supervisor/Auditor│──► CU-08, CU-09, CU-10
└─────────────────────┘
```

### Flujo Transversal: Autenticación

```
Inicio
  │
  ▼
[Usuario accede a módulo interno]
  │
  ▼
¿Está autenticado? ──No──► Redirigir a Login
  │                              │
  Sí                             ▼
  │                     Ingresar credenciales
  ▼                              │
[Continúa al módulo]            ▼
                          ¿Credenciales válidas?
                            │           │
                           Sí          No
                            │           │
                            ▼           ▼
                    [Token/Sesión]  Mostrar error
                            │       y reintentar
                            ▼
                    [Continúa al módulo]
```

---

## 2. Módulo Público (CU-01)

**Actor:** Público General  
**Precondición:** Ninguna (acceso libre)  
**Postcondición:** Usuario visualiza información institucional

### Flujo Principal

```
Inicio
  │
  ▼
[Usuario ingresa al sitio web]
  │
  ▼
┌─────────────────────────────────┐
│  MENÚ PÚBLICO (sin auth)        │
├─────────────────────────────────┤
│  ► Información Institucional    │──► Visualizar datos generales
│  ► Datos de Contacto            │──► Visualizar teléfonos, email
│  ► Ubicación de Sedes           │──► Visualizar direcciones/mapa
│  ► Descargar Folleto            │──► Descargar archivo (extend)
└─────────────────────────────────┘
  │
  ▼
Fin
```

### Flujo: Descargar Folleto Informativo (Extensión)

```
[Desde página de información institucional]
  │
  ▼
[Botón "Descargar Folleto"]
  │
  ▼
[Sistema genera/sirve el archivo PDF]
  │
  ▼
[Navegador descarga el archivo]
  │
  ▼
Fin
```

---

## 3. Módulo de Autenticación (CU-02)

**Actor:** Usuario Interno (Administrador, Gestor, Supervisor)  
**Precondición:** Usuario registrado en el sistema  
**Postcondición:** Sesión activa con token de autenticación

### Flujo Principal: Iniciar Sesión

```
Inicio
  │
  ▼
[Usuario accede a pantalla de Login]
  │
  ▼
[Ingresar correo electrónico]
  │
  ▼
[Ingresar contraseña]
  │
  ▼
[Enviar credenciales al backend]
  │
  ▼
┌──────────────────────────┐
│  Backend valida contra   │
│  PostgreSQL              │
├──────────────────────────┤
│ 1. ¿Existe el usuario?  │
│    │           │        │
│   Sí          No        │
│    │           │        │
│    ▼           ▼        │
│ 2. ¿Contraseña   Retornar│
│    coincide?    error   │
│    │     │              │
│   Sí    No              │
│    │     │              │
│    ▼     ▼              │
│  Generar Retornar       │
│  Token   error          │
└──────────────────────────┘
  │
  ▼
[Retornar token JWT + datos del usuario]
  │
  ▼
[Frontend almacena token]
  │
  ▼
[Redirigir al dashboard según rol]
  │
  ▼
Fin
```

### Flujo: Cerrar Sesión

```
Inicio
  │
  ▼
[Usuario presiona "Cerrar Sesión"]
  │
  ▼
[Frontend elimina token]
  │
  ▼
[Redirigir a pantalla de Login]
  │
  ▼
Fin
```

---

## 4. Módulo de Administración (CU-03)

**Actor:** Administrador  
**Precondición:** Sesión autenticada  
**Postcondición:** Cambios aplicados en el sistema

### Flujo Principal: Administrar Sistema y Usuarios

```
Inicio
  │
  ▼
[Administrador ingresa al módulo]
  │
  ▼
┌─────────────────────────────────────┐
│  PANEL DE ADMINISTRACIÓN            │
├─────────────────────────────────────┤
│                                     │
│  ► Gestionar Usuarios Internos      │
│  │   ├── Crear nuevo usuario        │
│  │   ├── Editar usuario existente   │
│  │   ├── Activar/Desactivar usuario │
│  │   └── Restablecer contraseña     │
│  │                                  │
│  ► Configuración del Sistema        │
│  │   ├── Parámetros generales       │
│  │   └── Roles y permisos           │
│  │                                  │
│  ► Auditoría de Administración      │
│      └── Log de acciones admin      │
│                                     │
└─────────────────────────────────────┘
  │
  ▼
Fin
```

### Flujo: CRUD de Usuarios Internos

```
Inicio
  │
  ▼
[Seleccionar "Gestionar Usuarios"]
  │
  ▼
[Listar usuarios existentes]
  │
  ├──► [Crear] ──► [Formulario: nombre, email, rol, estado]
  │                    │
  │                    ▼
  │              [Validar datos]
  │                    │
  │              ¿Válido? ──No──► Mostrar errores
  │                    │
  │                   Sí
  │                    │
  │                    ▼
  │              [Guardar en PostgreSQL]
  │                    │
  │                    ▼
  │              [Confirmar creación]
  │
  ├──► [Editar] ──► [Seleccionar usuario]
  │                    │
  │                    ▼
  │              [Formulario con datos actuales]
  │                    │
  │                    ▼
  │              [Modificar campos]
  │                    │
  │                    ▼
  │              [Guardar cambios]
  │
  ├──► [Activar/Desactivar] ──► [Toggle estado]
  │
  └──► [Restablecer Contraseña] ──► [Generar contraseña temporal]
                                       │
                                       ▼
                                 [Notificar al usuario]
```

---

## 5. Módulo de Gestión de Personal

### 5.1 CU-04: Registrar Personal Individual

**Actor:** Gestor de Personal  
**Precondición:** Sesión autenticada  
**Postcondición:** Personal registrado y asignado a departamento

```
Inicio
  │
  ▼
[Gestor accede a "Registrar Personal"]
  │
  ▼
[Formulario de registro]
  │
  ▼
[Ingresar datos del empleado]
  • Número de identificación
  • Nombres y apellidos
  • Cargo
  • Departamento (seleccionar)
  • Estado (activo/inactivo)
  │
  ▼
[Validar datos]
  │
  ├─ ¿Campos obligatorios completos? ──No──► Indicar errores
  │
  ├─ ¿ID ya existe? ──Sí──► Mostrar "Personal ya registrado"
  │
  └─ Todos válidos
       │
       ▼
[Guardar en tabla Personal]
       │
       ▼
[Asignar departamento]
       │
       ▼
[Confirmar registro]
       │
       ▼
Fin
```

### 5.2 CU-05: Carga Masiva desde Archivo Plano

**Actor:** Gestor de Personal  
**Precondición:** Sesión autenticada  
**Postcondición:** Múltiples registros cargados y validados

```
Inicio
  │
  ▼
[Gestor accede a "Carga Masiva"]
  │
  ▼
[Seleccionar archivo plano (CSV/TXT)]
  │
  ▼
[Subir archivo al servidor]
  │
  ▼
┌──────────────────────────────┐
│  VALIDACIÓN DE ESTRUCTURA    │
├──────────────────────────────┤
│ 1. ¿Formato válido?         │
│    (columnas, delimitador)  │
│    │           │            │
│   Sí          No            │
│    │           │            │
│    ▼           ▼            │
│ 2. ¿Encabezados Retornar    │
│    correctos?  error de     │
│    │     │    estructura    │
│   Sí    No                  │
│    │     │                  │
│    ▼     ▼                  │
│ 3. Continuar Retornar       │
│    a validación error       │
│    de info.                 │
└──────────────────────────────┘
  │
  ▼
┌──────────────────────────────┐
│  VALIDACIÓN DE INFORMACIÓN   │
│  (fila por fila)             │
├──────────────────────────────┤
│ Para cada fila:              │
│  • ¿ID válido y único?      │
│  • ¿Nombres completos?      │
│  • ¿Departamento existe?    │
│  • ¿Cargo válido?           │
│                              │
│ Resultado:                   │
│  • Filas válidas → Insertar  │
│  • Filas inválidas → Reporte │
└──────────────────────────────┘
  │
  ▼
[Insertar registros válidos en PostgreSQL]
  │
  ▼
[Mostrar resumen]
  • Registros exitosos: X
  • Registros con error: Y
  • Detalle de errores (descargable)
  │
  ▼
Fin
```

### 5.3 CU-06: Gestionar Permisos de Acceso

**Actor:** Gestor de Personal  
**Precondición:** Sesión autenticada, personal registrado  
**Postcondición:** Permisos de acceso actualizados

```
Inicio
  │
  ▼
[Gestor accede a "Gestionar Permisos"]
  │
  ▼
[Buscar empleado]
  │
  ▼
[Seleccionar empleado]
  │
  ▼
┌────────────────────────────────────┐
│  ACCIONES DE PERMISO               │
├────────────────────────────────────┤
│                                    │
│  ► OTORGAR ACCESO                  │
│  │   • Seleccionar áreas/zonas     │
│  │   • Definir horarios            │
│  │   • Definir vigencia            │
│  │   • Confirmar                   │
│  │                                 │
│  ► REVOCAR ACCESO                  │
│  │   • Seleccionar permiso activo  │
│  │   • Confirmar revocación        │
│  │                                 │
│  ► SUSPENDER ACCESO                │
│      • Seleccionar permiso activo  │
│      • Definir fecha reactivación  │
│      • Confirmar suspensión        │
│                                    │
└────────────────────────────────────┘
  │
  ▼
[Actualizar estado en PostgreSQL]
  │
  ▼
[Confirmar cambio al gestor]
  │
  ▼
Fin
```

### 5.4 CU-07: Buscar Personal por Filtros

**Actor:** Gestor de Personal  
**Precondición:** Sesión autenticada  
**Postcondición:** Lista de personal filtrada

```
Inicio
  │
  ▼
[Gestor accede a "Buscar Personal"]
  │
  ▼
┌────────────────────────────────┐
│  FORMULARIO DE BÚSQUEDA        │
├────────────────────────────────┤
│  Filtros disponibles:          │
│  • Número de identificación    │
│  • Nombre                      │
│  • Apellido                    │
│  • Departamento (dropdown)     │
│  • Estado (activo/inactivo)    │
│                                │
│  [Botón Buscar]                │
└────────────────────────────────┘
  │
  ▼
[Enviar filtros al backend]
  │
  ▼
[Backend construye query dinámica]
  │
  ▼
[Consultar PostgreSQL con filtros]
  │
  ▼
[Retornar resultados]
  │
  ▼
┌────────────────────────────────┐
│  TABLA DE RESULTADOS           │
├────────────────────────────────┤
│ ID │ Nombre │ Depto │ Estado  │
│────│────────│───────│─────────│
│    │        │       │         │
│    │        │       │         │
└────────────────────────────────┘
  │
  ▼
[Opciones: Ver detalle / Editar / Gestionar permisos]
  │
  ▼
Fin
```

---

## 6. Módulo de Control de Acceso Físico (CU-11)

**Actor:** Personal de Seguridad / Empleado  
**Precondición:** Ninguna (simulación transaccional, sin auth)  
**Postcondición:** Resultado del intento registrado en historial

### Flujo Principal: Simular Lectura de Credencial

```
Inicio
  │
  ▼
[Empleado ingresa número de identificación]
  │
  ▼
[Frontend envía POST /api/access/simulate]
  │
  ▼
┌──────────────────────────────────────────────┐
│  BACKEND (Spring Boot)                       │
├──────────────────────────────────────────────┤
│                                              │
│  [Recibir petición]                          │
│       │                                      │
│       ▼                                      │
│  [Consultar PostgreSQL por ID de usuario]    │
│       │                                      │
│       ▼                                      │
│  ┌─ ¿El usuario existe? ──────────────────┐ │
│  │                                         │ │
│  │  SÍ                       NO            │ │
│  │   │                         │           │ │
│  │   ▼                         ▼           │ │
│  │  ┌───────────┐      ┌─────────────┐    │ │
│  │  │ ¿Estado == │      │ Resultado:  │    │ │
│  │  │ Autorizado?│      │ "No         │    │ │
│  │  │    │    │  │      │ Registrado" │    │ │
│  │  │   SÍ    NO│      └─────────────┘    │ │
│  │  │    │     │                          │ │
│  │  │    ▼     ▼                          │ │
│  │  │ "Ingreso "Ingreso                   │ │
│  │  │ Autorizado" Denegado"               │ │
│  │  └───────────┘                         │ │
│  └─────────────────────────────────────────┘ │
│       │                                      │
│       ▼                                      │
│  [Construir objeto de auditoría]             │
│  {                                           │
│    id, fecha, hora, resultado,              │
│    id_usuario, ip_dispositivo               │
│  }                                           │
│       │                                      │
│       ▼                                      │
│  [INSERT en tabla Historial]                 │
│       │                                      │
│       ▼                                      │
│  [Retornar HTTP response + mensaje]          │
│                                              │
└──────────────────────────────────────────────┘
  │
  ▼
┌──────────────────────────────────────────────┐
│  FRONTEND (React / Vue)                      │
├──────────────────────────────────────────────┤
│                                              │
│  [Recibir respuesta]                         │
│       │                                      │
│       ▼                                      │
│  [Renderizar alerta visual]                  │
│                                              │
│  ┌──────────────────────────────────────┐    │
│  │  ✅ INGRESO AUTORIZADO    (verde)    │    │
│  │  ❌ INGRESO DENEGADO     (rojo)     │    │
│  │  ⚠️  NO REGISTRADO        (amarillo) │    │
│  └──────────────────────────────────────┘    │
│                                              │
└──────────────────────────────────────────────┘
  │
  ▼
Fin
```

### Diagrama de Flujo Compacto (Actividad)

```
[Frontend]           [Backend]              [PostgreSQL]
    │                    │                      │
    │  POST /api/access  │                      │
    │  /simulate         │                      │
    ├───────────────────►│                      │
    │                    │  SELECT * FROM        │
    │                    │  usuarios WHERE id =? │
    │                    ├─────────────────────►│
    │                    │◄─────────────────────┤
    │                    │                      │
    │                    │  [Evaluar resultado]  │
    │                    │                      │
    │                    │  INSERT INTO historial│
    │                    ├─────────────────────►│
    │                    │                      │
    │  HTTP Response     │                      │
    │◄───────────────────┤                      │
    │                    │                      │
    │  [Mostrar alerta]  │                      │
    │                    │                      │
```

---

## 7. Módulo de Reportes y Auditoría

### 7.1 CU-08: Consultar Historial de Accesos por Fechas

**Actor:** Supervisor / Auditor  
**Precondición:** Sesión autenticada  
**Postcondición:** Historial visualizado

```
Inicio
  │
  ▼
[Supervisor accede a "Consultar Historial"]
  │
  ▼
┌────────────────────────────────┐
│  FORMULARIO DE CONSULTA        │
├────────────────────────────────┤
│  • Fecha inicio (date picker)  │
│  • Fecha fin (date picker)     │
│  • Filtros opcionales:         │
│    - Empleado específico       │
│    - Departamento              │
│    - Resultado (aut/den)       │
│                                │
│  [Botón Consultar]             │
└────────────────────────────────┘
  │
  ▼
[Enviar filtros al backend]
  │
  ▼
[Backend construye query con rango de fechas]
  │
  ▼
[Consultar tabla Historial en PostgreSQL]
  │
  ▼
[Retornar registros]
  │
  ▼
┌────────────────────────────────────────┐
│  TABLA DE HISTORIAL                    │
├────────────────────────────────────────┤
│ Fecha │ Hora │ ID │ Empleado │ Result │
│───────│──────│────│──────────│────────│
│       │      │    │          │        │
└────────────────────────────────────────┘
  │
  ▼
[Opciones: Exportar PDF / Exportar CSV]
  │
  ▼
Fin
```

### 7.2 CU-09: Generar Documento Descargable

**Actor:** Supervisor / Auditor  
**Precondición:** Consulta de historial realizada (CU-08)  
**Postcondición:** Archivo descargado

```
Inicio (desde resultados de CU-08)
  │
  ▼
[Supervisor selecciona "Exportar"]
  │
  ▼
┌─────────────────────────────┐
│  SELECCIONAR FORMATO        │
├─────────────────────────────┤
│  ► PDF (documento formal)   │
│  ► CSV (datos crudos)       │
│  ► Excel (.xlsx)            │
└─────────────────────────────┘
  │
  ▼
[Backend genera el archivo]
  │
  ▼
[Incluir: encabezado, fecha, filtros aplicados, tabla de datos]
  │
  ▼
[Retornar archivo como descarga]
  │
  ▼
[Navegador descarga el archivo]
  │
  ▼
Fin
```

### 7.3 CU-10: Generar Archivo Periódico para Socios Internacionales

**Actor:** Supervisor / Auditor  
**Precondición:** Sesión autenticada  
**Postcondición:** Archivo generado y enviado al socio internacional

```
Inicio
  │
  ▼
[Supervisor accede a "Archivo Periódico"]
  │
  ▼
┌──────────────────────────────────────┐
│  CONFIGURACIÓN DEL REPORTE           │
├──────────────────────────────────────┤
│  • Período: [Mes/Año]               │
│  • Departamentos a incluir           │
│  • Formato de salida                 │
│                                      │
│  [Botón Generar Archivo]             │
└──────────────────────────────────────┘
  │
  ▼
[Backend agrupa accesos por departamento]
  │
  ▼
┌──────────────────────────────────────┐
│  AGRUPACIÓN POR DEPARTAMENTO         │
├──────────────────────────────────────┤
│  Depto A:                            │
│    • Empleado 1: X accesos          │
│    • Empleado 2: Y accesos          │
│  Depto B:                            │
│    • Empleado 3: Z accesos          │
│  ...                                 │
└──────────────────────────────────────┘
  │
  ▼
[Generar archivo de intercambio]
  │
  ▼
[Enviar/descargar archivo para Socio Internacional]
  │
  ▼
Fin
```

---

## Resumen de Flujos por Caso de Uso

| Caso de Uso | Módulo | Actor | Flujo Principal | Autenticación |
|-------------|--------|-------|----------------|---------------|
| CU-01 | Público | Público General | Consultar info + Descargar folleto | No |
| CU-02 | Autenticación | Usuario Interno | Login + Logout | No (es la auth) |
| CU-03 | Administración | Administrador | CRUD usuarios + Config sistema | Sí |
| CU-04 | Gestión Personal | Gestor | Registrar personal individual | Sí |
| CU-05 | Gestión Personal | Gestor | Carga masiva + Validación | Sí |
| CU-06 | Gestión Personal | Gestor | Otorgar/Revocar/Suspender permisos | Sí |
| CU-07 | Gestión Personal | Gestor | Búsqueda con filtros | Sí |
| CU-08 | Reportes | Supervisor | Consultar historial por fechas | Sí |
| CU-09 | Reportes | Supervisor | Generar documento descargable | Sí |
| CU-10 | Reportes | Supervisor | Archivo periódico para socios | Sí |
| CU-11 | Control Acceso | Seguridad/Empleado | Simular lectura de credencial | No (transaccional) |

---

## Notas Técnicas

- **API de acceso:** `POST /api/access/simulate` (CU-11, sin auth)
- **Autenticación:** JWT (token en headers para módulos internos)
- **Base de datos principal:** PostgreSQL
- **Tablas clave:** `usuarios`, `personal`, `departamentos`, `permisos`, `historial_accesos`
- **Frontend:** React o Vue.js
- **Backend:** Spring Boot (Java)
