import { CurrencyPipe, DecimalPipe } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { CatalogoService } from '../../core/catalogo.service';
import { mensajeDeError } from '../../core/errores';
import { LoteResumen, Producto } from '../../core/modelos';

/** Productos del catalogo y, para el producto elegido, sus lotes disponibles del mejor al peor. */
@Component({
  selector: 'app-catalogo',
  imports: [RouterLink, CurrencyPipe, DecimalPipe],
  templateUrl: './catalogo.html',
  styleUrl: './catalogo.css',
})
export class Catalogo implements OnInit {
  private readonly catalogo = inject(CatalogoService);

  protected readonly productos = signal<Producto[]>([]);
  protected readonly seleccionado = signal<Producto | null>(null);
  protected readonly lotes = signal<LoteResumen[]>([]);
  protected readonly cargando = signal(true);
  protected readonly cargandoLotes = signal(false);
  protected readonly error = signal<string | null>(null);

  ngOnInit(): void {
    this.catalogo.productos().subscribe({
      next: (productos) => {
        this.productos.set(productos);
        this.cargando.set(false);
        if (productos.length > 0) {
          this.elegir(productos[0]);
        }
      },
      error: (e) => {
        this.error.set(mensajeDeError(e));
        this.cargando.set(false);
      },
    });
  }

  protected elegir(producto: Producto): void {
    this.seleccionado.set(producto);
    this.cargandoLotes.set(true);
    this.catalogo.lotesDisponibles(producto.id).subscribe({
      next: (lotes) => {
        this.lotes.set(lotes);
        this.cargandoLotes.set(false);
      },
      error: (e) => {
        this.error.set(mensajeDeError(e));
        this.cargandoLotes.set(false);
      },
    });
  }

  protected etiquetaCalidad(puntaje: number): string {
    return puntaje >= 90 ? 'Excelente' : puntaje >= 75 ? 'Buena' : puntaje >= 55 ? 'Regular' : 'Industrial';
  }
}
