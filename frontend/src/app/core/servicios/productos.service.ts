import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Producto } from '../modelos';

/** Endpoints de /api/productos. Los componentes nunca escriben la URL: se la piden a este servicio. */
@Injectable({ providedIn: 'root' })
export class ProductosService {
  private readonly http = inject(HttpClient);
  private readonly url = `${environment.apiUrl}/productos`;

  productos(): Observable<Producto[]> {
    return this.http.get<Producto[]>(this.url);
  }
}
