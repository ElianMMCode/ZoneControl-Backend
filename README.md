# ZoneControl

Sistema de **control de acceso físico** para un laboratorio farmacéutico (caso de estudio en [`docs/zonecontrol.pdf`](docs/zonecontrol.pdf)). Backend Spring Boot completo y frontend React + Vite.

## Funcionalidades

| Módulo | Descripción |
|---|---|
| **Público** | Sitio corporativo (institucional, contacto, sedes, catálogo) y descarga del folleto PDF |
| **Autenticación** | Login JWT por rol, configuración/restablecimiento de contraseña vía **magic link**, cambio de contraseña voluntario |
| **Administración** | CRUD de usuarios internos (activar/desactivar invalida el JWT al instante), contenido público, áreas de producción y **matriz de roles** (solo consulta) |
| **Gestión de Personal** | Registro individual con foto, carga masiva CSV con reporte de errores, búsqueda con filtros, detalle/edición de empleado, permisos de acceso con **turnos y horarios por día** |
| **Control de Acceso** | Validación de credencial, **ocupación en tiempo real** (quién está dentro), **cierre de emergencia por zona**, alertas de anomalías y panel de zonas en vivo vía **SSE** |
| **Reportes / Auditoría** | Historial con filtros (incluye departamento), export **CSV / Excel / PDF** y archivo periódico para socios **agregado por departamento sin datos personales** |

**Roles del sistema**: `ADMIN`, `GESTOR_PERSONAL`, `SUPERVISOR_AUDITOR`.

**Estado**: backend y frontend implementados; **214 tests verdes** (`./mvnw test`).

## Índice

