import { provideHttpClient } from '@angular/common/http';
import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { App } from './app';

describe('App', () => {
  beforeEach(async () => {
    localStorage.clear();
    await TestBed.configureTestingModule({
      imports: [App],
      providers: [provideRouter([]), provideHttpClient()],
    }).compileComponents();
  });

  it('muestra la marca y el enlace al catalogo', async () => {
    // Preparar y ejecutar
    const fixture = TestBed.createComponent(App);
    await fixture.whenStable();
    const html = fixture.nativeElement as HTMLElement;

    // Verificar
    expect(html.querySelector('.marca')?.textContent).toContain('FrutiMarket');
    expect(html.querySelector('nav')?.textContent).toContain('Catálogo');
  });

  it('sin sesion no muestra el enlace de reportes y ofrece iniciar sesion', async () => {
    const fixture = TestBed.createComponent(App);
    await fixture.whenStable();
    const html = fixture.nativeElement as HTMLElement;

    expect(html.querySelector('nav')?.textContent).not.toContain('Reportes');
    expect(html.querySelector('.sesion')?.textContent).toContain('Iniciar sesión');
  });
});
