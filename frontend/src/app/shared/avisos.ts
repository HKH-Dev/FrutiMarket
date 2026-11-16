import { Component, inject } from '@angular/core';
import { AvisosService } from '../core/avisos.service';

/** Pila de avisos (toasts de Bootstrap) en la esquina de la pantalla. */
@Component({
  selector: 'app-avisos',
  template: `
    <div class="toast-container position-fixed top-0 end-0 p-3" aria-live="polite" aria-atomic="true">
      @for (a of avisos.avisos(); track a.id) {
        <div class="toast show aviso-toast" [class]="'aviso-' + a.tipo" [attr.role]="a.tipo === 'error' ? 'alert' : 'status'">
          <div class="d-flex">
            <div class="toast-body">{{ a.texto }}</div>
            <button type="button" class="btn-close me-2 m-auto" aria-label="Cerrar aviso" (click)="avisos.cerrar(a.id)"></button>
          </div>
        </div>
      }
    </div>
  `,
})
export class Avisos {
  protected readonly avisos = inject(AvisosService);
}
