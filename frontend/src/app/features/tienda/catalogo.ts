import { Component, OnInit, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { ApiService } from '../../core/api.service';
import { mensajeDeError } from '../../core/errores';
import { LoteResumen } from '../../core/modelos';
import { EtiquetaLote } from '../../shared/etiqueta-lote';

const TAMANO = 9;

@Component({
  selector: 'app-catalogo',
  imports: [ReactiveFormsModule, EtiquetaLote],
  template: `
    <div class="ancho tienda-contenido">
      <h1>Catálogo</h1>
      <form class="filtros" [formGroup]="filtros" (ngSubmit)="buscar(0)">
        <label class="campo">Buscar <input formControlName="texto" type="search" placeholder="Mango, mora, café" /></label>
        <label class="campo">Tipo
          <select formControlName="tipoCultivo">
            <option value="">Todos</option><option value="FRUTA">Fruta</option><option value="DERIVADO_PROCESADO">Procesado</option>
          </select>
        </label>
        <label class="campo">Precio máximo <input formControlName="precioMaximo" type="number" min="1" /></label>
        <button class="boton boton-verde" type="submit">Buscar</button>
      </form>
      @if (error()) {
        <p class="aviso aviso-error" role="alert">{{ error() }}</p>
      } @else if (cargando()) {
        <p class="sutil">Cargando lotes…</p>
      } @else if (lotes().length) {
        <p class="sutil" aria-live="polite">{{ total() }} lotes disponibles</p>
        <div class="rejilla-lotes">
          @for (l of lotes(); track l.id) { <app-etiqueta-lote [lote]="l" /> }
        </div>
        @if (paginas() > 1) {
          <nav class="paginacion" aria-label="Páginas">
            <button class="boton boton-linea boton-chico" [disabled]="pagina() === 0" (click)="buscar(pagina() - 1)">Anterior</button>
            <span>Página {{ pagina() + 1 }} de {{ paginas() }}</span>
            <button class="boton boton-linea boton-chico" [disabled]="pagina() + 1 >= paginas()" (click)="buscar(pagina() + 1)">Siguiente</button>
          </nav>
        }
      } @else {
        <div class="vacio"><h2>No hay lotes con esos filtros</h2><p class="sutil">Quita algún filtro o busca otro producto.</p></div>
      }
    </div>
  `,
})
export class Catalogo implements OnInit {
  private readonly api = inject(ApiService);
  protected readonly filtros = inject(FormBuilder).nonNullable.group({ texto: '', tipoCultivo: '', precioMaximo: null as number | null });
  protected readonly lotes = signal<LoteResumen[]>([]);
  protected readonly total = signal(0);
  protected readonly paginas = signal(0);
  protected readonly pagina = signal(0);
  protected readonly cargando = signal(true);
  protected readonly error = signal<string | null>(null);

  ngOnInit(): void {
    this.buscar(0);
  }

  protected buscar(pagina: number): void {
    this.cargando.set(true);
    this.error.set(null);
    this.api.buscarLotes({ ...this.filtros.getRawValue(), pagina, tamano: TAMANO }).subscribe({
      next: (r) => {
        this.lotes.set(r.contenido);
        this.total.set(r.totalElementos);
        this.paginas.set(r.totalPaginas);
        this.pagina.set(pagina);
        this.cargando.set(false);
      },
      error: (e) => {
        this.error.set(mensajeDeError(e));
        this.cargando.set(false);
      },
    });
  }
}
