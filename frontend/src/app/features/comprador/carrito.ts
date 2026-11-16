import { CurrencyPipe } from '@angular/common';
import { Component, computed, effect, inject, signal, untracked } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../core/auth.service';
import { AvisosService } from '../../core/avisos.service';
import { mensajeDeError } from '../../core/errores';
import { Comprobante, ItemCompra, Liquidacion, MetodoPago } from '../../core/modelos';
import { ComprasService } from '../../core/servicios/compras.service';
import { TiendaService } from '../../core/tienda.service';
import { Fruta } from '../../shared/fruta';

const DIGITOS = /^\d{4}$/;

/**
 * Carrito y pago simulado con formulario reactivo (POST protegido /api/pagos/checkout).
 * Los totales siempre los calcula el backend (cotizacion): el navegador solo los muestra.
 */
@Component({
  selector: 'app-carrito',
  imports: [CurrencyPipe, ReactiveFormsModule, RouterLink, Fruta],
  template: `
    <div class="container tienda-contenido">
      @if (comprobante(); as c) {
        <div class="vacio" role="status">
          <h1>Pago aprobado</h1>
          <p>Referencia {{ c.referencia }}. Total pagado: <strong>{{ c.liquidacion.total | currency: 'COP' : 'symbol-narrow' : '1.0-0' }}</strong></p>
          <a routerLink="/compras" class="btn btn-warning">Ver mis compras</a>
        </div>
      } @else if (!tienda.lineas().length) {
        <div class="vacio"><h1>Tu carrito está vacío</h1><p class="sutil">Elige un lote del catálogo para empezar.</p><a routerLink="/catalogo" class="btn btn-warning">Ver catálogo</a></div>
      } @else {
        <h1>Carrito</h1>
        <div class="detalle">
          <section class="card card-body lienzo">
            @for (l of tienda.lineas(); track l.lote.id) {
              <div class="linea-carrito">
                <app-fruta [nombre]="l.lote.producto" />
                <div><strong>{{ l.lote.producto }}</strong><div class="sutil">{{ l.lote.precioPorUnidad | currency: 'COP' : 'symbol-narrow' : '1.0-0' }} por {{ l.lote.unidad }}</div></div>
                <div><label class="oculto-a11y" [for]="'cantidad-' + l.lote.id">Cantidad de {{ l.lote.producto }}</label>
                  <input class="form-control" [id]="'cantidad-' + l.lote.id" type="number" min="1" [value]="l.cantidad" (change)="cambiar(l.lote.id, $event)" />
                </div>
                <button type="button" class="btn btn-outline-primary btn-sm" (click)="tienda.quitar(l.lote.id)">Quitar</button>
              </div>
            }
          </section>
          <aside class="card card-body lienzo">
            <h2>Pago</h2>
            @if (!auth.esComprador()) {
              <p class="alert alert-info mb-0">Inicia sesión como comprador para pagar.</p>
              <a routerLink="/login" [queryParams]="{ volver: '/carrito' }" class="btn btn-primary">Iniciar sesión</a>
            } @else {
              <form class="d-grid gap-3" [formGroup]="pago" (ngSubmit)="pagar()" novalidate>
                <div>
                  <label class="form-label" for="cupon">Cupón</label>
                  <div class="input-group">
                    <input id="cupon" class="form-control" formControlName="cupon" autocomplete="off" />
                    <button class="btn btn-outline-primary" type="button" (click)="cotizar()">Aplicar</button>
                  </div>
                </div>
                @if (liquidacion(); as t) {
                  <div class="totales" aria-live="polite">
                    <div><span>Subtotal</span><span>{{ t.subtotal | currency: 'COP' : 'symbol-narrow' : '1.0-0' }}</span></div>
                    @if (t.descuentoTemporada > 0) { <div><span>Descuento de temporada</span><span>-{{ t.descuentoTemporada | currency: 'COP' : 'symbol-narrow' : '1.0-0' }}</span></div> }
                    @if (t.descuentoCupon > 0) { <div><span>Cupón</span><span>-{{ t.descuentoCupon | currency: 'COP' : 'symbol-narrow' : '1.0-0' }}</span></div> }
                    <div class="total"><span>Total</span><span>{{ t.total | currency: 'COP' : 'symbol-narrow' : '1.0-0' }}</span></div>
                  </div>
                }
                <fieldset>
                  <legend class="form-label fs-6">Método de pago</legend>
                  <div class="metodos">
                    @for (m of metodos; track m.valor) {
                      <label><input type="radio" formControlName="metodo" [value]="m.valor" /> {{ m.texto }}</label>
                    }
                  </div>
                </fieldset>
                @if (metodo() === 'TARJETA') {
                  <div>
                    <label class="form-label" for="digitos">Últimos 4 dígitos de la tarjeta</label>
                    <input id="digitos" class="form-control" formControlName="ultimosDigitos" inputmode="numeric" maxlength="4" autocomplete="off"
                      [class.is-invalid]="pago.controls.ultimosDigitos.invalid && pago.controls.ultimosDigitos.touched" />
                    <div class="invalid-feedback">Escribe exactamente 4 números.</div>
                    <div class="form-text">Pago simulado: nunca escribas el número completo. 0000 simula un rechazo.</div>
                  </div>
                }
                @if (error()) { <div class="alert alert-danger mb-0" role="alert">{{ error() }}</div> }
                <button type="submit" class="btn btn-warning" [disabled]="pagando()">{{ pagando() ? 'Procesando pago…' : 'Pagar' }}</button>
              </form>
            }
          </aside>
        </div>
      }
    </div>
  `,
})
export class Carrito {
  private readonly compras = inject(ComprasService);
  private readonly avisos = inject(AvisosService);
  protected readonly auth = inject(AuthService);
  protected readonly tienda = inject(TiendaService);