- [1. Requisitos del sistema](#1-requisitos-del-sistema)
- [2. Instalación paso a paso](#2-instalación-paso-a-paso)
  - [2.1 PostgreSQL](#21-postgresql)
  - [2.2 Base de datos y credenciales](#22-base-de-datos-y-credenciales)
  - [2.3 JDK 21](#23-jdk-21)
  - [2.4 Node.js](#24-nodejs)
  - [2.5 Archivo `.env`](#25-archivo-env)
  - [2.6 Backend (Maven)](#26-backend-maven)
  - [2.7 Frontend (React + Vite)](#27-frontend-react--vite)
- [3. Usuarios seed para probar](#3-usuarios-seed-para-probar)
- [4. Comandos útiles](#4-comandos-útiles)
- [5. URLs](#5-urls)

## 1. Requisitos del sistema

| Requisito | Versión | Notas |
|---|---|---|
| PostgreSQL | 14+ (probado en 18) | Servidor corriendo en `localhost:5432`, BD `zonecontrol` |
| JDK | 21 | `JAVA_HOME` apuntando al JDK 21 |
| Maven | Wrapper incluido (`./mvnw`) | No hace falta instalarlo aparte |
| Node.js | 20.19+ / 22.12+ (lo exige Vite 8; probado en 24) | Para el frontend (Vite) |
| npm | 9+ | Viene con Node.js |

No se requieren variables de entorno extra: el proyecto usa `spring-dotenv` para leer `.env` de la raíz.

## 2. Instalación paso a paso

### 2.1 PostgreSQL

Instala el servidor:

```bash
# Arch Linux
sudo pacman -S postgresql

# Ubuntu / Debian
sudo apt update && sudo apt install postgresql
```

Inicializa el data dir y arranca el servicio. En Arch el data dir se inicializa
a mano (solo la primera vez); en Ubuntu/Debian el paquete ya crea un clúster por
defecto (`pg_createcluster`) y solo hay que arrancar el servicio:

```bash
# Arch Linux — inicializar data dir (solo la primera vez) y arrancar
sudo -u postgres initdb -D /var/lib/postgres/data --locale=C.UTF-8 --encoding=UTF8
sudo systemctl enable --now postgresql

# Ubuntu / Debian — el clúster ya existe; solo arrancar
sudo systemctl enable --now postgresql
```

### 2.2 Base de datos y credenciales

Crea la base de datos `zonecontrol` y fija la contraseña del usuario `postgres` (debe coincidir con la del `.env`):

```bash
sudo -u postgres psql -c "ALTER USER postgres WITH PASSWORD 'admin123*';"
sudo -u postgres createdb zonecontrol
```

Verifica la conexión:

```bash
PGPASSWORD='admin123*' psql -h localhost -U postgres -d zonecontrol -c "SELECT version();"
```

### 2.3 JDK 21

```bash
# Arch Linux
sudo pacman -S jdk21-openjdk

# Ubuntu / Debian
sudo apt update && sudo apt install openjdk-21-jdk

java -version   # debe mostrar 21.x
```

### 2.4 Node.js

```bash
# Arch Linux
sudo pacman -S nodejs npm

# Ubuntu / Debian — apt trae una versión de Node demasiado antigua para Vite 8;
# se instala una versión reciente vía NodeSource:
curl -fsSL https://deb.nodesource.com/setup_24.x | sudo -E bash -
sudo apt install -y nodejs

node --version && npm --version
```

### 2.5 Archivo `.env`

El proyecto requiere las credenciales de BD vía `.env` en la raíz (no hay defaults). Crea el archivo `.env` (está gitignored):

```env
DB_USERNAME=postgres
DB_PASSWORD=admin123*
```

### 2.6 Backend (Maven)

```bash
./mvnw test                  # opcional: comprueba que todo está verde (214 tests)
./mvnw spring-boot:run       # arranca la API en http://localhost:8080
```

> En el primer arranque el `DataInitializer` siembra departamentos, áreas, empleados, usuarios seed, permisos (con sus turnos) e historial de acceso de forma idempotente (solo si las tablas están vacías).

### 2.7 Frontend (React + Vite)

```bash
cd src/main/frontend
npm install                  # solo la primera vez (regenera node_modules)
npm run dev                  # Vite en http://localhost:5173
```

El front hace proxy de `/api` → `http://localhost:8080`, así que el backend debe estar corriendo.

Para integrar el front en Spring (build de producción):

```bash
npm run build                # regenera src/main/resources/static/
```

Tras el build, la aplicación completa también se sirve desde el backend en **http://localhost:8080/** (el SPA estático y sus rutas ya están permitidos por seguridad).

## 3. Usuarios seed para probar

El `DataInitializer` crea estos usuarios al primer arranque. Credenciales para probar cada rol:

| Rol | Email | Contraseña |
|---|---|---|
| ADMIN | `admin@zonecontrol.com` | `Admin123!` |
| GESTOR_PERSONAL | `gestor@zonecontrol.com` | `Gestor123!` |
| GESTOR_PERSONAL | `sandra.ruiz@laboratorioxzy.com.co` | `Demo1234!` |
| SUPERVISOR_AUDITOR | `javier.soto@laboratorioxzy.com.co` | `Demo1234!` |

> También se siembran `ana.martinez@...` (ADMIN) y `ricardo.diaz@...`
> (GESTOR_PERSONAL), pero tienen **setup token pendiente**: su contraseña se
> define vía magic link, no tienen credencial fija. `miguel.angel@...`
> (GESTOR_PERSONAL) se crea **INACTIVO** y no puede iniciar sesión.

## 4. Comandos útiles

```bash
./mvnw test                                  # todos los tests
./mvnw test -Dtest=AuthControllerTest        # un test concreto
./mvnw clean compile
./mvnw spring-boot:run                       # backend en :8080
cd src/main/frontend && npm run dev          # frontend en :5173
cd src/main/frontend && npm run lint         # oxlint
cd src/main/frontend && npm run typecheck    # tsc
```

## 5. URLs

| Recurso | URL |
|---|---|
| Frontend (dev) | http://localhost:5173 |
| App servida por Spring (tras build) | http://localhost:8080 |
| Backend API | http://localhost:8080 |
| Swagger UI | http://localhost:8080/swagger-ui.html |
| OpenAPI JSON | http://localhost:8080/v3/api-docs |

> Requiere PostgreSQL corriendo en `localhost:5432` y `.env` con `DB_USERNAME`/`DB_PASSWORD`.
