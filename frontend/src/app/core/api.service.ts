import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import {
  CampesinoAdmin, Compra, CompradorAdmin, Comprobante, Conversacion, Cupon, ItemCompra, Liquidacion, LoteResumen,
  Mensaje, MetodoPago, NuevoLote, Oferta, Pagina, Producto, ProductoVendido, ResumenPagos, Sugerencia, Trazabilidad, VentaCampesino,
} from './modelos';

/** Unico punto de contacto con la API REST: los componentes no conocen las URLs. */
@Injectable({ providedIn: 'root' })
export class ApiService {
  private readonly http = inject(HttpClient);

  // Catalogo
  productos(): Observable<Producto[]> { return this.http.get<Producto[]>('/api/productos'); }
  buscarLotes(filtros: { texto?: string; tipoCultivo?: string; precioMaximo?: number | null; pagina: number; tamano: number }): Observable<Pagina<LoteResumen>> {
    let params = new HttpParams().set('pagina', filtros.pagina).set('tamano', filtros.tamano);
    if (filtros.texto) params = params.set('texto', filtros.texto);
    if (filtros.tipoCultivo) params = params.set('tipoCultivo', filtros.tipoCultivo);
    if (filtros.precioMaximo) params = params.set('precioMaximo', filtros.precioMaximo);
    return this.http.get<Pagina<LoteResumen>>('/api/lotes/buscar', { params });
  }
  lote(id: string): Observable<LoteResumen> { return this.http.get<LoteResumen>(`/api/lotes/${id}`); }
  mejorLote(productoId: string): Observable<LoteResumen> { return this.http.get<LoteResumen>('/api/lotes/mejor', { params: { productoId } }); }
  trazabilidad(id: string): Observable<Trazabilidad> { return this.http.get<Trazabilidad>(`/api/lotes/${id}/trazabilidad`); }
  ofertasVigentes(): Observable<Oferta[]> { return this.http.get<Oferta[]>('/api/ofertas/vigentes'); }
  recomendaciones(limite = 6): Observable<Sugerencia[]> { return this.http.get<Sugerencia[]>('/api/recomendaciones', { params: { limite } }); }

  // Comprador
  cotizar(items: ItemCompra[], cupon: string | null): Observable<Liquidacion> { return this.http.post<Liquidacion>('/api/pagos/cotizacion', { items, cupon }); }
  pagar(items: ItemCompra[], cupon: string | null, metodo: MetodoPago, ultimosDigitos: string | null): Observable<Comprobante> {
    return this.http.post<Comprobante>('/api/pagos/checkout', { items, cupon, metodo, ultimosDigitos });
  }
  misCompras(): Observable<Compra[]> { return this.http.get<Compra[]>('/api/compras/mias'); }
  reembolsar(id: string): Observable<Compra> { return this.http.post<Compra>(`/api/compras/${id}/reembolso`, null); }
  favoritos(): Observable<string[]> { return this.http.get<string[]>('/api/favoritos'); }
  agregarFavorito(productoId: string): Observable<void> { return this.http.put<void>(`/api/favoritos/${productoId}`, null); }
  quitarFavorito(productoId: string): Observable<void> { return this.http.delete<void>(`/api/favoritos/${productoId}`); }
  misCupones(): Observable<Cupon[]> { return this.http.get<Cupon[]>('/api/cupones/mios'); }

  // Chat
  iniciarConversacion(loteId: string): Observable<Conversacion> { return this.http.post<Conversacion>('/api/chat/conversaciones', { loteId }); }
  conversaciones(): Observable<Conversacion[]> { return this.http.get<Conversacion[]>('/api/chat/conversaciones'); }
  mensajes(id: string): Observable<Mensaje[]> { return this.http.get<Mensaje[]>(`/api/chat/conversaciones/${id}/mensajes`); }

  // Campesino
  misLotes(): Observable<LoteResumen[]> { return this.http.get<LoteResumen[]>('/api/lotes/mios'); }
  registrarLote(lote: NuevoLote): Observable<{ id: string }> { return this.http.post<{ id: string }>('/api/lotes', lote); }
  publicarLote(id: string): Observable<void> { return this.http.post<void>(`/api/lotes/${id}/publicar`, null); }
  desactivarLote(id: string, motivo: string): Observable<void> { return this.http.post<void>(`/api/lotes/${id}/desactivar`, { motivo }); }
  reactivarLote(id: string): Observable<void> { return this.http.post<void>(`/api/lotes/${id}/reactivar`, null); }
  retirarLote(id: string): Observable<void> { return this.http.delete<void>(`/api/lotes/${id}`); }
  misVentas(): Observable<VentaCampesino[]> { return this.http.get<VentaCampesino[]>('/api/paneles/campesino/ventas'); }

  // Administrador
  resumen(): Observable<ResumenPagos> { return this.http.get<ResumenPagos>('/api/paneles/admin/resumen'); }
  masVendidos(limite = 10): Observable<ProductoVendido[]> { return this.http.get<ProductoVendido[]>('/api/reportes/productos-mas-vendidos', { params: { limite } }); }
  campesinos(): Observable<CampesinoAdmin[]> { return this.http.get<CampesinoAdmin[]>('/api/paneles/admin/campesinos'); }
  compradores(): Observable<CompradorAdmin[]> { return this.http.get<CompradorAdmin[]>('/api/paneles/admin/compradores'); }
  autorizarCampesino(id: string): Observable<void> { return this.http.post<void>(`/api/campesinos/${id}/autorizacion`, null); }
  ofertas(): Observable<Oferta[]> { return this.http.get<Oferta[]>('/api/ofertas'); }
  crearOferta(oferta: Omit<Oferta, 'id' | 'activo'>): Observable<Oferta> { return this.http.post<Oferta>('/api/ofertas', oferta); }
  terminarOferta(id: string): Observable<void> { return this.http.post<void>(`/api/ofertas/${id}/desactivar`, null); }
  cupones(): Observable<Cupon[]> { return this.http.get<Cupon[]>('/api/cupones'); }
  crearCupon(cupon: { codigo: string; compradorId: string; porcentaje: number; venceEl: string }): Observable<Cupon> { return this.http.post<Cupon>('/api/cupones', cupon); }
}
