import { CurrencyPipe } from '@angular/common';
import { Component, computed, inject, input } from '@angular/core';
import { RouterLink } from '@angular/router';
import { AuthService } from '../core/auth.service';
import { LoteResumen } from '../core/modelos';
import { TiendaService } from '../core/tienda.service';
import { Fruta } from './fruta';

/** Limite de la barra de frescura: un lote con 30 dias o mas de vida util la llena. */
const DIAS_BARRA_LLENA = 30;

/** Etiqueta de un lote, como la que se amarra a una caja de cosecha. */
@Component({
  selector: 'app-etiqueta-lote',
  imports: [CurrencyPipe, RouterLink, Fruta],
  host: { class: 'etiqueta-lote' },
  template: `
    @if (descuento() > 0) { <span class="sello">-{{ descuento() }}%</span> }
    <div class="etiqueta-cabeza">
      <app-fruta [nombre]="lote().producto" />
      <div>
        <h3><a [routerLink]="['/lotes', lote().id]">{{ lote().producto }}</a></h3>
        <span class="sutil">{{ lote().cantidadDisponible }} {{ lote().unidad }} disponibles</span>
      </div>
    </div>
    @if (motivo()) { <p class="motivo">{{ motivo() }}</p> }
    <div class="etiqueta-precio">
      <strong>{{ precioFinal() | currency: 'COP' : 'symbol-narrow' : '1.0-0' }}</strong>
      @if (descuento() > 0) { <s>{{ lote().precioPorUnidad | currency: 'COP' : 'symbol-narrow' : '1.0-0' }}</s> }
      <span class="sutil">por {{ lote().unidad }}</span>
    </div>
    @if (lote().diasVidaUtilRestante !== null) {
      <div class="frescura">
        <div class="frescura-barra"><span [class.poca]="lote().diasVidaUtilRestante! < 5" [style.width.%]="frescura()"></span></div>
        Se conserva {{ lote().diasVidaUtilRestante }} días más
      </div>
    } @else {
      <div class="frescura">No perecedero</div>
    }
    <div class="etiqueta-pie">
      <span class="chip">Calidad {{ lote().puntajeCalidad }}/100</span>
      @if (auth.esComprador()) {
        <button type="button" class="boton-corazon" [attr.aria-pressed]="esFavorito()"
          [attr.aria-label]="(esFavorito() ? 'Quitar de favoritos: ' : 'Guardar en favoritos: ') + lote().producto"
          (click)="tienda.alternarFavorito(lote().productoId)">
          <svg viewBox="0 0 24 24" width="20" height="20" aria-hidden="true"><path fill="currentColor" d="M12 21s-8-5.2-10-10C.7 7.5 3 4 6.5 4c2.2 0 4 1.200 5.500 3.200C13.500 5.200 15.300 4 17.500 4 21 4 23.300 7.500 22 11c-2 4.800-10 10-10 10Z"/></svg>
        </button>
      }
    </div>
  `,
})
export class EtiquetaLote {
  protected readonly auth = inject(AuthService);
  protected readonly tienda = inject(TiendaService);

  readonly lote = input.required<LoteResumen>();
  readonly motivo = input<string>();

  protected readonly descuento = computed(() => this.tienda.descuentoDe(this.lote().productoId));
  protected readonly precioFinal = computed(() => (this.lote().precioPorUnidad ?? 0) * (1 - this.descuento() / 100));
  protected readonly esFavorito = computed(() => this.tienda.favoritos().has(this.lote().productoId));
  protected readonly frescura = computed(() => Math.min(100, ((this.lote().diasVidaUtilRestante ?? 0) / DIAS_BARRA_LLENA) * 100));
}
