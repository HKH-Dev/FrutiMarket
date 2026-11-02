import { DatePipe } from '@angular/common';
import { Component, inject, input, signal, OnInit } from '@angular/core';
import { RouterLink } from '@angular/router';
import { CatalogoService } from '../../core/catalogo.service';
import { mensajeDeError } from '../../core/errores';
import { Trazabilidad } from '../../core/modelos';

/** Trazabilidad de un lote: origen, calidad y cadena de custodia (regla 8). */
@Component({
  selector: 'app-lote-detalle',
  imports: [RouterLink, DatePipe],
  templateUrl: './lote-detalle.html',
  styleUrl: './lote-detalle.css',
})
export class LoteDetalle implements OnInit {
  private readonly catalogo = inject(CatalogoService);

  /** Viene del parametro :id de la ruta (withComponentInputBinding en app.config). */
  readonly id = input.required<string>();

  protected readonly lote = signal<Trazabilidad | null>(null);
  protected readonly error = signal<string | null>(null);

  ngOnInit(): void {
    this.catalogo.trazabilidad(this.id()).subscribe({
      next: (lote) => this.lote.set(lote),
      error: (e) => this.error.set(mensajeDeError(e)),
    });
  }
}
