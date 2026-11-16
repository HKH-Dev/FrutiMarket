import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';
import { environment } from '../../environments/environment';
import { AuthService } from './auth.service';
import { AvisosService } from './avisos.service';

/**
 * Unico lugar donde se escribe "Authorization: Bearer <token>": ningun servicio ni pantalla lo hace a mano.
 * Si el backend responde 401 a una peticion que llevaba token, el token ya no sirve (vencido o alterado):
 * se cierra la sesion, se avisa y se manda al login.
 */
export const authInterceptor: HttpInterceptorFn = (peticion, siguiente) => {
  const auth = inject(AuthService);
  const avisos = inject(AvisosService);
  const router = inject(Router);
  const token = auth.tokenGuardado();
  const esApi = peticion.url.startsWith(`${environment.apiUrl}/`);
  const conToken = esApi && token ? peticion.clone({ setHeaders: { Authorization: `Bearer ${token}` } }) : peticion;

  return siguiente(conToken).pipe(
    catchError((error: unknown) => {
      if (error instanceof HttpErrorResponse && error.status === 401 && conToken !== peticion) {
        auth.cerrarSesion();
        avisos.error('Tu sesión venció. Inicia sesión de nuevo.');
        router.navigate(['/login'], { queryParams: { volver: router.url } });
      }
      return throwError(() => error);
    }),
  );
};
