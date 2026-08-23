# ZoneControl — Backend

API REST del sistema de **control de acceso físico** del laboratorio
[`docs/zonecontrol.pdf`](docs/zonecontrol.pdf) (caso de estudio). Spring Boot
3.4 + Java 21 + JPA + PostgreSQL + JWT. Sirve también el SPA estático del
frontend (`src/main/resources/static/`).

> **Regla del proyecto:** ZoneControl usa **exclusivamente pnpm** (en el
> repo de frontend). Aunque este repo no instala dependencias Node, no
> deben aparecer `package-lock.json`, `yarn.lock` ni `node_modules/` aquí.
> Ver `AGENTS.md` para detalles.

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

**Estado**: 214 tests verdes (`./mvnw test`).

## Índice

- [1. Requisitos del sistema](#1-requisitos-del-sistema)
- [2. Instalación paso a paso](#2-instalación-paso-a-paso)
  - [2.1 PostgreSQL](#21-postgresql)
  - [2.2 Base de datos y credenciales](#22-base-de-datos-y-credenciales)
  - [2.3 JDK 21](#23-jdk-21)
  - [2.4 Archivo `.env`](#24-archivo-env)
  - [2.5 Backend (Maven)](#25-backend-maven)
- [3. Usuarios seed para probar](#3-usuarios-seed-para-probar)
- [4. Comandos útiles](#4-comandos-útiles)
- [5. Sincronización del bundle del frontend](#5-sincronización-del-bundle-del-frontend)
- [6. URLs](#6-urls)

## 1. Requisitos del sistema

| Requisito | Versión | Notas |
|---|---|---|
| PostgreSQL | 14+ (probado en 18) | Servidor corriendo en `localhost:5432`, BD `zonecontrol` |
| JDK | 21 | `JAVA_HOME` apuntando al JDK 21 |
| Maven | Wrapper incluido (`./mvnw`) | No hace falta instalarlo aparte |

No se requieren variables de entorno extra: el proyecto usa `spring-dotenv`
para leer `.env` de la raíz.

## 2. Instalación paso a paso

### 2.1 PostgreSQL

Instala el servidor:

```bash
# Arch Linux
sudo pacman -S postgresql

# Ubuntu / Debian
sudo apt update && sudo apt install postgresql
```

Inicializa el data dir y arranca el servicio. En Arch el data dir se
inicializa a mano (solo la primera vez); en Ubuntu/Debian el paquete ya
crea un clúster por defecto (`pg_createcluster`) y solo hay que arrancar
el servicio:

```bash
# Arch Linux — inicializar data dir (solo la primera vez) y arrancar
sudo -u postgres initdb -D /var/lib/postgres/data --locale=C.UTF-8 --encoding=UTF8
sudo systemctl enable --now postgresql

# Ubuntu / Debian — el clúster ya existe; solo arrancar
sudo systemctl enable --now postgresql
```

### 2.2 Base de datos y credenciales

Crea la base de datos `zonecontrol` y fija la contraseña del usuario
`postgres` (debe coincidir con la del `.env`):

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

### 2.4 Archivo `.env`

El proyecto requiere las credenciales de BD vía `.env` en la raíz (no hay
defaults). Copia `.env.example` a `.env` (está gitignored) y rellena:

```bash
cp .env.example .env
```

```env
DB_USERNAME=postgres
DB_PASSWORD=admin123*
```

### 2.5 Backend (Maven)

```bash
./mvnw test                  # opcional: comprueba que todo está verde (214 tests)
./mvnw spring-boot:run       # arranca la API en http://localhost:8080
```

> En el primer arranque el `DataInitializer` siembra departamentos, áreas,
> empleados, usuarios seed, permisos (con sus turnos) e historial de acceso
> de forma idempotente (solo si las tablas están vacías).

Tras arrancar, la aplicación completa (SPA + API) se sirve desde
**http://localhost:8080/** siempre que el bundle esté sincronizado en
`src/main/resources/static/`. Ver [§5](#5-sincronización-del-bundle-del-frontend).

## 3. Usuarios seed para probar

El `DataInitializer` crea estos usuarios al primer arranque. Credenciales
para probar cada rol:

| Rol | Email | Contraseña |
|---|---|---|
| ADMIN | `admin@zonecontrol.com` | `Admin123!` |
| GESTOR_PERSONAL | `gestor@zonecontrol.com` | `Gestor123!` |
| GESTOR_PERSONAL | `sandra.ruiz@laboratorioxzy.com.co` | `Demo1234!` |
| SUPERVISOR_AUDITOR | `javier.soto@laboratorioxzy.com.co` | `Demo1234!` |

> También se siembran `ana.martinez@...` (ADMIN) y `ricardo.diaz@...`
> (GESTOR_PERSONAL), pero tienen **setup token pendiente**: su contraseña
> se define vía magic link, no tienen credencial fija. `miguel.angel@...`
> (GESTOR_PERSONAL) se crea **INACTIVO** y no puede iniciar sesión.

## 4. Comandos útiles

```bash
./mvnw test                                  # todos los tests
./mvnw test -Dtest=AuthControllerTest        # un test concreto
./mvnw clean compile
./mvnw spring-boot:run                       # backend en :8080
./mvnw clean package                         # JAR ejecutable en target/
```

## 5. Sincronización del bundle del frontend

El SPA se versiona en el repo separado
[`ZoneControl-Frontend`](https://github.com/ElianMMCode/ZoneControl-Frontend)
(gestionado con **pnpm**; ver su `README` y `AGENTS.md`). El bundle final
se copia a `src/main/resources/static/` para que Spring lo sirva.

Procedimiento manual (sin CI):

```bash
# (1) Clonar el frontend adyacente a este repo (o donde prefieras)
git clone https://github.com/ElianMMCode/ZoneControl-Frontend.git ../ZoneControl-Frontend

# (2) Build del frontend
cd ../ZoneControl-Frontend
pnpm install
pnpm run build   # produce ./dist/

# (3) Copiar el bundle al backend
rm -rf ../ZoneControl-Backend/src/main/resources/static/*
cp -r dist/* ../ZoneControl-Backend/src/main/resources/static/

# (4) Commit en el backend
cd ../ZoneControl-Backend
git add src/main/resources/static/
git commit -m "chore: sincronizar bundle SPA desde ZoneControl-Frontend"
```

> **Importante:** el frontend debe usar `pnpm` (regla dura del proyecto).
> Nunca `npm install` ni `yarn`.

## 6. URLs

| Recurso | URL |
|---|---|
| Backend API | http://localhost:8080 |
| App servida por Spring (SPA + API, tras sincronizar bundle) | http://localhost:8080 |
| Swagger UI | http://localhost:8080/swagger-ui.html |
| OpenAPI JSON | http://localhost:8080/v3/api-docs |
| Frontend dev (gestionado en repo aparte) | http://localhost:5173 |

> Requiere PostgreSQL corriendo en `localhost:5432` y `.env` con
> `DB_USERNAME`/`DB_PASSWORD`.