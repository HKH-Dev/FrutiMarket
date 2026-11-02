import { DecimalPipe } from '@angular/common';
import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { mensajeDeError } from '../../core/errores';
import { ProductoVendido } from '../../core/modelos';
import { ReportesService } from '../../core/reportes.service';

/** Ranking de productos mas vendidos (solo ADMIN). */
@Component({
  selector: 'app-reportes',
  imports: [DecimalPipe],
  templateUrl: './reportes.html',
  styleUrl: './reportes.css',
})
export class Reportes implements OnInit {
  private readonly reportes = inject(ReportesService);

  protected readonly ranking = signal<ProductoVendido[]>([]);
  protected readonly cargando = signal(true);
  protected readonly error = signal<string | null>(null);
  /** El mas vendido marca el 100 % de la barra. */
  protected readonly maximo = computed(() => Math.max(1, ...this.ranking().map((p) => p.cantidadVendida)));

  ngOnInit(): void {
    this.reportes.productosMasVendidos().subscribe({
      next: (ranking) => {
        this.ranking.set(ranking);
        this.cargando.set(false);
      },
      error: (e) => {
        this.error.set(mensajeDeError(e));
        this.cargando.set(false);
      },
    });
  }
}
