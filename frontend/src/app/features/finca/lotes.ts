import { CurrencyPipe } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { Observable } from 'rxjs';
import { ApiService } from '../../core/api.service';
import { mensajeDeError } from '../../core/errores';
import { LoteResumen } from '../../core/modelos';

const ESTADOS: Record<string, string> = { REGISTRADO: 'Sin publicar', PUBLICADO: 'A la venta', DESACTIVADO: 'Pausado', RETIRADO: 'Retirado', VENCIDO: 'Vencido', AGOTADO: 'Agotado' };

@Component({
  selector: 'app-lotes-finca',
  imports: [CurrencyPipe, RouterLink],
  host: { style: 'display: grid; gap: 1.5rem' },
  template: `
    <div class="fila-entre"><h1>Mis lotes</h1><a routerLink="/finca/lotes/nuevo" class="boton">Registrar lote</a></div>
    @if (error()) { <p class="aviso aviso-error" role="alert">{{ error() }}</p> }
    @if (lotes().length) {
      <div class="tabla-marco"><table>
        <thead><tr><th>Lote</th><th>Estado</th><th class="num">Disponible</th><th class="num">Precio</th><th>Acciones</th></tr></thead>
        <tbody>@for (l of lotes(); track l.id) {
          <tr>
            <td><a [routerLink]="['/lotes', l.id]">{{ l.producto }}</a><div class="sutil">{{ l.codigo }}</div></td>
            <td><span class="chip" [class.chip-naranja]="l.estado !== 'PUBLICADO'">{{ estado(l.estado) }}</span></td>
            <td class="num">{{ l.cantidadDisponible }} {{ l.unidad }}</td>
            <td class="num">{{ l.precioPorUnidad | currency: 'COP' : 'symbol-narrow' : '1.0-0' }}</td>
            <td><div class="fila">
              @if (l.estado === 'REGISTRADO') { <button class="boton boton-verde boton-chico" (click)="ejecutar(api.publicarLote(l.id))">Publicar</button> }
              @if (l.estado === 'PUBLICADO') { <button class="boton boton-linea boton-chico" (click)="pausar(l)">Pausar</button> }
              @if (l.estado === 'DESACTIVADO') { <button class="boton boton-verde boton-chico" (click)="ejecutar(api.reactivarLote(l.id))">Reactivar</button> }
              @if (l.estado !== 'RETIRADO') { <button class="boton boton-peligro boton-chico" (click)="retirar(l)">Retirar</button> }
            </div></td>
          </tr>
        }</tbody>
      </table></div>
    } @else if (!cargando()) {
      <div class="vacio"><h2>Aún no has registrado lotes</h2><a routerLink="/finca/lotes/nuevo" class="boton">Registrar mi primer lote</a></div>
    }
  `,
})
export class LotesFinca implements OnInit {
  protected readonly api = inject(ApiService);
  protected readonly lotes = signal<LoteResumen[]>([]);
  protected readonly cargando = signal(true);
  protected readonly error = signal<string | null>(null);

  ngOnInit(): void {
    this.recargar();
  }

  protected estado(codigo: string): string {
    return ESTADOS[codigo] ?? codigo;
  }

  protected ejecutar(accion: Observable<void>): void {
    this.error.set(null);
    accion.subscribe({ next: () => this.recargar(), error: (e) => this.error.set(mensajeDeError(e)) });
  }

  protected pausar(l: LoteResumen): void {
    const motivo = prompt('¿Por qué pausas este lote?');
    if (motivo?.trim()) {
      this.ejecutar(this.api.desactivarLote(l.id, motivo.trim()));
    }
  }

  protected retirar(l: LoteResumen): void {
    if (confirm(`¿Retirar ${l.codigo}? Saldrá del catálogo y no se podrá volver a vender.`)) {
      this.ejecutar(this.api.retirarLote(l.id));
    }
  }

  private recargar(): void {
    this.api.misLotes().subscribe({
      next: (l) => { this.lotes.set(l); this.cargando.set(false); },
      error: (e) => { this.error.set(mensajeDeError(e)); this.cargando.set(false); },
    });
  }
}
