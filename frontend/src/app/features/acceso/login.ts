import { Component, inject, input, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../core/auth.service';
import { mensajeDeError } from '../../core/errores';

@Component({
  selector: 'app-login',
  imports: [ReactiveFormsModule, RouterLink],
  template: `
    <div class="ancho tienda-contenido">
      <div class="acceso">
        <div class="acceso-lado">
          <h1>Entra a tu cuenta</h1>
          <p>Compradores, campesinos y operadores entran por aquí. Cada uno llega a su propio espacio.</p>
        </div>
        <form class="acceso-form" [formGroup]="formulario" (ngSubmit)="entrar()">
          <label class="campo">Correo <input type="email" formControlName="correo" autocomplete="username" /></label>
          <label class="campo">Contraseña <input type="password" formControlName="clave" autocomplete="current-password" /></label>
          @if (error()) { <p class="aviso aviso-error" role="alert">{{ error() }}</p> }
          <button class="boton boton-verde" type="submit" [disabled]="enviando()">{{ enviando() ? 'Entrando…' : 'Iniciar sesión' }}</button>
          <p class="sutil">¿Aún no tienes cuenta? <a routerLink="/registro">Crear cuenta</a></p>
        </form>
      </div>
    </div>
  `,
})
export class Login {
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);

  /** Ruta a la que volver despues de entrar (?volver=/compras). */
  readonly volver = input<string>();

  protected readonly formulario = inject(FormBuilder).nonNullable.group({
    correo: ['', [Validators.required, Validators.email]],
    clave: ['', Validators.required],
  });
  protected readonly enviando = signal(false);
  protected readonly error = signal<string | null>(null);

  protected entrar(): void {
    if (this.formulario.invalid) {
      this.error.set('Escribe tu correo y tu contraseña.');
      return;
    }
    const { correo, clave } = this.formulario.getRawValue();
    this.enviando.set(true);
    this.error.set(null);
    this.auth.iniciarSesion(correo, clave).subscribe({
      next: () => this.router.navigateByUrl(this.volver() ?? this.auth.inicioDelRol()),
      error: (e) => {
        this.error.set(mensajeDeError(e));
        this.enviando.set(false);
      },
    });
  }
}
