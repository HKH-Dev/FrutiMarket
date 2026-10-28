# FrutiMarket
Proyeto semestre 2026-02 asignatura programacion avanzada

Backend Spring Boot 4 (Java 21) en la carpeta `ecommerce/`, con arquitectura hexagonal,
persistencia JPA (H2 o MariaDB) y API REST documentada con Swagger.

- Swagger: http://localhost:8080/swagger-ui.html
- Consola H2: http://localhost:8080/h2-console (JDBC URL `jdbc:h2:mem:ecommercedb'

Ahora usamos **H2**; En ambos casos el usuario es `ecommerce_app`

## Seguridad (JWT)

La ruta `GET /api/reportes/productos-mas-vendidos` solo la puede usar un **ADMIN**; el resto de la API
sigue abierta mientras no exista el registro de usuarios. Para obtener un token:
`POST /api/auth/login` con uno de los usuarios de demostración (las claves se cambian en `.env`):

| Usuario | Clave por defecto | Rol |
|---|---|---|
| `admin` | `admin123` | ADMIN |
| `campesino` | `campesino123` | CAMPESINO |
| `comprador` | `comprador123` | COMPRADOR |

En Swagger: ejecute el login, copie el `token`, pulse **Authorize** y péguelo.

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
