import { Component, inject, input, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../core/auth.service';
import { mensajeDeError } from '../../core/errores';

/** Inicio de sesion por correo. Usuarios de demostracion: admin, campesino y comprador @frutimarket.com. */
@Component({
  selector: 'app-login',
  imports: [ReactiveFormsModule],
  templateUrl: './login.html',
  styleUrl: './login.css',
})
export class Login {
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);

  /** Ruta a la que volver despues de entrar (?volver=/reportes). */
  readonly volver = input<string>();

  protected readonly formulario = inject(FormBuilder).nonNullable.group({
    correo: ['', [Validators.required, Validators.email]],
    clave: ['', Validators.required],
  });
  protected readonly enviando = signal(false);
  protected readonly error = signal<string | null>(null);

  protected entrar(): void {
    if (this.formulario.invalid) {
      this.formulario.markAllAsTouched();
      return;
    }
    const { correo, clave } = this.formulario.getRawValue();
    this.enviando.set(true);
    this.error.set(null);
    this.auth.iniciarSesion(correo, clave).subscribe({
      next: () => this.router.navigateByUrl(this.volver() ?? (this.auth.esAdmin() ? '/reportes' : '/')),
      error: (e) => {
        this.error.set(mensajeDeError(e));
        this.enviando.set(false);
      },
    });
  }
}
