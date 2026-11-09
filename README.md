# FrutiMarket

**Nicho:** comercio directo de productos del campo del Quindío. Los campesinos publican **lotes**
de lo que cosechan (con su calidad, precio de finca y trazabilidad desde la finca) y los compradores
(hogares, tiendas, restaurantes y mayoristas) los compran sin intermediarios. La plataforma
controla la disponibilidad, la vida útil, la cadena de frío y la logística hasta la entrega.

Proyecto del semestre 2026-02, asignatura Programación Avanzada — Universidad del Quindío.

## Integrantes

| Nombre | Usuario de GitHub |
|---|---|
| _(completar)_ | `hkh-dev` / `HKH-Dev` |
| _(completar)_ | `mariajuanita00` |
| _(completar)_ | `Marlonceron6` |

## Tecnologías

Backend Spring Boot 4 (Java 21) en `ecommerce/`, con arquitectura hexagonal (dominio sin Spring ni
JPA), persistencia JPA con H2 (desarrollo) o MariaDB con Flyway, seguridad JWT con roles y API REST
documentada con Swagger. El frontend Angular de `frontend/` es de la Entrega 3.

## Cómo ejecutarlo

Requisitos: **Java 21**. No hay que instalar base de datos: por defecto usa **H2** en memoria con
datos de ejemplo.

| | Linux | Windows (PowerShell) |
|---|---|---|
| Backend con H2 | `cd ecommerce` y `./gradlew bootRun` | `cd ecommerce` y `.\gradlew.bat bootRun` |
| Backend con MariaDB | `./gradlew bootRun --args='--spring.profiles.active=mariadb'` | `.\gradlew.bat bootRun --args="--spring.profiles.active=mariadb"` |
| Pruebas backend | `./gradlew test` | `.\gradlew.bat test` |
| Frontend (Entrega 3) | `cd frontend`, `npm install` y `npm start` | lo mismo |

- **Swagger:** http://localhost:8080/swagger-ui.html

- Instalación paso a paso (Node, MariaDB, `.env`): [docs/GUIA-LINUX.md](docs/GUIA-LINUX.md) ·
  [docs/GUIA-WINDOWS.md](docs/GUIA-WINDOWS.md)

## Usuarios de prueba

Se crean al arrancar (claves cifradas con BCrypt). Las claves se pueden cambiar en `.env`.

| Correo | Contraseña | Rol | Perfil vinculado |
|---|---|---|---|
| `admin@frutimarket.com` | `admin123` | ADMIN | — |
| `campesino@frutimarket.com` | `campesino123` | CAMPESINO (vendedor) | María Gómez, autorizada para publicar |
| `comprador@frutimarket.com` | `comprador123` | COMPRADOR | Restaurante El Solar (Armenia) |

Cualquier persona puede registrarse como COMPRADOR o CAMPESINO en `POST /api/auth/registro`. Un
campesino nuevo no puede ofrecer lotes hasta que un ADMIN lo autorice (regla 1).

## Seguridad (JWT)

- `POST /api/auth/login` devuelve un token JWT con el **correo**, el **rol** y la **fecha de
  vencimiento**. La clave secreta y la duración están en `application.properties` (`JWT_SECRET` en
  `.env`), no en el código.
- **Quién actúa lo dice el token**, no el cuerpo de la petición: cada usuario está vinculado a su
  campesino o comprador (claim `actorId`), así nadie puede actuar en nombre de otro.
- **Sin token → 401; token con otro rol → 403.**
- En Swagger: ejecute el login, copie el `token`, pulse **Authorize** y péguelo.

## Endpoints principales

| Método y ruta | Rol | Caso de uso |
|---|---|---|
| `POST /api/auth/registro` | público | Registrar usuario (crea su perfil de campesino o comprador) |
| `POST /api/auth/login` | público | Iniciar sesión (token JWT) |
| `GET /api/productos` | público | Consultar catálogo de productos |
| `GET /api/lotes/buscar?texto=&tipoCultivo=&precioMaximo=&pagina=&tamano=` | público | **Listado paginado** con búsqueda y filtros |
| `GET /api/lotes?productoId=` · `GET /api/lotes/{id}/trazabilidad` | público | Consultar lotes y su trazabilidad |
| `POST /api/lotes` | CAMPESINO | Registrar lote |
| `POST /api/lotes/{id}/publicar` · `/desactivar` · `/reactivar` | CAMPESINO dueño | Publicar / gestionar lote |
| `DELETE /api/lotes/{id}` | CAMPESINO dueño | Retirar lote (**borrado lógico**: pasa a RETIRADO) |
| `GET /api/lotes/mios` | CAMPESINO | Mis lotes |
| `POST /api/compras` | COMPRADOR | Realizar compra |
| `GET /api/compras/mias` | COMPRADOR | Mis compras |
| `POST /api/compras/{id}/reembolso` | COMPRADOR dueño | Solicitar reembolso (reglas 19 y 20) |
| `POST /api/campesinos/{id}/autorizacion` | ADMIN | Autorizar campesino (regla 1) |
| `GET /api/reportes/productos-mas-vendidos` | ADMIN | **Ranking** con JOIN, GROUP BY y SUM |
| `/api/lotes/{id}/ingresos`, `/despachos/**`, `/entrega`, `/inspecciones`, `/temperaturas`, `/incidencias` | ADMIN | Logística y calidad |

