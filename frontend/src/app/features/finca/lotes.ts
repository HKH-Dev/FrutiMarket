import { CurrencyPipe } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { Observable } from 'rxjs';
import { LotesService } from '../../core/servicios/lotes.service';
import { AvisosService } from '../../core/avisos.service';
import { mensajeDeError } from '../../core/errores';
import { LoteResumen } from '../../core/modelos';

const ESTADOS: Record<string, string> = { REGISTRADO: 'Sin publicar', PUBLICADO: 'A la venta', DESACTIVADO: 'Pausado', RETIRADO: 'Retirado', VENCIDO: 'Vencido', AGOTADO: 'Agotado' };

@Component({
  selector: 'app-lotes-finca',
  imports: [CurrencyPipe, RouterLink],
  host: { style: 'display: grid; gap: 1.5rem' },
  template: `
    <div class="fila-entre"><h1>Mis lotes</h1><a routerLink="/finca/lotes/nuevo" class="btn btn-warning">Registrar lote</a></div>
    @if (error()) { <p class="alert alert-danger" role="alert">{{ error() }}</p> }
    @if (lotes().length) {
      <div class="tabla-marco"><table class="table table-hover align-middle mb-0">
        <thead><tr><th>Lote</th><th>Estado</th><th class="num">Disponible</th><th class="num">Precio</th><th>Acciones</th></tr></thead>
        <tbody>@for (l of lotes(); track l.id) {
          <tr>
            <td><a [routerLink]="['/lotes', l.id]">{{ l.producto }}</a><div class="sutil">{{ l.codigo }}</div></td>
            <td><span class="badge chip" [class.chip-naranja]="l.estado !== 'PUBLICADO'">{{ estado(l.estado) }}</span></td>
            <td class="num">{{ l.cantidadDisponible }} {{ l.unidad }}</td>
            <td class="num">{{ l.precioPorUnidad | currency: 'COP' : 'symbol-narrow' : '1.0-0' }}</td>
            <td><div class="fila">
              @if (l.estado === 'REGISTRADO') { <button class="btn btn-primary btn-sm" (click)="ejecutar(lotesApi.publicarLote(l.id), 'Lote publicado en el catálogo.')">Publicar</button> }
              @if (l.estado === 'PUBLICADO') { <button class="btn btn-outline-primary btn-sm" (click)="pausar(l)">Pausar</button> }
              @if (l.estado === 'DESACTIVADO') { <button class="btn btn-primary btn-sm" (click)="ejecutar(lotesApi.reactivarLote(l.id), 'Lote de nuevo a la venta.')">Reactivar</button> }
              @if (l.estado !== 'RETIRADO') { <button class="btn btn-outline-danger btn-sm" (click)="retirar(l)">Retirar</button> }
            </div></td>
          </tr>
        }</tbody>
      </table></div>
    } @else if (!cargando()) {
      <div class="vacio"><h2>Aún no has registrado lotes</h2><a routerLink="/finca/lotes/nuevo" class="btn btn-warning">Registrar mi primer lote</a></div>
    }
  `,
})
export class LotesFinca implements OnInit {
  private readonly avisos = inject(AvisosService);
  protected readonly lotesApi = inject(LotesService);
  protected readonly lotes = signal<LoteResumen[]>([]);
  protected readonly cargando = signal(true);
  protected readonly error = signal<string | null>(null);

  ngOnInit(): void {
    this.recargar();
  }

  protected estado(codigo: string): string {
    return ESTADOS[codigo] ?? codigo;
  }

  protected ejecutar(accion: Observable<void>, exito: string): void {
    this.error.set(null);
    accion.subscribe({
      next: () => { this.avisos.exito(exito); this.recargar(); },
      error: (e) => { this.error.set(mensajeDeError(e)); this.avisos.error(mensajeDeError(e)); },
    });
  }

  protected pausar(l: LoteResumen): void {
    const motivo = prompt('¿Por qué pausas este lote?');
    if (motivo?.trim()) {
      this.ejecutar(this.lotesApi.desactivarLote(l.id, motivo.trim()), 'Lote pausado.');
    }
  }

  protected retirar(l: LoteResumen): void {
    if (confirm(`¿Retirar ${l.codigo}? Saldrá del catálogo y no se podrá volver a vender.`)) {
      this.ejecutar(this.lotesApi.retirarLote(l.id), 'Lote retirado del catálogo.');
    }
  }

  private recargar(): void {
    this.lotesApi.misLotes().subscribe({
      next: (l) => { this.lotes.set(l); this.cargando.set(false); },
      error: (e) => { this.error.set(mensajeDeError(e)); this.cargando.set(false); },
    });
  }
}
