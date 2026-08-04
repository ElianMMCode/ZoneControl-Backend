# HU-05 - CREAR USUARIO INTERNO

| Campo | Valor |
|---|---|
| **Código** | HU-05 |
| **Nombre** | Crear Usuario Interno |
| **Complejidad** | Alta |
| **HU Relacionada** | HU-03, HU-06, HU-07, HU-08 |
| **Módulo** | Módulo de Administración |

## Descripción

**Yo como** administrador del sistema
**Requiero** crear nuevos usuarios internos con roles específicos, datos completos y una contraseña que cumpla con los requisitos de seguridad establecidos
**Para** que otros usuarios puedan acceder al sistema según sus funciones asignadas con credenciales robustas

## Requerimiento

El sistema debe permitir al administrador crear usuarios internos del sistema, asignándoles un rol (Administrador, Gestor de Personal o Supervisor/Auditor) y un estado inicial (Activo/Inactivo). Cada usuario debe estar vinculado a un empleado existente (relación @OneToOne obligatoria), garantizando que todo usuario del sistema es también personal de la empresa. El email debe ser único en el sistema.

**Seguridad**: La contraseña no se define en el formulario de creación por seguridad. En su lugar, el sistema genera internamente un token de un solo uso (setupToken), lo almacena hasheado en BD con expiración de 24h y envía un **magic link** al email del usuario para que éste establezca su propia contraseña. De esta forma:
- El administrador nunca ve ni conoce la contraseña del usuario
- La contraseña nunca viaja por email en texto plano
- El usuario elige su propia contraseña (mayor retentión y seguridad)
- Se alinea con NIST SP 800-63B (no enviar secrets por canales no seguros)

## Criterios de Aceptación

Condición 01

Dado: que el administrador está autenticado y completa el formulario seleccionando un empleado válido, ingresando un email único, un rol y un estado

Cuando: envía el formulario de creación

Entonces: el sistema deriva firstName, lastName y email del Employee vinculado (el email es el correo personal del empleado, no corporativo), genera un setupToken criptográfico aleatorio de 96 caracteres hex, lo hashea con SHA-256 (permite búsqueda directa por hash, no requiere KDF lento por tratarse de un token de alta entropía), lo almacena en la columna setupToken junto con setupTokenExpiry = now() + 24h, guarda el usuario en PostgreSQL con password = null, retorna HTTP 201 y actualiza la lista de usuarios en el frontend. El sistema también envía un email con el magic link al correo personal del empleado.

Condición 02

Dado: que el administrador ingresa un email

Cuando: el email ya está registrado en el sistema

Entonces: el sistema retorna HTTP 409 y muestra el mensaje "El email ingresado ya se encuentra registrado en el sistema"

Condición 03

Dado: que el usuario recibe el email con el magic link

Cuando: hace clic en el enlace dentro de las 24 horas

Entonces: el sistema valida el setupToken contra el hash almacenado en BD, verifica que setupTokenExpiry no haya vencido, redirige al usuario a una pantalla de configuración de contraseña donde debe ingresar una nueva contraseña que cumpla los requisitos (mínimo 8 caracteres, al menos 1 mayúscula, 1 minúscula, 1 dígito, 1 carácter especial @$!%*?&). Al enviar, el sistema encripta la contraseña con BCrypt, la guarda en la columna password, limpia setupToken y setupTokenExpiry, marca requirePasswordChange = false y redirige al dashboard del rol correspondiente

Condición 04

Dado: que el usuario intenta usar el magic link

Cuando: han pasado más de 24 horas desde la creación del usuario o el enlace ya fue usado

Entonces: el sistema muestra el mensaje "El enlace de configuración ha expirado. Contacte al administrador para generar un nuevo enlace"

Condición 05

Dado: que el administrador envía el formulario de creación

Cuando: hay campos obligatorios vacíos (empleado, rol, estado)

Entonces: el sistema muestra los mensajes de error específicos debajo de cada campo inválido e impide el envío hasta que todos los errores sean corregidos

Condición 06

Dado: que el administrador selecciona un empleado para crear el usuario

Cuando: el empleado no tiene un correo personal registrado en Gestión Personal

Entonces: el sistema retorna HTTP 400 y muestra el mensaje "El empleado no tiene un correo registrado. Regístrelo en Gestión Personal para poder crear el usuario"

## Notas Técnicas

