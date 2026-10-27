# FrutiMarket
Proyeto semestre 2026-02 asignatura programacion avanzada

Backend Spring Boot 4 (Java 21) en la carpeta `ecommerce/`, con arquitectura hexagonal,
persistencia JPA (H2 o MariaDB) y API REST documentada con Swagger.

## Instalación

- **Linux (Ubuntu / Kali):** [docs/GUIA-LINUX.md](docs/GUIA-LINUX.md)
- **Windows:** [docs/GUIA-WINDOWS.md](docs/GUIA-WINDOWS.md)

## Inicio rápido

Desde `ecommerce/`:

| | Linux | Windows (PowerShell) |
|---|---|---|
| H2 (lo que usamos ahora) | `./gradlew bootRun` | `.\gradlew.bat bootRun` |
| MariaDB | `./gradlew bootRun --args='--spring.profiles.active=mariadb'` | `.\gradlew.bat bootRun --args="--spring.profiles.active=mariadb"` |
| Pruebas | `./gradlew test` | `.\gradlew.bat test` |

- Swagger: http://localhost:8080/swagger-ui.html
- Consola H2: http://localhost:8080/h2-console (JDBC URL `jdbc:h2:mem:ecommercedb`, usuario y
  contraseña = `DB_USER` y `DB_PASSWORD` de su `ecommerce/.env`)

Ahora usamos **H2**; MariaDB queda para más adelante. En ambos casos el usuario es `ecommerce_app`
(nunca root ni `sa`) y cada integrante pone su contraseña en `ecommerce/.env`, que no se sube a git.
Las guías explican cómo crearlo.
