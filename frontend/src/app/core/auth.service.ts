import { HttpClient } from '@angular/common/http';
import { Injectable, computed, inject, signal } from '@angular/core';
import { Observable, tap } from 'rxjs';
import { environment } from '../../environments/environment';
import { Registro, Rol, Token } from './modelos';

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
  readonly rol = computed(() => (this.roles()[0] as Rol | undefined) ?? null);
  readonly esComprador = computed(() => this.rol() === 'COMPRADOR');
  /** Id del campesino o comprador del usuario: claim actorId del JWT. */
  readonly actorId = computed(() => leerActor(this.sesion()?.token));

  /** El token tal cual esta guardado. Si vencio, el backend respondera 401 y el interceptor cerrara la sesion. */
  tokenGuardado(): string | null {
    // Se lee de localStorage en cada peticion: asi se nota si alguien lo cambia a mano (Application > Local storage).
    try {
      const texto = localStorage.getItem(CLAVE_SESION);
      if (texto) {
        return (JSON.parse(texto) as Sesion).token;
      }
    } catch {
      // Almacenamiento bloqueado o JSON alterado: se usa la sesion en memoria.
    }
    return this.sesion()?.token ?? null;
  }

  token(): string | null {
    const sesion = this.sesion();
    if (sesion && new Date(sesion.expiraEn) <= new Date()) {
      this.cerrarSesion(); // token vencido: se descarta
      return null;
    }
    return sesion?.token ?? null;
  }

  iniciarSesion(correo: string, clave: string): Observable<Token> {
    return this.http.post<Token>(`${environment.apiUrl}/auth/login`, { correo, clave }).pipe(
      tap((respuesta) => {
        const sesion: Sesion = {
          usuario: respuesta.correo,
          token: respuesta.token,
          roles: [respuesta.rol],
          expiraEn: respuesta.expiraEn,
        };
        guardar(sesion);
        this.sesion.set(sesion);
      }),
    );
  }

  registrar(datos: Registro): Observable<unknown> {
    return this.http.post(`${environment.apiUrl}/auth/registro`, datos);
  }

  /** Pantalla inicial de cada perfil. */
  inicioDelRol(): string {
    return this.rol() === 'ADMIN' ? '/consola' : this.rol() === 'CAMPESINO' ? '/finca' : '/';
  }

  cerrarSesion(): void {
    guardar(null);
    this.sesion.set(null);
  }
}

function leerActor(token: string | undefined): string | null {
  try {
    const carga = token!.split('.')[1].replace(/-/g, '+').replace(/_/g, '/');
    return (JSON.parse(atob(carga)) as { actorId?: string }).actorId ?? null;
  } catch {
    return null;
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
