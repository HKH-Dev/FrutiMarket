import { Component, OnInit, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { LotesService } from '../../core/servicios/lotes.service';
import { ProductosService } from '../../core/servicios/productos.service';
import { AvisosService } from '../../core/avisos.service';
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
    <form class="card card-body lienzo" [formGroup]="f" (ngSubmit)="guardar()" novalidate>
      <div class="rejilla-form">
        <label class="campo">Producto
          <select class="form-select" formControlName="productoId">@for (p of productos(); track p.id) { <option [value]="p.id">{{ p.nombre }}</option> }</select>
        </label>
        <label class="campo">Código del lote <input class="form-control" formControlName="codigo" placeholder="MANGO-2026-01" [class.is-invalid]="mal('codigo')" /><span class="invalid-feedback">Escribe un código para reconocer el lote.</span></label>
        <label class="campo">Fecha de cosecha <input class="form-control" type="date" formControlName="fechaCosecha" [max]="maximo" [class.is-invalid]="mal('fechaCosecha')" /><span class="invalid-feedback">Elige la fecha de cosecha.</span></label>
        <label class="campo">Cantidad <input class="form-control" type="number" min="1" formControlName="cantidad" [class.is-invalid]="mal('cantidad')" /><span class="invalid-feedback">La cantidad debe ser mayor que cero.</span></label>
        <label class="campo">Precio por unidad <input class="form-control" type="number" min="1" formControlName="precioPorUnidad" [class.is-invalid]="mal('precioPorUnidad')" /><span class="invalid-feedback">El precio debe ser mayor que cero.</span></label>
        <label class="campo">Técnica
          <select class="form-select" formControlName="tecnicaProduccion"><option value="TRADICIONAL">Tradicional</option><option value="AGROECOLOGICA">Agroecológica</option><option value="ORGANICA_CERTIFICADA">Orgánica certificada</option></select>
        </label>
        <label class="campo">Categoría
          <select class="form-select" formControlName="categoriaCalidad"><option value="EXTRA">Extra</option><option value="PRIMERA">Primera</option><option value="SEGUNDA">Segunda</option><option value="INDUSTRIAL">Industrial</option></select>
        </label>
        <label class="campo">Defectos (%) <input class="form-control" type="number" min="0" max="100" formControlName="porcentajeDefectos" [class.is-invalid]="mal('porcentajeDefectos')" /><span class="invalid-feedback">Debe estar entre 0 y 100.</span></label>
      </div>
      <label class="casilla"><input type="checkbox" formControlName="certificadoOrganico" /> Tiene certificado orgánico</label>
      @if (error()) { <p class="alert alert-danger" role="alert">{{ error() }}</p> }
      <div><button class="btn btn-warning" type="submit" [disabled]="enviando()">Registrar lote</button></div>
    </form>
  `,
})
export class NuevoLoteFinca implements OnInit {
  private readonly avisos = inject(AvisosService);
  private readonly lotesApi = inject(LotesService);
  private readonly productosApi = inject(ProductosService);
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
    this.productosApi.productos().subscribe((p) => {
      this.productos.set(p);
      if (p.length) {
        this.f.controls.productoId.setValue(p[0].id);
      }
    });
  }

  protected mal(campo: keyof typeof this.f.controls): boolean {
    const control = this.f.controls[campo];
    return control.invalid && control.touched;
  }

  protected guardar(): void {
    if (this.f.invalid) {
      this.f.markAllAsTouched();
      return;
    }
    const v = this.f.getRawValue();
    const unidad = this.productos().find((p) => p.id === v.productoId)?.unidadVenta ?? 'KILOGRAMO';
    this.enviando.set(true);
    this.error.set(null);
    this.lotesApi.registrarLote({ ...v, cantidad: String(v.cantidad), unidadMedida: unidad, fincaOrigenId: nuevoId() }).subscribe({
      next: () => {
        this.avisos.exito('Lote registrado. Publícalo cuando esté listo.');
        this.router.navigateByUrl('/finca/lotes');
      },
      error: (e) => { this.error.set(mensajeDeError(e)); this.avisos.error(mensajeDeError(e)); this.enviando.set(false); },
    });
  }
}
