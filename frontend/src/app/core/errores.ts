import { HttpErrorResponse } from '@angular/common/http';
import { Problema } from './modelos';

/**
 * Traduce un error HTTP a un mensaje para una persona. El backend responde ProblemDetail (RFC 9457):
 * en 400 trae el detalle por campo en "errores" y en 422 la regla de negocio que se rompio.
 */
export function mensajeDeError(error: unknown): string {
  if (!(error instanceof HttpErrorResponse)) {
    return 'Ocurrió un error inesperado. Inténtalo de nuevo.';
  }
  const problema = error.error as Problema | null;
  switch (error.status) {
    case 0:
      return 'No hay conexión con el servidor. Revisa tu red e inténtalo de nuevo.';
    case 400: {
      const campos = problema?.errores ? Object.values(problema.errores).join('. ') : '';
      return `Revisa los datos${campos ? `: ${campos}` : '.'}`;
    }
    case 401:
      return 'Inicia sesión para continuar.';
    case 403:
      return 'Tu usuario no tiene permiso para esta acción.';
    case 404:
      return `No existe lo que buscas${problema?.detail ? `: ${problema.detail}` : '.'}`;
    case 409:
      return 'Ese dato ya existe. Usa otro, por ejemplo un código distinto.';
    case 422:
      return problema?.detail ?? 'La operación no cumple las reglas del negocio.';
    default:
      return 'El servidor tuvo un problema. Inténtalo más tarde.';
  }
}
