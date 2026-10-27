-- Crea la base de datos y el usuario de la aplicacion en un MariaDB instalado directamente
-- (sin Docker). Ejecutar UNA vez como administrador (root), pasando la contrasena en @clave:
--
--   sudo mariadb -e "SET @clave='su_clave'; SOURCE db/crear-usuario.sql;"
--
-- En Ubuntu/Debian el usuario root de MariaDB entra por el socket del sistema (unix_socket),
-- no con contrasena: "mariadb -u root -p" da "Access denied". Por eso se usa sudo.
-- Ejecutar desde la carpeta ecommerce/ (la ruta de SOURCE es relativa a donde se ejecuta).
-- Si la consulta final a mysql.user falla con "SELECT command denied", el usuario que la
-- ejecuta no es root/administrador.
--
-- La contrasena NO se escribe en este archivo para que nunca termine en git.
-- Se puede volver a ejecutar: si el usuario ya existe, solo actualiza su contrasena.

CREATE DATABASE IF NOT EXISTS ecommercedb CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 'localhost' y '127.0.0.1' cubren las dos formas en que Java se conecta en el mismo equipo.
-- No se crea con '%' para que el usuario no pueda entrar desde otros equipos de la red.

SET @sql = CONCAT('CREATE USER IF NOT EXISTS ''ecommerce_app''@''localhost'' IDENTIFIED BY ', QUOTE(@clave));
PREPARE sentencia FROM @sql; EXECUTE sentencia; DEALLOCATE PREPARE sentencia;
SET @sql = CONCAT('ALTER USER ''ecommerce_app''@''localhost'' IDENTIFIED BY ', QUOTE(@clave));
PREPARE sentencia FROM @sql; EXECUTE sentencia; DEALLOCATE PREPARE sentencia;

SET @sql = CONCAT('CREATE USER IF NOT EXISTS ''ecommerce_app''@''127.0.0.1'' IDENTIFIED BY ', QUOTE(@clave));
PREPARE sentencia FROM @sql; EXECUTE sentencia; DEALLOCATE PREPARE sentencia;
SET @sql = CONCAT('ALTER USER ''ecommerce_app''@''127.0.0.1'' IDENTIFIED BY ', QUOTE(@clave));
PREPARE sentencia FROM @sql; EXECUTE sentencia; DEALLOCATE PREPARE sentencia;

-- Todos los permisos, pero SOLO sobre ecommercedb: Hibernate necesita crear y alterar tablas.
GRANT ALL PRIVILEGES ON ecommercedb.* TO 'ecommerce_app'@'localhost';
GRANT ALL PRIVILEGES ON ecommercedb.* TO 'ecommerce_app'@'127.0.0.1';
FLUSH PRIVILEGES;

SELECT CONCAT(User, '@', Host) AS usuario_creado FROM mysql.user WHERE User = 'ecommerce_app';
