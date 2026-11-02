import { Routes } from '@angular/router';
import { adminGuard } from './core/admin.guard';

/** Cada pantalla se carga solo cuando se visita (loadComponent), asi la app inicial pesa menos. */
export const routes: Routes = [
  {
    path: '',
    title: 'Catálogo · FrutiMarket',
    loadComponent: () => import('./features/catalogo/catalogo').then((m) => m.Catalogo),
  },
  {
    path: 'lotes/:id',
    title: 'Trazabilidad · FrutiMarket',
    loadComponent: () => import('./features/lote-detalle/lote-detalle').then((m) => m.LoteDetalle),
  },
  {
    path: 'login',
    title: 'Iniciar sesión · FrutiMarket',
    loadComponent: () => import('./features/login/login').then((m) => m.Login),
  },
  {
    path: 'reportes',
    title: 'Reportes · FrutiMarket',
    canActivate: [adminGuard],
    loadComponent: () => import('./features/reportes/reportes').then((m) => m.Reportes),
  },
  { path: '**', redirectTo: '' },
];
