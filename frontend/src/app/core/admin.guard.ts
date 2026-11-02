import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from './auth.service';

/**
 * Solo deja entrar a la ruta a un ADMIN; si no, lo manda al login.
 * Es comodidad para el usuario: la seguridad real la aplica el backend (401/403).
 */
export const adminGuard: CanActivateFn = (_ruta, estado) => {
  const auth = inject(AuthService);
  return auth.esAdmin()
    ? true
    : inject(Router).createUrlTree(['/login'], { queryParams: { volver: estado.url } });
};
