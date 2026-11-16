import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Compra, Comprobante, ItemCompra, Liquidacion, MetodoPago } from '../modelos';

/** Endpoints de /api/compras y /api/pagos (solo COMPRADOR). */
@Injectable({ providedIn: 'root' })
export class ComprasService {
  private readonly http = inject(HttpClient);
  private readonly compras = `${environment.apiUrl}/compras`;
  private readonly pagos = `${environment.apiUrl}/pagos`;

  cotizar(items: ItemCompra[], cupon: string | null): Observable<Liquidacion> {
    return this.http.post<Liquidacion>(`${this.pagos}/cotizacion`, { items, cupon });
  }
  pagar(items: ItemCompra[], cupon: string | null, metodo: MetodoPago, ultimosDigitos: string | null): Observable<Comprobante> {
    return this.http.post<Comprobante>(`${this.pagos}/checkout`, { items, cupon, metodo, ultimosDigitos });
  }
  misCompras(): Observable<Compra[]> { return this.http.get<Compra[]>(`${this.compras}/mias`); }
  reembolsar(id: string): Observable<Compra> { return this.http.post<Compra>(`${this.compras}/${id}/reembolso`, null); }
}
