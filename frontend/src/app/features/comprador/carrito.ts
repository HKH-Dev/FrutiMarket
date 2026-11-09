import { CurrencyPipe } from '@angular/common';
import { Component, OnInit, computed, effect, inject, signal, untracked } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { ApiService } from '../../core/api.service';
import { AuthService } from '../../core/auth.service';
import { mensajeDeError } from '../../core/errores';
import { Comprobante, ItemCompra, Liquidacion, MetodoPago } from '../../core/modelos';
import { TiendaService } from '../../core/tienda.service';
import { Fruta } from '../../shared/fruta';

/** Carrito y pago simulado. Los totales siempre los calcula el backend (cotizacion). */
@Component({
  selector: 'app-carrito',
  imports: [CurrencyPipe, FormsModule, RouterLink, Fruta],
  template: `
    <div class="ancho tienda-contenido">
      @if (comprobante(); as c) {
        <div class="vacio" role="status">
          <h1>Pago aprobado</h1>
          <p>Referencia {{ c.referencia }}. Total pagado: <strong>{{ c.liquidacion.total | currency: 'COP' : 'symbol-narrow' : '1.0-0' }}</strong></p>
          <a routerLink="/compras" class="boton">Ver mis compras</a>
        </div>
      } @else if (!tienda.lineas().length) {
        <div class="vacio"><h1>Tu carrito está vacío</h1><p class="sutil">Elige un lote del catálogo para empezar.</p><a routerLink="/catalogo" class="boton">Ver catálogo</a></div>
      } @else {
        <h1>Carrito</h1>
        <div class="detalle">
          <section class="lienzo">
            @for (l of tienda.lineas(); track l.lote.id) {
              <div class="linea-carrito">
                <app-fruta [nombre]="l.lote.producto" />
                <div><strong>{{ l.lote.producto }}</strong><div class="sutil">{{ l.lote.precioPorUnidad | currency: 'COP' : 'symbol-narrow' : '1.0-0' }} por {{ l.lote.unidad }}</div></div>
                <label class="campo"><span class="oculto-a11y">Cantidad de {{ l.lote.producto }}</span>
                  <input type="number" min="1" [ngModel]="l.cantidad" (ngModelChange)="cambiar(l.lote.id, $event)" />
                </label>
                <button type="button" class="boton boton-linea boton-chico" (click)="quitar(l.lote.id)">Quitar</button>
              </div>
            }
          </section>
          <aside class="lienzo">
            <h2>Pago</h2>
            @if (!auth.esComprador()) {
              <p class="aviso">Inicia sesión como comprador para pagar.</p>
              <a routerLink="/login" [queryParams]="{ volver: '/carrito' }" class="boton boton-verde">Iniciar sesión</a>
            } @else {
              <form class="fila" (submit)="$event.preventDefault(); cotizar()" style="flex-wrap: nowrap; align-items: end">
                <label class="campo" style="flex: 1">Cupón <input name="cupon" [(ngModel)]="cupon" autocomplete="off" /></label>
                <button class="boton boton-linea" type="submit">Aplicar</button>
              </form>
              @if (liquidacion(); as t) {
                <div class="totales" aria-live="polite">
                  <div><span>Subtotal</span><span>{{ t.subtotal | currency: 'COP' : 'symbol-narrow' : '1.0-0' }}</span></div>
                  @if (t.descuentoTemporada > 0) { <div><span>Descuento de temporada</span><span>-{{ t.descuentoTemporada | currency: 'COP' : 'symbol-narrow' : '1.0-0' }}</span></div> }
                  @if (t.descuentoCupon > 0) { <div><span>Cupón</span><span>-{{ t.descuentoCupon | currency: 'COP' : 'symbol-narrow' : '1.0-0' }}</span></div> }
                  <div class="total"><span>Total</span><span>{{ t.total | currency: 'COP' : 'symbol-narrow' : '1.0-0' }}</span></div>
                </div>
              }
              <div class="metodos" role="radiogroup" aria-label="Método de pago">
                @for (m of metodos; track m) { <label><input type="radio" name="metodo" [value]="m" [(ngModel)]="metodo" /> {{ m === 'TARJETA' ? 'Tarjeta' : m === 'PSE' ? 'PSE' : 'Nequi' }}</label> }
              </div>
              @if (metodo() === 'TARJETA') {
                <label class="campo">Últimos 4 dígitos
                  <input name="digitos" inputmode="numeric" maxlength="4" [(ngModel)]="digitos" autocomplete="off" />
                  <span class="ayuda">Pago simulado: no escribas el número completo. 0000 simula un rechazo.</span>
                </label>
              }
              @if (error()) { <p class="aviso aviso-error" role="alert">{{ error() }}</p> }
              <button type="button" class="boton" (click)="pagar()" [disabled]="pagando() || !puedePagar()">{{ pagando() ? 'Procesando pago…' : 'Pagar' }}</button>
            }
          </aside>
        </div>
      }
    </div>
  `,
})
export class Carrito implements OnInit {
  private readonly api = inject(ApiService);
  protected readonly auth = inject(AuthService);
  protected readonly tienda = inject(TiendaService);
  protected readonly router = inject(Router);

  protected readonly metodos: MetodoPago[] = ['TARJETA', 'PSE', 'NEQUI'];
  protected readonly cupon = signal('');
  protected readonly metodo = signal<MetodoPago>('TARJETA');
  protected readonly digitos = signal('');
  protected readonly liquidacion = signal<Liquidacion | null>(null);
  protected readonly comprobante = signal<Comprobante | null>(null);
  protected readonly error = signal<string | null>(null);
  protected readonly pagando = signal(false);
  protected readonly puedePagar = computed(() => this.metodo() !== 'TARJETA' || /^\d{4}$/.test(this.digitos()));

  private readonly items = computed<ItemCompra[]>(() => this.tienda.lineas().map((l) => ({ loteId: l.lote.id, cantidad: l.cantidad })));

  constructor() {
    effect(() => {
      this.items();
      untracked(() => this.cotizar());
    });
  }

  ngOnInit(): void {
    this.error.set(null);
  }

  protected cambiar(loteId: string, cantidad: number): void {
    if (cantidad > 0) {
      this.tienda.cambiarCantidad(loteId, cantidad);
    }
  }

  protected quitar(loteId: string): void {
    this.tienda.quitar(loteId);
  }

  protected cotizar(): void {
    if (!this.auth.esComprador() || !this.items().length) {
      return;
    }
    this.error.set(null);
    this.api.cotizar(this.items(), this.cupon().trim() || null).subscribe({
      next: (t) => this.liquidacion.set(t),
      error: (e) => this.error.set(mensajeDeError(e)),
    });
  }

  protected pagar(): void {
    this.pagando.set(true);
    this.error.set(null);
    const digitos = this.metodo() === 'TARJETA' ? this.digitos() : null;
    this.api.pagar(this.items(), this.cupon().trim() || null, this.metodo(), digitos).subscribe({
      next: (c) => {
        this.comprobante.set(c);
        this.tienda.vaciar();
        this.pagando.set(false);
      },
      error: (e) => {
        this.error.set(mensajeDeError(e));
        this.pagando.set(false);
      },
    });
  }
}
