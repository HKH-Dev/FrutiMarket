import { CurrencyPipe, DatePipe } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { ComprasService } from '../../core/servicios/compras.service';
import { AvisosService } from '../../core/avisos.service';
import { mensajeDeError } from '../../core/errores';
import { Compra } from '../../core/modelos';

const PLAZO_MS = 3 * 24 * 60 * 60 * 1000;

@Component({
  selector: 'app-compras',
  imports: [CurrencyPipe, DatePipe, RouterLink],
  template: `
    <div class="container tienda-contenido">
      <h1>Mis compras</h1>
      @if (error()) { <p class="alert alert-danger" role="alert">{{ error() }}</p> }
      <div class="pila">
        @for (c of compras(); track c.compraId) {
          <section class="card card-body lienzo">
            <div class="fila-entre">
              <div><h3>{{ c.fecha | date: 'mediumDate' }}</h3><span class="sutil">{{ c.detalles.length }} {{ c.detalles.length === 1 ? 'lote' : 'lotes' }}, {{ c.total | currency: 'COP' : 'symbol-narrow' : '1.0-0' }} antes de descuentos</span></div>
              <span class="badge chip" [class.chip-naranja]="c.estado === 'REEMBOLSADA'">{{ c.estado === 'REEMBOLSADA' ? 'Reembolsada' : c.estado === 'COMPLETADA' ? 'Completada' : c.estado }}</span>
            </div>
            <ul class="sutil" style="margin: 0; padding-left: 1.1rem">
              @for (d of c.detalles; track d.loteId) { <li><a [routerLink]="['/lotes', d.loteId]">{{ d.cantidad }} {{ d.unidadMedida }}</a> a {{ d.precioUnitario | currency: 'COP' : 'symbol-narrow' : '1.0-0' }}</li> }
            </ul>
            @if (reembolsable(c)) {
              <div><button type="button" class="btn btn-outline-danger btn-sm" (click)="reembolsar(c)">Pedir reembolso</button></div>
            }
          </section>
        } @empty {
          @if (!cargando()) { <div class="vacio"><h2>Aún no has comprado</h2><a routerLink="/catalogo" class="btn btn-warning">Ver catálogo</a></div> }
        }
      </div>
    </div>
  `,
})
export class Compras implements OnInit {
  private readonly avisos = inject(AvisosService);
  private readonly comprasApi = inject(ComprasService);
  protected readonly compras = signal<Compra[]>([]);
  protected readonly cargando = signal(true);
  protected readonly error = signal<string | null>(null);

  ngOnInit(): void {
    this.comprasApi.misCompras().subscribe({
      next: (c) => { this.compras.set(c); this.cargando.set(false); },
      error: (e) => { this.error.set(mensajeDeError(e)); this.cargando.set(false); },
    });
  }

  protected reembolsable(c: Compra): boolean {
    return c.estado === 'COMPLETADA' && Date.now() - new Date(c.fecha).getTime() <= PLAZO_MS;
  }

  protected reembolsar(c: Compra): void {
    if (!confirm('¿Pedir el reembolso de esta compra?')) {
      return;
    }
    this.comprasApi.reembolsar(c.compraId).subscribe({
      next: (nueva) => {
        this.compras.update((l) => l.map((x) => (x.compraId === c.compraId ? nueva : x)));
        this.avisos.exito('Reembolso aprobado.');
      },
      error: (e) => this.avisos.error(mensajeDeError(e)),
    });
  }
}
