import { HttpErrorResponse } from '@angular/common/http';
import { Problema } from './modelos';

/** Convierte cualquier error HTTP en un mensaje legible para el usuario. */
export function mensajeDeError(error: unknown): string {
  if (!(error instanceof HttpErrorResponse)) {
    return 'Ocurrió un error inesperado. Inténtalo de nuevo.';
  }
  if (error.status === 0) {
    return 'No hay conexión con el servidor. Revisa tu red e inténtalo de nuevo.';
  }
  const problema = error.error as Problema | null;
  if (problema?.detail) {
    return problema.detail;
  }
  if (error.status === 401) {
    return 'Inicia sesión para continuar.';
  }
  if (error.status === 403) {
    return 'Tu usuario no tiene permiso para esta acción.';
  }
  return `Error ${error.status}`;
}
