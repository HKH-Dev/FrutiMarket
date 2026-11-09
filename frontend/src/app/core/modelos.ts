/**
 * Tipos de las respuestas de la API. Son espejo de los DTO de Java
 * (ProductoResponse, LoteResumenResponse, TrazabilidadResponse...): el frontend
 * nunca ve las clases del dominio, solo estos objetos planos.
 */

export interface Producto {
  id: string;
  nombre: string;
  linea: 'MATERIA_PRIMA' | 'TRANSFORMADO';
  tipoCultivo: string;
  unidadVenta: string;
  simboloUnidad: string;
  conservacion: string;
  vidaUtilDias: number;
  temporada: string;
  enTemporada: boolean;
}

export interface LoteResumen {
  id: string;
  productoId: string;
  codigo: string;
  producto: string;
  campesinoId: string;
  cantidadDisponible: number;
  unidad: string;
  precioPorUnidad: number | null;
  puntajeCalidad: number;
  /** null si el producto no es perecedero */
  diasVidaUtilRestante: number | null;
  estado: string;
}

export interface Calidad {
  categoria: string;
  porcentajeDefectos: number;
  certificadoOrganico: boolean;
  puntaje: number;
}

export interface RegistroCustodia {
  tipo: string;
  descripcion: string;
  almacenId: string | null;
  lugar: string;
  responsable: string;
  momento: string;
  temperaturaC: number | null;
  observacion: string;
}

export interface Trazabilidad {
  id: string;
  codigo: string;
  producto: string;
  campesinoId: string;
  fincaOrigen: string;
  tecnicaProduccion: string;
  fechaCosecha: string;
  fechaLimiteConsumo: string | null;
  diasVidaUtilRestante: number | null;
  calidadDeclarada: Calidad;
  calidadVerificada: Calidad | null;
  custodia: RegistroCustodia[];
}

export interface ProductoVendido {
  posicion: number;
  productoId: string;
  nombre: string;
  cantidadVendida: number;
  unidad: string;
  numeroCompras: number;
}

export interface Token {
  token: string;
  tipo: string;
  correo: string;
  rol: string;
  expiraEn: string;
}

/** Error de la API en formato RFC 9457 (ProblemDetail), como lo arma ManejadorErrores. */
export interface Problema {
  title?: string;
  detail?: string;
  status?: number;
  regla?: string;
  errores?: Record<string, string>;
}

export type Rol = 'ADMIN' | 'CAMPESINO' | 'COMPRADOR';

export interface Pagina<T> {
  contenido: T[];
  totalElementos: number;
  totalPaginas: number;
  pagina: number;
}

export interface Oferta {
  id: string;
  nombre: string;
  productoId: string | null;
  porcentaje: number;
  inicio: string;
  fin: string;
  activo: boolean;
}

export interface Cupon {
  id: string;
  codigo: string;
  compradorId: string;
  porcentaje: number;
  venceEl: string;
  usadoEn: string | null;
}

export interface Sugerencia {
  motivo: string;
  descuento: number;
  lote: LoteResumen;
}

export interface Liquidacion {
  subtotal: number;
  descuentoTemporada: number;
  descuentoCupon: number;
  total: number;
}

export type MetodoPago = 'TARJETA' | 'PSE' | 'NEQUI';

export interface ItemCompra {
  loteId: string;
  cantidad: number;
}

export interface DetalleCompra {
  loteId: string;
  cantidad: number;
  unidadMedida: string;
  precioUnitario: number;
  subtotal: number;
}

export interface Compra {
  compraId: string;
  compradorId: string;
  estado: string;
  fecha: string;
  total: number;
  detalles: DetalleCompra[];
}

export interface Comprobante {
  compra: Compra;
  pagoId: string;
  estadoPago: string;
  referencia: string;
  metodo: MetodoPago;
  liquidacion: Liquidacion;
}

export interface Conversacion {
  id: string;
  compradorId: string;
  comprador: string;
  campesinoId: string;
  campesino: string;
  ultimoMensaje: string | null;
  ultimaActividad: string;
}

export interface Mensaje {
  id: string;
  conversacionId: string;
  autorId: string;
  texto: string;
  momento: string;
}

export interface ResumenPagos {
  ingresos: number;
  descuentos: number;
  aprobados: number;
  reembolsados: number;
}

export interface CampesinoAdmin {
  id: string;
  nombre: string;
  numeroIdentificacion: string;
  autorizado: boolean;
}

export interface CompradorAdmin {
  id: string;
  nombre: string;
  tipo: string;
  municipio: string;
}

export interface VentaCampesino {
  productoId: string;
  nombre: string;
  cantidadVendida: number;
  unidad: string;
  ingresos: number;
  numeroCompras: number;
}

export interface NuevoLote {
  codigo: string;
  productoId: string;
  fincaOrigenId: string;
  tecnicaProduccion: string;
  categoriaCalidad: string;
  porcentajeDefectos: number;
  certificadoOrganico: boolean;
  cantidad: string;
  unidadMedida: string;
  precioPorUnidad: number;
  fechaCosecha: string;
}

export interface Registro {
  correo: string;
  clave: string;
  rol: Rol;
  nombre: string;
  numeroIdentificacion?: string;
  municipio?: string;
  tipoComprador?: string;
}
