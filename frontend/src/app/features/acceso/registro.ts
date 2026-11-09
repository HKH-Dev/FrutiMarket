import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { switchMap } from 'rxjs';
import { AuthService } from '../../core/auth.service';
import { mensajeDeError } from '../../core/errores';

@Component({
  selector: 'app-registro',
  imports: [ReactiveFormsModule, RouterLink],
  template: `
    <div class="ancho tienda-contenido">
      <div class="acceso">
        <div class="acceso-lado">
          <h1>Crea tu cuenta</h1>
          <p>Si vendes, un operador revisa tus datos antes de que publiques tu primer lote.</p>
        </div>
        <form class="acceso-form" [formGroup]="f" (ngSubmit)="registrar()">
          <div class="metodos" style="grid-template-columns: 1fr 1fr" role="radiogroup" aria-label="Tipo de cuenta">
            <label><input type="radio" formControlName="rol" value="COMPRADOR" /> Quiero comprar</label>
            <label><input type="radio" formControlName="rol" value="CAMPESINO" /> Quiero vender</label>
          </div>
          <label class="campo">Nombre o negocio <input formControlName="nombre" autocomplete="name" /></label>
          <label class="campo">Correo <input type="email" formControlName="correo" autocomplete="email" /></label>
          <label class="campo">Contraseña <input type="password" formControlName="clave" autocomplete="new-password" /><span class="ayuda">Mínimo 8 caracteres.</span></label>
          @if (f.controls.rol.value === 'CAMPESINO') {
            <label class="campo">Número de identificación <input formControlName="numeroIdentificacion" /></label>
          } @else {
            <label class="campo">Municipio <input formControlName="municipio" /></label>
            <label class="campo">Compro para
              <select formControlName="tipoComprador">
                <option value="CONSUMIDOR">Mi hogar</option><option value="TIENDA">Una tienda</option><option value="RESTAURANTE">Un restaurante</option>
              </select>
            </label>
          }
          @if (error()) { <p class="aviso aviso-error" role="alert">{{ error() }}</p> }
          <button class="boton boton-verde" type="submit" [disabled]="enviando()">Crear cuenta</button>
          <p class="sutil">¿Ya tienes cuenta? <a routerLink="/login">Iniciar sesión</a></p>
        </form>
      </div>
    </div>
  `,
})
export class Registro {
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);
  protected readonly f = inject(FormBuilder).nonNullable.group({
    rol: ['COMPRADOR' as 'COMPRADOR' | 'CAMPESINO'],
    nombre: ['', Validators.required],
    correo: ['', [Validators.required, Validators.email]],
    clave: ['', [Validators.required, Validators.minLength(8)]],
    numeroIdentificacion: [''],
    municipio: [''],
    tipoComprador: ['CONSUMIDOR'],
  });
  protected readonly enviando = signal(false);
  protected readonly error = signal<string | null>(null);

  protected registrar(): void {
    if (this.f.invalid) {
      this.error.set('Completa nombre, un correo válido y una contraseña de al menos 8 caracteres.');
      return;
    }
    const v = this.f.getRawValue();
    const datos = v.rol === 'CAMPESINO'
      ? { correo: v.correo, clave: v.clave, rol: v.rol, nombre: v.nombre, numeroIdentificacion: v.numeroIdentificacion }
      : { correo: v.correo, clave: v.clave, rol: v.rol, nombre: v.nombre, municipio: v.municipio, tipoComprador: v.tipoComprador };
    this.enviando.set(true);
    this.error.set(null);
    this.auth.registrar(datos).pipe(switchMap(() => this.auth.iniciarSesion(v.correo, v.clave))).subscribe({
      next: () => this.router.navigateByUrl(this.auth.inicioDelRol()),
      error: (e) => {
        this.error.set(mensajeDeError(e));
        this.enviando.set(false);
      },
    });
  }
}
