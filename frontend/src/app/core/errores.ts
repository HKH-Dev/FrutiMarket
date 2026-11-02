import { HttpErrorResponse } from '@angular/common/http';
import { Problema } from './modelos';

/** Convierte cualquier error HTTP en un mensaje legible para el usuario. */
export function mensajeDeError(error: unknown): string {
  if (!(error instanceof HttpErrorResponse)) {
    return 'Ocurrio un error inesperado.';
  }
  if (error.status === 0) {
    return 'No hay conexion con el servidor. ¿Esta corriendo el backend en el puerto 8080?';
  }
  const problema = error.error as Problema | null;
  if (problema?.detail) {
    return problema.regla ? `${problema.detail} (regla ${problema.regla})` : problema.detail;
  }
  if (error.status === 401) {
    return 'Debe iniciar sesion.';
  }
  if (error.status === 403) {
    return 'Su usuario no tiene permiso para ver esto.';
  }
  return `Error ${error.status}`;
}
