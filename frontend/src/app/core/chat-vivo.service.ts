import { Injectable, inject, signal } from '@angular/core';
import { Client, StompSubscription } from '@stomp/stompjs';
import { AuthService } from './auth.service';
import { Mensaje } from './modelos';

/**
 * Conexion STOMP sobre WebSocket. El token viaja en el frame CONNECT, nunca en la URL,
 * y se vuelve a suscribir solo si la conexion se cae y se recupera.
 */
@Injectable({ providedIn: 'root' })
export class ChatVivoService {
  private readonly auth = inject(AuthService);
  private cliente: Client | null = null;
  private suscripcion: StompSubscription | null = null;
  private conversacion: string | null = null;
  private alRecibir: (mensaje: Mensaje) => void = () => undefined;

  readonly conectado = signal(false);

  conectar(alRecibir: (mensaje: Mensaje) => void): void {
    this.alRecibir = alRecibir;
    if (this.cliente) {
      return;
    }
    const protocolo = location.protocol === 'https:' ? 'wss' : 'ws';
    this.cliente = new Client({
      brokerURL: `${protocolo}://${location.host}/ws`,
      reconnectDelay: 3000,
      beforeConnect: async (cliente) => {
        cliente.connectHeaders = { Authorization: `Bearer ${this.auth.token() ?? ''}` };
      },
      onConnect: () => {
        this.conectado.set(true);
        this.suscribir();
      },
      onDisconnect: () => this.conectado.set(false),
      onWebSocketClose: () => this.conectado.set(false),
    });
    this.cliente.activate();
  }

  abrir(conversacion: string): void {
    this.conversacion = conversacion;
    this.suscribir();
  }

  enviar(texto: string): boolean {
    if (!this.cliente?.connected || !this.conversacion) {
      return false;
    }
    this.cliente.publish({ destination: `/app/conversaciones/${this.conversacion}`, body: JSON.stringify({ texto }) });
    return true;
  }

  desconectar(): void {
    this.cliente?.deactivate();
    this.cliente = null;
    this.suscripcion = null;
    this.conversacion = null;
    this.conectado.set(false);
  }

  private suscribir(): void {
    if (!this.cliente?.connected || !this.conversacion) {
      return;
    }
    this.suscripcion?.unsubscribe();
    this.suscripcion = this.cliente.subscribe(`/topic/conversaciones/${this.conversacion}`, (frame) =>
      this.alRecibir(JSON.parse(frame.body) as Mensaje),
    );
  }
}
