/**
 * Configuracion de produccion (ng build). La API se sirve en el mismo origen que la app:
 * nginx reenvia /api y /ws al backend, asi que no hace falta escribir ningun dominio.
 */
export const environment = {
  produccion: true,
  apiUrl: '/api',
  wsRuta: '/ws',
};