  protected readonly metodos: { valor: MetodoPago; texto: string }[] = [
    { valor: 'TARJETA', texto: 'Tarjeta' }, { valor: 'PSE', texto: 'PSE' }, { valor: 'NEQUI', texto: 'Nequi' },
  ];
  protected readonly pago = inject(FormBuilder).nonNullable.group({
    cupon: [''],
    metodo: ['TARJETA' as MetodoPago, Validators.required],
    ultimosDigitos: ['', [Validators.required, Validators.pattern(DIGITOS)]],
  });
  protected readonly metodo = toSignal(this.pago.controls.metodo.valueChanges, { initialValue: this.pago.controls.metodo.value });
  protected readonly liquidacion = signal<Liquidacion | null>(null);
  protected readonly comprobante = signal<Comprobante | null>(null);
  protected readonly error = signal<string | null>(null);
  protected readonly pagando = signal(false);

  private readonly items = computed<ItemCompra[]>(() => this.tienda.lineas().map((l) => ({ loteId: l.lote.id, cantidad: l.cantidad })));

  constructor() {
    // Si cambia el carrito, el backend vuelve a calcular el total.
    effect(() => {
      this.items();
      untracked(() => this.cotizar());
    });
    // Los digitos solo son obligatorios con tarjeta.
    effect(() => {
      const digitos = this.pago.controls.ultimosDigitos;
      if (this.metodo() === 'TARJETA') {
        digitos.enable();
      } else {
        digitos.disable();
      }
    });
  }

  protected cambiar(loteId: string, evento: Event): void {
    const cantidad = Number((evento.target as HTMLInputElement).value);
    if (cantidad > 0) {
      this.tienda.cambiarCantidad(loteId, cantidad);
    }
  }

  protected cotizar(): void {
    if (!this.auth.esComprador() || !this.items().length) {
      return;
    }
    this.error.set(null);
    this.compras.cotizar(this.items(), this.cupon()).subscribe({
      next: (t) => this.liquidacion.set(t),
      error: (e) => this.error.set(mensajeDeError(e)),
    });
  }

  protected pagar(): void {
    if (this.pago.invalid) {
      this.pago.markAllAsTouched();
      return;
    }
    const { metodo, ultimosDigitos } = this.pago.getRawValue();
    this.pagando.set(true);
    this.error.set(null);
    this.compras.pagar(this.items(), this.cupon(), metodo, metodo === 'TARJETA' ? ultimosDigitos : null).subscribe({
      next: (c) => {
        this.comprobante.set(c);
        this.tienda.vaciar();
        this.pagando.set(false);
        this.avisos.exito('Pago aprobado. Tu compra quedó registrada.');
      },
      error: (e) => {
        const mensaje = mensajeDeError(e);
        this.error.set(mensaje);
        this.avisos.error(mensaje);
        this.pagando.set(false);
      },
    });
  }

  private cupon(): string | null {
    return this.pago.controls.cupon.value.trim() || null;
  }
}
