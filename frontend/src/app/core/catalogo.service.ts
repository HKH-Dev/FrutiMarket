import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { LoteResumen, Producto, Trazabilidad } from './modelos';

/** Consultas publicas del catalogo (no requieren token). */
@Injectable({ providedIn: 'root' })
export class CatalogoService {
  private readonly http = inject(HttpClient);

  productos(): Observable<Producto[]> {
    return this.http.get<Producto[]>('/api/productos');
  }

  lotesDisponibles(productoId: string): Observable<LoteResumen[]> {
    return this.http.get<LoteResumen[]>('/api/lotes', { params: { productoId } });
  }

  trazabilidad(loteId: string): Observable<Trazabilidad> {
    return this.http.get<Trazabilidad>(`/api/lotes/${loteId}/trazabilidad`);
  }
}
