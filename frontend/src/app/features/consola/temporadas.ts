import { Component, OnInit, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ApiService } from '../../core/api.service';
import { mensajeDeError } from '../../core/errores';
import { Oferta, Producto } from '../../core/modelos';
import { TiendaService } from '../../core/tienda.service';

@Component({
  selector: 'app-temporadas',
  imports: [ReactiveFormsModule],
  host: { style: 'display: grid; gap: 1.5rem' },
  template: `
    <h1>Descuentos de temporada</h1>
    <form class="lienzo" [formGroup]="f" (ngSubmit)="crear()">
      <div class="rejilla-form">
        <label class="campo">Nombre <input formControlName="nombre" placeholder="Cosecha de mango" /></label>
        <label class="campo">Producto
          <select formControlName="productoId"><option value="">Todo el catálogo</option>@for (p of productos(); track p.id) { <option [value]="p.id">{{ p.nombre }}</option> }</select>
        </label>
        <label class="campo">Descuento (%) <input type="number" min="1" max="70" formControlName="porcentaje" /></label>
        <label class="campo">Desde <input type="date" formControlName="inicio" /></label>
        <label class="campo">Hasta <input type="date" formControlName="fin" /></label>
      </div>
      @if (error()) { <p class="aviso aviso-error" role="alert">{{ error() }}</p> }
      <div><button class="boton boton-verde" type="submit">Crear campaña</button></div>
    </form>
    <div class="tabla-marco"><table>
      <thead><tr><th>Campaña</th><th>Producto</th><th class="num">Descuento</th><th>Vigencia</th><th>Estado</th><th></th></tr></thead>
      <tbody>@for (o of ofertas(); track o.id) {
        <tr>
          <td>{{ o.nombre }}</td><td>{{ nombre(o.productoId) }}</td><td class="num">{{ o.porcentaje }}%</td><td>{{ o.inicio }} a {{ o.fin }}</td>
          <td><span class="chip" [class.chip-rojo]="!o.activo">{{ o.activo ? 'Activa' : 'Terminada' }}</span></td>
          <td>@if (o.activo) { <button class="boton boton-peligro boton-chico" (click)="terminar(o)">Terminar</button> }</td>
        </tr>
      } @empty { <tr><td colspan="6" class="sutil">Aún no hay campañas. Crea la primera con el formulario.</td></tr> }</tbody>
    </table></div>
  `,
})
export class Temporadas implements OnInit {
  private readonly api = inject(ApiService);
  private readonly tienda = inject(TiendaService);
  protected readonly productos = signal<Producto[]>([]);
  protected readonly ofertas = signal<Oferta[]>([]);
  protected readonly error = signal<string | null>(null);
  protected readonly f = inject(FormBuilder).nonNullable.group({
    nombre: ['', Validators.required], productoId: [''], porcentaje: [10, Validators.required], inicio: ['', Validators.required], fin: ['', Validators.required],
  });

  ngOnInit(): void {
    this.api.productos().subscribe((p) => this.productos.set(p));
    this.recargar();
  }

  protected nombre(id: string | null): string {
    return id === null ? 'Todo el catálogo' : (this.productos().find((p) => p.id === id)?.nombre ?? '');
  }

  protected crear(): void {
    if (this.f.invalid) {
      this.error.set('Completa nombre, descuento y fechas.');
      return;
    }
    const v = this.f.getRawValue();
    this.error.set(null);
    this.api.crearOferta({ ...v, productoId: v.productoId || null }).subscribe({
      next: () => { this.f.reset(); this.recargar(); },
      error: (e) => this.error.set(mensajeDeError(e)),
    });
  }

  protected terminar(o: Oferta): void {
    this.api.terminarOferta(o.id).subscribe({ next: () => this.recargar(), error: (e) => this.error.set(mensajeDeError(e)) });
  }

  private recargar(): void {
    this.api.ofertas().subscribe((o) => this.ofertas.set(o));
    this.tienda.cargar();
  }
}
