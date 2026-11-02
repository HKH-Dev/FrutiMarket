import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { AuthService } from './auth.service';

/** Agrega "Authorization: Bearer <token>" a las peticiones a la API cuando hay sesion. */
export const authInterceptor: HttpInterceptorFn = (peticion, siguiente) => {
  const token = inject(AuthService).token();
  if (!token || !peticion.url.startsWith('/api/')) {
    return siguiente(peticion);
  }
  return siguiente(peticion.clone({ setHeaders: { Authorization: `Bearer ${token}` } }));
};
