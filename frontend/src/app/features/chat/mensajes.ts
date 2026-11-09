import { DatePipe } from '@angular/common';
import { Component, ElementRef, OnDestroy, OnInit, computed, inject, input, signal, viewChild } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ApiService } from '../../core/api.service';
import { AuthService } from '../../core/auth.service';
import { ChatVivoService } from '../../core/chat-vivo.service';
import { mensajeDeError } from '../../core/errores';
import { Conversacion, Mensaje } from '../../core/modelos';

/** Chat en vivo entre comprador y vendedor. El historial llega por REST y lo nuevo por WebSocket. */
@Component({
  selector: 'app-mensajes',
  imports: [DatePipe, FormsModule],
  host: { '[class.ancho]': 'esTienda()', '[class.tienda-contenido]': 'esTienda()', style: 'display: grid; gap: 1rem' },
  template: `
    <h1>Mensajes</h1>
    @if (error()) { <p class="aviso aviso-error" role="alert">{{ error() }}</p> }
    @if (conversaciones().length) {
      <div class="chat">
        <div class="chat-lista">
          @for (c of conversaciones(); track c.id) {
            <button type="button" [attr.aria-current]="c.id === activa()?.id" (click)="abrir(c)">
              <strong>{{ otro(c) }}</strong>
              <span class="recorte">{{ c.ultimoMensaje ?? 'Sin mensajes todavía' }}</span>
            </button>
          }
        </div>
        @if (activa(); as a) {
          <div class="chat-hilo">
            <div class="chat-cabeza fila-entre">
              <strong>{{ otro(a) }}</strong>
              <span class="estado-vivo" aria-live="polite">{{ chat.conectado() ? 'En vivo' : 'Conectando…' }}</span>
            </div>
            <div class="chat-mensajes" #hilo aria-live="polite">
              @for (m of mensajes(); track m.id) {
                <div class="globo" [class.mio]="m.autorId === auth.actorId()">
                  {{ m.texto }}<time>{{ m.momento | date: 'short' }}</time>
                </div>
              } @empty { <p class="sutil">Escribe el primer mensaje.</p> }
            </div>
            <form class="chat-form" (ngSubmit)="enviar()">
              <label class="oculto-a11y" for="texto">Mensaje</label>
              <input id="texto" name="texto" [(ngModel)]="texto" maxlength="1000" autocomplete="off" placeholder="Escribe un mensaje" />
              <button class="boton boton-verde" type="submit" [disabled]="!texto().trim() || !chat.conectado()">Enviar</button>
            </form>
          </div>
        } @else {
          <div class="vacio" style="border: 0"><p class="sutil">Elige una conversación.</p></div>
        }
      </div>
    } @else if (!cargando()) {
      <div class="vacio">
        <h2>No tienes conversaciones</h2>
        <p class="sutil">{{ esTienda() ? 'Abre un lote y escribe a quien lo vende.' : 'Cuando un comprador te escriba, verás aquí su mensaje.' }}</p>
      </div>
    }
  `,
})
export class Mensajes implements OnInit, OnDestroy {
  private readonly api = inject(ApiService);
  protected readonly auth = inject(AuthService);
  protected readonly chat = inject(ChatVivoService);
  private readonly hilo = viewChild<ElementRef<HTMLElement>>('hilo');

  /** ?c=id abre una conversacion concreta. */
  readonly c = input<string>();

  protected readonly conversaciones = signal<Conversacion[]>([]);
  protected readonly activa = signal<Conversacion | null>(null);
  protected readonly mensajes = signal<Mensaje[]>([]);
  protected readonly texto = signal('');
  protected readonly cargando = signal(true);
  protected readonly error = signal<string | null>(null);
  protected readonly esTienda = computed(() => this.auth.esComprador());

  ngOnInit(): void {
    this.chat.conectar((m) => this.recibir(m));
    this.api.conversaciones().subscribe({
      next: (lista) => {
        this.conversaciones.set(lista);
        this.cargando.set(false);
        const inicial = lista.find((c) => c.id === this.c()) ?? lista[0];
        if (inicial) {
          this.abrir(inicial);
        }
      },
      error: (e) => { this.error.set(mensajeDeError(e)); this.cargando.set(false); },
    });
  }

  ngOnDestroy(): void {
    this.chat.desconectar();
  }

  protected otro(c: Conversacion): string {
    return this.esTienda() ? c.campesino : c.comprador;
  }

  protected abrir(c: Conversacion): void {
    this.activa.set(c);
    this.chat.abrir(c.id);
    this.api.mensajes(c.id).subscribe({
      next: (m) => { this.mensajes.set(m); this.bajar(); },
      error: (e) => this.error.set(mensajeDeError(e)),
    });
  }

  protected enviar(): void {
    const texto = this.texto().trim();
    if (texto && this.chat.enviar(texto)) {
      this.texto.set('');
    }
  }

  private recibir(m: Mensaje): void {
    if (m.conversacionId === this.activa()?.id && !this.mensajes().some((x) => x.id === m.id)) {
      this.mensajes.update((l) => [...l, m]);
      this.bajar();
    }
    this.conversaciones.update((l) => l.map((c) => (c.id === m.conversacionId ? { ...c, ultimoMensaje: m.texto } : c)));
  }

  private bajar(): void {
    setTimeout(() => {
      const el = this.hilo()?.nativeElement;
      if (el) {
        el.scrollTop = el.scrollHeight;
      }
    });
  }
}
