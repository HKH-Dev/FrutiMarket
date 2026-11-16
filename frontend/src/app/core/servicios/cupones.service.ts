import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Cupon } from '../modelos';

/** Endpoints de /api/cupones. */
@Injectable({ providedIn: 'root' })
export class CuponesService {
  private readonly http = inject(HttpClient);
  private readonly url = `${environment.apiUrl}/cupones`;

  misCupones(): Observable<Cupon[]> { return this.http.get<Cupon[]>(`${this.url}/mios`); }
  cupones(): Observable<Cupon[]> { return this.http.get<Cupon[]>(this.url); }
  crearCupon(cupon: { codigo: string; compradorId: string; porcentaje: number; venceEl: string }): Observable<Cupon> {
    return this.http.post<Cupon>(this.url, cupon);
  }
}
