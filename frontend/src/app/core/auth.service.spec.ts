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
    servicio.iniciarSesion('admin@frutimarket.com', 'admin123').subscribe();
    const peticion = http.expectOne('/api/auth/login');
    peticion.flush({ token: 'abc', tipo: 'Bearer', correo: 'admin@frutimarket.com', rol: 'ADMIN', expiraEn: '2999-01-01T00:00:00Z' });

    // Verificar
    expect(peticion.request.body).toEqual({ correo: 'admin@frutimarket.com', clave: 'admin123' });
    expect(servicio.token()).toBe('abc');
    expect(servicio.esAdmin()).toBe(true);
    expect(servicio.usuario()).toBe('admin@frutimarket.com');
  });

  it('un token vencido se descarta', () => {
    servicio.iniciarSesion('comprador', 'x').subscribe();
    http.expectOne('/api/auth/login')
      .flush({ token: 'viejo', tipo: 'Bearer', correo: 'c@x.com', rol: 'COMPRADOR', expiraEn: '2000-01-01T00:00:00Z' });

    expect(servicio.token()).toBeNull();
    expect(servicio.autenticado()).toBe(false);
  });

  it('cerrar sesion borra el token', () => {
    servicio.iniciarSesion('admin', 'x').subscribe();
    http.expectOne('/api/auth/login')
      .flush({ token: 'abc', tipo: 'Bearer', correo: 'a@x.com', rol: 'ADMIN', expiraEn: '2999-01-01T00:00:00Z' });

    servicio.cerrarSesion();

    expect(servicio.token()).toBeNull();
    expect(servicio.esAdmin()).toBe(false);
  });
});
