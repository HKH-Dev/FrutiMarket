import { CurrencyPipe } from '@angular/common';
import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { ApiService } from '../../core/api.service';
import { LoteResumen, VentaCampesino } from '../../core/modelos';

@Component({
  selector: 'app-resumen-finca',
  imports: [CurrencyPipe, RouterLink],
  template: `
    <div class="fila-entre"><h1>Tu cosecha hoy</h1><a routerLink="/finca/lotes/nuevo" class="boton">Registrar lote</a></div>
    <div class="cifras">
      <div class="cifra"><strong>{{ publicados() }}</strong>lotes a la venta</div>
      <div class="cifra"><strong>{{ borradores() }}</strong>lotes sin publicar</div>
      <div class="cifra"><strong>{{ ingresos() | currency: 'COP' : 'symbol-narrow' : '1.0-0' }}</strong>vendido a precio de finca</div>
    </div>
    @if (porVencer().length) {
      <p class="aviso" style="background: var(--acento)">Se acaban pronto: {{ nombresPorVencer() }}. Revisa si conviene bajar el precio o despachar primero.</p>
    }
    <section class="pila">
      <h2>Ventas por producto</h2>
      @if (ventas().length) {
        <div class="tabla-marco"><table>
          <thead><tr><th>Producto</th><th class="num">Vendido</th><th class="num">Compras</th><th class="num">Ingresos</th></tr></thead>
          <tbody>@for (v of ventas(); track v.productoId) {
            <tr><td>{{ v.nombre }}</td><td class="num">{{ v.cantidadVendida }} {{ v.unidad }}</td><td class="num">{{ v.numeroCompras }}</td><td class="num">{{ v.ingresos | currency: 'COP' : 'symbol-narrow' : '1.0-0' }}</td></tr>
          }</tbody>
        </table></div>
      } @else {
        <div class="vacio"><p>Tus ventas aparecerán aquí cuando alguien compre uno de tus lotes.</p></div>
      }
    </section>
  `,
  host: { style: 'display: grid; gap: 1.5rem' },
})
export class ResumenFinca implements OnInit {
  private readonly api = inject(ApiService);
  private readonly lotes = signal<LoteResumen[]>([]);
  protected readonly ventas = signal<VentaCampesino[]>([]);
  protected readonly publicados = computed(() => this.lotes().filter((l) => l.estado === 'PUBLICADO').length);
  protected readonly borradores = computed(() => this.lotes().filter((l) => l.estado === 'REGISTRADO').length);
  protected readonly ingresos = computed(() => this.ventas().reduce((s, v) => s + v.ingresos, 0));
  protected readonly porVencer = computed(() => this.lotes().filter((l) => l.estado === 'PUBLICADO' && l.diasVidaUtilRestante !== null && l.diasVidaUtilRestante < 5));
  protected readonly nombresPorVencer = computed(() => this.porVencer().map((l) => l.producto).join(', '));

  ngOnInit(): void {
    this.api.misLotes().subscribe((l) => this.lotes.set(l));
    this.api.misVentas().subscribe((v) => this.ventas.set(v));
  }
}
