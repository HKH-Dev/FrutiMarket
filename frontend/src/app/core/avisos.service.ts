import { Injectable, signal } from '@angular/core';

export type TipoAviso = 'exito' | 'error' | 'info';

export interface Aviso {
  id: number;
  tipo: TipoAviso;
  texto: string;
}

/** Duracion de un aviso en pantalla; los errores se quedan un poco mas para alcanzar a leerlos. */
const DURACION_MS: Record<TipoAviso, number> = { exito: 4000, info: 4000, error: 6000 };

/** Mensajes cortos que aparecen y desaparecen solos. Los muestra el componente Avisos. */
@Injectable({ providedIn: 'root' })
export class AvisosService {
  private siguienteId = 1;
  readonly avisos = signal<Aviso[]>([]);

  exito(texto: string): void { this.mostrar('exito', texto); }
  error(texto: string): void { this.mostrar('error', texto); }
  info(texto: string): void { this.mostrar('info', texto); }

  cerrar(id: number): void {
    this.avisos.update((lista) => lista.filter((a) => a.id !== id));
  }

  private mostrar(tipo: TipoAviso, texto: string): void {
    const id = this.siguienteId++;
    this.avisos.update((lista) => [...lista, { id, tipo, texto }]);
    setTimeout(() => this.cerrar(id), DURACION_MS[tipo]);
  }
}
