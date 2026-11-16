import { Component, OnInit, inject, signal } from '@angular/core';
import { ReportesService } from '../../core/servicios/reportes.service';
import { AvisosService } from '../../core/avisos.service';
import { mensajeDeError } from '../../core/errores';
import { CampesinoAdmin } from '../../core/modelos';

@Component({
  selector: 'app-campesinos',
  host: { style: 'display: grid; gap: 1.5rem' },
  template: `
    <h1>Campesinos</h1>
    @if (error()) { <p class="alert alert-danger" role="alert">{{ error() }}</p> }
    <div class="tabla-marco"><table class="table table-hover align-middle mb-0">
      <thead><tr><th>Nombre</th><th>Identificación</th><th>Estado</th><th></th></tr></thead>
      <tbody>@for (c of campesinos(); track c.id) {
        <tr>
          <td>{{ c.nombre }}</td><td>{{ c.numeroIdentificacion }}</td>
          <td><span class="badge chip" [class.chip-naranja]="!c.autorizado">{{ c.autorizado ? 'Autorizado' : 'Por revisar' }}</span></td>
          <td>@if (!c.autorizado) { <button class="btn btn-primary btn-sm" (click)="autorizar(c)">Autorizar</button> }</td>
        </tr>
      }</tbody>
    </table></div>
  `,
})
export class Campesinos implements OnInit {
  private readonly avisos = inject(AvisosService);
  private readonly reportesApi = inject(ReportesService);
  protected readonly campesinos = signal<CampesinoAdmin[]>([]);
  protected readonly error = signal<string | null>(null);

  ngOnInit(): void {
    this.reportesApi.campesinos().subscribe((c) => this.campesinos.set(c));
  }

  protected autorizar(c: CampesinoAdmin): void {
    this.reportesApi.autorizarCampesino(c.id).subscribe({
      next: () => {
        this.campesinos.update((l) => l.map((x) => (x.id === c.id ? { ...x, autorizado: true } : x)));
        this.avisos.exito(`${c.nombre} ya puede publicar lotes.`);
      },
      error: (e) => this.error.set(mensajeDeError(e)),
    });
  }
}
