package com.uniquindio.ecommerce.infraestructure.seguridad;

/** Roles de la plataforma. En el token van sin prefijo; Spring Security les agrega "ROLE_". */
public enum Rol {
    ADMIN,
    CAMPESINO,
    COMPRADOR
}
