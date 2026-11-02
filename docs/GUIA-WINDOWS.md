# Guía de instalación — Windows 10 / 11

**Ahora el equipo usa H2** (una base de datos que viene dentro del proyecto: no hay que instalar
nada aparte). MariaDB queda para más adelante, al final de esta guía. Los comandos son para
**PowerShell**.

## Cómo manejamos el usuario de la base de datos (léalo una vez)

- **Todos usamos el mismo nombre de usuario: `ecommerce_app`.** Cada integrante pone **su propia
  contraseña** en su archivo `ecommerce\.env`, que **no se sube a git**.
- **Con H2 el usuario no se crea a mano:** al arrancar, la aplicación crea la base y la deja a nombre
  del usuario de `.env`. Ese mismo usuario y contraseña son los de la consola H2.
- **No hay nada que sincronizar.** Cada uno tiene su propia base; las tablas salen del código.
  Con H2 la base vive en memoria: **se borra al apagar** y al arrancar se vuelven a cargar los datos de
  ejemplo (`data.sql`).
- **No se usa root ni `sa`.** Si no crea el `.env`, el proyecto usa `sa` sin contraseña para que igual
  arranque, pero lo correcto es tener su `.env`.

---

## 1. Instalar lo necesario

Abra **PowerShell** y ejecute:
```powershell
winget install --id Git.Git -e
winget install --id EclipseAdoptium.Temurin.21.JDK -e
```
Cierre y vuelva a abrir PowerShell, y compruebe que `java -version` diga **21**.

## 2. Obtener la última versión del proyecto

**Si aún no tiene el proyecto:**
```powershell
git clone https://github.com/HKH-Dev/FrutiMarket.git
cd FrutiMarket
git checkout develop
```

**Si ya lo tiene:** primero guarde aparte cualquier cambio local que no haya subido, y luego actualice:
```powershell
cd FrutiMarket
git status
git stash
git checkout develop
git pull origin develop
```
`git stash` guarda sus cambios sin borrarlos; puede recuperarlos después con `git stash pop`.

## 3. Crear su archivo `.env` (su usuario de base de datos)

```powershell
cd ecommerce
Copy-Item .env.example .env
notepad .env
```
Deje `DB_USER=ecommerce_app` y cambie `DB_PASSWORD` por una contraseña suya. No ponga espacios
alrededor del `=` ni comillas. Guarde y cierre el Bloc de notas.

Con H2 se usan `DB_USER`, `DB_PASSWORD` y `DB_NAME`. Cambie también `JWT_SECRET` por un texto
largo (32 caracteres o más): es la clave con la que se firman los tokens de inicio de sesión.
`ADMIN_CLAVE`, `CAMPESINO_CLAVE` y `COMPRADOR_CLAVE` son las claves de los usuarios de prueba
de `POST /api/auth/login`. Lo de `DB_HOST`, `DB_PORT` y `DB_ROOT_PASSWORD` es solo para MariaDB.

> El archivo debe llamarse exactamente `.env`, no `.env.txt`. Compruébelo con
> `Get-ChildItem -Force .env`.

## 4. Ejecutar el proyecto con H2

Desde la carpeta `ecommerce`:
```powershell
.\gradlew.bat bootRun
```
Cuando vea `Started EcommerceApplication`, está listo.

**En IntelliJ:** ejecute `EcommerceApplication` (con **Active profiles** vacío). Spring lee el `.env`
solo, no hay que configurar variables.

## 5. Conectarse a la base de datos H2

Abra http://localhost:8080/h2-console y llene el formulario así:

| Campo | Valor |
|---|---|
| Driver Class | `org.h2.Driver` |
| JDBC URL | `jdbc:h2:mem:ecommercedb` |
| User Name | el `DB_USER` de su `.env` (`ecommerce_app`) |
| Password | el `DB_PASSWORD` de su `.env` |

Pulse **Connect**. A la izquierda verá las 13 tablas (`LOTES`, `PRODUCTOS`, `COMPRAS`...).
Pruebe por ejemplo:
```sql
SELECT * FROM productos;
```

> La consola solo funciona **mientras la aplicación está corriendo**: la base vive dentro de ella.
> Si aparece *Wrong user name or password*, revise que usuario y clave sean exactamente los de su
> `.env` y que haya reiniciado la aplicación después de cambiarlo.

## 6. Comprobar la API

| Qué | Dónde |
|---|---|
| Swagger (probar la API) | http://localhost:8080/swagger-ui.html |
| Token para rutas protegidas | En Swagger: `POST /api/auth/login` con `admin` / su `ADMIN_CLAVE`, copie el `token` y péguelo en **Authorize** |
| Productos de ejemplo | http://localhost:8080/api/productos |

