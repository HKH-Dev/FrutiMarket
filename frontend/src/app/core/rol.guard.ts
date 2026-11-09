import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from './auth.service';
import { Rol } from './modelos';

/**
 * Solo deja entrar a los roles indicados en data.roles; si no, lleva al login o a la pantalla del perfil.
 * Es comodidad para el usuario: la seguridad real la aplica el backend (401/403).
 */
export const rolGuard: CanActivateFn = (ruta, estado) => {
  const auth = inject(AuthService);
  const router = inject(Router);
  const rol = auth.rol();
  if (rol && (ruta.data['roles'] as Rol[]).includes(rol)) {
    return true;
  }
  return rol ? router.parseUrl(auth.inicioDelRol()) : router.createUrlTree(['/login'], { queryParams: { volver: estado.url } });
};
