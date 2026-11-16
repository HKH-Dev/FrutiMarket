import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

/** Endpoints de /api/favoritos (solo COMPRADOR). */
@Injectable({ providedIn: 'root' })
export class FavoritosService {
  private readonly http = inject(HttpClient);
  private readonly url = `${environment.apiUrl}/favoritos`;

  favoritos(): Observable<string[]> { return this.http.get<string[]>(this.url); }
  agregarFavorito(productoId: string): Observable<void> { return this.http.put<void>(`${this.url}/${productoId}`, null); }
  quitarFavorito(productoId: string): Observable<void> { return this.http.delete<void>(`${this.url}/${productoId}`); }
}
