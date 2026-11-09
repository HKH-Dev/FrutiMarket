import { Component, OnInit, computed, effect, inject } from '@angular/core';
import { NgTemplateOutlet } from '@angular/common';
import { Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { AuthService } from './core/auth.service';
import { ChatVivoService } from './core/chat-vivo.service';
import { TiendaService } from './core/tienda.service';

interface Enlace {
  ruta: string;
  texto: string;
  exacto?: boolean;
}

const MENU_FINCA: Enlace[] = [
  { ruta: '/finca', texto: 'Resumen', exacto: true },
  { ruta: '/finca/lotes', texto: 'Mis lotes' },
  { ruta: '/mensajes', texto: 'Mensajes' },
];

const MENU_CONSOLA: Enlace[] = [
  { ruta: '/consola', texto: 'Ventas', exacto: true },
  { ruta: '/consola/campesinos', texto: 'Campesinos' },
  { ruta: '/consola/temporadas', texto: 'Temporadas' },
  { ruta: '/consola/cupones', texto: 'Cupones' },
];

/** Marco de la aplicacion: cada perfil tiene el suyo (tienda, finca o consola). */
@Component({
  selector: 'app-root',
  imports: [RouterOutlet, RouterLink, RouterLinkActive, NgTemplateOutlet],
  templateUrl: './app.html',
})
export class App implements OnInit {
  protected readonly auth = inject(AuthService);
  protected readonly tienda = inject(TiendaService);
  private readonly chat = inject(ChatVivoService);
  private readonly router = inject(Router);

  protected readonly menuPanel = computed(() => (this.auth.rol() === 'CAMPESINO' ? MENU_FINCA : this.auth.rol() === 'ADMIN' ? MENU_CONSOLA : null));
  protected readonly clasePanel = computed(() => (this.auth.rol() === 'ADMIN' ? 'panel perfil-consola' : 'panel perfil-finca'));
  protected readonly nombrePanel = computed(() => (this.auth.rol() === 'ADMIN' ? 'Consola de operación' : 'Mi finca'));

  constructor() {
    // Al entrar o salir cambian los favoritos del usuario.
    effect(() => {
      this.auth.rol();
      this.tienda.recargarFavoritos();
    });
  }

  ngOnInit(): void {
    this.tienda.cargar();
  }

  protected salir(): void {
    this.chat.desconectar();
    this.auth.cerrarSesion();
    this.router.navigateByUrl('/');
  }
}
