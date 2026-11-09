import { Component, OnInit, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { catchError, forkJoin, of } from 'rxjs';
import { ApiService } from '../../core/api.service';
import { LoteResumen } from '../../core/modelos';
import { TiendaService } from '../../core/tienda.service';
import { EtiquetaLote } from '../../shared/etiqueta-lote';

/** Los favoritos son productos; se muestra el mejor lote disponible de cada uno. */
@Component({
  selector: 'app-favoritos',
  imports: [RouterLink, EtiquetaLote],
  template: `
    <div class="ancho tienda-contenido">
      <h1>Favoritos</h1>
      @if (lotes().length) {
        <div class="rejilla-lotes">
          @for (l of visibles(); track l.id) { <app-etiqueta-lote [lote]="l" /> }
        </div>
      } @else if (!cargando()) {
        <div class="vacio"><h2>Aún no tienes favoritos</h2><p class="sutil">Toca el corazón de un lote para guardarlo aquí.</p><a routerLink="/catalogo" class="boton">Ver catálogo</a></div>
      }
    </div>
  `,
})
export class Favoritos implements OnInit {
  private readonly api = inject(ApiService);
  private readonly tienda = inject(TiendaService);
  protected readonly lotes = signal<LoteResumen[]>([]);
  protected readonly cargando = signal(true);

  protected visibles(): LoteResumen[] {
    return this.lotes().filter((l) => this.tienda.favoritos().has(l.productoId));
  }

  ngOnInit(): void {
    this.api.favoritos().subscribe((ids) => {
      if (!ids.length) {
        this.cargando.set(false);
        return;
      }
      forkJoin(ids.map((id) => this.api.mejorLote(id).pipe(catchError(() => of(null))))).subscribe((r) => {
        this.lotes.set(r.filter((l): l is LoteResumen => l !== null));
        this.cargando.set(false);
      });
    });
  }
}
