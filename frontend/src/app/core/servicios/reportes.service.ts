import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { CampesinoAdmin, CompradorAdmin, ProductoVendido, ResumenPagos, VentaCampesino } from '../modelos';

/** Endpoints de reportes y paneles: /api/reportes, /api/paneles y /api/campesinos. */
@Injectable({ providedIn: 'root' })
export class ReportesService {
  private readonly http = inject(HttpClient);
  private readonly api = environment.apiUrl;

  resumen(): Observable<ResumenPagos> { return this.http.get<ResumenPagos>(`${this.api}/paneles/admin/resumen`); }
  masVendidos(limite = 10): Observable<ProductoVendido[]> {
    return this.http.get<ProductoVendido[]>(`${this.api}/reportes/productos-mas-vendidos`, { params: new HttpParams().set('limite', limite) });
  }
  campesinos(): Observable<CampesinoAdmin[]> { return this.http.get<CampesinoAdmin[]>(`${this.api}/paneles/admin/campesinos`); }
  compradores(): Observable<CompradorAdmin[]> { return this.http.get<CompradorAdmin[]>(`${this.api}/paneles/admin/compradores`); }
  autorizarCampesino(id: string): Observable<void> { return this.http.post<void>(`${this.api}/campesinos/${id}/autorizacion`, null); }
  misVentas(): Observable<VentaCampesino[]> { return this.http.get<VentaCampesino[]>(`${this.api}/paneles/campesino/ventas`); }
}
