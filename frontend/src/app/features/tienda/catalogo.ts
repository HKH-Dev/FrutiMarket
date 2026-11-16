import { Component, computed, inject, signal } from '@angular/core';
import { toObservable, toSignal } from '@angular/core/rxjs-interop';
import { catchError, debounceTime, distinctUntilChanged, map, of, startWith, switchMap } from 'rxjs';
import { mensajeDeError } from '../../core/errores';
import { LoteResumen, Pagina } from '../../core/modelos';
import { LotesService } from '../../core/servicios/lotes.service';
import { EtiquetaLote } from '../../shared/etiqueta-lote';

const TAMANO = 3;
const ESPERA_MS = 350;

interface Estado {
  cargando: boolean;
  pagina?: Pagina<LoteResumen>;
  error?: string;
}

/**
 * Catalogo reactivo: cada filtro es una senal, y la busqueda sale sola mientras la persona escribe,
 * con una pequena espera (debounce) para no pedir en cada tecla. La pagina la corta el backend.
 */
@Component({
  selector: 'app-catalogo',
  imports: [EtiquetaLote],
  template: `
    <div class="container tienda-contenido">
      <h1>Catálogo</h1>
      <div class="row g-3 align-items-end" role="search">
        <div class="col-12 col-md-6">
          <label class="form-label" for="buscar">Buscar</label>
          <input id="buscar" class="form-control" type="search" placeholder="Mango, mora, café" [value]="texto()" (input)="cambiarTexto($event)" />
        </div>
        <div class="col-6 col-md-3">
          <label class="form-label" for="tipo">Tipo</label>
          <select id="tipo" class="form-select" (change)="cambiarTipo($event)">
            <option value="">Todos</option><option value="FRUTA">Fruta</option><option value="DERIVADO_PROCESADO">Procesado</option>
          </select>
        </div>
        <div class="col-6 col-md-3">
          <label class="form-label" for="precio">Precio máximo</label>
          <input id="precio" class="form-control" type="number" min="1" (input)="cambiarPrecio($event)" />
        </div>
      </div>
      @if (estado().error; as error) {
        <div class="alert alert-danger" role="alert">{{ error }}</div>
      } @else if (estado().pagina; as p) {
        @if (p.contenido.length) {
          <p class="sutil" aria-live="polite">{{ p.totalElementos }} lotes disponibles</p>
          <div class="row row-cols-1 row-cols-sm-2 row-cols-lg-3 g-4" [class.opacity-50]="estado().cargando">
            @for (l of p.contenido; track l.id) { <div class="col"><app-etiqueta-lote [lote]="l" /></div> }
          </div>
          <nav class="paginacion" aria-label="Páginas del catálogo">
            <button class="btn btn-outline-primary btn-sm" [disabled]="pagina() === 0" (click)="pagina.set(pagina() - 1)">Anterior</button>
            <span>Página {{ pagina() + 1 }} de {{ p.totalPaginas }}</span>
            <button class="btn btn-outline-primary btn-sm" [disabled]="pagina() + 1 >= p.totalPaginas" (click)="pagina.set(pagina() + 1)">Siguiente</button>
          </nav>
        } @else {
          <div class="vacio"><h2>No encontramos lotes con esa búsqueda</h2><p class="sutil">Prueba con otra palabra o quita algún filtro.</p></div>
        }
      } @else {
        <p class="sutil">Cargando lotes…</p>
      }
    </div>
  `,
})
export class Catalogo {
  private readonly lotes = inject(LotesService);

  protected readonly texto = signal('');
  protected readonly tipoCultivo = signal('');
  protected readonly precioMaximo = signal<number | null>(null);
  protected readonly pagina = signal(0);

  /** Los filtros combinados en una sola senal: cuando cualquiera cambia, se pide de nuevo. */
  private readonly filtros = computed(() => ({
    texto: this.texto().trim(),
    tipoCultivo: this.tipoCultivo(),
    precioMaximo: this.precioMaximo(),
    pagina: this.pagina(),
    tamano: TAMANO,
  }));

  protected readonly estado = toSignal(
    toObservable(this.filtros).pipe(
      debounceTime(ESPERA_MS),
      distinctUntilChanged((a, b) => JSON.stringify(a) === JSON.stringify(b)),
      // switchMap cancela la peticion anterior si la persona sigue escribiendo
      switchMap((filtros) => this.lotes.buscarLotes(filtros).pipe(
        map((pagina): Estado => ({ cargando: false, pagina })),
        catchError((e) => of<Estado>({ cargando: false, error: mensajeDeError(e) })),
        startWith<Estado>({ cargando: true, pagina: this.ultima }),
      )),
    ),
    { initialValue: { cargando: true } as Estado },
  );

  private get ultima(): Pagina<LoteResumen> | undefined {
    return this.estado?.()?.pagina;
  }

  protected cambiarTexto(evento: Event): void {
    this.texto.set((evento.target as HTMLInputElement).value);
    this.pagina.set(0);
  }

  protected cambiarTipo(evento: Event): void {
    this.tipoCultivo.set((evento.target as HTMLSelectElement).value);
    this.pagina.set(0);
  }

  protected cambiarPrecio(evento: Event): void {
    const valor = Number((evento.target as HTMLInputElement).value);
    this.precioMaximo.set(valor > 0 ? valor : null);
    this.pagina.set(0);
  }
}