Errores en JSON (`application/problem+json`): **400** datos inválidos (con `errores` por campo),
**401/403** seguridad, **404** no existe, **409** conflicto de datos, **422** regla de negocio rota
(con el código de la regla en `regla`, por ejemplo `"R3"`).

## Flujo de punta a punta en Swagger

1. **Login como vendedor** (`campesino@frutimarket.com`) → Authorize.
2. `POST /api/lotes` registra un lote de mango y `POST /api/lotes/{id}/publicar` lo publica.
3. **Login como comprador** (`comprador@frutimarket.com`) → Authorize.
4. `GET /api/lotes/buscar?texto=mango` lo encuentra; `POST /api/compras` lo compra.
5. `GET /api/compras/mias` muestra la compra; `POST /api/compras/{id}/reembolso` la reembolsa.
6. Repetir el reembolso → **422** `INV-COMPRA-REEMBOLSO`. Comprar más de lo disponible → **422** `R3`.
7. **Login como ADMIN** → `GET /api/reportes/productos-mas-vendidos`.

Este mismo flujo está automatizado en `ApiRestTest.flujoCompletoDesdeElRegistroHastaElReembolso`.

## Correcciones de la Entrega 1

| Observación que nos dieron | Qué cambiamos | Dónde se ve (archivo o rama) |
|---|---|---|
| _(completar con la observación del profesor)_ | Las reglas R1, R9, R13, R15, R16, INV-COMPRADOR e INV-CODIGO estaban dentro de los casos de uso; ahora las exige el dominio | `Campesino.exigirAutorizacionParaPublicar`, `PuntoAlmacenamiento.exigirCobertura`, `SeleccionLotes.exigirPrioridadDeDespacho`, `PoliticaCodigoLote`; rama `feature/reglas-en-dominio` |
| _(completar)_ | El reembolso no tenía plazo ni dueño; ahora el agregado `Compra` exige R19 (solo su comprador) y R20 (3 días) | `Compra.reembolsar`; rama `feature/reembolso-compras` |
| _(completar)_ | | |

## Cómo cumple los mínimos de la Entrega 2

| Mínimo | Dónde |
|---|---|
| Dominio: ≥3 entidades, ≥5 Value Objects, 2 agregados, excepción de dominio | Entidades `Campesino`, `Comprador`, `Producto`, `PuntoAlmacenamiento`; agregados `Lote` y `Compra`; Value Objects `Cantidad`, `PrecioFinca`, `Merma`, `Calidad`, `FichaTrazabilidad`, `TemporadaCosecha` e ids tipados; `ReglaDeNegocioVioladaException`. `domain/` no importa Spring ni JPA |
| ≥6 casos de uso (2 comprador, 2 vendedor) | Vendedor: `RegistrarLote`, `PublicarLote`, `GestionarDisponibilidad` (merma). Comprador: `RealizarCompra`, `SolicitarReembolso`, `ConsultarCompras`. Además `ConsultarLote`, `GestionarLogistica`, `ControlarCalidad`, `AutorizarCampesino`, `ConsultarCatalogo`, `ConsultarReportes` |
| API REST | Controllers delgados en `infraestructure/web`, DTO con `@Valid`, mappers DTO ↔ dominio (`aComando`, `desde`), `ManejadorErrores` |
| Persistencia | Entidades JPA separadas en `persistence/jpa/entity` con mappers en `persistence/jpa/mapper`; datos de prueba en `data.sql` (H2) y Flyway (MariaDB); borrado lógico (`RETIRADO`, `activo`) |
| Consultas | Listado paginado JPQL con parámetros con nombre: `LoteJpaRepository.buscarDisponibles`; ranking JOIN + GROUP BY + SUM: `CompraJpaRepository.rankingProductosVendidos` |
| Seguridad | Registro y login, BCrypt, clave y duración en `application.properties`, 3 roles, rutas por rol en `SeguridadConfig` |
| Pruebas (106, todas en verde) | Dominio: `LoteTest`, `CompraTest`, `CantidadTest`... · Casos de uso con repositorios en memoria: `RealizarCompraServiceTest`, `SolicitarReembolsoServiceTest`... · Controller `@WebMvcTest`: `LoteControllerPublicarTest` · Repositorio: `LoteRepositoryDataJpaTest`, `BusquedaLotesDataJpaTest` |

## Pruebas avanzadas

Todas corren con `./gradlew test` (Windows: `.\gradlew.bat test`) y usan el patrón AAA
(Preparar, Ejecutar, Verificar).

