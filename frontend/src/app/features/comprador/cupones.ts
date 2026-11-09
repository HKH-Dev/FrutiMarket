import { DatePipe } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { ApiService } from '../../core/api.service';
import { Cupon } from '../../core/modelos';

@Component({
  selector: 'app-cupones',
  imports: [DatePipe],
  template: `
    <div class="ancho tienda-contenido">
      <h1>Mis cupones</h1>
      <div class="pila">
        @for (c of cupones(); track c.id) {
          <div class="cupon" [class.gastado]="c.usadoEn">
            <div><strong>{{ c.codigo }}</strong><div>{{ c.porcentaje }}% de descuento en una compra</div></div>
            <span>{{ c.usadoEn ? 'Usado' : 'Vence el ' + (c.venceEl | date: 'mediumDate') }}</span>
          </div>
        } @empty {
          <div class="vacio"><h2>No tienes cupones por ahora</h2><p class="sutil">Cuando recibas uno, aparecerá aquí.</p></div>
        }
      </div>
    </div>
  `,
})
export class Cupones implements OnInit {
  private readonly api = inject(ApiService);
  protected readonly cupones = signal<Cupon[]>([]);

  ngOnInit(): void {
    this.api.misCupones().subscribe((c) => this.cupones.set(c));
  }
}
