import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Conversacion, Mensaje } from '../modelos';

/** Endpoints REST del chat (/api/chat). Lo que llega en vivo va por ChatVivoService (WebSocket). */
@Injectable({ providedIn: 'root' })
export class ChatService {
  private readonly http = inject(HttpClient);
  private readonly url = `${environment.apiUrl}/chat/conversaciones`;

  iniciarConversacion(loteId: string): Observable<Conversacion> { return this.http.post<Conversacion>(this.url, { loteId }); }
  conversaciones(): Observable<Conversacion[]> { return this.http.get<Conversacion[]>(this.url); }
  mensajes(id: string): Observable<Mensaje[]> { return this.http.get<Mensaje[]>(`${this.url}/${id}/mensajes`); }
}
