import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { AuthService } from './auth.service';

describe('AuthService', () => {
  let servicio: AuthService;
  let http: HttpTestingController;

  beforeEach(() => {
    localStorage.clear();
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    servicio = TestBed.inject(AuthService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('al iniciar sesion guarda el token y reconoce el rol ADMIN', () => {
    // Ejecutar
    servicio.iniciarSesion('admin', 'admin123').subscribe();
    const peticion = http.expectOne('/api/auth/login');
    peticion.flush({ token: 'abc', tipo: 'Bearer', expiraEn: '2999-01-01T00:00:00Z', roles: ['ADMIN'] });

    // Verificar
    expect(peticion.request.body).toEqual({ usuario: 'admin', clave: 'admin123' });
    expect(servicio.token()).toBe('abc');
    expect(servicio.esAdmin()).toBe(true);
    expect(servicio.usuario()).toBe('admin');
  });

  it('un token vencido se descarta', () => {
    servicio.iniciarSesion('comprador', 'x').subscribe();
    http.expectOne('/api/auth/login')
      .flush({ token: 'viejo', tipo: 'Bearer', expiraEn: '2000-01-01T00:00:00Z', roles: ['COMPRADOR'] });

    expect(servicio.token()).toBeNull();
    expect(servicio.autenticado()).toBe(false);
  });

  it('cerrar sesion borra el token', () => {
    servicio.iniciarSesion('admin', 'x').subscribe();
    http.expectOne('/api/auth/login')
      .flush({ token: 'abc', tipo: 'Bearer', expiraEn: '2999-01-01T00:00:00Z', roles: ['ADMIN'] });

    servicio.cerrarSesion();

    expect(servicio.token()).toBeNull();
    expect(servicio.esAdmin()).toBe(false);
  });
});