### Dependencia: Employee debe existir antes que User

- `POST /api/admin/users` requiere `employeeCode` de un `Employee` ya registrado en el sistema
- Relación `@OneToOne` con `employee_id NOT NULL UNIQUE` en la tabla `users`
- Un `Employee` solo puede tener un `User` asociado (error HTTP 409 si ya existe uno)
- No todo `Employee` necesita un `User` de sistema (empleados con solo permiso de acceso físico no requieren credenciales del sistema)
- Flujo correcto:
  1. `POST /api/personal` → registra `Employee` (genera código EMP-XXXXXX)
  2. `POST /api/admin/users` con `employeeCode` → crea `User` vinculado

### Orden de creación

```
┌─────────────────────────────────────┐
│  Paso 1: Registrar empleado         │
│  POST /api/personal                     │
│  → Crea Employee con EMP-XXXXXX     │
│  → Retorna { id, employeeCode }     │
└─────────────────────────────────────┘
              │
              ▼
┌─────────────────────────────────────┐
│  Paso 2: Crear usuario sistema      │
│  POST /api/admin/users                  │
│  { employeeCode: "EMP-000001", ... }│
│  → Valida que Employee exista       │
│  → Valida que no tenga User ya      │
│  → Crea User con @OneToOne          │
└─────────────────────────────────────┘
```

## Tareas

| No | Descripción |
|---|---|---|
| 1 | Diseñar formulario de creación de usuario con campos: empleado (selector que busca empleados existentes y muestra cargo), nombre completo (readonly desde empleado), cargo (readonly), rol (select: ADMIN/GESTOR_PERSONAL/SUPERVISOR_AUDITOR), estado (select: ACTIVO/INACTIVO). Sin campos de contraseña ni email |
| 2 | Implementar endpoint POST /api/admin/users en Spring Boot que: valida empleado existente y sin User previo, valida que el empleado tenga email registrado, valida email único, deriva firstName/lastName/email del Employee, genera setupToken criptográfico de 96 caracteres hex, lo hashea con SHA-256, guarda User con password=null y setupTokenExpiry=now+24h |
| 3 | Implementar servicio MagicLinkNotifier que construye el magic link con el setupToken sin hashear: {app.app-url}/configurar-contrasena?token={rawToken}. Actualmente registra el enlace en log; se reemplazará por JavaMailSender cuando exista SMTP |
| 4 | Implementar endpoint GET /api/setup-password?token=... que valida el token contra el hash SHA-256 en BD, verifica expiración y devuelve datos del usuario para la pantalla de configuración |
| 5 | Implementar endpoint POST /api/setup-password que recibe token + nueva contraseña, valida requisitos de seguridad (mínimo 8 caracteres, 1 mayúscula, 1 minúscula, 1 dígito, 1 carácter especial @$!%*?&), encripta con BCrypt, guarda en password, limpia setupToken y setupTokenExpiry, marca requirePasswordChange=false |
| 6 | Agregar en User.java: setupToken (String, unique, nullable), setupTokenExpiry (LocalDateTime, nullable), password ahora nullable = true |
| 7 | Agregar email personal (nullable) en Employee.java, RegisterEmployeeRequest y UpdateEmployeeRequest |
| 8 | Implementar GET /api/admin/users con búsqueda (nombre/email/código), filtros por rol y estado, y paginación |
| 9 | Implementar GET /api/admin/users/{id} con detalle del usuario y su empleado vinculado |
| 10 | Retornar HTTP 201 con ID del usuario creado y mostrar notificación de éxito en el frontend indicando que se envió magic link |
| 11 | Manejar respuestas de error: 409 para email duplicado o empleado ya vinculado, 400 para empleado inexistente o sin email, 410 para token expirado, 404 para token inválido |

## Demo (sin SMTP)

Mientras no exista SMTP configurado, `MagicLinkNotifier` solo registra el enlace en el log del backend y **no envía correo**. Para poder probar el flujo, `POST /api/admin/users` devuelve en la respuesta el campo `setupUrl` con el enlace completo; el frontend muestra un botón **"Abrir configuración"** que abre esa URL en una nueva ventana (vista `/configurar-contrasena?token=...`).

## Control de Versiones

| Versión | Fecha | Autor | Revisión | Descripción | Aprobador |
|---|---|---|---|---|---|
| 1.0 | 2026-07-26 | | | Versión inicial | |
