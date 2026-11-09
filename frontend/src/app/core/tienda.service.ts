import { Injectable, computed, inject, signal } from '@angular/core';
import { ApiService } from './api.service';
import { AuthService } from './auth.service';
import { LoteResumen, Oferta } from './modelos';

const CLAVE_CARRITO = 'frutimarket.carrito';

export interface LineaCarrito {
  lote: LoteResumen;
  cantidad: number;
}

/** Estado compartido de la tienda: carrito (persistente), ofertas vigentes y favoritos del comprador. */
@Injectable({ providedIn: 'root' })
export class TiendaService {
  private readonly api = inject(ApiService);
  private readonly auth = inject(AuthService);

  readonly lineas = signal<LineaCarrito[]>(leerCarrito());
  readonly cuantos = computed(() => this.lineas().length);
  readonly ofertas = signal<Oferta[]>([]);
  readonly favoritos = signal<ReadonlySet<string>>(new Set());

  cargar(): void {
    this.api.ofertasVigentes().subscribe((ofertas) => this.ofertas.set(ofertas));
  }

  recargarFavoritos(): void {
    if (this.auth.esComprador()) {
      this.api.favoritos().subscribe((ids) => this.favoritos.set(new Set(ids)));
    } else {
      this.favoritos.set(new Set());
    }
  }

  /** Mayor descuento vigente para el producto; el valor que se cobra lo calcula siempre el backend. */
  descuentoDe(productoId: string): number {
    return Math.max(0, ...this.ofertas().filter((o) => o.productoId === null || o.productoId === productoId).map((o) => o.porcentaje));
  }

  alternarFavorito(productoId: string): void {
    const actuales = new Set(this.favoritos());
    const accion = actuales.has(productoId) ? this.api.quitarFavorito(productoId) : this.api.agregarFavorito(productoId);
    if (!actuales.delete(productoId)) {
      actuales.add(productoId);
    }
    accion.subscribe({ next: () => this.favoritos.set(actuales) });
  }

  agregar(lote: LoteResumen, cantidad: number): void {
    const existente = this.lineas().find((l) => l.lote.id === lote.id);
    this.guardar(existente
      ? this.lineas().map((l) => (l === existente ? { ...l, cantidad: l.cantidad + cantidad } : l))
      : [...this.lineas(), { lote, cantidad }]);
  }

  cambiarCantidad(loteId: string, cantidad: number): void {
    this.guardar(this.lineas().map((l) => (l.lote.id === loteId ? { ...l, cantidad } : l)));
  }

  quitar(loteId: string): void {
    this.guardar(this.lineas().filter((l) => l.lote.id !== loteId));
  }

  vaciar(): void {
    this.guardar([]);
  }

  private guardar(lineas: LineaCarrito[]): void {
    this.lineas.set(lineas);
    try {
      localStorage.setItem(CLAVE_CARRITO, JSON.stringify(lineas));
    } catch {
      // Almacenamiento bloqueado: el carrito dura mientras la pagina este abierta.
    }
  }
}

function leerCarrito(): LineaCarrito[] {
  try {
    return JSON.parse(localStorage.getItem(CLAVE_CARRITO) ?? '[]') as LineaCarrito[];
  } catch {
    return [];
  }
}
