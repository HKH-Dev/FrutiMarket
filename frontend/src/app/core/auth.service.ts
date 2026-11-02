import { HttpClient } from '@angular/common/http';
import { Injectable, computed, inject, signal } from '@angular/core';
import { Observable, tap } from 'rxjs';
import { Token } from './modelos';

const CLAVE_SESION = 'frutimarket.sesion';

interface Sesion {
  usuario: string;
  token: string;
  roles: string[];
  expiraEn: string;
}

/**
 * Inicio de sesion con JWT. Guarda el token en localStorage para que sobreviva a recargar la
 * pagina, y expone la sesion como signals para que la interfaz reaccione sola al entrar o salir.
 */
@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly sesion = signal<Sesion | null>(leerSesionGuardada());

  readonly usuario = computed(() => this.sesion()?.usuario ?? null);
  readonly roles = computed(() => this.sesion()?.roles ?? []);
  readonly autenticado = computed(() => this.sesion() !== null);
  readonly esAdmin = computed(() => this.roles().includes('ADMIN'));

  token(): string | null {
    const sesion = this.sesion();
    if (sesion && new Date(sesion.expiraEn) <= new Date()) {
      this.cerrarSesion(); // token vencido: se descarta
      return null;
    }
    return sesion?.token ?? null;
  }

  iniciarSesion(usuario: string, clave: string): Observable<Token> {
    return this.http.post<Token>('/api/auth/login', { usuario, clave }).pipe(
      tap((respuesta) => {
        const sesion: Sesion = {
          usuario,
          token: respuesta.token,
          roles: respuesta.roles,
          expiraEn: respuesta.expiraEn,
        };
        guardar(sesion);
        this.sesion.set(sesion);
      }),
    );
  }

  cerrarSesion(): void {
    guardar(null);
    this.sesion.set(null);
  }
}

function leerSesionGuardada(): Sesion | null {
  try {
    const texto = localStorage.getItem(CLAVE_SESION);
    const sesion = texto ? (JSON.parse(texto) as Sesion) : null;
    return sesion && new Date(sesion.expiraEn) > new Date() ? sesion : null;
  } catch {
    return null;
  }
}

function guardar(sesion: Sesion | null): void {
  try {
    if (sesion) {
      localStorage.setItem(CLAVE_SESION, JSON.stringify(sesion));
    } else {
      localStorage.removeItem(CLAVE_SESION);
    }
  } catch {
    // Navegacion privada o almacenamiento bloqueado: la sesion dura solo mientras la pagina este abierta.
  }
}