| Punto | Prueba | Qué comprueba |
|---|---|---|
| 0.1 Guardar y recuperar | `LoteRepositoryDataJpaTest` | Un lote con merma, reserva y calidad verificada vuelve de la base con todos sus Value Objects iguales |
| 0.2 Ranking (JOIN + GROUP BY) | `ReporteVentasRepositoryDataJpaTest` | Con 3 compras conocidas (una cancelada) el ranking es Mango 50 kg, Café 40 kg, Mora 5 kg, y respeta el límite |
| 1. Endpoint con seguridad | `ReporteSeguridadTest` | Sin token 401, token de COMPRADOR 403, token de ADMIN 200 (más token alterado y login) |
| 2. `@DataJpaTest` | las dos pruebas del punto 0 | Solo la capa JPA, con base de pruebas y rollback |
| 3. Integración | `CompraIntegracionTest` | Servidor real por HTTP: registrar, publicar y comprar, y revisar la base; una compra inválida no deja nada a medias |
| 4. Más casos en el controller | `LoteControllerPublicarTest` | Publicar un lote inexistente da 404 y una regla rota (R1, R5) da 422; en ambos `verify(loteRepository, never()).almacenar(any())` |

### 1. Probar un endpoint con seguridad

Protegimos el ranking de ventas para que solo lo vea el administrador. La seguridad está en
`SeguridadConfig`: Spring Security trabaja como servidor de recursos OAuth2, valida la firma del JWT
con una clave HS256 que viene del `.env` y lee los roles del claim `roles`. La prueba usa tokens
reales firmados con esa misma clave, así que recorre todo el filtro de seguridad y no un atajo.
Sin la cabecera `Authorization` el filtro corta antes de llegar al controller y responde 401
("no sé quién eres"). Con un token válido de COMPRADOR la identidad es correcta pero el rol no
alcanza, por eso es 403 ("sé quién eres, pero no puedes"). Con ADMIN responde 200 y el ranking.

### 2. `@DataJpaTest` contra `@SpringBootTest`

`@SpringBootTest` levanta la aplicación completa: controllers, servicios, seguridad y base de datos.
Es más lento, pero prueba cómo trabajan todas las piezas juntas. `@DataJpaTest` solo carga lo de JPA:
entidades, repositorios de Spring Data y una base H2 de pruebas. Cada prueba corre en una transacción
que se deshace al terminar, así que las pruebas no se ensucian entre sí. Como nuestros adaptadores
(`LoteRepositoryJpa`...) son clases propias y no interfaces de Spring Data, hay que agregarlos con
`@Import`. En Spring Boot 4 la anotación cambió de paquete a
`org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest` (y `TestEntityManager` a
`org.springframework.boot.jpa.test.autoconfigure`), y viene en el starter aparte
`spring-boot-starter-data-jpa-test`, que agregamos en `build.gradle` como `testImplementation`.

### 3. Una prueba de integración

La prueba arranca el servidor de verdad en un puerto libre (`RANDOM_PORT`) y le hace peticiones HTTP
como lo haría el frontend; no hay ningún mock. Cada petición pasa por seguridad, controller,
validación, caso de uso, dominio, adaptador JPA y la base H2, y al final revisamos la base con los
repositorios para confirmar que lo que respondió la API quedó guardado. Una prueba unitaria aísla una
sola clase y reemplaza lo demás con dobles: es rápida y dice exactamente dónde está el error, pero no
detecta fallas de conexión entre capas. La de integración sí: por ejemplo, comprueba que si el segundo
ítem de una compra falla, la transacción deshace también el primero. Conviene para los flujos
importantes de punta a punta, y dejar las reglas en detalle a las pruebas unitarias, que son más baratas.

## Entrega 3: producto final

Ejecutar todo con Docker: ver [docs/DESPLIEGUE.md](docs/DESPLIEGUE.md). La tienda queda en `http://localhost:8088`.

Cada perfil tiene su propia experiencia:

| Perfil | Entra a | Puede |
|---|---|---|
| Visitante y comprador | Tienda (`/`) | Catálogo, trazabilidad, carrito, pago simulado, compras y reembolsos, favoritos, cupones, recomendaciones, chat |
| Campesino | Mi finca (`/finca`) | Registrar, publicar, pausar y retirar lotes; ver ventas; responder mensajes |
| Administrador | Consola (`/consola`) | Ventas y ranking, autorizar campesinos, campañas de temporada, cupones personalizados |

Funcionalidades adicionales:

| Funcionalidad | Endpoints |
|---|---|
| Descuentos por temporada | `GET /api/ofertas/vigentes`, `GET/POST /api/ofertas`, `POST /api/ofertas/{id}/desactivar` |
| Recomendaciones | `GET /api/recomendaciones` |
| Chat en vivo | STOMP en `/ws`; `GET/POST /api/chat/conversaciones`, `GET/POST /api/chat/conversaciones/{id}/mensajes` |
| Pagos simulados | `POST /api/pagos/cotizacion`, `POST /api/pagos/checkout` (tarjeta terminada en 0000: rechazo) |
| Favoritos | `GET /api/favoritos`, `PUT/DELETE /api/favoritos/{productoId}` |
| Cupones personalizados | `GET /api/cupones/mios`, `GET/POST /api/cupones` (cupón de prueba del comprador demo: `BIENVENIDA10`) |
