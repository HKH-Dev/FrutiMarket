import { CurrencyPipe, DatePipe } from '@angular/common';
import { Component, OnInit, computed, inject, input, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { forkJoin } from 'rxjs';
import { ApiService } from '../../core/api.service';
import { AuthService } from '../../core/auth.service';
import { mensajeDeError } from '../../core/errores';
import { LoteResumen, Trazabilidad } from '../../core/modelos';
import { TiendaService } from '../../core/tienda.service';
import { Fruta } from '../../shared/fruta';

/** Detalle de un lote: compra, contacto con el vendedor y trazabilidad (regla 8). */
@Component({
  selector: 'app-lote-detalle',
  imports: [CurrencyPipe, DatePipe, FormsModule, RouterLink, Fruta],
  template: `
    <div class="ancho tienda-contenido">
      <a routerLink="/catalogo">Volver al catálogo</a>
      @if (error()) {
        <p class="aviso aviso-error" role="alert">{{ error() }}</p>
      }
      @if (traza(); as t) {
        <div class="detalle">
          <div class="pila">
            <div class="fila">
              <div style="width: 5rem"><app-fruta [nombre]="t.producto" /></div>
              <div><h1>{{ t.producto }}</h1><span class="sutil">Lote {{ t.codigo }}</span></div>
            </div>
            <section class="lienzo">
              <h2>Origen y calidad</h2>
              <dl class="datos">
                <dt>Cosecha</dt><dd>{{ t.fechaCosecha | date: 'longDate' }}</dd>
                <dt>Técnica</dt><dd>{{ t.tecnicaProduccion }}</dd>
                <dt>Calidad declarada</dt>
                <dd>{{ t.calidadDeclarada.categoria }}, {{ t.calidadDeclarada.porcentajeDefectos }}% de defectos
                  @if (t.calidadDeclarada.certificadoOrganico) { <span class="chip">Orgánico</span> }
                </dd>
                <dt>Verificada</dt>
                <dd>@if (t.calidadVerificada; as v) { {{ v.categoria }}, {{ v.porcentajeDefectos }}% de defectos } @else { Aún sin inspección }</dd>
                @if (t.fechaLimiteConsumo) { <dt>Consumir antes del</dt><dd>{{ t.fechaLimiteConsumo | date: 'longDate' }}</dd> }
              </dl>
            </section>
            <section class="lienzo">
              <h2>Recorrido del lote</h2>
              <ol class="custodia">
                @for (paso of t.custodia; track $index) {
                  <li>
                    <strong>{{ paso.descripcion }}</strong>
                    <span>{{ paso.lugar }}, a cargo de {{ paso.responsable }}</span>
                    <span class="sutil">{{ paso.momento | date: 'medium' }}@if (paso.temperaturaC !== null) { , {{ paso.temperaturaC }} °C }</span>
                  </li>
                }
              </ol>
            </section>
          </div>
          @if (lote(); as l) {
            <aside class="lienzo">
              @if (descuento() > 0) { <span class="chip chip-amarillo" style="justify-self: start">{{ descuento() }}% menos por temporada</span> }
              <div class="etiqueta-precio">
                <strong>{{ precio() | currency: 'COP' : 'symbol-narrow' : '1.0-0' }}</strong>
                @if (descuento() > 0) { <s>{{ l.precioPorUnidad | currency: 'COP' : 'symbol-narrow' : '1.0-0' }}</s> }
                <span class="sutil">por {{ l.unidad }}</span>
              </div>
              <p class="sutil">{{ l.cantidadDisponible }} {{ l.unidad }} disponibles</p>
              @if (l.estado === 'PUBLICADO' && !esVendedorOAdmin()) {
                <label class="campo">Cantidad en {{ l.unidad }}
                  <input type="number" min="1" [max]="l.cantidadDisponible" [(ngModel)]="cantidad" />
                </label>
                <button type="button" class="boton" (click)="agregar(l)" [disabled]="cantidad() <= 0 || cantidad() > l.cantidadDisponible">Agregar al carrito</button>
                @if (agregado()) { <p class="aviso" role="status">Agregado. <a routerLink="/carrito">Ir al carrito</a></p> }
                <button type="button" class="boton boton-linea" (click)="contactar(l.id)">Escribir al vendedor</button>
              } @else if (l.estado !== 'PUBLICADO') {
                <p class="aviso">Este lote no está a la venta.</p>
              }
            </aside>
          }
        </div>
      } @else if (!error()) {
        <p class="sutil">Cargando lote…</p>
      }
    </div>
  `,
})
export class LoteDetalle implements OnInit {
  private readonly api = inject(ApiService);
  private readonly auth = inject(AuthService);
  private readonly tienda = inject(TiendaService);
  private readonly router = inject(Router);

  /** Viene del parametro :id de la ruta (withComponentInputBinding en app.config). */
  readonly id = input.required<string>();

  protected readonly traza = signal<Trazabilidad | null>(null);
  protected readonly lote = signal<LoteResumen | null>(null);
  protected readonly error = signal<string | null>(null);
  protected readonly cantidad = signal(1);
  protected readonly agregado = signal(false);
  protected readonly esVendedorOAdmin = computed(() => this.auth.autenticado() && !this.auth.esComprador());
  protected readonly descuento = computed(() => { const l = this.lote(); return l ? this.tienda.descuentoDe(l.productoId) : 0; });
  protected readonly precio = computed(() => (this.lote()?.precioPorUnidad ?? 0) * (1 - this.descuento() / 100));

  ngOnInit(): void {
    forkJoin({ traza: this.api.trazabilidad(this.id()), lote: this.api.lote(this.id()) }).subscribe({
      next: ({ traza, lote }) => {
        this.traza.set(traza);
        this.lote.set(lote);
      },
      error: (e) => this.error.set(mensajeDeError(e)),
    });
  }

  protected agregar(lote: LoteResumen): void {
    this.tienda.agregar(lote, this.cantidad());
    this.agregado.set(true);
  }

  protected contactar(loteId: string): void {
    if (!this.auth.autenticado()) {
      this.router.navigate(['/login'], { queryParams: { volver: `/lotes/${loteId}` } });
      return;
    }
    this.api.iniciarConversacion(loteId).subscribe({
      next: (c) => this.router.navigate(['/mensajes'], { queryParams: { c: c.id } }),
      error: (e) => this.error.set(mensajeDeError(e)),
    });
  }
}
