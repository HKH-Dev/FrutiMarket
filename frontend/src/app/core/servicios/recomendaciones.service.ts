import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Sugerencia } from '../modelos';

/** Endpoint de /api/recomendaciones. */
@Injectable({ providedIn: 'root' })
export class RecomendacionesService {
  private readonly http = inject(HttpClient);

  recomendaciones(limite = 6): Observable<Sugerencia[]> {
    return this.http.get<Sugerencia[]>(`${environment.apiUrl}/recomendaciones`, { params: new HttpParams().set('limite', limite) });
  }
}
