# Despliegue con Docker

La aplicación completa son tres contenedores definidos en `docker-compose.yml`:

| Servicio | Imagen | Función | Puerto público |
|---|---|---|---|
| `frontend` | nginx sin root con Angular compilado | Sirve la app y hace de proxy de `/api`, `/ws` y Swagger | Sí (`PUERTO_WEB`) |
| `backend` | JRE 21 sin root con el jar de Spring Boot | API REST y WebSocket, perfil `mariadb` | No |
| `db` | MariaDB 11.8 con volumen persistente | Datos; Flyway crea el esquema | No |

## Ejecutar

```bash
cp .env.example .env
```

Cambie todos los valores de `.env` y luego:

```bash
docker compose up --build -d
```

La aplicación queda en `http://localhost:8088` y Swagger en `http://localhost:8088/swagger-ui.html`.

Si Docker muestra `docker-credential-desktop: executable file not found`, borre la línea `credsStore` de `~/.docker/config.json`.

Comandos útiles:

```bash
docker compose logs -f backend
```

```bash
docker compose down
```

`docker compose down -v` además **borra la base de datos**.

## Publicar en internet (opción recomendada)

Un servidor virtual pequeño (2 GB de RAM) con Docker, esta misma pila y un proxy con HTTPS delante:

1. Apunte un dominio al servidor.
2. En `.env` ponga `ORIGEN_PUBLICO=https://su-dominio` y `PUERTO_WEB=8088`. El chat rechaza cualquier otro origen.
3. Instale Caddy y configure `/etc/caddy/Caddyfile`:

```
su-dominio {
    reverse_proxy localhost:8088
}
```

Caddy obtiene y renueva el certificado solo, y reenvía WebSockets sin configuración adicional. El navegador usará `wss://` automáticamente.

Antes de producción real:

- Quite `classpath:db/datos-ejemplo` de `spring.flyway.locations` para no cargar datos de demostración.
- Cambie `SPRINGDOC_API_DOCS_ENABLED=false` y `SPRINGDOC_SWAGGER_UI_ENABLED=false` en el servicio `backend`.
- Programe copias de seguridad del volumen `datos-db` con `mariadb-dump`.
