import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { ProductoVendido } from './modelos';

/** Reportes del administrador. La ruta esta protegida: el interceptor agrega el token. */
@Injectable({ providedIn: 'root' })
export class ReportesService {
  private readonly http = inject(HttpClient);

  productosMasVendidos(limite = 10): Observable<ProductoVendido[]> {
    return this.http.get<ProductoVendido[]>('/api/reportes/productos-mas-vendidos', {
      params: { limite },
    });
  }
}
