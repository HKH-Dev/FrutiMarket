import { CurrencyPipe } from '@angular/common';
import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { ApiService } from '../../core/api.service';
import { ProductoVendido, ResumenPagos } from '../../core/modelos';

@Component({
  selector: 'app-resumen-consola',
  imports: [CurrencyPipe],
  host: { style: 'display: grid; gap: 1.5rem' },
  template: `
    <h1>Ventas</h1>
    @if (resumen(); as r) {
      <div class="cifras">
        <div class="cifra"><strong>{{ r.ingresos | currency: 'COP' : 'symbol-narrow' : '1.0-0' }}</strong>cobrado</div>
        <div class="cifra"><strong>{{ r.descuentos | currency: 'COP' : 'symbol-narrow' : '1.0-0' }}</strong>en descuentos</div>
        <div class="cifra"><strong>{{ r.aprobados }}</strong>pagos aprobados</div>
        <div class="cifra"><strong>{{ r.reembolsados }}</strong>reembolsos</div>
      </div>
    }
    <section class="pila">
      <h2>Productos más vendidos</h2>
      @if (ranking().length) {
        <div class="tabla-marco"><table>
          <thead><tr><th>#</th><th>Producto</th><th style="width: 40%">Volumen</th><th class="num">Vendido</th><th class="num">Compras</th></tr></thead>
          <tbody>@for (p of ranking(); track p.productoId) {
            <tr><td>{{ p.posicion }}</td><td>{{ p.nombre }}</td><td><div class="barra-ranking" [style.width.%]="(p.cantidadVendida / maximo()) * 100"></div></td><td class="num">{{ p.cantidadVendida }} {{ p.unidad }}</td><td class="num">{{ p.numeroCompras }}</td></tr>
          }</tbody>
        </table></div>
      } @else {
        <div class="vacio"><p>El ranking aparecerá con la primera venta.</p></div>
      }
    </section>
  `,
})
export class ResumenConsola implements OnInit {
  private readonly api = inject(ApiService);
  protected readonly resumen = signal<ResumenPagos | null>(null);
  protected readonly ranking = signal<ProductoVendido[]>([]);
  protected readonly maximo = computed(() => Math.max(1, ...this.ranking().map((p) => p.cantidadVendida)));

  ngOnInit(): void {
    this.api.resumen().subscribe((r) => this.resumen.set(r));
    this.api.masVendidos().subscribe((r) => this.ranking.set(r));
  }
}
