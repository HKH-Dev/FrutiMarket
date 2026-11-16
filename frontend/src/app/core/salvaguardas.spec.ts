import { HttpClient, provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { ActivatedRouteSnapshot, Router, RouterStateSnapshot, UrlTree, provideRouter } from '@angular/router';
import { authInterceptor } from './auth.interceptor';
import { AuthService } from './auth.service';
import { AvisosService } from './avisos.service';
import { mensajeDeError } from './errores';
import { invitadoGuard, sesionGuard } from './guards';
import { HttpErrorResponse } from '@angular/common/http';

const SESION = { usuario: 'c@x.com', token: 'abc', roles: ['COMPRADOR'], expiraEn: '2999-01-01T00:00:00Z' };

describe('Salvaguardas', () => {
  let http: HttpClient;
  let backend: HttpTestingController;

  beforeEach(() => {
    localStorage.clear();
    TestBed.configureTestingModule({
      providers: [provideRouter([]), provideHttpClient(withInterceptors([authInterceptor])), provideHttpClientTesting()],
    });
  });

  function iniciar(conSesion: boolean): void {
    if (conSesion) {
      localStorage.setItem('frutimarket.sesion', JSON.stringify(SESION));
    }
    http = TestBed.inject(HttpClient);
    backend = TestBed.inject(HttpTestingController);
  }

  it('el interceptor agrega el token a las peticiones de la API', () => {
    iniciar(true);
    http.get('/api/compras/mias').subscribe();
    expect(backend.expectOne('/api/compras/mias').request.headers.get('Authorization')).toBe('Bearer abc');
  });

  it('con un 401 el interceptor cierra la sesion, avisa y manda al login', () => {
    iniciar(true);
    const navegar = vi.spyOn(TestBed.inject(Router), 'navigate').mockResolvedValue(true);
    http.get('/api/compras/mias').subscribe({ error: () => undefined });
    backend.expectOne('/api/compras/mias').flush(null, { status: 401, statusText: 'Unauthorized' });

    expect(TestBed.inject(AuthService).autenticado()).toBe(false);
    expect(TestBed.inject(AvisosService).avisos()[0].tipo).toBe('error');
    expect(navegar).toHaveBeenCalledWith(['/login'], expect.anything());
  });

  it('sesionGuard manda al login sin sesion y deja pasar con sesion', () => {
    iniciar(false);
    const estado = { url: '/compras' } as RouterStateSnapshot;
    const sinSesion = TestBed.runInInjectionContext(() => sesionGuard({} as ActivatedRouteSnapshot, estado));
    expect((sinSesion as UrlTree).toString()).toBe('/login?volver=%2Fcompras');
  });

  it('invitadoGuard saca de /login a quien ya tiene sesion', () => {
    iniciar(true);
    const resultado = TestBed.runInInjectionContext(() => invitadoGuard({} as ActivatedRouteSnapshot, {} as RouterStateSnapshot));
    expect((resultado as UrlTree).toString()).toBe('/');
  });

  it('traduce los codigos HTTP a mensajes para personas', () => {
    expect(mensajeDeError(new HttpErrorResponse({ status: 400 }))).toContain('Revisa los datos');
    expect(mensajeDeError(new HttpErrorResponse({ status: 404 }))).toContain('No existe');
    expect(mensajeDeError(new HttpErrorResponse({ status: 401 }))).toContain('Inicia sesión');
  });
});