## 7. Problemas frecuentes

| Mensaje | Causa y solución |
|---|---|
| `Wrong user name or password` en la consola | Use el `DB_USER`/`DB_PASSWORD` de su `.env` (revise que no se llame `.env.txt`). Si cambió el `.env`, reinicie la aplicación. |
| `Database "mem:ecommercedb" not found` | La aplicación no está corriendo, o escribió mal la JDBC URL. |
| `Port 8080 was already in use` | Ya hay otra copia corriendo. Deténgala (`Ctrl+C` o el botón rojo de IntelliJ). |
| `.\gradlew.bat` no se reconoce | Está en la carpeta equivocada: debe estar dentro de `ecommerce`. |

---

## Más adelante: MariaDB

Cuando el equipo pase a MariaDB, los datos dejarán de borrarse al apagar. El usuario sigue siendo el
mismo `ecommerce_app` del `.env`; solo hay que crear la base y elegir el puerto.

Las tablas las crea **Flyway** con los scripts de `ecommerce/src/main/resources/db/` (no Hibernate).
La primera vez verá en la consola `Successfully applied 2 migrations`; las siguientes,
`Schema ... is up to date`. Necesita **MariaDB 10.7 o superior** (se recomienda 11).

> Si ya tenía una base `ecommercedb` creada antes por Hibernate, bórrela y créela de nuevo: Flyway no
> trabaja sobre tablas que no creó él (`Found non-empty schema(s)`).

### Opción A: Docker Desktop

```powershell
winget install --id Docker.DockerDesktop -e
```
Reinicie, abra Docker Desktop y espere *Engine running*. En `.env` ponga `DB_ROOT_PASSWORD` y deje
`DB_PORT=3307`. Desde `ecommerce`:
```powershell
docker compose up -d
```
La primera vez el contenedor crea la base y el usuario `ecommerce_app` con su `DB_PASSWORD`.
Si antes creó un contenedor `mariadb-ecommerce` a mano, bórrelo primero con
`docker rm -f mariadb-ecommerce`.

### Opción B: MariaDB instalado

Instalador de MariaDB (`winget install --id MariaDB.Server -e`; anote la clave de root que le pide).
Desde `ecommerce`, con `su_clave` igual a su `DB_PASSWORD`:
```powershell
& "C:\Program Files\MariaDB 11.8\bin\mariadb.exe" -u root -p -e "SET @clave='su_clave'; SOURCE db/crear-usuario.sql;"
```
Ajuste la carpeta a su versión (`dir "C:\Program Files\"`). En `.env` ponga `DB_PORT=3306`.

> **XAMPP no sirve:** trae MariaDB 10.4, que no tiene el tipo `uuid` que usan las tablas.
> Use el instalador oficial de MariaDB o Docker.

### Ejecutar con MariaDB
```powershell
.\gradlew.bat bootRun --args="--spring.profiles.active=mariadb"
```
En IntelliJ: **Active profiles** = `mariadb`.

| Mensaje | Causa y solución |
|---|---|
| `Access denied for user 'ecommerce_app'` | La contraseña de `.env` no coincide con la de la base. |
| `Communications link failure` | MariaDB no está encendido o el `DB_PORT` no es el correcto (3307 Docker, 3306 instalado). |
| `error during connect` en Docker | Docker Desktop no está abierto. |
| `Found non-empty schema(s) ... without schema history table` | La base tenía tablas de antes de Flyway. Bórrela y créela de nuevo (con Docker: `docker compose down -v` y `docker compose up -d`). |
| `Migration checksum mismatch` | Alguien editó un script de `db/migration` ya aplicado. Los scripts aplicados no se editan: los cambios van en un `V2__...sql` nuevo. |
| `Schema-validation: missing column` | Se cambió una entidad JPA sin escribir su migración. |

---

## Frontend (Angular)

El frontend está en la carpeta `frontend` (Angular 21). Necesita **Node 22.12 o superior con npm 11**:
```powershell
winget install --id OpenJS.NodeJS.LTS -e
```
Cierre y abra PowerShell. Con el backend corriendo en el puerto 8080, desde `frontend`:
```powershell
npm install
npm start
```
Abra http://localhost:4200. Las llamadas a `/api` pasan al backend por el proxy de Angular
(`proxy.conf.json`). Pruebas: `npm test -- --watch=false`.

> Si `npm install` falla con `Cannot read properties of null (reading 'edgesOut')`, es un error de
> npm 10: actualice a npm 11 (`npm install -g npm@11`).
