import { Component, OnInit, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ApiService } from '../../core/api.service';
import { mensajeDeError } from '../../core/errores';
import { CompradorAdmin, Cupon } from '../../core/modelos';

@Component({
  selector: 'app-cupones-consola',
  imports: [ReactiveFormsModule],
  host: { style: 'display: grid; gap: 1.5rem' },
  template: `
    <h1>Cupones personalizados</h1>
    <form class="lienzo" [formGroup]="f" (ngSubmit)="crear()">
      <div class="rejilla-form">
        <label class="campo">Código <input formControlName="codigo" placeholder="CLIENTE-FIEL" /></label>
        <label class="campo">Comprador
          <select formControlName="compradorId">@for (c of compradores(); track c.id) { <option [value]="c.id">{{ c.nombre }}</option> }</select>
        </label>
        <label class="campo">Descuento (%) <input type="number" min="1" max="50" formControlName="porcentaje" /></label>
        <label class="campo">Vence el <input type="date" formControlName="venceEl" /></label>
      </div>
      @if (error()) { <p class="aviso aviso-error" role="alert">{{ error() }}</p> }
      <div><button class="boton boton-verde" type="submit">Crear cupón</button></div>
    </form>
    <div class="tabla-marco"><table>
      <thead><tr><th>Código</th><th>Comprador</th><th class="num">Descuento</th><th>Vence</th><th>Estado</th></tr></thead>
      <tbody>@for (c of cupones(); track c.id) {
        <tr><td><strong>{{ c.codigo }}</strong></td><td>{{ nombre(c.compradorId) }}</td><td class="num">{{ c.porcentaje }}%</td><td>{{ c.venceEl }}</td>
          <td><span class="chip" [class.chip-naranja]="c.usadoEn">{{ c.usadoEn ? 'Usado' : 'Disponible' }}</span></td></tr>
      } @empty { <tr><td colspan="5" class="sutil">Aún no hay cupones.</td></tr> }</tbody>
    </table></div>
  `,
})
export class CuponesConsola implements OnInit {
  private readonly api = inject(ApiService);
  protected readonly compradores = signal<CompradorAdmin[]>([]);
  protected readonly cupones = signal<Cupon[]>([]);
  protected readonly error = signal<string | null>(null);
  protected readonly f = inject(FormBuilder).nonNullable.group({
    codigo: ['', Validators.required], compradorId: ['', Validators.required], porcentaje: [10, Validators.required], venceEl: ['', Validators.required],
  });

  ngOnInit(): void {
    this.api.compradores().subscribe((c) => {
      this.compradores.set(c);
      if (c.length) {
        this.f.controls.compradorId.setValue(c[0].id);
      }
    });
    this.recargar();
  }

  protected nombre(id: string): string {
    return this.compradores().find((c) => c.id === id)?.nombre ?? '';
  }

  protected crear(): void {
    if (this.f.invalid) {
      this.error.set('Completa código, comprador, descuento y fecha.');
      return;
    }
    this.error.set(null);
    this.api.crearCupon(this.f.getRawValue()).subscribe({
      next: () => { this.f.controls.codigo.reset(); this.recargar(); },
      error: (e) => this.error.set(mensajeDeError(e)),
    });
  }

  private recargar(): void {
    this.api.cupones().subscribe((c) => this.cupones.set(c));
  }
}
