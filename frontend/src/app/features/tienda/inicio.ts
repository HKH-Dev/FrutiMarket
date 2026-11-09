import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { ApiService } from '../../core/api.service';
import { AuthService } from '../../core/auth.service';
import { Sugerencia } from '../../core/modelos';
import { TiendaService } from '../../core/tienda.service';
import { EtiquetaLote } from '../../shared/etiqueta-lote';

@Component({
  selector: 'app-inicio',
  imports: [RouterLink, EtiquetaLote],
  template: `
    <section class="portada">
      <div class="ancho">
        <div>
          <h1>Fruta del Quindío, con el nombre de quien la cosechó.</h1>
          <p>Compra directo a la finca. Cada lote muestra su origen, su calidad y por qué manos pasó antes de llegar a ti.</p>
          <div class="fila">
            <a routerLink="/catalogo" class="boton">Ver lo que hay hoy</a>
            @if (!auth.autenticado()) { <a routerLink="/registro" class="boton boton-claro">Crear cuenta</a> }
          </div>
        </div>
        @if (destacada(); as d) { <app-etiqueta-lote [lote]="d.lote" [motivo]="d.motivo" /> }
      </div>
    </section>
    <div class="ancho tienda-contenido">
      @if (error()) { <p class="aviso aviso-error" role="alert">{{ error() }}</p> }
      @if (tienda.ofertas().length) {
        <section class="seccion" aria-label="Ofertas de temporada">
          <div class="fila">
            @for (o of tienda.ofertas(); track o.id) { <span class="chip chip-amarillo">{{ o.nombre }}: {{ o.porcentaje }}% menos hasta el {{ o.fin }}</span> }
          </div>
        </section>
      }
      <section class="seccion">
        <div class="seccion-cabeza">
          <h2>{{ auth.esComprador() ? 'Elegidos para ti' : 'Lo mejor de esta semana' }}</h2>
          <a routerLink="/catalogo">Ver todo el catálogo</a>
        </div>
        <div class="rejilla-lotes">
          @for (s of resto(); track s.lote.id) { <app-etiqueta-lote [lote]="s.lote" [motivo]="s.motivo" /> }
        </div>
      </section>
    </div>
  `,
})
export class Inicio implements OnInit {
  private readonly api = inject(ApiService);
  protected readonly auth = inject(AuthService);
  protected readonly tienda = inject(TiendaService);

  private readonly sugerencias = signal<Sugerencia[]>([]);
  protected readonly destacada = computed(() => this.sugerencias()[0] ?? null);
  protected readonly resto = computed(() => this.sugerencias().slice(1));
  protected readonly error = signal<string | null>(null);

  ngOnInit(): void {
    this.api.recomendaciones(7).subscribe({
      next: (s) => this.sugerencias.set(s),
      error: () => this.error.set('No se pudo cargar la selección de hoy. Recarga la página para intentarlo de nuevo.'),
    });
  }
}
