import { Routes } from '@angular/router';
import { Rol } from './core/modelos';
import { rolGuard } from './core/rol.guard';

const solo = (...roles: Rol[]) => ({ canActivate: [rolGuard], data: { roles } });
const F = 'FrutiMarket';

/** Cada pantalla se carga solo cuando se visita (loadComponent), asi la app inicial pesa menos. */
export const routes: Routes = [
  { path: '', title: F, loadComponent: () => import('./features/tienda/inicio').then((m) => m.Inicio) },
  { path: 'catalogo', title: `Catálogo | ${F}`, loadComponent: () => import('./features/tienda/catalogo').then((m) => m.Catalogo) },
  { path: 'lotes/:id', title: `Lote | ${F}`, loadComponent: () => import('./features/tienda/lote-detalle').then((m) => m.LoteDetalle) },
  { path: 'login', title: `Iniciar sesión | ${F}`, loadComponent: () => import('./features/acceso/login').then((m) => m.Login) },
  { path: 'registro', title: `Crear cuenta | ${F}`, loadComponent: () => import('./features/acceso/registro').then((m) => m.Registro) },
  { path: 'carrito', title: `Carrito | ${F}`, loadComponent: () => import('./features/comprador/carrito').then((m) => m.Carrito) },
  { path: 'compras', title: `Mis compras | ${F}`, ...solo('COMPRADOR'), loadComponent: () => import('./features/comprador/compras').then((m) => m.Compras) },
  { path: 'favoritos', title: `Favoritos | ${F}`, ...solo('COMPRADOR'), loadComponent: () => import('./features/comprador/favoritos').then((m) => m.Favoritos) },
  { path: 'cupones', title: `Mis cupones | ${F}`, ...solo('COMPRADOR'), loadComponent: () => import('./features/comprador/cupones').then((m) => m.Cupones) },
  { path: 'mensajes', title: `Mensajes | ${F}`, ...solo('COMPRADOR', 'CAMPESINO'), loadComponent: () => import('./features/chat/mensajes').then((m) => m.Mensajes) },
  { path: 'finca', title: `Mi finca | ${F}`, ...solo('CAMPESINO'), loadComponent: () => import('./features/finca/resumen').then((m) => m.ResumenFinca) },
  { path: 'finca/lotes', title: `Mis lotes | ${F}`, ...solo('CAMPESINO'), loadComponent: () => import('./features/finca/lotes').then((m) => m.LotesFinca) },
  { path: 'finca/lotes/nuevo', title: `Registrar lote | ${F}`, ...solo('CAMPESINO'), loadComponent: () => import('./features/finca/nuevo-lote').then((m) => m.NuevoLoteFinca) },
  { path: 'consola', title: `Consola | ${F}`, ...solo('ADMIN'), loadComponent: () => import('./features/consola/resumen').then((m) => m.ResumenConsola) },
  { path: 'consola/campesinos', title: `Campesinos | ${F}`, ...solo('ADMIN'), loadComponent: () => import('./features/consola/campesinos').then((m) => m.Campesinos) },
  { path: 'consola/temporadas', title: `Temporadas | ${F}`, ...solo('ADMIN'), loadComponent: () => import('./features/consola/temporadas').then((m) => m.Temporadas) },
  { path: 'consola/cupones', title: `Cupones | ${F}`, ...solo('ADMIN'), loadComponent: () => import('./features/consola/cupones').then((m) => m.CuponesConsola) },
  { path: '**', redirectTo: '' },
];
