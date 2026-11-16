import { Component, OnInit, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { catchError, forkJoin, of } from 'rxjs';
import { FavoritosService } from '../../core/servicios/favoritos.service';
import { LotesService } from '../../core/servicios/lotes.service';
import { LoteResumen } from '../../core/modelos';
import { TiendaService } from '../../core/tienda.service';
import { EtiquetaLote } from '../../shared/etiqueta-lote';

/** Los favoritos son productos; se muestra el mejor lote disponible de cada uno. */
@Component({
  selector: 'app-favoritos',
  imports: [RouterLink, EtiquetaLote],
  template: `
    <div class="container tienda-contenido">
      <h1>Favoritos</h1>
      @if (lotes().length) {
        <div class="row row-cols-1 row-cols-sm-2 row-cols-lg-3 g-4">
          @for (l of visibles(); track l.id) { <app-etiqueta-lote [lote]="l" /> }
        </div>
      } @else if (!cargando()) {
        <div class="vacio"><h2>Aún no tienes favoritos</h2><p class="sutil">Toca el corazón de un lote para guardarlo aquí.</p><a routerLink="/catalogo" class="btn btn-warning">Ver catálogo</a></div>
      }
    </div>
  `,
})
export class Favoritos implements OnInit {
  private readonly favoritosApi = inject(FavoritosService);
  private readonly lotesApi = inject(LotesService);
  private readonly tienda = inject(TiendaService);
  protected readonly lotes = signal<LoteResumen[]>([]);
  protected readonly cargando = signal(true);

  protected visibles(): LoteResumen[] {
    return this.lotes().filter((l) => this.tienda.favoritos().has(l.productoId));
  }

  ngOnInit(): void {
    this.favoritosApi.favoritos().subscribe((ids) => {
      if (!ids.length) {
        this.cargando.set(false);
        return;
      }
      forkJoin(ids.map((id) => this.lotesApi.mejorLote(id).pipe(catchError(() => of(null))))).subscribe((r) => {
        this.lotes.set(r.filter((l): l is LoteResumen => l !== null));
        this.cargando.set(false);
      });
    });
  }
}
