import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from './auth.service';
import { Rol } from './modelos';

/*
 * Los guards son comodidad para el usuario: evitan que entre a una pantalla que no le va a funcionar.
 * La seguridad real la pone el backend, que rechaza con 401/403 cualquier peticion sin el token o el rol correcto,
 * porque el codigo de Angular corre en el navegador y cualquiera puede modificarlo.
 */

/** Rutas privadas: sin sesion, al login (recordando a donde queria ir). */
export const sesionGuard: CanActivateFn = (_ruta, estado) => {
  const auth = inject(AuthService);
  return auth.autenticado() ? true : inject(Router).createUrlTree(['/login'], { queryParams: { volver: estado.url } });
};

/** Login y registro: quien ya tiene sesion no tiene nada que hacer ahi; va a la pantalla de su perfil. */
export const invitadoGuard: CanActivateFn = () => {
  const auth = inject(AuthService);
  return auth.autenticado() ? inject(Router).parseUrl(auth.inicioDelRol()) : true;
};

/** Rutas de un perfil (data.roles). Se usa despues de sesionGuard: con sesion pero otro rol, a su propia pantalla. */
export const rolGuard: CanActivateFn = (ruta) => {
  const auth = inject(AuthService);
  const rol = auth.rol();
  return rol && (ruta.data['roles'] as Rol[]).includes(rol) ? true : inject(Router).parseUrl(auth.inicioDelRol());
};
