import { Component, OnInit, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { ApiService } from '../../core/api.service';
import { mensajeDeError } from '../../core/errores';
import { Producto } from '../../core/modelos';

function hoy(): string {
  return new Date().toLocaleDateString('en-CA');
}

/** crypto.randomUUID solo existe en https o localhost. */
function nuevoId(): string {
  return 'xxxxxxxx-xxxx-4xxx-8xxx-xxxxxxxxxxxx'.replace(/x/g, () => Math.floor(Math.random() * 16).toString(16));
}

@Component({
  selector: 'app-nuevo-lote',
  imports: [ReactiveFormsModule, RouterLink],
  host: { style: 'display: grid; gap: 1.5rem; max-width: 46rem' },
  template: `
    <a routerLink="/finca/lotes">Volver a mis lotes</a>
    <h1>Registrar lote</h1>
    <form class="lienzo" [formGroup]="f" (ngSubmit)="guardar()">
      <div class="rejilla-form">
        <label class="campo">Producto
          <select formControlName="productoId">@for (p of productos(); track p.id) { <option [value]="p.id">{{ p.nombre }}</option> }</select>
        </label>
        <label class="campo">Código del lote <input formControlName="codigo" placeholder="MANGO-2026-01" /></label>
        <label class="campo">Fecha de cosecha <input type="date" formControlName="fechaCosecha" [max]="maximo" /></label>
        <label class="campo">Cantidad <input type="number" min="1" formControlName="cantidad" /></label>
        <label class="campo">Precio por unidad <input type="number" min="1" formControlName="precioPorUnidad" /></label>
        <label class="campo">Técnica
          <select formControlName="tecnicaProduccion"><option value="TRADICIONAL">Tradicional</option><option value="AGROECOLOGICA">Agroecológica</option><option value="ORGANICA_CERTIFICADA">Orgánica certificada</option></select>
        </label>
        <label class="campo">Categoría
          <select formControlName="categoriaCalidad"><option value="EXTRA">Extra</option><option value="PRIMERA">Primera</option><option value="SEGUNDA">Segunda</option><option value="INDUSTRIAL">Industrial</option></select>
        </label>
        <label class="campo">Defectos (%) <input type="number" min="0" max="100" formControlName="porcentajeDefectos" /></label>
      </div>
      <label class="casilla"><input type="checkbox" formControlName="certificadoOrganico" /> Tiene certificado orgánico</label>
      @if (error()) { <p class="aviso aviso-error" role="alert">{{ error() }}</p> }
      <div><button class="boton" type="submit" [disabled]="enviando()">Registrar lote</button></div>
    </form>
  `,
})
export class NuevoLoteFinca implements OnInit {
  private readonly api = inject(ApiService);
  private readonly router = inject(Router);
  protected readonly maximo = hoy();
  protected readonly productos = signal<Producto[]>([]);
  protected readonly enviando = signal(false);
  protected readonly error = signal<string | null>(null);
  protected readonly f = inject(FormBuilder).nonNullable.group({
    productoId: ['', Validators.required],
    codigo: ['', Validators.required],
    fechaCosecha: [hoy(), Validators.required],
    cantidad: [100, [Validators.required, Validators.min(1)]],
    precioPorUnidad: [3000, [Validators.required, Validators.min(1)]],
    tecnicaProduccion: ['TRADICIONAL'],
    categoriaCalidad: ['PRIMERA'],
    porcentajeDefectos: [5, [Validators.min(0), Validators.max(100)]],
    certificadoOrganico: [false],
  });

  ngOnInit(): void {
    this.api.productos().subscribe((p) => {
      this.productos.set(p);
      if (p.length) {
        this.f.controls.productoId.setValue(p[0].id);
      }
    });
  }

  protected guardar(): void {
    if (this.f.invalid) {
      this.error.set('Revisa los campos: código, cantidad y precio son obligatorios.');
      return;
    }
    const v = this.f.getRawValue();
    const unidad = this.productos().find((p) => p.id === v.productoId)?.unidadVenta ?? 'KILOGRAMO';
    this.enviando.set(true);
    this.error.set(null);
    this.api.registrarLote({ ...v, cantidad: String(v.cantidad), unidadMedida: unidad, fincaOrigenId: nuevoId() }).subscribe({
      next: () => this.router.navigateByUrl('/finca/lotes'),
      error: (e) => { this.error.set(mensajeDeError(e)); this.enviando.set(false); },
    });
  }
}
