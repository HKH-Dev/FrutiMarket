import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Oferta } from '../modelos';

/** Endpoints de /api/ofertas: descuentos por temporada. */
@Injectable({ providedIn: 'root' })
export class OfertasService {
  private readonly http = inject(HttpClient);
  private readonly url = `${environment.apiUrl}/ofertas`;

  ofertasVigentes(): Observable<Oferta[]> { return this.http.get<Oferta[]>(`${this.url}/vigentes`); }
  ofertas(): Observable<Oferta[]> { return this.http.get<Oferta[]>(this.url); }
  crearOferta(oferta: Omit<Oferta, 'id' | 'activo'>): Observable<Oferta> { return this.http.post<Oferta>(this.url, oferta); }
  terminarOferta(id: string): Observable<void> { return this.http.post<void>(`${this.url}/${id}/desactivar`, null); }
}
