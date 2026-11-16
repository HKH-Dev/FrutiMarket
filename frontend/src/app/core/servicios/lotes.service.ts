import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { LoteResumen, NuevoLote, Pagina, Trazabilidad } from '../modelos';

export interface FiltrosCatalogo {
  texto?: string;
  tipoCultivo?: string;
  precioMaximo?: number | null;
  pagina: number;
  tamano: number;
}

/** Endpoints de /api/lotes: catalogo publico y gestion de lotes del campesino. */
@Injectable({ providedIn: 'root' })
export class LotesService {
  private readonly http = inject(HttpClient);
  private readonly url = `${environment.apiUrl}/lotes`;

  /** La paginacion y la busqueda las hace el backend (JPQL): aqui solo se mandan los parametros. */
  buscarLotes(filtros: FiltrosCatalogo): Observable<Pagina<LoteResumen>> {
    let params = new HttpParams().set('pagina', filtros.pagina).set('tamano', filtros.tamano);
    if (filtros.texto) params = params.set('texto', filtros.texto);
    if (filtros.tipoCultivo) params = params.set('tipoCultivo', filtros.tipoCultivo);
    if (filtros.precioMaximo) params = params.set('precioMaximo', filtros.precioMaximo);
    return this.http.get<Pagina<LoteResumen>>(`${this.url}/buscar`, { params });
  }
  lote(id: string): Observable<LoteResumen> { return this.http.get<LoteResumen>(`${this.url}/${id}`); }
  mejorLote(productoId: string): Observable<LoteResumen> { return this.http.get<LoteResumen>(`${this.url}/mejor`, { params: { productoId } }); }
  trazabilidad(id: string): Observable<Trazabilidad> { return this.http.get<Trazabilidad>(`${this.url}/${id}/trazabilidad`); }

  misLotes(): Observable<LoteResumen[]> { return this.http.get<LoteResumen[]>(`${this.url}/mios`); }
  registrarLote(lote: NuevoLote): Observable<{ id: string }> { return this.http.post<{ id: string }>(this.url, lote); }
  publicarLote(id: string): Observable<void> { return this.http.post<void>(`${this.url}/${id}/publicar`, null); }
  desactivarLote(id: string, motivo: string): Observable<void> { return this.http.post<void>(`${this.url}/${id}/desactivar`, { motivo }); }
  reactivarLote(id: string): Observable<void> { return this.http.post<void>(`${this.url}/${id}/reactivar`, null); }
  retirarLote(id: string): Observable<void> { return this.http.delete<void>(`${this.url}/${id}`); }
}
