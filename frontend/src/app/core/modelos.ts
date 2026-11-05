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
