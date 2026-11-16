import { Component, inject, input, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../core/auth.service';
import { AvisosService } from '../../core/avisos.service';
import { mensajeDeError } from '../../core/errores';

/** Login con formulario reactivo. El token queda en localStorage (AuthService) y lo envia el interceptor. */
@Component({
  selector: 'app-login',
  imports: [ReactiveFormsModule, RouterLink],
  template: `
    <div class="container tienda-contenido">
      <div class="acceso">
        <div class="acceso-lado">
          <h1>Entra a tu cuenta</h1>
          <p>Compradores, campesinos y operadores entran por aquí. Cada uno llega a su propio espacio.</p>
        </div>
        <form class="acceso-form" [formGroup]="formulario" (ngSubmit)="entrar()" novalidate>
          <div>
            <label class="form-label" for="correo">Correo</label>
            <input id="correo" type="email" class="form-control" formControlName="correo" autocomplete="username"
              [class.is-invalid]="invalido('correo')" />
            @if (formulario.controls.correo.hasError('required')) {
              <div class="invalid-feedback">Escribe tu correo.</div>
            } @else {
              <div class="invalid-feedback">El correo no tiene un formato válido, por ejemplo ana&#64;correo.com.</div>
            }
          </div>
          <div>
            <label class="form-label" for="clave">Contraseña</label>
            <input id="clave" type="password" class="form-control" formControlName="clave" autocomplete="current-password"
              [class.is-invalid]="invalido('clave')" />
            <div class="invalid-feedback">Escribe tu contraseña.</div>
          </div>
          @if (error()) { <div class="alert alert-danger mb-0" role="alert">{{ error() }}</div> }
          <button class="btn btn-primary" type="submit" [disabled]="enviando()">{{ enviando() ? 'Entrando…' : 'Iniciar sesión' }}</button>
          <p class="sutil">¿Aún no tienes cuenta? <a routerLink="/registro">Crear cuenta</a></p>
        </form>
      </div>
    </div>
  `,
})
export class Login {
  private readonly auth = inject(AuthService);
  private readonly avisos = inject(AvisosService);
  private readonly router = inject(Router);

  /** Ruta a la que volver despues de entrar (?volver=/compras). */
  readonly volver = input<string>();

  protected readonly formulario = inject(FormBuilder).nonNullable.group({
    correo: ['', [Validators.required, Validators.email]],
    clave: ['', Validators.required],
  });
  protected readonly enviando = signal(false);
  protected readonly error = signal<string | null>(null);

  protected invalido(campo: 'correo' | 'clave'): boolean {
    const control = this.formulario.controls[campo];
    return control.invalid && control.touched;
  }

  protected entrar(): void {
    if (this.formulario.invalid) {
      this.formulario.markAllAsTouched();
      return;
    }
    const { correo, clave } = this.formulario.getRawValue();
    this.enviando.set(true);
    this.error.set(null);
    this.auth.iniciarSesion(correo, clave).subscribe({
      next: () => {
        this.avisos.exito('Sesión iniciada.');
        this.router.navigateByUrl(this.volver() ?? this.auth.inicioDelRol());
      },
      error: (e) => {
        this.error.set(e instanceof HttpErrorResponse && e.status === 401 ? 'Correo o contraseña incorrectos.' : mensajeDeError(e));
        this.enviando.set(false);
      },
    });
  }
}
